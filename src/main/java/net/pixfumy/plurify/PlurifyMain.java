package net.pixfumy.plurify;

import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.pixfumy.plurify.gui.AlterDetailsGui;
import net.pixfumy.plurify.gui.MainAltersGui;
import net.pixfumy.plurify.networking.CloseAlterSkinScreenC2SPayload;
import net.pixfumy.plurify.networking.OpenAlterSkinScreenS2CPayload;
import net.pixfumy.plurify.networking.PlayerSwitchAlterS2CPacket;
import net.pixfumy.plurify.networking.PlurifyHandshakeC2SPayload;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class PlurifyMain implements ModInitializer {
	public static final String MOD_ID = "plurify";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		LOGGER.info("Plurify successfully loaded.");

		PayloadTypeRegistry.clientboundPlay().register(OpenAlterSkinScreenS2CPayload.ID, OpenAlterSkinScreenS2CPayload.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(PlayerSwitchAlterS2CPacket.ID, PlayerSwitchAlterS2CPacket.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(PlurifyHandshakeC2SPayload.ID, PlurifyHandshakeC2SPayload.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(CloseAlterSkinScreenC2SPayload.ID, CloseAlterSkinScreenC2SPayload.CODEC);

		ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> {
			ServerPlayer player = serverPlayNetworkHandler.player;
			AltersIOHelper.loadPlayerAltersFromFile(player);
			Alter currentAlter = ((IAltersOwner) player).plurify$getCurrentAlter();
			ServerPlayNetworking.send(serverPlayNetworkHandler.player,
					new PlayerSwitchAlterS2CPacket(currentAlter.getUuid().toString(), currentAlter.getName().getString()));
		});

		ServerPlayNetworking.registerGlobalReceiver(PlurifyHandshakeC2SPayload.ID, ((plurifyHandshakeC2SPayload, context) -> {
			((IAltersOwner) context.player()).plurify$setClientLoaded(true);
		}));

		ServerPlayNetworking.registerGlobalReceiver(CloseAlterSkinScreenC2SPayload.ID, ((closeAlterSkinScreenC2SPayload, context) -> {
			ServerPlayer player = context.player();
			IAltersOwner playerAsAltersOwner = (IAltersOwner) player;

			Alter selectedAlter = playerAsAltersOwner.plurify$getAlters().getOrDefault(
					UUID.fromString(closeAlterSkinScreenC2SPayload.alterUuid()),
					((IAltersOwner) context.player()).plurify$getCurrentAlter()
			);

			playerAsAltersOwner.plurify$switchToAlter(playerAsAltersOwner.plurify$getCurrentAlter());

			new AlterDetailsGui(player, selectedAlter, new MainAltersGui(player)).open();
		}));
	}

}