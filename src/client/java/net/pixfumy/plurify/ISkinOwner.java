package net.pixfumy.plurify;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.player.PlayerSkinType;

public interface ISkinOwner {
    PlayerSkinType plurify$getPlayerSkinType();

    NativeImage plurify$getSkinTextureAsNativeImage();

    void plurify$setCustomSkin(NativeImage image, String alterUuid);

    String plurify$getAlterId();
}
