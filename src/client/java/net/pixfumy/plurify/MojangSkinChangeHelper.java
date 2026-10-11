package net.pixfumy.plurify;

import kong.unirest.HttpResponse;
import kong.unirest.Unirest;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ConnectScreen;
import net.minecraft.client.multiplayer.ServerData;
import net.minecraft.client.multiplayer.resolver.ServerAddress;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.PlayerModelType;

import java.io.File;

public class MojangSkinChangeHelper {
    public static void setSkinTextureGlobal(File skinFile, PlayerModelType playerSkinType) {
        Minecraft minecraftClient = Minecraft.getInstance();
        LocalPlayer player = minecraftClient.player;

        HttpResponse<String> httpResponse = Unirest.post("https://api.minecraftservices.com/minecraft/profile/skins")
                .header("Authorization", "Bearer " + Minecraft.getInstance().getUser().getAccessToken())
                .field("file", skinFile)
                .field("variant", playerSkinType == PlayerModelType.WIDE ? "classic" : "slim")
                .asString();

        if (httpResponse.isSuccess()) {
            ServerData serverInfo = minecraftClient.getCurrentServer();
            if (minecraftClient.getSingleplayerServer() == null && (serverInfo == null || !serverInfo.isLan())) {
                Minecraft.getInstance().disconnectFromWorld(Component.translatable("plurify.skin_change_helper.server_restart_message"));
                ConnectScreen.startConnecting(minecraftClient.gui.screen(), minecraftClient, ServerAddress.parseString(serverInfo.ip), serverInfo, false, null);
                player.sendSystemMessage(Component.translatable("plurify.skin_change_helper.change_success").withColor(CommonColors.GREEN));
            }
        } else {
            PlurifyClient.LOGGER.info("Error changing global skin to " + skinFile + ". Got back response " + httpResponse.getStatus());
            player.sendSystemMessage(Component.translatable("plurify.skin_change_helper.change_fail").withColor(CommonColors.RED));
        }
    }
}
