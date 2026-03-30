package net.pixfumy.plurify;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.entity.player.PlayerSkinType;

public interface ISkinOwner {
    void plurify$setCustomSkin(NativeImage image, PlayerSkinType playerSkinType, String alterName);
}
