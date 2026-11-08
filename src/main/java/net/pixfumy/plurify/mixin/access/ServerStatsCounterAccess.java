package net.pixfumy.plurify.mixin.access;

import com.mojang.serialization.Codec;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

import java.util.Map;

@Mixin(ServerStatsCounter.class)
public interface ServerStatsCounterAccess {
    @Accessor @Final
    static Codec<Map<Stat<?>, Integer>> getSTATS_CODEC() {
        throw new AssertionError();
    }
}
