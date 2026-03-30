package net.pixfumy.plurify;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.entity.EntityEquipment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.minidev.json.JSONArray;
import net.minidev.json.JSONObject;
import net.minidev.json.JSONStyle;
import net.minidev.json.parser.JSONParser;
import net.minidev.json.parser.ParseException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.io.*;
import java.nio.file.Path;
import java.util.ArrayList;
import java.util.List;

public class Plurify implements ModInitializer {
	public static final String MOD_ID = "plurify";
	private static final String MOD_CONFIG_DIR = MOD_ID;

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Plurify successfully loaded.");
		ServerPlayConnectionEvents.JOIN.register((handler, sender, server) -> {
			loadPlayerAltersFromConfigFile(handler.player);
		});
	}

	private static void loadPlayerAltersFromConfigFile(ServerPlayerEntity player) {
		Path plurifyDir = FabricLoader.getInstance().getConfigDir().resolve(MOD_CONFIG_DIR);

		String uuid = player.getUuidAsString();
		File playerFile = plurifyDir.resolve(uuid + ".json").toFile();

		if (playerFile.exists()) {
			try {
				JSONParser jsonParser = new JSONParser();

				FileInputStream fileInputStream = new FileInputStream(playerFile);
				Object playerObj = jsonParser.parse(fileInputStream);
				JSONObject playerJson = (JSONObject) playerObj;

				Object altersObj = playerJson.get("alters");
				JSONArray altersJsonArray = (JSONArray) altersObj;

				List<Alter> alters = new ArrayList<Alter>();
				for (Object alterObj : altersJsonArray) {
					JSONObject alter = (JSONObject) alterObj;
					Text alterName = Text.literal((String) alter.get("name"));
					alters.add(new Alter(alterName, new PlayerInventory(player, new EntityEquipment())));
				}

				((IAltersOwner) player).setAlters(alters);
				fileInputStream.close();
            } catch (IOException | ParseException | NumberFormatException e) {
				LOGGER.error("Error reading alter config file " + playerFile.getName());
			}
		} else {
			JSONObject jsonObject = new JSONObject();
			jsonObject.put("uuid", uuid);
			jsonObject.put("inGameName", player.getNameForScoreboard());

			JSONObject defaultAlter = new JSONObject();
			defaultAlter.put("name", "default");
			jsonObject.put("alters", List.of(defaultAlter));
            try {
				Writer output = new BufferedWriter(new FileWriter(playerFile));
				output.write(jsonObject.toJSONString(JSONStyle.NO_COMPRESS));
				output.close();
            } catch (IOException e) {
				LOGGER.error("Error writing alter config file " + playerFile.getName());
			}
        }
	}

}