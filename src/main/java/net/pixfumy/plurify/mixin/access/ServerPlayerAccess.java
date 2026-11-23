package net.pixfumy.plurify.mixin.access;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(ServerPlayer.class)
public interface ServerPlayerAccess {
    @Invoker
    void invokeSetShoulderEntityLeft(final CompoundTag tag);

    @Invoker
    void invokeSetShoulderEntityRight(final CompoundTag tag);
}
