package net.pixfumy.plurify.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.renderer.texture.SkinTextureDownloader;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(SkinTextureDownloader.class)
public interface PlayerSkinTextureDownloaderAccess {
    @Invoker
    static NativeImage invokeProcessLegacySkin(NativeImage image, String uri) {
        throw new AssertionError();
    }
}
