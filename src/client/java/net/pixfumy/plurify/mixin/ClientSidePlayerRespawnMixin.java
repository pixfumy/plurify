package net.pixfumy.plurify.mixin;

import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientCommonPacketListenerImpl;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.client.multiplayer.CommonListenerCookie;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.Connection;
import net.pixfumy.plurify.ISkinOwner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ClientPacketListener.class)
public abstract class ClientSidePlayerRespawnMixin extends ClientCommonPacketListenerImpl {
    protected ClientSidePlayerRespawnMixin(Minecraft client, Connection connection, CommonListenerCookie connectionState) {
        super(client, connection, connectionState);
    }

    @Redirect(method = "handleRespawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/player/LocalPlayer;setId(I)V"))
    private void setSkinOnPlayerRespawn(LocalPlayer player, int id) {
        player.setId(id);
        ((ISkinOwner) player).plurify$setCustomSkin(((ISkinOwner) this.minecraft.player).plurify$getSkinTextureAsNativeImage(),
                ((ISkinOwner) this.minecraft.player).plurify$getAlterId());
    }
}
