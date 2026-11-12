package net.pixfumy.plurify;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.TextColor;
import net.minecraft.resources.Identifier;
import net.minecraft.util.CommonColors;
import net.pixfumy.plurify.alter.PlayerAltersConfig;
import net.pixfumy.plurify.fileio.AltersIOHelper;
import net.pixfumy.plurify.gui.AlterSkinScreen;
import net.pixfumy.plurify.network.*;
import net.pixfumy.plurify.network.altersync.RequestAltersSyncS2CPacket;
import net.pixfumy.plurify.network.altersync.SyncAltersC2SPacket;
import net.pixfumy.plurify.network.altersync.SyncAltersS2CPacket;
import net.pixfumy.plurify.network.handshake.PlurifyHandshakeC2SPacket;
import net.pixfumy.plurify.network.handshake.PlurifyHandshakeS2CPacket;
import net.pixfumy.plurify.network.skinchange.OpenAlterSkinScreenS2CPacket;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlurifyClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger(PlurifyMain.MOD_ID + "_client");

	private static final KeyMapping.Category CATEGORY = KeyMapping.Category.register(
			Identifier.fromNamespaceAndPath(PlurifyMain.MOD_ID, PlurifyMain.MOD_ID + "_keybinds")
	);

	@Override
	public void onInitializeClient() {

		KeyMapping plurifyOpenAltersKey = KeyMappingHelper.registerKeyMapping(
				new KeyMapping("key." + PlurifyMain.MOD_ID + ".open_alters_menu",
						InputConstants.Type.KEYBOARD,
						InputConstants.KEY_K,
						CATEGORY
				));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (plurifyOpenAltersKey.isDown()) {
				client.getConnection().sendCommand("alters");
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(PlurifyHandshakeS2CPacket.ID, ((playerSwitchAlterS2CPacket, context) -> {
			LOGGER.info("PlurifyClient$globalReceiver(PlurifyHandshakeS2CPacket) :: received PlurifyHandshakeS2CPacket");
			LOGGER.info("PlurifyClient$globalReceiver(PlurifyHandshakeS2CPacket) :: sending back PlurifyHandshakeC2SPacket");

			ClientPlayNetworking.send(new PlurifyHandshakeC2SPacket());
		}));

		ClientPlayNetworking.registerGlobalReceiver(PlayerSwitchAlterS2CPacket.ID, ((playerSwitchAlterS2CPacket, context) -> {
			LOGGER.info("PlurifyClient$globalReceiver(PlayerSwitchAlterS2CPacket) :: received PlayerSwitchAlterS2CPacket: " + playerSwitchAlterS2CPacket.toString());

			NativeImage skinAsNativeImage = SkinsIOHelper.readPlayerAlterSkinFromFile(context.player(), playerSwitchAlterS2CPacket.alterId());

			LOGGER.info("PlurifyClient$globalReceiver(PlayerSwitchAlterS2CPacket) :: processed skin from local file - " +
					"attempting to set it for alter " + playerSwitchAlterS2CPacket.alterName() + " with UUID " + playerSwitchAlterS2CPacket.alterId());

			((ISkinOwner) context.player()).plurify$setCustomSkin(skinAsNativeImage, playerSwitchAlterS2CPacket.alterId());
		}));

		ClientPlayNetworking.registerGlobalReceiver(OpenAlterSkinScreenS2CPacket.ID, (payload, context) -> {
			LOGGER.info("PlurifyClient$globalReceiver(OpenAlterSkinScreenS2CPacket) :: received OpenAlterSkinScreenS2CPacket: " + payload.toString());
			LOGGER.info("PlurifyClient$globalReceiver(OpenAlterSkinScreenS2CPacket) :: attempting to open AlterSkinScreen.");

			context.client().gui.setScreen(new AlterSkinScreen(context.player(), payload.alterUuid(), payload.alterName()));
		});

		ClientPlayNetworking.registerGlobalReceiver(SyncAltersS2CPacket.ID, (payload, context) -> {
			LOGGER.info("PlurifyClient$globalReceiver(SyncAltersS2CPacket) :: received SyncAltersS2CPacket packet: " + payload.toString());
			AltersIOHelper.syncNewAltersToJsonFile(payload.playerAltersConfig());
			context.player().sendSystemMessage(Component.translatable("plurify.system_message.sync_alters_s2c.success").withColor(CommonColors.GREEN));
		});

		ClientPlayNetworking.registerGlobalReceiver(RequestAltersSyncS2CPacket.ID, (payload, context) -> {
			LOGGER.info("PlurifyClient$globalReceiver(RequestAltersSyncS2CPacket) :: received RequestAltersSyncS2CPacket");

			PlayerAltersConfig clientPlayerAltersConfig = AltersIOHelper.getPlayerAltersConfig(context.player().getUUID());
			if (clientPlayerAltersConfig != null) {
				SyncAltersC2SPacket outgoingPacket = new SyncAltersC2SPacket(AltersIOHelper.getPlayerAltersConfig(context.player().getUUID()));

				LOGGER.info("PlurifyClient$globalReceiver(RequestAltersSyncS2CPacket) :: sending back SyncAltersC2SPacket: " + outgoingPacket);

				ClientPlayNetworking.send(outgoingPacket);
			} else {
				context.player().sendSystemMessage(Component.translatable("plurify.system_message.sync_alters_c2s.fail").withColor(CommonColors.RED));
			}
		});
	}
}