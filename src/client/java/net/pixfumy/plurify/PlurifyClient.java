package net.pixfumy.plurify;

import com.mojang.blaze3d.platform.InputConstants;
import com.mojang.blaze3d.platform.NativeImage;
import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.KeyMapping;
import net.minecraft.resources.Identifier;
import net.pixfumy.plurify.gui.AlterSkinScreen;
import net.pixfumy.plurify.networking.OpenAlterSkinScreenS2CPayload;
import net.pixfumy.plurify.networking.PlayerSwitchAlterS2CPacket;
import net.pixfumy.plurify.networking.PlurifyHandshakeC2SPayload;
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

		ClientPlayNetworking.registerGlobalReceiver(PlayerSwitchAlterS2CPacket.ID, ((playerSwitchAlterS2CPacket, context) -> {
			NativeImage skinAsNativeImage = SkinsIOHelper.readPlayerAlterSkinFromFile(context.player(), playerSwitchAlterS2CPacket.alterId());

			((ISkinOwner) context.player()).plurify$setCustomSkin(skinAsNativeImage, playerSwitchAlterS2CPacket.alterId());

			ClientPlayNetworking.send(new PlurifyHandshakeC2SPayload());
		}));

		ClientPlayNetworking.registerGlobalReceiver(OpenAlterSkinScreenS2CPayload.ID, (payload, context) -> {
			context.client().gui.setScreen(new AlterSkinScreen(context.player(), payload.alterUuid(), payload.alterName()));
		});
	}
}