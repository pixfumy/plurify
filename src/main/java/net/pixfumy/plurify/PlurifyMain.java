package net.pixfumy.plurify;

import com.mojang.serialization.Codec;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.pixfumy.plurify.networking.OpenAlterSkinScreenPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlurifyMain implements ModInitializer {
	public static final String MOD_ID = "plurify";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Plurify successfully loaded.");
		// In your common initializer method
		PayloadTypeRegistry.playS2C().register(OpenAlterSkinScreenPayload.ID, OpenAlterSkinScreenPayload.CODEC);
	}

}