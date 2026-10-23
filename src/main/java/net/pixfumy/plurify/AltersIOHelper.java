package net.pixfumy.plurify;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.UUIDUtil;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.nbt.*;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.ai.attributes.AttributeInstance;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.vehicle.VehicleEntity;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.LevelResource;
import net.minecraft.world.level.storage.TagValueInput;
import net.minecraft.world.level.storage.TagValueOutput;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.pixfumy.plurify.gui.icon.PlurifyIcon;
import net.pixfumy.plurify.gui.icon.PlurifyIconUtil;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class AltersIOHelper {
    private static final String MOD_DIR = PlurifyMain.MOD_ID;

    public static void loadPlayerAltersFromFile(ServerPlayer player) {
        File playerFile = getPlayerAltersConfigFile(player).toFile();
        HashMap<UUID, Alter> alters;

        if (playerFile.exists()) {
            try {
                JsonObject playerJson = (JsonObject) JsonParser.parseReader(new FileReader(playerFile));

                String currentAlterUuid = playerJson.get("currentAlterUuid").getAsString();

                Object altersObj = playerJson.get("alters");
                JsonArray altersJsonArray = (JsonArray) altersObj;

                alters = new HashMap<>();
                for (JsonElement alterObj : altersJsonArray) {
                    JsonObject alterJson = (JsonObject) alterObj;
                    String alterUuidAsString = alterJson.get("uuid").getAsString();
                    UUID alterUuid = UUID.fromString(alterUuidAsString);

                    String alterNameAsString = alterJson.get("name").getAsString();

                    Component alterName = Component.literal(alterNameAsString);

                    Alter alter = new Alter(alterUuid, alterName, player, false);

                    JsonObject alterIconJson = alterJson.getAsJsonObject("icon");
                    ItemStack itemStack;
                    if (alterIconJson.has("plurifyIconId")) {
                        String plurifyIconId = alterIconJson.get("plurifyIconId").getAsString();
                        itemStack = PlurifyIconUtil.getPlurifyIconById(plurifyIconId).getAsItemStack();
                    } else {
                        itemStack = BuiltInRegistries.ITEM.getValue(Identifier.parse(alterIconJson.get("item").getAsString())).getDefaultInstance();
                    }
                    alter.setIcon(itemStack);

                    alters.put(alterUuid, alter);
                    readPlayerAlterEntityDataFromFile(player, alter);

                    if (currentAlterUuid.equals(alterUuidAsString)) {
                        ((IAltersOwner)player).plurify$switchToAlter(alter);
                    }
                }

                ((IAltersOwner) player).plurify$setAlters(alters);

            } catch (IOException | NumberFormatException e) {
                PlurifyMain.LOGGER.error("Error reading alterJson config file " + playerFile.getName());
            }
        } else {
            Alter alter = new Alter(player, true);
            ((IAltersOwner) player).plurify$setAlters(new HashMap<>() {{put(alter.getUuid(), alter);}});
            ((IAltersOwner) player).plurify$setCurrentAlter(alter);
            writePlayerAltersToFile(player);
        }
    }

    public static void writePlayerAltersToFile(ServerPlayer player) {
        Path playerDir = getPlurifyWorldPlayerDir(player);
        File playerFile = getPlayerAltersConfigFile(player).toFile();

        JsonObject playerJson = new JsonObject();
        playerJson.addProperty("uuid", player.getStringUUID());
        playerJson.addProperty("inGameName", player.getScoreboardName());
        playerJson.addProperty("currentAlterUuid", ((IAltersOwner)player).plurify$getCurrentAlter().getUuid().toString());

        HashMap<UUID, Alter> alters = ((IAltersOwner)player).plurify$getAlters();
        JsonArray alterJsons = new JsonArray();

        for (Alter alter : alters.values()) {
            JsonObject alterJson = new JsonObject();
            alterJson.addProperty("uuid", alter.getUuid().toString());
            alterJson.addProperty("name", alter.getName().getString());

            JsonObject alterIcon = new JsonObject();
            ItemStack alterIconStack = alter.getIcon();
            alterIcon.addProperty("item", BuiltInRegistries.ITEM.getKey(alterIconStack.getItem()).toString());
            PlurifyIcon alterIconAsPlurifyIcon = PlurifyIconUtil.getAsPlurifyItem(alterIconStack);
            if (alterIconAsPlurifyIcon != null) {
                alterIcon.addProperty("plurifyIconId", alterIconAsPlurifyIcon.id);
            }
            alterJson.add("icon", alterIcon);

            alterJsons.add(alterJson);
            writePlayerAlterEntityDataToFile(player, alter);
        }

        playerJson.add("alters", alterJsons);

        try {
            getPlurifyWorldDir(player).toFile().mkdir();
            playerDir.toFile().mkdir();
            playerFile.createNewFile();

            Writer output = new BufferedWriter(new FileWriter(playerFile));
            output.write(playerJson.toString());
            output.close();
        } catch (IOException e) {
            PlurifyMain.LOGGER.error("Error writing alterJson config file " + playerFile.getName());
        }
    }

    private static void readPlayerAlterEntityDataFromFile(ServerPlayer player, Alter alter) {
        Path entityDataFile = getPlurifyWorldPlayerAlterFile(player, alter);
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

            alter.setLevel(player.level().getServer().getLevel(nbtReadView.read("World", ServerLevel.RESOURCE_KEY_CODEC).get()));
            alter.setPosition(nbtReadView.read("Position", Vec3.CODEC).get());
            alter.setRotation(nbtReadView.read("Rotation", Vec2.CODEC).get());

            alter.setRespawn(nbtReadView.read("Respawn", ServerPlayer.RespawnConfig.CODEC).orElse(null));

            alter.setHealth(nbtReadView.read("Health", Codec.FLOAT).get());
            alter.setAbsorptionAmount(nbtReadView.read("AbsorptionAmount", Codec.FLOAT).get());

            alter.setExperienceLevel(nbtReadView.read("ExperienceLevel", Codec.INT).get());
            alter.setTotalExperience(nbtReadView.read("TotalExperience", Codec.INT).get());

            alter.setFoodLevel(nbtReadView.read("FoodLevel", Codec.INT).get());
            alter.setSaturationLevel(nbtReadView.read("SaturationLevel", Codec.FLOAT).get());
            alter.setExhaustion(nbtReadView.read("ExperienceLevel", Codec.FLOAT).get());
            alter.setFoodTickTimer(nbtReadView.read("FoodTickTimer", Codec.INT).get());

            alter.setVehicle(nbtReadView.read("Vehicle", CompoundTag.CODEC).get());


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
        for (int i = 0; i < alter.getAttributes().size(); i++) {
            packedAttributeOutputList.add(packedAttributes.get(i));
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

        nbtWriteView.store("FoodLevel", Codec.INT, alter.getFoodLevel());
        nbtWriteView.store("SaturationLevel", Codec.FLOAT, alter.getSaturationLevel());
        nbtWriteView.store("Exhaustion", Codec.FLOAT, alter.getExhaustion());
        nbtWriteView.store("FoodTickTimer", Codec.INT, alter.getTotalExperience());

        nbtWriteView.store("KeepInventory", Codec.BOOL, alter.hasKeepInventory());

        CompoundTag vehicle = alter.getVehicle();
        if (vehicle != null) {
            nbtWriteView.store("Vehicle", CompoundTag.CODEC, vehicle);
        }

        Path entityDataFile = getPlurifyWorldPlayerAlterFile(player, alter);
        try {
            NbtIo.write(nbtWriteView.buildResult(), entityDataFile);
        } catch (IOException e) {
            PlurifyMain.LOGGER.error("Error writing entity data file " + entityDataFile);
        }
    }

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
        Path plurifyConfigDir = getPlurifyConfigDir();
        plurifyConfigDir.toFile().mkdir();
        return plurifyConfigDir.resolve(player.getStringUUID());
    }

    /**
     * Config file containing the instance-wide metadata for this player and corresponding alters. Metadata
     * consists of alter uuids, names, icons, and (client only) skin files.
     */
    private static Path getPlayerAltersConfigFile(Player player) {
        Path playerConfigDir = getPlayerConfigDir(player);
        playerConfigDir.toFile().mkdir();
        return playerConfigDir.resolve("alter_data.json");
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
    private static Path getPlurifyWorldPlayerAlterFile(Player player, Alter alter) {
        Path plurifyWorldPlayerDir = getPlurifyWorldPlayerDir(player);
        plurifyWorldPlayerDir.toFile().mkdir();
        return plurifyWorldPlayerDir.resolve(alter.getUuid() + "-" + "entitydata.nbt");
    }
}
