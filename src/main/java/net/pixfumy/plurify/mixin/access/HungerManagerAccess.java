package net.pixfumy.plurify.mixin.access;

import net.minecraft.entity.player.HungerManager;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;

@Mixin(HungerManager.class)
public interface HungerManagerAccess {
    @Accessor
    float getExhaustion();

    @Accessor
    void setExhaustion(float exhaustion);

    @Accessor
    int getFoodTickTimer();

    @Accessor
    void setFoodTickTimer(int foodTickTimer);
}
