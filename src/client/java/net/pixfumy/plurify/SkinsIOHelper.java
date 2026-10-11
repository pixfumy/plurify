package net.pixfumy.plurify;

import org.apache.commons.io.FileUtils;
import com.mojang.blaze3d.platform.NativeImage;
import java.io.*;
import java.nio.file.Path;

import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.util.CommonColors;
import net.minecraft.world.entity.player.PlayerModelType;

public class SkinsIOHelper {
    public static NativeImage readPlayerAlterSkinFromFile(AbstractClientPlayer player, String alterId) {
        File skinFile = getPlayerAlterSkinFile(player, alterId.toString());
        try {
            return NativeImage.read(new FileInputStream(skinFile));
        } catch (IOException e) {
            PlurifyClient.LOGGER.warn("File " + skinFile + " does not exist or could not be converted to a skin texture. " +
                    "Using the default skin for this account.");
        }
        return null;
    }

    public static void writePlayerAlterSkinToFile(AbstractClientPlayer player, String alterId, File skinFile) {
        File source = skinFile;
        File dest = getPlayerAlterSkinFile(player, alterId.toString());
        try {
            FileUtils.copyFile(source, dest);
        } catch (IOException e) {
            PlurifyClient.LOGGER.error("Could not write file " + skinFile + ".");
        }
    }

    public static File getPlayerAlterSkinFile(AbstractClientPlayer player, String alterId) {
        Path playerConfigDir = AltersIOHelper.getPlayerConfigDir(player);
        playerConfigDir.toFile().mkdir();
        return playerConfigDir.resolve(alterId + ".png").toFile();
    }

    /**
     * Sample the player's arm (a la blood test) to determine if it's wide or slim
     */
    public static PlayerModelType determinePlayerSkinType(NativeImage skinAsNativeImage) {
        int armSample = skinAsNativeImage.getPixel(55, 20);
        if (armSample == CommonColors.BLACK) {
            return PlayerModelType.SLIM;
        } else {
            return PlayerModelType.WIDE;
        }
    }
}
