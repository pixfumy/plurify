package net.pixfumy.plurify.mixin.advancement;

import net.minecraft.advancements.AdvancementHolder;
import net.minecraft.advancements.AdvancementProgress;
import net.minecraft.server.PlayerAdvancements;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.players.PlayerList;
import net.pixfumy.plurify.advancement.IAdvancementsOwner;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;

import java.util.Set;

@Mixin(PlayerAdvancements.class)
public abstract class PlayerAdvancementsMixin implements IAdvancementsOwner {
    @Shadow
    private ServerPlayer player;

    @Shadow @Final
    private PlayerList playerList;

    @Shadow @Final
    private Set<AdvancementHolder> progressChanged;

    @Shadow
    public abstract AdvancementProgress getOrStartProgress(final AdvancementHolder advancement);

    @Shadow
    private void unregisterListeners(final AdvancementHolder holder) {
        throw new AssertionError();
    }

    @Shadow
    private void markForVisibilityUpdate(final AdvancementHolder advancement) {
        throw new AssertionError();
    }

    @Override
    public void plurify$awardAdvancementSilently(final AdvancementHolder holder, final String criterion) {
        boolean result = false;
        AdvancementProgress progress = this.getOrStartProgress(holder);
        boolean wasDone = progress.isDone();
        if (progress.grantProgress(criterion)) {
            this.unregisterListeners(holder);
            this.progressChanged.add(holder);
            result = true;
        }

        if (!wasDone && progress.isDone()) {
            this.markForVisibilityUpdate(holder);
        }

    }
}
