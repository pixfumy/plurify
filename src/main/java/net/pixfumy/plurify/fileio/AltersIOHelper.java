package net.pixfumy.plurify.fileio;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.stats.Stat;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.pixfumy.plurify.PlurifyMain;
import net.pixfumy.plurify.advancement.AdvancementUtil;
import net.pixfumy.plurify.alter.Alter;
import net.pixfumy.plurify.alter.AlterBasicDetail;
import net.pixfumy.plurify.alter.IAltersOwner;
import net.pixfumy.plurify.alter.PlayerAltersConfig;
import net.pixfumy.plurify.gui.icon.PlurifyIcon;
import net.pixfumy.plurify.gui.icon.PlurifyIconUtil;
import net.pixfumy.plurify.mixin.access.ServerStatsCounterAccess;
import org.apache.logging.log4j.core.config.plugins.validation.Constraint;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class AltersIOHelper {
    private static final String MOD_DIR = PlurifyMain.MOD_ID;

    public static void loadPlayerAltersFromFile(ServerPlayer player) {
        File playerFile = getPlayerAltersConfigFile(player).toFile();

        if (playerFile.exists()) {
            PlurifyMain.LOGGER.info("AltersIOHelper :: found alters file for " + player.getScoreboardName());

            PlayerAltersConfig playerAltersConfig = getPlayerAltersConfig(player);
            UUID currentAlterUuid = playerAltersConfig.currentAlterUuid();

            HashMap<UUID, Alter> alters = new HashMap<>();

            for (AlterBasicDetail alterBasicDetail : playerAltersConfig.alters()) {
                Alter alter = new Alter(alterBasicDetail.uuid(), Component.literal(alterBasicDetail.name()), player, false);

                readPlayerAlterEntityDataFromFile(player, alter);
                readPlayerAlterAdvancementsFromFile(player, alter);
                readPlayerAlterStatsFromFile(player, alter);

                if (currentAlterUuid.equals(alter.getUuid())) {
                    ((IAltersOwner)player).plurify$switchToAlter(alter);
                }

                alter.setIcon(alterBasicDetail.icon().getAsItemStack());

                alters.put(alter.getUuid(), alter);
            }

            PlurifyMain.LOGGER.info("AltersIOHelper :: Successfully loaded " + alters.size() + " alters for player " + player.getScoreboardName());

            ((IAltersOwner) player).plurify$setAlters(alters);
        } else {
            PlurifyMain.LOGGER.info("AltersIOHelper :: No alter_json.data found for " + player.getScoreboardName() + ", creating 1 default alter.");

            Alter alter = new Alter(player, true);
            ((IAltersOwner) player).plurify$setAlters(new HashMap<>() {{put(alter.getUuid(), alter);}});
            ((IAltersOwner) player).plurify$setCurrentAlter(alter);
            writePlayerAltersToFile(player);
        }
    }

    public static void writePlayerAltersToFile(ServerPlayer player) {
        Path playerDir = getPlurifyWorldPlayerDir(player);
        File playerFile = getPlayerAltersConfigFile(player).toFile();

        HashMap<UUID, Alter> alters = ((IAltersOwner)player).plurify$getAlters();

        PlurifyMain.LOGGER.info("AltersIOHelper :: Writing " + alters.size() + " alters for player " + player.getScoreboardName());

        for (Alter alter : alters.values()) {
            writePlayerAlterEntityDataToFile(player, alter);
            writePlayerAlterAdvancementsToFile(player, alter);
            writePlayerAlterStatsToFile(player, alter);
        }

        try {
            JsonObject playerAltersConfigJson = (JsonObject) PlayerAltersConfig.CODEC.encodeStart(JsonOps.INSTANCE, new PlayerAltersConfig(player)).getOrThrow();
            getPlurifyWorldDir(player).toFile().mkdir();
            playerDir.toFile().mkdir();
            playerFile.createNewFile();

            Writer output = new BufferedWriter(new FileWriter(playerFile));
            output.write(playerAltersConfigJson.toString());
            output.close();

            PlurifyMain.LOGGER.info("AltersIOHelper :: Successfully wrote " + alters.size() + " alters for player " + player.getScoreboardName());
        } catch (IOException e) {
            PlurifyMain.LOGGER.error("Error writing alterJson config file " + playerFile.getName());
        }
    }

    private static void readPlayerAlterEntityDataFromFile(ServerPlayer player, Alter alter) {
        Path entityDataFile = getPlurifyWorldPlayerAlterDataFile(player, alter);
        if (!entityDataFile.toFile().exists()) {
            return;
        }

        try {
            CompoundTag entityDataNbt = NbtIo.read(entityDataFile);

            ValueInput nbtReadView = TagValueInput.create(new ProblemReporter.ScopedCollector(PlurifyMain.LOGGER), player.registryAccess(), entityDataNbt);
            List<ItemStack> mainInventoryListReadView = Objects.requireNonNull(nbtReadView.listOrEmpty("Inventory", ItemStack.OPTIONAL_CODEC).stream().toList());
            for (int i = 0; i < mainInventoryListReadView.size(); i++) {
                alter.getPlayerInventory().setItem(i, mainInventoryListReadView.get(i));
            }

            List<ItemStack> enderChestInventoryListReadView = Objects.requireNonNull(nbtReadView.listOrEmpty("EnderItems", ItemStack.OPTIONAL_CODEC).stream().toList());
            for (int i = 0; i < enderChestInventoryListReadView.size(); i++) {
                alter.getEnderChestInventory().setItem(i, enderChestInventoryListReadView.get(i));
            }

            List<AttributeInstance.Packed> packedAttributes = Objects.requireNonNull(nbtReadView.listOrEmpty("PackedAttributes", AttributeInstance.Packed.CODEC).stream().toList());
            alter.setAttributes(packedAttributes);

            List<MobEffectInstance> activeEffects = Objects.requireNonNull(nbtReadView.listOrEmpty("ActiveEffects", MobEffectInstance.CODEC).stream().toList());
            alter.setActiveEffects(activeEffects);

            alter.setLevel(player.level().getServer().getLevel(nbtReadView.read("World", ServerLevel.RESOURCE_KEY_CODEC).get()));
            alter.setPosition(nbtReadView.read("Position", Vec3.CODEC).get());
            alter.setRotation(nbtReadView.read("Rotation", Vec2.CODEC).get());

            alter.setRespawn(nbtReadView.read("Respawn", ServerPlayer.RespawnConfig.CODEC).orElse(null));

            alter.setHealth(nbtReadView.read("Health", Codec.FLOAT).get());
            alter.setAbsorptionAmount(nbtReadView.read("AbsorptionAmount", Codec.FLOAT).get());

            alter.setExperienceLevel(nbtReadView.read("ExperienceLevel", Codec.INT).get());
            alter.setTotalExperience(nbtReadView.read("TotalExperience", Codec.INT).get());
            alter.setExperienceProgress(nbtReadView.read("ExperienceProgress", Codec.FLOAT).get());

            alter.setFoodLevel(nbtReadView.read("FoodLevel", Codec.INT).get());
            alter.setSaturationLevel(nbtReadView.read("SaturationLevel", Codec.FLOAT).get());
            alter.setExhaustion(nbtReadView.read("ExperienceLevel", Codec.FLOAT).get());
            alter.setFoodTickTimer(nbtReadView.read("FoodTickTimer", Codec.INT).get());

            alter.setRemainingFireTicks(nbtReadView.read("RemainingFireTicks", Codec.INT).get());
            alter.setAirSupply(nbtReadView.read("AirSupply", Codec.INT).get());
            alter.setTicksFrozen(nbtReadView.read("TicksFrozen", Codec.INT).get());

            alter.setVehicle(nbtReadView.read("Vehicle", CompoundTag.CODEC).orElse(null));

            alter.setEnderPearls(nbtReadView.read("EnderPearls", CompoundTag.CODEC).get());

            alter.setShoulderEntityLeft(nbtReadView.read("ShoulderEntityLeft", CompoundTag.CODEC).get());
            alter.setShoulderEntityRight(nbtReadView.read("ShoulderEntityRight", CompoundTag.CODEC).get());

            alter.setKeepInventory(nbtReadView.read("KeepInventory", Codec.BOOL).get());

        } catch (IOException | NoSuchElementException | NullPointerException e) {
            PlurifyMain.LOGGER.error("Error reading entity data file " + entityDataFile);
        }
    }

    private static void writePlayerAlterEntityDataToFile(ServerPlayer player, Alter alter) {
        Inventory playerInventory = alter.getPlayerInventory();

        TagValueOutput nbtWriteView = TagValueOutput.createWithContext(new ProblemReporter.ScopedCollector(PlurifyMain.LOGGER), player.registryAccess());
        ValueOutput.TypedOutputList<ItemStack> mainInventoryListAppender = nbtWriteView.list("Inventory", ItemStack.OPTIONAL_CODEC);
        for (int i = 0; i < playerInventory.getContainerSize(); i++) {
            mainInventoryListAppender.add(playerInventory.getItem(i));
        }

        PlayerEnderChestContainer enderChestInventory = alter.getEnderChestInventory();
        ValueOutput.TypedOutputList<ItemStack> enderChestInventoryListAppender = nbtWriteView.list("EnderItems", ItemStack.OPTIONAL_CODEC);
        for (int i = 0; i < alter.getEnderChestInventory().getContainerSize(); i++) {
            enderChestInventoryListAppender.add(enderChestInventory.getItem(i));
        }

        List<AttributeInstance.Packed> packedAttributes = alter.getAttributes();
        ValueOutput.TypedOutputList<AttributeInstance.Packed> packedAttributeOutputList = nbtWriteView.list("PackedAttributes", AttributeInstance.Packed.CODEC);
        for (int i = 0; i < packedAttributes.size(); i++) {
            packedAttributeOutputList.add(packedAttributes.get(i));
        }

        List<MobEffectInstance> activeEffects = alter.getActiveEffects();
        ValueOutput.TypedOutputList<MobEffectInstance> activeEffectsOutputList = nbtWriteView.list("ActiveEffects", MobEffectInstance.CODEC);
        for (int i = 0; i < activeEffects.size(); i++) {
            activeEffectsOutputList.add(activeEffects.get(i));
        }

        nbtWriteView.store("World", ServerLevel.RESOURCE_KEY_CODEC, alter.getLevel().dimension());
        nbtWriteView.store("Position", Vec3.CODEC, alter.getPosition());
        nbtWriteView.store("Rotation", Vec2.CODEC, alter.getRotation());

        if (alter.getRespawn() != null) {
            nbtWriteView.store("Respawn", ServerPlayer.RespawnConfig.CODEC, alter.getRespawn());
        }

        nbtWriteView.store("Health", Codec.FLOAT, alter.getHealth());
        nbtWriteView.store("AbsorptionAmount", Codec.FLOAT, alter.getAbsorptionAmount());

        nbtWriteView.store("ExperienceLevel", Codec.INT, alter.getExperienceLevel());
        nbtWriteView.store("TotalExperience", Codec.INT, alter.getTotalExperience());
        nbtWriteView.store("ExperienceProgress", Codec.FLOAT, alter.getExperienceProgress());

        nbtWriteView.store("FoodLevel", Codec.INT, alter.getFoodLevel());
        nbtWriteView.store("SaturationLevel", Codec.FLOAT, alter.getSaturationLevel());
        nbtWriteView.store("Exhaustion", Codec.FLOAT, alter.getExhaustion());
        nbtWriteView.store("FoodTickTimer", Codec.INT, alter.getTotalExperience());

        nbtWriteView.store("RemainingFireTicks", Codec.INT, alter.getRemainingFireTicks());
        nbtWriteView.store("AirSupply", Codec.INT, alter.getAirSupply());
        nbtWriteView.store("TicksFrozen", Codec.INT, alter.getTicksFrozen());

        nbtWriteView.store("KeepInventory", Codec.BOOL, alter.hasKeepInventory());

        CompoundTag vehicle = alter.getVehicle();
        if (vehicle != null) {
            nbtWriteView.store("Vehicle", CompoundTag.CODEC, vehicle);
        }

        nbtWriteView.store("EnderPearls", CompoundTag.CODEC, alter.getEnderPearls());

        nbtWriteView.store("ShoulderEntityLeft", CompoundTag.CODEC, alter.getShoulderEntityLeft());
        nbtWriteView.store("ShoulderEntityRight", CompoundTag.CODEC, alter.getShoulderEntityRight());

        Path entityDataFile = getPlurifyWorldPlayerAlterDataFile(player, alter);
        try {
            NbtIo.write(nbtWriteView.buildResult(), entityDataFile);
        } catch (IOException e) {
            PlurifyMain.LOGGER.error("Error writing entity data file " + entityDataFile);
        }
    }

    private static void readPlayerAlterAdvancementsFromFile(ServerPlayer player, Alter alter) {
        File alterAdvancementsFile = getPlurifyWorldPlayerAlterAdvancementsFile(player, alter).toFile();
        try {
            if (alterAdvancementsFile.exists()) {
                JsonObject alterAdvancementsJson = (JsonObject) JsonParser.parseReader(new FileReader(alterAdvancementsFile));
                PlayerAdvancements.Data playerAdvancementsData = AdvancementUtil.decodeAdvancementsFromJson(alterAdvancementsJson);
                alter.setAdvancements(playerAdvancementsData);
            }
        } catch (FileNotFoundException e) {
            PlurifyMain.LOGGER.error("Error reading alter advancements file " + alterAdvancementsFile.getName());
        }
    }

    private static void writePlayerAlterAdvancementsToFile(ServerPlayer player, Alter alter) {
        JsonElement alterAdvancementsJson = AdvancementUtil.encodeAdvancementsToJson(alter.getAdvancements());
        File alterAdvancementsFile = null;
        try {
            alterAdvancementsFile = getPlurifyWorldPlayerAlterAdvancementsFile(player, alter).toFile();

            Writer output = new BufferedWriter(new FileWriter(alterAdvancementsFile));
            output.write(alterAdvancementsJson.toString());
            output.close();
        } catch (IOException e) {
            PlurifyMain.LOGGER.error("Error writing alter advancements file " + alterAdvancementsFile.getName());
        }
    }

    private static void readPlayerAlterStatsFromFile(ServerPlayer player, Alter alter) {
        File alterStatsFile = getPlurifyWorldPlayerAlterStatsFile(player, alter).toFile();
        try {
            if (alterStatsFile.exists()) {
                JsonObject alterStatsJson = (JsonObject) JsonParser.parseReader(new FileReader(alterStatsFile));
                Map<Stat<?>, Integer> serverStatsCounter = ServerStatsCounterAccess.getSTATS_CODEC().decode(JsonOps.INSTANCE, alterStatsJson).getOrThrow().getFirst();
                alter.setStats(serverStatsCounter);
            }
        } catch (FileNotFoundException e) {
            PlurifyMain.LOGGER.error("Error reading alter stats file " + alterStatsFile.getName());
        }
    }

    private static void writePlayerAlterStatsToFile(ServerPlayer player, Alter alter) {
        JsonElement alterStatsJson = ServerStatsCounterAccess.getSTATS_CODEC().encodeStart(JsonOps.INSTANCE, alter.getStats()).getOrThrow();
        File alterStatsFile = null;
        try {
            alterStatsFile = getPlurifyWorldPlayerAlterStatsFile(player, alter).toFile();

            Writer output = new BufferedWriter(new FileWriter(alterStatsFile));
            output.write(alterStatsJson.toString());
            output.close();
        } catch (IOException e) {
            PlurifyMain.LOGGER.error("Error writing alter stats file " + alterStatsFile.getName());
        }
    }

    /**
     * Used to combine the given PlayerAltersConfig into the existing alter_data.json file. Used for S2C sync
     * since the alters are not needed immediately by the client - they just need to be persisted to the
     * client-side alter_data.json
     */
    public static void syncNewAltersToJsonFile(PlayerAltersConfig newPlayerAltersConfig) {
        PlurifyMain.LOGGER.info("AltersIOHelper :: syncing new alters to json");
        try {
            PlayerAltersConfig combinedPlayerAltersConfig = combineNewAltersToExistingPlayerConfig(newPlayerAltersConfig);
            File playerFile = getPlayerAltersConfigFile(newPlayerAltersConfig.playerUuid()).toFile();
            Writer output = new BufferedWriter(new FileWriter(playerFile));

            JsonObject altersJson = (JsonObject) PlayerAltersConfig.CODEC.encodeStart(JsonOps.INSTANCE, combinedPlayerAltersConfig).getOrThrow();
            output.write(altersJson.toString());
            output.close();

        } catch (IOException e) {
            throw new RuntimeException(e);
        }
    }

    /**
     * Used to combine the given PlayerAltersConfig into the existing ServerPlayer. New alters only have their
     * headers (name, uuid, icon) created, no entity data is created until the player switches to those new alters.
     */
    public static void syncNewAltersToServerPlayer(ServerPlayer player, PlayerAltersConfig newPlayerAltersConfig) {
        PlurifyMain.LOGGER.info("AltersIOHelper :: syncing new alters to ServerPlayer");

        if (!player.getUUID().equals(newPlayerAltersConfig.playerUuid())) {
            PlurifyMain.LOGGER.error("AltersIOHelper :: player supplied does not match new alters config");
            return;
        }

        HashMap<UUID, Alter> existingPlayerAlters = ((IAltersOwner) player).plurify$getAlters();

        for (AlterBasicDetail alterBasicDetail : newPlayerAltersConfig.alters()) {
            existingPlayerAlters.computeIfAbsent(alterBasicDetail.uuid(), uuid -> {
                Alter alter = new Alter(alterBasicDetail.uuid(), Component.literal(alterBasicDetail.name()), player, false);
                alter.setIcon(alterBasicDetail.icon().getAsItemStack());
                return alter;
            });
        }

    }

    private static PlayerAltersConfig combineNewAltersToExistingPlayerConfig(PlayerAltersConfig newPlayerAltersConfig) {
        List<AlterBasicDetail> combinedAlters = new ArrayList<>();

        PlayerAltersConfig oldPlayerAltersConfig = getPlayerAltersConfig(newPlayerAltersConfig.playerUuid());
        if (oldPlayerAltersConfig != null) {
            combinedAlters.addAll(oldPlayerAltersConfig.alters());
        }

        for (AlterBasicDetail newAlter : newPlayerAltersConfig.alters()) {
            AlterBasicDetail matchingOldAlter = (combinedAlters.stream().filter(
                    oldAlter -> oldAlter.uuid().equals(newAlter.uuid()))).findFirst().orElse(null);

            if (matchingOldAlter != null) {
                combinedAlters.remove(matchingOldAlter);
            }

            combinedAlters.add(newAlter);
        }

        PlayerAltersConfig combinedPlayerAltersConfig = new PlayerAltersConfig(
                newPlayerAltersConfig.playerUuid(),
                newPlayerAltersConfig.inGameName(),
                newPlayerAltersConfig.currentAlterUuid(),
                combinedAlters
        );

        return combinedPlayerAltersConfig;
    }

    public static @Nullable PlayerAltersConfig getPlayerAltersConfig(UUID playerUUID) {
        File playerFile = getPlayerAltersConfigFile(playerUUID).toFile();
        try {
            if (!playerFile.exists()) {
                return null;
            }
            JsonObject altersJson = (JsonObject) JsonParser.parseReader(new FileReader(playerFile));
            PlayerAltersConfig playerAltersConfig = PlayerAltersConfig.CODEC.decode(JsonOps.INSTANCE, altersJson).getOrThrow().getFirst();
            return playerAltersConfig;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static PlayerAltersConfig getPlayerAltersConfig(Player player) {
        return getPlayerAltersConfig(player.getUUID());
    }

    // ---- HELPER METHODS FOR NAVIGATINGTO PLAYER/ALTER FILES ----

    /**
     * Config file containing the instance-wide metadata for all players. Metadata
     * consists of alter names, icons, and (client only) skin files.
     */
    public static Path getPlurifyConfigDir() {
        return FabricLoader.getInstance().getConfigDir().resolve(PlurifyMain.MOD_ID);
    }

    /**
     * Config directory for metadata for a specific player. Metadata consists of alter names, icons,
     * and (client only) skin files.
     */
    public static Path getPlayerConfigDir(Player player) {
        return getPlayerConfigDir(player.getUUID());
    }

    /**
     * Config directory for metadata for a specific player. Metadata consists of alter names, icons,
     * and (client only) skin files.
     */
    public static Path getPlayerConfigDir(UUID playerUuid) {
        Path plurifyConfigDir = getPlurifyConfigDir();
        plurifyConfigDir.toFile().mkdir();
        return plurifyConfigDir.resolve(playerUuid.toString());
    }

    public static Path getPlayerAltersConfigFile(UUID uuid) {
        Path playerConfigDir = getPlayerConfigDir(uuid);
        playerConfigDir.toFile().mkdir();
        return playerConfigDir.resolve("alter_data.json");
    }

    /**
     * Config file containing the instance-wide metadata for this player and corresponding alters. Metadata
     * consists of alter uuids, names, icons, and (client only) skin files.
     */
    private static Path getPlayerAltersConfigFile(Player player) {
        return getPlayerAltersConfigFile(player.getUUID());
    }

    /**
     * World-specific directory for all plurify data
     */
    public static Path getPlurifyWorldDir(Player player) {
        return player.level().getServer().getWorldPath(LevelResource.ROOT).resolve(MOD_DIR);
    }

    /**
     * World-specific directory for specific player
     */
    private static Path getPlurifyWorldPlayerDir(Player player) {
        Path plurifyWorldDir = getPlurifyWorldDir(player);
        plurifyWorldDir.toFile().mkdir();
        return plurifyWorldDir.resolve(player.getStringUUID());
    }

    /**
     * World-specific file containing NBT data for a single alter for a single player
     */
    private static Path getPlurifyWorldPlayerAlterDataFile(Player player, Alter alter) {
        Path plurifyWorldPlayerDir = getPlurifyWorldPlayerDir(player);
        plurifyWorldPlayerDir.toFile().mkdir();
        return plurifyWorldPlayerDir.resolve(alter.getUuid() + "-" + "entitydata.nbt");
    }

    private static Path getPlurifyWorldPlayerAlterAdvancementsFile(Player player, Alter alter) {
        Path plurifyWorldPlayerDir = getPlurifyWorldPlayerDir(player);
        plurifyWorldPlayerDir.toFile().mkdir();
        return plurifyWorldPlayerDir.resolve(alter.getUuid() + "-" + "advancements.json");
    }

    private static Path getPlurifyWorldPlayerAlterStatsFile(Player player, Alter alter) {
        Path plurifyWorldPlayerDir = getPlurifyWorldPlayerDir(player);
        plurifyWorldPlayerDir.toFile().mkdir();
        return plurifyWorldPlayerDir.resolve(alter.getUuid() + "-" + "stats.json");
    }
}
