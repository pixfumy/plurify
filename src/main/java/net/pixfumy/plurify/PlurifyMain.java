package net.pixfumy.plurify;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.network.ServerPlayerEntity;
import net.pixfumy.plurify.networking.OpenAlterSkinScreenS2CPayload;
import net.pixfumy.plurify.networking.PlayerSwitchAlterS2CPacket;
import net.pixfumy.plurify.networking.PlurifyHandshakeC2SPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlurifyMain implements ModInitializer {
	public static final String MOD_ID = "plurify";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Plurify successfully loaded.");

		PayloadTypeRegistry.playS2C().register(OpenAlterSkinScreenS2CPayload.ID, OpenAlterSkinScreenS2CPayload.CODEC);
		PayloadTypeRegistry.playS2C().register(PlayerSwitchAlterS2CPacket.ID, PlayerSwitchAlterS2CPacket.CODEC);
		PayloadTypeRegistry.playC2S().register(PlurifyHandshakeC2SPayload.ID, PlurifyHandshakeC2SPayload.CODEC);

		ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> {
			ServerPlayerEntity player = serverPlayNetworkHandler.player;
			AltersIOHelper.loadPlayerAltersFromFile(player);
			Alter currentAlter = ((IAltersOwner) player).plurify$getCurrentAlter();
			ServerPlayNetworking.send(serverPlayNetworkHandler.player,
					new PlayerSwitchAlterS2CPacket(currentAlter.getUuid().toString(), currentAlter.getName().getString()));
		});

		ServerPlayNetworking.registerGlobalReceiver(PlurifyHandshakeC2SPayload.ID, ((plurifyHandshakeC2SPayload, context) -> {
			((IAltersOwner) context.player()).plurify$setClientLoaded(true);
		}));
	}

}