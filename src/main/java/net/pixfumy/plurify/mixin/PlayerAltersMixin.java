package net.pixfumy.plurify.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueOutput;
import net.pixfumy.plurify.AltersIOHelper;
import net.pixfumy.plurify.IAltersOwner;
import net.pixfumy.plurify.Alter;
import net.pixfumy.plurify.networking.PlayerSwitchAlterS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(ServerPlayer.class)
public abstract class PlayerAltersMixin extends Player implements IAltersOwner {

    private HashMap<UUID, Alter> alters;
    private Alter currentAlter;

    private boolean isClientLoaded;


    public PlayerAltersMixin(Level world, GameProfile profile) {
        super(world, profile);
    }

    @Override
    public Alter plurify$getCurrentAlter() {
        return currentAlter;
    }

    @Override
    public void plurify$setCurrentAlter(Alter alter) {
        this.currentAlter = alter;
    }

    @Override
    public HashMap<UUID, Alter> plurify$getAlters() {
        if (this.alters == null) {
            this.alters = new HashMap<UUID, Alter>();
        }
        return alters;
    }

    @Override
    public void plurify$setAlters(HashMap<UUID, Alter> alters) {
        this.alters = alters;
    }

    @Override
    public void plurify$addToAlters(Alter alter) {
        if (this.alters == null) {
            this.alters = new HashMap<UUID, Alter>();
        }
        this.alters.put(alter.getUuid(), alter);
    }

    @Override
    public void plurify$removeFromAlters(Alter alter) {
        if (this.alters == null || this.alters.size() <= 1) {
            return;
        }
        this.alters.remove(alter.getUuid());
    }

    /**
     * Switches to the specified Alter. There are two cases:
     *
     * Case 1: World load - currentAlter is null, switch directly to the currentAlter defined in the config file.
     *         This accounts for switching worlds after switching alters on a different world - you should be automatically
     *         switched to the alter you selected on the previous world.
     *
       Case 2: In-game switching - currentAlter is the previous alter, so we need to sync its entity data to the player's
                current state before switching.
     */
    @Override
    public void plurify$switchToAlter(Alter alter) {
        ServerPlayer thisPlayer = (ServerPlayer) (Object) this;

        // Case 2 only
        if (plurify$getCurrentAlter() != null) {
            plurify$getCurrentAlter().setAlterEntityDataFromPlayer();
        }

        // set the currentAlter to the new alter and set the player data
        this.plurify$setCurrentAlter(alter);
        alter.setPlayerEntityDataFromAlter();

        ServerPlayNetworking.send(thisPlayer,
                new PlayerSwitchAlterS2CPacket(currentAlter.getUuid().toString(), currentAlter.getName().getString()));
    }

    @Override
    public boolean plurify$isClientLoaded() {
        return isClientLoaded;
    }

    @Override
    public void plurify$setClientLoaded(boolean isClientLoaded) {
        this.isClientLoaded = isClientLoaded;
    }

    @Inject(method = "restoreFrom", at = @At(value = "HEAD"))
    private void copyEntityDataToNewPlayer(ServerPlayer oldPlayer, boolean alive, CallbackInfo ci) {
        this.alters = ((IAltersOwner) oldPlayer).plurify$getAlters();
        this.currentAlter = ((IAltersOwner) oldPlayer).plurify$getCurrentAlter();
        this.isClientLoaded = ((IAltersOwner) oldPlayer).plurify$isClientLoaded();

        alters.values().forEach(alter -> alter.setPlayer((ServerPlayer) (Object) this));
    }

    @Inject(method = "addAdditionalSaveData", at = @At("HEAD"))
    private void writePlayerEntityDataOnAutosave(ValueOutput view, CallbackInfo ci) {
        ServerPlayer thisPlayer = (ServerPlayer) (Object) this;
        Alter currentAlter = plurify$getCurrentAlter();
        currentAlter.setAlterEntityDataFromPlayer();

        AltersIOHelper.writePlayerAltersToFile(thisPlayer);
    }
}