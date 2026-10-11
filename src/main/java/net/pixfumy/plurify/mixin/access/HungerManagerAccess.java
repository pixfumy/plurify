package net.pixfumy.plurify.mixin.access;

import net.minecraft.world.food.FoodData;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(FoodData.class)
public interface HungerManagerAccess {
    @Accessor
    float getExhaustionLevel();

    @Accessor
    void setExhaustionLevel(float exhaustion);

    @Accessor
    int getTickTimer();

    @Accessor
    void setTickTimer(int foodTickTimer);
}
