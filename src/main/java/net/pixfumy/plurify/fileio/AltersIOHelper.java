package net.pixfumy.plurify.fileio;

import com.google.gson.*;
import com.google.gson.stream.JsonWriter;
import com.mojang.authlib.minecraft.client.ObjectMapper;
import com.mojang.serialization.Codec;
import com.mojang.serialization.JsonOps;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
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
import net.pixfumy.plurify.mixin.access.ServerStatsCounterAccess;
import org.jspecify.annotations.Nullable;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

/**
 * Handles common IO operations for Plurify alters.
 *
 * Each player has an alters config file under /config/plurify/<playerUuid>/alter_data.json which stores metadata -
 * the uuid of the selected Alter, as well as the names, uuid's, icons of all alters. This file is shared
 * across worlds.
 *
 * Each alter subsequently has 3 world-specific files - advancements, stats, and entity data.
 *
 * The file structure is as follows:
 *
 * MULTIPLAYER SERVERS:
 * .minecraft
 *      /config/plurify/<playerUuid>/alter_data.json
 *      /world/plurify/<playerUuid>/
 *          <alterUuid>-entitydata.nbt
 *          <alterUuid>-advancements.json
 *          <alterUuid>-stats.json
 *
 * SINGLE PLAYER INSTANCES:
 *      /config/plurify/<playerUuid>/alter_data.json
 *      /saves/<world>/plurify/<playerUuid>
 *          <alterUuid>-entitydata.nbt
 *          <alterUuid>-advancements.json
 *          <alterUuid>-stats.json
 *
 * Where you may have several playerUuids on a multiplayer server, and several alterUuids for one player.
 *
 * Alters are read/written first by metadata, and then by advancements, stats, and entity data.
 *
 * @Author pixfumy
 */
public class AltersIOHelper {
    private static final String MOD_DIR = PlurifyMain.MOD_ID;

