package net.pixfumy.plurify;

import com.google.gson.*;
import com.mojang.serialization.Codec;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.ItemStack;
import net.minecraft.nbt.*;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.storage.NbtReadView;
import net.minecraft.storage.NbtWriteView;
import net.minecraft.storage.ReadView;
import net.minecraft.storage.WriteView;
import net.minecraft.text.Text;
import net.minecraft.util.ErrorReporter;
import net.minecraft.util.Identifier;
import net.minecraft.util.WorldSavePath;
import net.minecraft.util.math.GlobalPos;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;

import java.io.*;
import java.nio.file.Path;
import java.util.*;

public class AltersIOHelper {
    private static final String MOD_DIR = PlurifyMain.MOD_ID;

    public static void loadPlayerAltersFromFile(ServerPlayerEntity player) {
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

                    Text alterName = Text.literal(alterNameAsString);

                    Alter alter = new Alter(alterUuid, alterName, player, false);

                    String alterIconIdentifier = alterJson.get("icon").getAsString();
                    alter.setIcon(Registries.ITEM.get(Identifier.of(alterIconIdentifier)));

                    alters.put(alterUuid, alter);
                    readPlayerAlterEntityDataFromFile(player, alter);

                    if (currentAlterUuid.equals(alterUuidAsString)) {
                        ((IAltersOwner)player).plurify$setCurrentAlter(alter);
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

    public static void writePlayerAltersToFile(ServerPlayerEntity player) {
        Path playerDir = getPlurifyWorldPlayerDir(player);
        File playerFile = getPlayerAltersConfigFile(player).toFile();

        JsonObject playerJson = new JsonObject();
        playerJson.addProperty("uuid", player.getUuidAsString());
        playerJson.addProperty("inGameName", player.getNameForScoreboard());
        playerJson.addProperty("currentAlterUuid", ((IAltersOwner)player).plurify$getCurrentAlter().getUuid().toString());

        HashMap<UUID, Alter> alters = ((IAltersOwner)player).plurify$getAlters();
        JsonArray alterJsons = new JsonArray();

        for (Alter alter : alters.values()) {
            JsonObject alterJson = new JsonObject();
            alterJson.addProperty("uuid", alter.getUuid().toString());
            alterJson.addProperty("name", alter.getName().getString());
            alterJson.addProperty("icon", Registries.ITEM.getId(alter.getIcon()).toString());
            alterJsons.add(alterJson);
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

    public static void readPlayerAlterEntityDataFromFile(ServerPlayerEntity player, Alter alter) {
        Path entityDataFile = getPlurifyWorldPlayerAlterFile(player, alter);
        if (!entityDataFile.toFile().exists()) {
            return;
        }

        try {
            NbtCompound entityDataNbt = NbtIo.read(entityDataFile);

            ReadView nbtReadView = NbtReadView.create(new ErrorReporter.Logging(PlurifyMain.LOGGER), player.getRegistryManager(), entityDataNbt);
            List<ItemStack> mainInventoryListReadView = Objects.requireNonNull(nbtReadView.getTypedListView("Inventory", ItemStack.OPTIONAL_CODEC).stream().toList());
            for (int i = 0; i < mainInventoryListReadView.size(); i++) {
                alter.getPlayerInventory().setStack(i, mainInventoryListReadView.get(i));
            }

            List<ItemStack> enderChestInventoryListReadView = Objects.requireNonNull(nbtReadView.getTypedListView("EnderItems", ItemStack.OPTIONAL_CODEC).stream().toList());
            for (int i = 0; i < enderChestInventoryListReadView.size(); i++) {
                alter.getEnderChestInventory().setStack(i, enderChestInventoryListReadView.get(i));
            }

            alter.setPosition(nbtReadView.read("Position", Vec3d.CODEC).get());
            alter.setRotation(nbtReadView.read("Rotation", Vec2f.CODEC).get());

            alter.setRespawn(nbtReadView.read("Respawn", ServerPlayerEntity.Respawn.CODEC).get());

            alter.setHealth(nbtReadView.read("Health", Codec.FLOAT).get());

            alter.setExperienceLevel(nbtReadView.read("ExperienceLevel", Codec.INT).get());
            alter.setTotalExperience(nbtReadView.read("TotalExperience", Codec.INT).get());

            alter.setFoodLevel(nbtReadView.read("FoodLevel", Codec.INT).get());
            alter.setSaturationLevel(nbtReadView.read("SaturationLevel", Codec.FLOAT).get());
            alter.setExhaustion(nbtReadView.read("ExperienceLevel", Codec.FLOAT).get());
            alter.setFoodTickTimer(nbtReadView.read("FoodTickTimer", Codec.INT).get());

            alter.setKeepInventory(nbtReadView.read("KeepInventory", Codec.BOOL).get());

            alter.setWorld(player.getEntityWorld().getServer().getWorld(nbtReadView.read("World", World.CODEC).get()));

        } catch (IOException | NoSuchElementException | NullPointerException e) {
            PlurifyMain.LOGGER.error("Error reading entity data file " + entityDataFile);
        }
    }

    public static void writePlayerAlterEntityDataToFile(ServerPlayerEntity player, Alter alter) {
        PlayerInventory playerInventory = alter.getPlayerInventory();

        NbtWriteView nbtWriteView = NbtWriteView.create(new ErrorReporter.Logging(PlurifyMain.LOGGER), player.getRegistryManager());
        WriteView.ListAppender<ItemStack> mainInventoryListAppender = nbtWriteView.getListAppender("Inventory", ItemStack.OPTIONAL_CODEC);
        for (int i = 0; i < playerInventory.size(); i++) {
            mainInventoryListAppender.add(playerInventory.getStack(i));
        }

        EnderChestInventory enderChestInventory = alter.getEnderChestInventory();
        WriteView.ListAppender<ItemStack> enderChestInventoryListAppender = nbtWriteView.getListAppender("EnderItems", ItemStack.OPTIONAL_CODEC);
        for (int i = 0; i < alter.getEnderChestInventory().size(); i++) {
            enderChestInventoryListAppender.add(enderChestInventory.getStack(i));
        }

        nbtWriteView.put("Position", Vec3d.CODEC, alter.getPosition());
        nbtWriteView.put("Rotation", Vec2f.CODEC, alter.getRotation());

        if (alter.getRespawn() != null) {
            nbtWriteView.put("Respawn", ServerPlayerEntity.Respawn.CODEC, alter.getRespawn());
        }

        nbtWriteView.put("Health", Codec.FLOAT, alter.getHealth());

        nbtWriteView.put("ExperienceLevel", Codec.INT, alter.getExperienceLevel());
        nbtWriteView.put("TotalExperience", Codec.INT, alter.getTotalExperience());

        nbtWriteView.put("FoodLevel", Codec.INT, alter.getFoodLevel());
        nbtWriteView.put("SaturationLevel", Codec.FLOAT, alter.getSaturationLevel());
        nbtWriteView.put("Exhaustion", Codec.FLOAT, alter.getExhaustion());
        nbtWriteView.put("FoodTickTimer", Codec.INT, alter.getTotalExperience());

        nbtWriteView.put("KeepInventory", Codec.BOOL, alter.hasKeepInventory());

        nbtWriteView.put("World", World.CODEC, alter.getWorld().getRegistryKey());

        Path entityDataFile = getPlurifyWorldPlayerAlterFile(player, alter);
        try {
            NbtIo.write(nbtWriteView.getNbt(), entityDataFile);
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
    public static Path getPlayerConfigDir(PlayerEntity player) {
        Path plurifyConfigDir = getPlurifyConfigDir();
        plurifyConfigDir.toFile().mkdir();
        return plurifyConfigDir.resolve(player.getUuidAsString());
    }

    /**
     * Config file containing the instance-wide metadata for this player and corresponding alters. Metadata
     * consists of alter uuids, names, icons, and (client only) skin files.
     */
    private static Path getPlayerAltersConfigFile(PlayerEntity player) {
        Path playerConfigDir = getPlayerConfigDir(player);
        playerConfigDir.toFile().mkdir();
        return playerConfigDir.resolve("alter_data.json");
    }

    /**
     * World-specific directory for all plurify data
     */
    public static Path getPlurifyWorldDir(PlayerEntity player) {
        return player.getEntityWorld().getServer().getSavePath(WorldSavePath.ROOT).resolve(MOD_DIR);
    }

    /**
     * World-specific directory for specific player
     */
    private static Path getPlurifyWorldPlayerDir(PlayerEntity player) {
        Path plurifyWorldDir = getPlurifyWorldDir(player);
        plurifyWorldDir.toFile().mkdir();
        return plurifyWorldDir.resolve(player.getUuidAsString());
    }

    /**
     * World-specific file containing NBT data for a single alter for a single player
     */
    private static Path getPlurifyWorldPlayerAlterFile(PlayerEntity player, Alter alter) {
        Path plurifyWorldPlayerDir = getPlurifyWorldPlayerDir(player);
        plurifyWorldPlayerDir.toFile().mkdir();
        return plurifyWorldPlayerDir.resolve(alter.getUuid() + "-" + "entitydata.nbt");
    }
}
