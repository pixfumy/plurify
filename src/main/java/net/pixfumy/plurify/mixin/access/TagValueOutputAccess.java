package net.pixfumy.plurify.mixin.access;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.storage.TagValueOutput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

@Mixin(TagValueOutput.class)
public interface TagValueOutputAccess {
    @Accessor
    CompoundTag getOutput();
}
