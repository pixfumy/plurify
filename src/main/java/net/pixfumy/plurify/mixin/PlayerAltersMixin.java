package net.pixfumy.plurify.mixin;

import com.mojang.authlib.GameProfile;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.storage.WriteView;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.pixfumy.plurify.AltersIOHelper;
import net.pixfumy.plurify.IAltersOwner;
import net.pixfumy.plurify.Alter;
import net.minecraft.server.network.ServerPlayerEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Set;

@Mixin(ServerPlayerEntity.class)
public abstract class PlayerAltersMixin extends PlayerEntity implements IAltersOwner {

    private HashMap<String, Alter> alters;
    private Alter currentAlter;

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
    public HashMap<String, Alter> plurify$getAlters() {
        if (this.alters == null) {
            this.alters = new HashMap<String, Alter>();
        }
        return alters;
    }

    @Override
    public void plurify$setAlters(HashMap<String, Alter> alters) {
        this.alters = alters;
    }

    @Override
    public void plurify$addToAlters(Alter alter) {
        if (this.alters == null) {
            this.alters = new HashMap<String, Alter>();
        }
        this.alters.put(alter.getName().getString(), alter);

        ServerPlayerEntity thisPlayer = (ServerPlayerEntity) (Object) this;
        AltersIOHelper.writePlayerAltersToFile(thisPlayer);
    }

    @Override
    public void plurify$removeFromAlters(Alter alter) {
        if (this.alters == null || this.alters.size() <= 1) {
            return;
        }
        this.alters.remove(alter.getName().getString());
        AltersIOHelper.writePlayerAltersToFile((ServerPlayerEntity) (Object) this);
    }

    @Override
    public void plurify$switchToAlter(Alter alter) {
        ServerPlayerEntity thisPlayer = (ServerPlayerEntity) (Object) this;

        // save current inventory, give player the new inventory
        plurify$getCurrentAlter().syncToPlayer();
        AltersIOHelper.writePlayerAlterEntityDataToFile(thisPlayer, plurify$getCurrentAlter());
        for (int i = 0; i < alter.getPlayerInventory().size(); ++i) {
            thisPlayer.getInventory().setStack(i, alter.getPlayerInventory().getStack(i));
        }

        Vec3d alterPos = alter.getPosition();
        Vec2f alterRot = alter.getRotation();
        thisPlayer.teleport(alter.getWorld(), alterPos.x, alterPos.y, alterPos.z, Set.of(), alterRot.y, alterRot.x, false);

        // set the currentAlter and write to the main json file
        this.plurify$setCurrentAlter(alter);
        AltersIOHelper.writePlayerAltersToFile(thisPlayer);

    }

    @Inject(method = "writeCustomData", at = @At("HEAD"))
    private void writePlayerEntityDataOnAutosave(WriteView view, CallbackInfo ci) {
        ServerPlayerEntity thisPlayer = (ServerPlayerEntity) (Object) this;
        Alter alter = plurify$getCurrentAlter();
        if (alter != null) {
            AltersIOHelper.writePlayerAlterEntityDataToFile(thisPlayer, plurify$getCurrentAlter());
        }
    }
}