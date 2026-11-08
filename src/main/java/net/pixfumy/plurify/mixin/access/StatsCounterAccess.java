package net.pixfumy.plurify.mixin.access;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import net.minecraft.stats.ServerStatsCounter;
import net.minecraft.stats.Stat;
import net.minecraft.stats.StatsCounter;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(StatsCounter.class)
public interface StatsCounterAccess {
    @Accessor
    Object2IntMap<Stat<?>> getStats();
}
