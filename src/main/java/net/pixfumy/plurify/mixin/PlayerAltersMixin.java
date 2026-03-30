package net.pixfumy.plurify.mixin;

import com.mojang.authlib.GameProfile;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.storage.WriteView;
import net.minecraft.world.World;
import net.pixfumy.plurify.AltersIOHelper;
import net.pixfumy.plurify.IAltersOwner;
import net.pixfumy.plurify.Alter;
import net.minecraft.server.network.ServerPlayerEntity;
import net.pixfumy.plurify.networking.PlayerSwitchAlterS2CPacket;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.*;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerAltersMixin extends PlayerEntity implements IAltersOwner {

    private HashMap<UUID, Alter> alters;
    private Alter currentAlter;

    private boolean isClientLoaded;


    public PlayerAltersMixin(World world, GameProfile profile) {
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

        ServerPlayerEntity thisPlayer = (ServerPlayerEntity) (Object) this;
        AltersIOHelper.writePlayerAltersToFile(thisPlayer);
    }

    @Override
    public void plurify$removeFromAlters(Alter alter) {
        if (this.alters == null || this.alters.size() <= 1) {
            return;
        }
        this.alters.remove(alter.getUuid());
        AltersIOHelper.writePlayerAltersToFile((ServerPlayerEntity) (Object) this);
    }

    @Override
    public void plurify$switchToAlter(Alter alter) {
        ServerPlayerEntity thisPlayer = (ServerPlayerEntity) (Object) this;

        // save current alter data
        plurify$getCurrentAlter().setAlterEntityDataFromPlayer();
        AltersIOHelper.writePlayerAlterEntityDataToFile(thisPlayer, plurify$getCurrentAlter());

        // set the currentAlter to the new alter, set the player data, and write to the main json file
        this.plurify$setCurrentAlter(alter);
        alter.setPlayerEntityDataFromAlter();
        AltersIOHelper.writePlayerAltersToFile(thisPlayer);

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

    @Inject(method = "copyFrom", at = @At(value = "HEAD"))
    private void copyEntityDataToNewPlayer(ServerPlayerEntity oldPlayer, boolean alive, CallbackInfo ci) {
        this.alters = ((IAltersOwner) oldPlayer).plurify$getAlters();
        this.currentAlter = ((IAltersOwner) oldPlayer).plurify$getCurrentAlter();
        this.isClientLoaded = ((IAltersOwner) oldPlayer).plurify$isClientLoaded();

        alters.values().forEach(alter -> alter.setPlayer((ServerPlayerEntity) (Object) this));
    }

    @Inject(method = "writeCustomData", at = @At("HEAD"))
    private void writePlayerEntityDataOnAutosave(WriteView view, CallbackInfo ci) {
        ServerPlayerEntity thisPlayer = (ServerPlayerEntity) (Object) this;
        Alter alter = plurify$getCurrentAlter();
        if (alter != null) {
            alter.setAlterEntityDataFromPlayer();
            AltersIOHelper.writePlayerAlterEntityDataToFile(thisPlayer, plurify$getCurrentAlter());
        }
    }
}