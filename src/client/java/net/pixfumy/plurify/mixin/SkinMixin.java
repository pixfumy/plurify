package net.pixfumy.plurify.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.network.PlayerListEntry;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.client.texture.PlayerSkinTextureDownloader;
import net.minecraft.client.util.DefaultSkinHelper;
import net.minecraft.client.world.ClientWorld;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Colors;
import net.minecraft.util.Identifier;
import net.minecraft.util.math.ColorHelper;
import net.pixfumy.plurify.AltersIOHelper;
import net.pixfumy.plurify.ISkinOwner;
import net.pixfumy.plurify.PlurifyMain;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class SkinMixin implements ISkinOwner {

    private NativeImage skinTextureAsNativeImage;
    private String alterName;
    private PlayerSkinType playerSkinType;

    @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
    private void getSkin(CallbackInfoReturnable<SkinTextures> cir) {
        if (skinTextureAsNativeImage != null) {
            cir.setReturnValue(SkinTextures.create(new AssetInfo.TextureAssetInfo(Identifier.of("plurify", this.alterName), Identifier.of("plurify", this.alterName)),
                    null, null, playerSkinType));
        }
    }

    @Override
    public void plurify$setCustomSkin(NativeImage nativeImage, PlayerSkinType playerSkinType, String alterName) {
        this.skinTextureAsNativeImage = nativeImage;
        this.alterName = alterName.strip().toLowerCase();
        this.playerSkinType = playerSkinType;

        NativeImageBackedTexture nativeImageBackedTexture = new NativeImageBackedTexture(() -> this.alterName, skinTextureAsNativeImage);
        MinecraftClient.getInstance().getTextureManager().registerTexture(Identifier.of("plurify", this.alterName), nativeImageBackedTexture);
    }
}
