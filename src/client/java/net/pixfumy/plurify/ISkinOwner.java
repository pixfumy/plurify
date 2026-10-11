package net.pixfumy.plurify;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.world.entity.player.PlayerModelType;

public interface ISkinOwner {
    PlayerModelType plurify$getPlayerSkinType();

    NativeImage plurify$getSkinTextureAsNativeImage();

    void plurify$setCustomSkin(NativeImage image, String alterUuid);

    String plurify$getAlterId();
}
