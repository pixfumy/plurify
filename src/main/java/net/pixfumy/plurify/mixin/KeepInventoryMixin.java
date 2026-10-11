package net.pixfumy.plurify.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.pixfumy.plurify.IAltersOwner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Random;

@Mixin(ServerPlayer.class)
public abstract class KeepInventoryMixin extends Player {
    public KeepInventoryMixin(Level world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    protected void dropEquipment(ServerLevel world) {
        boolean hasPerAlterKeepInv = ((IAltersOwner) this).plurify$getCurrentAlter().hasKeepInventory();
        if (!(world.getGameRules().get(GameRules.KEEP_INVENTORY) || hasPerAlterKeepInv)) {
            this.destroyVanishingCursedItems();
            this.getInventory().dropAll();
        }
    }

    @WrapOperation(method = "restoreFrom", at = @At(value = "INVOKE", target = "Ljava/lang/Boolean;booleanValue()Z"))
    private boolean transferInventoryIfPerAlterKeepInv(Boolean instance, Operation<Boolean> original) {
        boolean hasPerAlterKeepInv = ((IAltersOwner) this).plurify$getCurrentAlter().hasKeepInventory();
        if (hasPerAlterKeepInv) {
            return true;
        }
        return original.call(instance);
    }

}
