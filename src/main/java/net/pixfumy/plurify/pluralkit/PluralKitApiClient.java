package net.pixfumy.plurify.pluralkit;

import com.google.gson.*;
import kong.unirest.Unirest;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.CommonColors;
import net.pixfumy.plurify.PlurifyMain;
import net.pixfumy.plurify.alter.AlterBasicDetail;
import net.pixfumy.plurify.alter.PlayerAltersConfig;
import net.pixfumy.plurify.gui.icon.PlurifyIconUtil;

import java.util.ArrayList;
import java.util.InputMismatchException;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.atomic.AtomicReference;

class PluralKitApiClient {
    public static PlayerAltersConfig requestSystemMembersFromPluralKit(ServerPlayer player, String pkSystemId) {
        AtomicReference<PlayerAltersConfig> playerAltersConfig = new AtomicReference<>();
        try {
            if (!pkSystemId.matches("^[a-zA-Z0-9_]*$")) {
                throw new InputMismatchException();
            }

            Unirest.get("https://api.pluralkit.me/v2/systems/{pkSystemId}/members")
                    .routeParam("pkSystemId", pkSystemId)
                    .asString()
                    .ifFailure(stringHttpResponse -> {
                        String errorMessage = "";

                        if (stringHttpResponse.getBody() != null) {
                            JsonObject errorBody = (JsonObject) JsonParser.parseString(stringHttpResponse.getBody());
                            errorMessage = ": " + errorBody.get("message").getAsString();
                        }

                        PlurifyMain.LOGGER.error("PluralKitApiClient :: request failed with error " + errorMessage);
                        player.sendSystemMessage(Component.translatable("plurify.system_message.sync_alters_pk.fail.generic", errorMessage).withColor(CommonColors.RED));
                    })
                    .ifSuccess(stringHttpResponse -> {

                        List<AlterBasicDetail> members = new ArrayList<>();
                        JsonArray membersJsonArray = (JsonArray) JsonParser.parseString(stringHttpResponse.getBody());
                        for (JsonElement memberAsJsonElement : membersJsonArray) {
                            JsonObject memberAsJsonObject = (JsonObject) memberAsJsonElement;

                            String memberUuid = memberAsJsonObject.get("uuid").getAsString();
                            String memberName = memberAsJsonObject.get("name").getAsString();
                            members.add(new AlterBasicDetail(UUID.fromString(memberUuid), memberName, PlurifyIconUtil.getRandomAlterIcon()));
                        }

                        playerAltersConfig.set(new PlayerAltersConfig(player, members));
                        player.sendSystemMessage(Component.translatable("plurify.system_message.sync_alters_pk.success").withColor(CommonColors.GREEN));
                    });
        } catch (InputMismatchException e) {
            PlurifyMain.LOGGER.error("PluralKitApiClient :: request failed because supplied system ID is not an alphanumeric.", e);
            player.sendSystemMessage(Component.translatable("plurify.system_message.sync_alters_pk.fail.alphanumeric").withColor(CommonColors.RED));
        }
        return playerAltersConfig.get();
    }
}
