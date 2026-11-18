package net.pixfumy.plurify.mixin.vault;

import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.entity.vault.VaultServerData;
import net.pixfumy.plurify.alter.IAltersOwner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

import java.util.Set;
import java.util.UUID;

@Mixin(VaultServerData.class)
public class VaultServerDataMixin {
    @Redirect(method = "addToRewardedPlayers", at = @At(value = "INVOKE", target = "Ljava/util/Set;add(Ljava/lang/Object;)Z"))
    private boolean addAlterUuidInsteadOfPlayerUuid(Set<UUID> rewardedPlayers, Object playerUuid, final Player player) {
        return rewardedPlayers.add(((IAltersOwner) player).plurify$getCurrentAlter().getUuid());
    }

    @Redirect(method = "hasRewardedPlayer", at = @At(value = "INVOKE", target = "Ljava/util/Set;contains(Ljava/lang/Object;)Z"))
    private boolean checkAlterUuidInsteadOfPlayerUuid(Set<UUID> rewardedPlayers, Object playerUuid, final Player player) {
        return rewardedPlayers.contains(((IAltersOwner) player).plurify$getCurrentAlter().getUuid());
    }
}
