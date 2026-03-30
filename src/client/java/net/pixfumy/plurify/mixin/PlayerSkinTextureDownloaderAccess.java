package net.pixfumy.plurify.mixin;

import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.PlayerSkinTextureDownloader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(PlayerSkinTextureDownloader.class)
public interface PlayerSkinTextureDownloaderAccess {
    @Invoker
    static NativeImage invokeRemapTexture(NativeImage image, String uri) {
        throw new AssertionError();
    }
}
