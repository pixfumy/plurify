package net.pixfumy.plurify;

import net.fabricmc.api.ClientModInitializer;
import net.fabricmc.fabric.api.client.event.lifecycle.v1.ClientTickEvents;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.minecraft.client.gui.screen.ingame.InventoryScreen;
import net.minecraft.client.option.KeyBinding;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.util.InputUtil;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;
import net.pixfumy.plurify.gui.AlterSkinScreen;
import net.pixfumy.plurify.networking.OpenAlterSkinScreenPayload;
import org.lwjgl.glfw.GLFW;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class PlurifyClient implements ClientModInitializer {

	public static final Logger LOGGER = LoggerFactory.getLogger(PlurifyMain.MOD_ID + "Client");

	private static final KeyBinding.Category CATEGORY = KeyBinding.Category.create(
			Identifier.of(PlurifyMain.MOD_ID, PlurifyMain.MOD_ID + "_keybinds")
	);

	@Override
	public void onInitializeClient() {

		KeyBinding plurifyOpenAltersKey = KeyBindingHelper.registerKeyBinding(
				new KeyBinding("key." + PlurifyMain.MOD_ID + ".open_alters_menu",
						InputUtil.Type.KEYSYM,
						GLFW.GLFW_KEY_K,
						CATEGORY
						));

		ClientTickEvents.END_CLIENT_TICK.register(client -> {
			if (plurifyOpenAltersKey.wasPressed()) {
				client.getNetworkHandler().sendChatCommand("alters");
			}
		});

		ClientPlayNetworking.registerGlobalReceiver(OpenAlterSkinScreenPayload.ID, (payload, context) -> {
			context.client().setScreen(new AlterSkinScreen(context.player(), payload.alterName()));
		});
	}
}