    /**
     * Attempts to load the player's alters from the instance-wide alters config file, and then
     * attempts to load the world-specific data (advancements, stats, entity data) for each alter.
     *
     * If the mod is being introduced to this instance for the first time, the alters config file will not exist,
     * so we create a default one copying over the player's data.
     *
     * If the mod is being introduced to a world on an instance which does recognize the system, the alters config file
     * exists but the world-specific files don't, so copy the player data to the currently selected alter and proceed.
     */
    public static void loadPlayerAltersFromFile(ServerPlayer player) {

        HashMap<UUID, Alter> alters = new HashMap<>();

        // Read the alter_data.json file if it exists
        PlayerAltersConfig playerAltersConfig = loadPlayerAltersConfigFromFile(player);

        // If not, create a default alter, copying the player's entity data, achievements, stats
        if (playerAltersConfig == null) {
            PlurifyMain.LOGGER.info("AltersIOHelper :: No alter_data.json found for " + player.getScoreboardName() + ", creating 1 default alter.");

            Alter alter = new Alter(player, true);
            ((IAltersOwner) player).plurify$setAlters(new HashMap<>() {{put(alter.getUuid(), alter);}});
            ((IAltersOwner) player).plurify$setCurrentAlter(alter);

            playerAltersConfig = new PlayerAltersConfig(player);
        }

        UUID currentAlterUuid = playerAltersConfig.currentAlterUuid();


        // If the world has a non-empty /plurify directory, read world-specific data for all alters.
        File plurifyWorldPlayerDir = getPlurifyWorldPlayerDir(player).toFile();
        if (plurifyWorldPlayerDir.isDirectory() && plurifyWorldPlayerDir.list().length > 0) {

            PlurifyMain.LOGGER.info("AltersIOHelper :: loading entity data, stats, and advancements for " + playerAltersConfig.alters().size()
                    + " alters for player " + player.getScoreboardName());

            for (AlterBasicDetail alterBasicDetail : playerAltersConfig.alters()) {
                Alter alter = new Alter(alterBasicDetail.uuid(), Component.literal(alterBasicDetail.name()), player, false);

                readPlayerAlterEntityDataFromFile(player, alter);
                readPlayerAlterAdvancementsFromFile(player, alter);
                readPlayerAlterStatsFromFile(player, alter);

                if (currentAlterUuid.equals(alter.getUuid())) {
                    ((IAltersOwner) player).plurify$switchToAlter(alter);
                }

                alter.setIcon(alterBasicDetail.icon().getAsItemStack());

                alters.put(alter.getUuid(), alter);
            }

            PlurifyMain.LOGGER.info("AltersIOHelper :: Successfully loaded entity data, stats, and advancements for " + playerAltersConfig.alters().size()
                    + " alters for player " + player.getScoreboardName());
        } else { // Otherwise, copy the entity data of the player to the currently selected alter and create all other alters from scratch

            PlurifyMain.LOGGER.info("AltersIOHelper :: Found no world-specific files for player "
                    + player.getScoreboardName() + ". Copying player data to current alter.");

            for (AlterBasicDetail alterBasicDetail : playerAltersConfig.alters()) {

                boolean isCurrentAlter = currentAlterUuid.equals(alterBasicDetail.uuid());

                Alter alter = new Alter(
                        alterBasicDetail.uuid(),
                        Component.literal(alterBasicDetail.name()),
                        player,
                        isCurrentAlter // copyEntityDataFromPlayer if and only if isCurrentAlter
                );

                alter.setIcon(alterBasicDetail.icon().getAsItemStack());

                alters.put(alter.getUuid(), alter);
            }
        }

        // in all cases, set the alters to the player and switch to the current alter
        ((IAltersOwner) player).plurify$setAlters(alters);
        ((IAltersOwner) player).plurify$switchToAlter(alters.get(currentAlterUuid));

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

            JsonWriter jsonWriter = new JsonWriter(new FileWriter(playerFile));

            Writer output = new BufferedWriter(new FileWriter(playerFile));
            output.write(playerAltersConfigJson.toString());
            output.close();

            PlurifyMain.LOGGER.info("AltersIOHelper :: Successfully wrote " + alters.size() + " alters for player " + player.getScoreboardName());
        } catch (IOException e) {
            PlurifyMain.LOGGER.error("Error writing alterJson config file " + playerFile.getName());
        }
    }

    private static int readPlayerAlterEntityDataFromFile(ServerPlayer player, Alter alter) {
        Path entityDataFile = getPlurifyWorldPlayerAlterDataFile(player, alter);
        if (!entityDataFile.toFile().exists()) {
            return -1;
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

            return 0;

        } catch (IOException | NoSuchElementException | NullPointerException e) {
            PlurifyMain.LOGGER.error("Error reading entity data file " + entityDataFile);

            return 1;
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

    private static int readPlayerAlterAdvancementsFromFile(ServerPlayer player, Alter alter) {
        File alterAdvancementsFile = getPlurifyWorldPlayerAlterAdvancementsFile(player, alter).toFile();
        try {
            if (!alterAdvancementsFile.exists()) {
                return -1;
            }
            JsonObject alterAdvancementsJson = (JsonObject) JsonParser.parseReader(new FileReader(alterAdvancementsFile));
            PlayerAdvancements.Data playerAdvancementsData = AdvancementUtil.decodeAdvancementsFromJson(alterAdvancementsJson);
            alter.setAdvancements(playerAdvancementsData);
            return 0;
        } catch (FileNotFoundException e) {
            PlurifyMain.LOGGER.error("Error reading alter advancements file " + alterAdvancementsFile.getName());
            return 1;
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

    private static int readPlayerAlterStatsFromFile(ServerPlayer player, Alter alter) {
        File alterStatsFile = getPlurifyWorldPlayerAlterStatsFile(player, alter).toFile();
        try {
            if (!alterStatsFile.exists()) {
                return -1;
            }
            JsonObject alterStatsJson = (JsonObject) JsonParser.parseReader(new FileReader(alterStatsFile));
            Map<Stat<?>, Integer> serverStatsCounter = ServerStatsCounterAccess.getSTATS_CODEC().decode(JsonOps.INSTANCE, alterStatsJson).getOrThrow().getFirst();
            alter.setStats(serverStatsCounter);
            return 0;
        } catch (FileNotFoundException e) {
            PlurifyMain.LOGGER.error("Error reading alter stats file " + alterStatsFile.getName());
            return 1;
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
     * client-side config file.
     */
    public static void syncNewAltersToConfigFile(PlayerAltersConfig newPlayerAltersConfig) {
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
     * metadata (name, uuid, icon) created, no entity data is created until the player switches to those new alters.
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

        PlayerAltersConfig oldPlayerAltersConfig = loadPlayerAltersConfigFromFile(newPlayerAltersConfig.playerUuid());
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

    public static @Nullable PlayerAltersConfig loadPlayerAltersConfigFromFile(UUID playerUUID) {
        File playerFile = getPlayerAltersConfigFile(playerUUID).toFile();
        try {
            if (!playerFile.exists()) {
                return null;
            }
            PlurifyMain.LOGGER.info("AltersIOHelper :: found alter_data.json file for " + playerUUID);

            JsonObject altersJson = (JsonObject) JsonParser.parseReader(new FileReader(playerFile));
            PlayerAltersConfig playerAltersConfig = PlayerAltersConfig.CODEC.decode(JsonOps.INSTANCE, altersJson).getOrThrow().getFirst();
            return playerAltersConfig;
        } catch (FileNotFoundException e) {
            throw new RuntimeException(e);
        }
    }

    public static @Nullable PlayerAltersConfig loadPlayerAltersConfigFromFile(Player player) {
        return loadPlayerAltersConfigFromFile(player.getUUID());
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
    static Path getPlurifyWorldPlayerAlterDataFile(Player player, Alter alter) {
        Path plurifyWorldPlayerDir = getPlurifyWorldPlayerDir(player);
        plurifyWorldPlayerDir.toFile().mkdir();
        return plurifyWorldPlayerDir.resolve(alter.getUuid() + "-" + "entitydata.nbt");
    }

    public static Path getPlurifyWorldPlayerAlterAdvancementsFile(Player player, Alter alter) {
        Path plurifyWorldPlayerDir = getPlurifyWorldPlayerDir(player);
        plurifyWorldPlayerDir.toFile().mkdir();
        return plurifyWorldPlayerDir.resolve(alter.getUuid() + "-" + "advancements.json");
    }

    static Path getPlurifyWorldPlayerAlterStatsFile(Player player, Alter alter) {
        Path plurifyWorldPlayerDir = getPlurifyWorldPlayerDir(player);
        plurifyWorldPlayerDir.toFile().mkdir();
        return plurifyWorldPlayerDir.resolve(alter.getUuid() + "-" + "stats.json");
    }
}
