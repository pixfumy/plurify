package net.pixfumy.plurify.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.AbstractClientPlayerEntity;
import net.minecraft.client.texture.NativeImage;
import net.minecraft.client.texture.NativeImageBackedTexture;
import net.minecraft.entity.player.PlayerSkinType;
import net.minecraft.entity.player.SkinTextures;
import net.minecraft.util.AssetInfo;
import net.minecraft.util.Identifier;
import net.pixfumy.plurify.ISkinOwner;
import net.pixfumy.plurify.SkinsIOHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayerEntity.class)
public abstract class SkinMixin implements ISkinOwner {

    private NativeImage skinTextureAsNativeImage;
    private String alterId;
    private PlayerSkinType playerSkinType;

    @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
    private void overrideGetSkin(CallbackInfoReturnable<SkinTextures> cir) {
        if (skinTextureAsNativeImage != null && playerSkinType != null) {
            cir.setReturnValue(SkinTextures.create(new AssetInfo.TextureAssetInfo(Identifier.of("plurify", this.alterId), Identifier.of("plurify", this.alterId)),
                    null, null, playerSkinType));
        }
    }

    @Override
    public void plurify$setCustomSkin(@Nullable NativeImage nativeImage, String alterUuid) {
        if (nativeImage == null) {
            skinTextureAsNativeImage = null;
            playerSkinType = null;
            return;
        }

        this.skinTextureAsNativeImage = nativeImage.applyToCopy(i -> i);

        // remaps 32 x 64 image to 64 x 64, and fills in the unused spaces with black pixels
        PlayerSkinTextureDownloaderAccess.invokeRemapTexture(skinTextureAsNativeImage, this.alterId);

        this.alterId = alterUuid;
        this.playerSkinType = SkinsIOHelper.determinePlayerSkinType(skinTextureAsNativeImage);

        NativeImageBackedTexture nativeImageBackedTexture = new NativeImageBackedTexture(() -> this.alterId, skinTextureAsNativeImage);
        MinecraftClient.getInstance().getTextureManager().registerTexture(Identifier.of("plurify", this.alterId), nativeImageBackedTexture);
    }

    @Override
    public NativeImage plurify$getSkinTextureAsNativeImage() {
        return skinTextureAsNativeImage;
    }

    @Override
    public PlayerSkinType plurify$getPlayerSkinType() {
        return playerSkinType;
    }

    @Override
    public String plurify$getAlterId() {
        return alterId;
    }
}
