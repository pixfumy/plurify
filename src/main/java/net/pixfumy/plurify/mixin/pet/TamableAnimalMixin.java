package net.pixfumy.plurify.mixin.pet;

import net.minecraft.core.UUIDUtil;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.*;
import net.minecraft.world.entity.animal.equine.AbstractHorse;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.pixfumy.plurify.alter.IAltersOwner;
import net.pixfumy.plurify.pet.IAlterOwnableEntity;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;
import java.util.UUID;

@Mixin(TamableAnimal.class)
public abstract class TamableAnimalMixin implements IAlterOwnableEntity, OwnableEntity {

    @Nullable
    private UUID ownerAlterUuid;

    @Inject(method = "setOwner", at = @At("TAIL"))
    private void setOwnerAlterUuid(final @Nullable LivingEntity owner, CallbackInfo ci) {
        if (owner instanceof ServerPlayer) {
            ownerAlterUuid = ((IAltersOwner) owner).plurify$getCurrentAlter().getUuid();
        }
    }

    @Inject(method = "getOwnerReference", at = @At("RETURN"), cancellable = true)
    private void returnAlterOwnerIfExists(CallbackInfoReturnable<@Nullable EntityReference<LivingEntity>> cir) {
        LivingEntity owner = EntityReference.getLivingEntity(cir.getReturnValue(), this.level());
        if (this.ownerAlterUuid != null && owner != null && owner instanceof ServerPlayer) {
            ServerPlayer player = (ServerPlayer) owner;
            if (!((IAltersOwner) player).plurify$getCurrentAlter().getUuid().equals(ownerAlterUuid)) {
                cir.setReturnValue(null);
            }
        }
    }

    @Inject(method = "addAdditionalSaveData", at = @At("TAIL"))
    private void saveAlterUuid(final ValueOutput output, CallbackInfo ci) {
        if (ownerAlterUuid != null) {
            output.putIntArray("ownerAlterUuid", UUIDUtil.uuidToIntArray(ownerAlterUuid));
        }
    }

    @Inject(method = "readAdditionalSaveData", at = @At("TAIL"))
    private void readAlterUuid(final ValueInput input, CallbackInfo ci) {
        input.getIntArray("ownerAlterUuid").ifPresent(ints -> this.ownerAlterUuid = UUIDUtil.uuidFromIntArray(ints));
    }
}
