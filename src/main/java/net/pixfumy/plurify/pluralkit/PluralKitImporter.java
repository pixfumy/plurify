package net.pixfumy.plurify.pluralkit;

import net.minecraft.server.level.ServerPlayer;
import net.pixfumy.plurify.alter.PlayerAltersConfig;
import net.pixfumy.plurify.fileio.AltersIOHelper;
import net.pixfumy.plurify.gui.MainAltersGui;

public class PluralKitImporter {

    public static void importAltersFromPluralKit(ServerPlayer player, String pkSystemId) {
        PlayerAltersConfig pkAltersConfig = PluralKitApiClient.requestSystemMembersFromPluralKit(player, pkSystemId);
        if (pkAltersConfig != null) {
            AltersIOHelper.syncNewAltersToJsonFile(pkAltersConfig);
            AltersIOHelper.loadPlayerAltersFromFile(player);
        }
        // Unlike SyncAltersC2SPacket, this flow is synchronous so we don't need to do a hack to reopen any Gui here
    }

}
