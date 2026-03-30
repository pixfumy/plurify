package net.pixfumy.plurify.mixin;

import net.minecraft.client.MinecraftClient;
import net.minecraft.client.network.*;
import net.minecraft.entity.Entity;
import net.minecraft.entity.attribute.AttributeContainer;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.network.ClientConnection;
import net.pixfumy.plurify.ISkinOwner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPlayNetworkHandler.class)
public abstract class ClientSidePlayerRespawnMixin extends ClientCommonNetworkHandler {
    protected ClientSidePlayerRespawnMixin(MinecraftClient client, ClientConnection connection, ClientConnectionState connectionState) {
        super(client, connection, connectionState);
    }

    @Redirect(method = "onPlayerRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/network/ClientPlayerEntity;setId(I)V"))
    private void setSkinOnPlayerRespawn(ClientPlayerEntity player, int id) {
        player.setId(id);
        ((ISkinOwner) player).plurify$setCustomSkin(((ISkinOwner) this.client.player).plurify$getSkinTextureAsNativeImage(),
                ((ISkinOwner) this.client.player).plurify$getAlterId());
    }
}
