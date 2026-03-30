package net.pixfumy.plurify;

import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.util.Colors;
import org.apache.commons.io.FileUtils;

import java.io.*;
import java.nio.file.Path;
import java.util.UUID;

public class SkinsIOHelper {
    public static NativeImage readPlayerAlterSkinFromFile(AbstractClientPlayerEntity player, String alterId) {
        File skinFile = getPlayerAlterSkinFile(player, alterId.toString());
        try {
            return NativeImage.read(new FileInputStream(skinFile));
        } catch (IOException e) {
            PlurifyClient.LOGGER.warn("File " + skinFile + " does not exist or could not be converted to a skin texture. " +
                    "Using the default skin for this account.");
        }
        return null;
    }

    public static void writePlayerAlterSkinToFile(AbstractClientPlayerEntity player, String alterId, File skinFile) {
        File source = skinFile;
        File dest = getPlayerAlterSkinFile(player, alterId.toString());
        try {
            FileUtils.copyFile(source, dest);
        } catch (IOException e) {
            PlurifyClient.LOGGER.error("Could not write file " + skinFile + ".");
        }
    }

    public static File getPlayerAlterSkinFile(AbstractClientPlayerEntity player, String alterId) {
        Path playerConfigDir = AltersIOHelper.getPlayerConfigDir(player);
        playerConfigDir.toFile().mkdir();
        return playerConfigDir.resolve(alterId + ".png").toFile();
    }

    /**
     * Sample the player's arm (a la blood test) to determine if it's wide or slim
     */
    public static PlayerSkinType determinePlayerSkinType(NativeImage skinAsNativeImage) {
        int armSample = skinAsNativeImage.getColorArgb(55, 20);
        if (armSample == Colors.BLACK) {
            return PlayerSkinType.SLIM;
        } else {
            return PlayerSkinType.WIDE;
        }
    }
}
