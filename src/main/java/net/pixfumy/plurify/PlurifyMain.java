package net.pixfumy.plurify;

import eu.pb4.polymer.resourcepack.api.PolymerResourcePackUtils;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.ModInitializer;

import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry;
import net.fabricmc.fabric.api.networking.v1.ServerPlayConnectionEvents;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.CommonColors;
import net.pixfumy.plurify.alter.Alter;
import net.pixfumy.plurify.alter.IAltersOwner;
import net.pixfumy.plurify.fileio.AltersIOHelper;
import net.pixfumy.plurify.gui.AlterDetailsGui;
import net.pixfumy.plurify.gui.MainAltersGui;
import net.pixfumy.plurify.network.*;
import net.pixfumy.plurify.network.altersync.RequestAltersSyncS2CPacket;
import net.pixfumy.plurify.network.altersync.SyncAltersC2SPacket;
import net.pixfumy.plurify.network.altersync.SyncAltersS2CPacket;
import net.pixfumy.plurify.network.handshake.PlurifyHandshakeC2SPacket;
import net.pixfumy.plurify.network.handshake.PlurifyHandshakeS2CPacket;
import net.pixfumy.plurify.network.skinchange.CloseAlterSkinScreenC2SPacket;
import net.pixfumy.plurify.network.skinchange.OpenAlterSkinScreenS2CPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.UUID;

public class PlurifyMain implements ModInitializer {
	public static final String MOD_ID = "plurify";

	public static final Logger LOGGER = LoggerFactory.getLogger(MOD_ID);

	@Override
	public void onInitialize() {
		if (FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {
			PolymerResourcePackUtils.addModAssets(MOD_ID);
		}

		// Packets used for syncing alters between client and server config files
		PayloadTypeRegistry.clientboundPlay().register(RequestAltersSyncS2CPacket.ID, RequestAltersSyncS2CPacket.CODEC);
		PayloadTypeRegistry.clientboundPlay().register(SyncAltersS2CPacket.ID, SyncAltersS2CPacket.PACKET_CODEC);
		PayloadTypeRegistry.serverboundPlay().register(SyncAltersC2SPacket.ID, SyncAltersC2SPacket.PACKET_CODEC);

		// Packets used for opening and closing the AlterSkinScreen on the client
		PayloadTypeRegistry.clientboundPlay().register(OpenAlterSkinScreenS2CPacket.ID, OpenAlterSkinScreenS2CPacket.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(CloseAlterSkinScreenC2SPacket.ID, CloseAlterSkinScreenC2SPacket.CODEC);
		// Currently only used for skins, but can be used generally to notify the client that the alter has switched
		PayloadTypeRegistry.clientboundPlay().register(PlayerSwitchAlterS2CPacket.ID, PlayerSwitchAlterS2CPacket.CODEC);

		// Packets used to establish the "Plurify Handshake", where server knows client has the mod and vice-versa.
		// This is used to enable some optional client-only features
		PayloadTypeRegistry.clientboundPlay().register(PlurifyHandshakeS2CPacket.ID, PlurifyHandshakeS2CPacket.CODEC);
		PayloadTypeRegistry.serverboundPlay().register(PlurifyHandshakeC2SPacket.ID, PlurifyHandshakeC2SPacket.CODEC);

		ServerPlayConnectionEvents.JOIN.register((serverPlayNetworkHandler, packetSender, minecraftServer) -> {
			ServerPlayer player = serverPlayNetworkHandler.player;

			LOGGER.info("PlurifyMain$ServerPlayConnectionEvents.JOIN :: player joined with name " + player.getScoreboardName() + " with UUID " + player.getUUID());
			LOGGER.info("PlurifyMain$ServerPlayConnectionEvents.JOIN :: attempting to load player's system from file.");

			AltersIOHelper.loadPlayerAltersFromFile(player);
			Alter currentAlter = ((IAltersOwner) player).plurify$getCurrentAlter();

			LOGGER.info("PlurifyMain$ServerPlayConnectionEvents.JOIN :: player's current alter is " + currentAlter.getName() + " with UUID " + currentAlter.getUuid());

			LOGGER.info("PlurifyMain$ServerPlayConnectionEvents.JOIN :: sending PlurifyHandshakeS2CPacket");
			ServerPlayNetworking.send(serverPlayNetworkHandler.player,
					new PlurifyHandshakeS2CPacket());

			PlayerSwitchAlterS2CPacket playerSwitchAlterS2CPacket = new PlayerSwitchAlterS2CPacket(currentAlter.getUuid().toString(), currentAlter.getName().getString());
			LOGGER.info("PlurifyMain$ServerPlayConnectionEvents.JOIN :: sending PlayerSwitchAlterS2CPacket: " + playerSwitchAlterS2CPacket);
			ServerPlayNetworking.send(serverPlayNetworkHandler.player, playerSwitchAlterS2CPacket);
		});

		ServerPlayNetworking.registerGlobalReceiver(PlurifyHandshakeC2SPacket.ID, ((plurifyHandshakeC2SPayload, context) -> {
			LOGGER.info("PlurifyMain$globalReceiver(PlurifyHandshakeC2SPacket) :: received PlurifyHandshakeC2SPacket: " + plurifyHandshakeC2SPayload);
			LOGGER.info("PlurifyMain$globalReceiver(PlurifyHandshakeC2SPacket) :: enabling all client-side features for player " + context.player().getScoreboardName());

			((IAltersOwner) context.player()).plurify$setClientLoaded(true);
		}));

		ServerPlayNetworking.registerGlobalReceiver(SyncAltersC2SPacket.ID, ((syncAltersC2SPacket, context) -> {
			LOGGER.info("PlurifyMain$globalReceiver(SyncAltersC2SPacket) :: received SyncAltersC2SPacket: " + syncAltersC2SPacket);

			AltersIOHelper.syncNewAltersToJsonFile(syncAltersC2SPacket.playerAltersConfig());
			AltersIOHelper.loadPlayerAltersFromFile(context.player());
			// return to MainAltersGUI (last user action was triggering this sync from SyncSystemGui) to reflect the newly imported alters pretty seamlessly
			new MainAltersGui(context.player()).open();

			LOGGER.info("PlurifyMain$globalReceiver(SyncAltersC2SPacket) :: alters successfully synced for player " + context.player().getScoreboardName());
			context.player().sendSystemMessage(Component.translatable("plurify.system_message.sync_alters_c2s.success").withColor(CommonColors.GREEN));
		}));

		ServerPlayNetworking.registerGlobalReceiver(CloseAlterSkinScreenC2SPacket.ID, ((closeAlterSkinScreenC2SPayload, context) -> {
			LOGGER.info("PlurifyMain$globalReceiver(CloseAlterSkinScreenC2SPacket) :: received CloseAlterSkinScreenC2SPacket: " + closeAlterSkinScreenC2SPayload);

			ServerPlayer player = context.player();
			IAltersOwner playerAsAltersOwner = (IAltersOwner) player;

			Alter selectedAlter = playerAsAltersOwner.plurify$getAlters().getOrDefault(
					UUID.fromString(closeAlterSkinScreenC2SPayload.alterUuid()),
					((IAltersOwner) context.player()).plurify$getCurrentAlter()
			);

			playerAsAltersOwner.plurify$switchToAlter(playerAsAltersOwner.plurify$getCurrentAlter());

			new AlterDetailsGui(player, selectedAlter, new MainAltersGui(player)).open();
		}));

		LOGGER.info("Plurify successfully loaded.");
	}

}