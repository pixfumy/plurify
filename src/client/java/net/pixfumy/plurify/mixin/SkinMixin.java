package net.pixfumy.plurify.mixin;

import com.mojang.blaze3d.platform.NativeImage;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.AbstractClientPlayer;
import net.minecraft.client.renderer.texture.DynamicTexture;
import net.minecraft.core.ClientAsset;
import net.minecraft.resources.Identifier;
import net.minecraft.world.entity.player.PlayerModelType;
import net.minecraft.world.entity.player.PlayerSkin;
import net.pixfumy.plurify.ISkinOwner;
import net.pixfumy.plurify.SkinsIOHelper;
import org.jetbrains.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(AbstractClientPlayer.class)
public abstract class SkinMixin implements ISkinOwner {

    private NativeImage skinTextureAsNativeImage;
    private String alterId;
    private PlayerModelType playerSkinType;

    @Inject(method = "getSkin", at = @At("HEAD"), cancellable = true)
    private void overrideGetSkin(CallbackInfoReturnable<PlayerSkin> cir) {
        if (skinTextureAsNativeImage != null && playerSkinType != null) {
            cir.setReturnValue(PlayerSkin.insecure(new ClientAsset.ResourceTexture(Identifier.fromNamespaceAndPath("plurify", this.alterId), Identifier.fromNamespaceAndPath("plurify", this.alterId)),
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

        this.skinTextureAsNativeImage = nativeImage.mappedCopy(i -> i);

        // remaps 32 x 64 image to 64 x 64, and fills in the unused spaces with black pixels
        PlayerSkinTextureDownloaderAccess.invokeProcessLegacySkin(skinTextureAsNativeImage, this.alterId);

        this.alterId = alterUuid;
        this.playerSkinType = SkinsIOHelper.determinePlayerSkinType(skinTextureAsNativeImage);

        DynamicTexture nativeImageBackedTexture = new DynamicTexture(() -> this.alterId, skinTextureAsNativeImage);
        Minecraft.getInstance().getTextureManager().register(Identifier.fromNamespaceAndPath("plurify", this.alterId), nativeImageBackedTexture);
    }

    @Override
    public NativeImage plurify$getSkinTextureAsNativeImage() {
        return skinTextureAsNativeImage;
    }

    @Override
    public PlayerModelType plurify$getPlayerSkinType() {
        return playerSkinType;
    }

    @Override
    public String plurify$getAlterId() {
        return alterId;
    }
}
