package net.pixfumy.plurify;

import com.google.gson.Gson;
import com.mojang.authlib.GameProfile;
import kong.unirest.HttpResponse;
import kong.unirest.JsonNode;
import kong.unirest.Unirest;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.gui.screen.TitleScreen;
import net.minecraft.client.gui.screen.multiplayer.ConnectScreen;
import net.minecraft.client.gui.screen.multiplayer.MultiplayerScreen;
import net.minecraft.client.network.ClientLoginNetworkHandler;
import net.minecraft.client.network.ClientPlayerEntity;
import net.minecraft.client.network.ServerAddress;
import net.minecraft.client.network.ServerInfo;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.network.ClientConnection;
import net.minecraft.network.NetworkPhase;
import net.minecraft.network.packet.c2s.handshake.ConnectionIntent;
import net.minecraft.network.packet.c2s.handshake.HandshakeC2SPacket;
import net.minecraft.network.packet.c2s.login.LoginHelloC2SPacket;
import net.minecraft.text.Text;

import java.io.BufferedReader;
import java.io.File;
import java.io.IOException;
import java.io.InputStreamReader;
import java.net.*;
import java.time.Duration;
import java.util.List;
import java.util.Optional;

public class MojangSkinChangeHelper {
    public static void setSkinTextureGlobal(File skinFile, PlayerSkinType playerSkinType) {
        MinecraftClient minecraftClient = MinecraftClient.getInstance();
        ClientPlayerEntity player = minecraftClient.player;

        HttpResponse<String> httpResponse = Unirest.post("https://api.minecraftservices.com/minecraft/profile/skins")
                .header("Authorization", "Bearer " + MinecraftClient.getInstance().getSession().getAccessToken())
                .field("file", skinFile)
                .field("variant", playerSkinType == PlayerSkinType.WIDE ? "classic" : "slim")
                .asString();

        if (httpResponse.isSuccess()) {

            player.networkHandler.sendChatMessage("Global skin successfully changed to " + skinFile + ".");

            ServerInfo serverInfo = minecraftClient.getCurrentServerEntry();
            if (minecraftClient.getServer() == null && (serverInfo == null || !serverInfo.isLocal())) {
                MinecraftClient.getInstance().disconnect(Text.literal("Changing Skin"));
                ConnectScreen.connect(minecraftClient.currentScreen, minecraftClient, ServerAddress.parse(serverInfo.address), serverInfo, false, null);
            }
        } else {
            PlurifyClient.LOGGER.info("Error changing global skin to " + skinFile + ". Got back response " + httpResponse.getStatus());
            player.networkHandler.sendChatMessage("Error changing global skin to " + skinFile + ". Mojang servers may be unavailable.");
        }
    }
}
