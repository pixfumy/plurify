package net.pixfumy.plurify.mixin;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.item.ItemStack;
import net.minecraft.network.packet.s2c.play.PlaySoundS2CPacket;
import net.minecraft.registry.Registries;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.sound.SoundCategory;
import net.minecraft.sound.SoundEvents;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.World;
import net.minecraft.world.rule.GameRules;
import net.pixfumy.plurify.IAltersOwner;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Random;

@Mixin(ServerPlayerEntity.class)
public abstract class KeepInventoryMixin extends PlayerEntity {
    public KeepInventoryMixin(World world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    protected void dropInventory(ServerWorld world) {
        boolean hasPerAlterKeepInv = ((IAltersOwner) this).plurify$getCurrentAlter().hasKeepInventory();
        if (!(world.getGameRules().getValue(GameRules.KEEP_INVENTORY) || hasPerAlterKeepInv)) {
            this.vanishCursedItems();
            this.getInventory().dropAll();
        }
    }

    @WrapOperation(method = "copyFrom", at = @At(value = "INVOKE", target = "Ljava/lang/Boolean;booleanValue()Z"))
    private boolean transferInventoryIfPerAlterKeepInv(Boolean instance, Operation<Boolean> original) {
        boolean hasPerAlterKeepInv = ((IAltersOwner) this).plurify$getCurrentAlter().hasKeepInventory();
        if (hasPerAlterKeepInv) {
            return true;
        }
        return original.call(instance);
    }

}
