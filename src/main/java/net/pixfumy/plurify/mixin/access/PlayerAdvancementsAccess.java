package net.pixfumy.plurify.mixin.access;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementNode;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.ServerAdvancementManager;
import net.minecraft.server.level.ServerPlayer;
import org.jspecify.annotations.Nullable;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Accessor;
import org.spongepowered.asm.mixin.gen.Invoker;

import java.util.Map;
import java.util.Set;

@Mixin(PlayerAdvancements.class)
public interface PlayerAdvancementsAccess {
    @Invoker
    void invokeApplyFrom(final ServerAdvancementManager manager, final PlayerAdvancements.Data data);

    @Invoker
    PlayerAdvancements.Data invokeAsData();

    @Invoker
    void invokeClearTriggers();

    @Accessor
    Map<AdvancementHolder, AdvancementProgress> getProgress();

    @Accessor
    Set<AdvancementHolder> getVisible();

    @Accessor
    Set<AdvancementHolder> getProgressChanged();

    @Accessor
    Set<AdvancementNode> getRootsToUpdate();

    @Accessor
    void setLastSelectedTab(@Nullable AdvancementHolder holder);

    @Accessor
    void setIsFirstPacket(boolean isFirstPacket);
}
