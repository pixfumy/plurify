package net.pixfumy.plurify;

import net.minecraft.entity.EntityEquipment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.inventory.EnderChestInventory;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.TeleportTarget;
import net.minecraft.world.rule.GameRules;
import net.pixfumy.plurify.mixin.access.HungerManagerAccess;

import java.util.*;

public class Alter {
    private UUID uuid;
    private Text name;

    private ServerPlayerEntity player;
    private PlayerInventory playerInventory;
    private EnderChestInventory enderChestInventory;
    private Vec3d position;
    private Vec2f rotation;

    private float health = 20;

    private float experienceProgress = 0.0F;
    private int experienceLevel;
    private int totalExperience;

    private int foodLevel = 20;
    private float saturationLevel = 5.0F;
    private float exhaustion;
    private int foodTickTimer;

    private boolean keepInventory;

    private ServerWorld world;

    private Item icon;

    public Alter(ServerPlayerEntity player, boolean copyEntityDataFromPlayer) {
        this(null, player.getName().copy(), player, copyEntityDataFromPlayer);
    }

    public Alter(UUID uuid, Text name, ServerPlayerEntity player, boolean copyEntityDataFromPlayer) {
        this.uuid = uuid == null ? UUID.randomUUID() : uuid;
        this.name = name;
        this.player = player;
        this.playerInventory = new PlayerInventory(player, new EntityEquipment());
        this.enderChestInventory = new EnderChestInventory();

        if (copyEntityDataFromPlayer) {
            for (int i = 0; i < player.getInventory().size(); i++) {
                this.playerInventory.setStack(i, player.getInventory().getStack(i));
            }
            for (int i = 0; i < player.getEnderChestInventory().size(); i++) {
                this.enderChestInventory.setStack(i, player.getEnderChestInventory().getStack(i));
            }
            this.position = player.getEntityPos();
            this.rotation = player.getRotationClient();
            this.keepInventory = player.getEntityWorld().getGameRules().getValue(GameRules.KEEP_INVENTORY);
        } else {
            this.position = player.getRespawnTarget(true, TeleportTarget.NO_OP).position();
            this.rotation = new Vec2f(0, 0);
        }

        this.world = player.getEntityWorld().getServer().getOverworld();
        this.icon = Items.MAGENTA_DYE;
    }

    public UUID getUuid() {
        return uuid;
    }

    public Text getName() {
        return this.name;
    }

    public void setName(Text name) {
        this.name = name;
    }

    public ServerPlayerEntity getPlayer() {
        return player;
    }

    public void setPlayer(ServerPlayerEntity player) {
        this.player = player;
    }

    public PlayerInventory getPlayerInventory() {
        return this.playerInventory;
    }

    public void setPlayerInventory(PlayerInventory playerInventory) {
        this.playerInventory = playerInventory;
    }


    public EnderChestInventory getEnderChestInventory() {
        return enderChestInventory;
    }

    public void setEnderChestInventory(EnderChestInventory enderChestInventory) {
        this.enderChestInventory = enderChestInventory;
    }

    public Vec3d getPosition() {
        return position;
    }

    public void setPosition(Vec3d position) {
        this.position = position;
    }

    public Vec2f getRotation() {
        return rotation;
    }

    public void setRotation(Vec2f rotation) {
        this.rotation = rotation;
    }

    public int getFoodTickTimer() {
        return foodTickTimer;
    }

    public void setFoodTickTimer(int foodTickTimer) {
        this.foodTickTimer = foodTickTimer;
    }

    public float getExhaustion() {
        return exhaustion;
    }

    public void setExhaustion(float exhaustion) {
        this.exhaustion = exhaustion;
    }

    public float getSaturationLevel() {
        return saturationLevel;
    }

    public void setSaturationLevel(float saturationLevel) {
        this.saturationLevel = saturationLevel;
    }

    public int getFoodLevel() {
        return foodLevel;
    }

    public void setFoodLevel(int foodLevel) {
        this.foodLevel = foodLevel;
    }

    public float getExperienceProgress() {
        return experienceProgress;
    }

    public void setExperienceProgress(float experienceProgress) {
        this.experienceProgress = experienceProgress;
    }

    public int getTotalExperience() {
        return totalExperience;
    }

    public void setTotalExperience(int totalExperience) {
        this.totalExperience = totalExperience;
    }

    public int getExperienceLevel() {
        return experienceLevel;
    }

    public void setExperienceLevel(int experienceLevel) {
        this.experienceLevel = experienceLevel;
    }

    public float getHealth() {
        return health;
    }

    public void setHealth(float health) {
        this.health = health;
    }

    public ServerWorld getWorld() {
        return world;
    }

    public boolean hasKeepInventory() {
        return keepInventory;
    }

    public void setKeepInventory(boolean keepInventory) {
        this.keepInventory = keepInventory;
    }

    public void setWorld(ServerWorld world) {
        this.world = world;
    }

    public Item getIcon() {
        return icon;
    }

    public void setIcon(Item icon) {
        this.icon = icon;
    }

    public boolean isCurrentAlter() {
        return ((IAltersOwner) player).plurify$getCurrentAlter() == this;
    }

    public void setAlterEntityDataFromPlayer() {
        for (int i = 0; i < this.player.getInventory().size(); i++) {
            this.playerInventory.setStack(i, this.player.getInventory().getStack(i));
        }

        for (int i = 0; i < this.player.getEnderChestInventory().size(); i++) {
            this.enderChestInventory.setStack(i, this.player.getEnderChestInventory().getStack(i));
        }

        this.position = this.player.getEntityPos();
        this.rotation = this.player.getRotationClient();

        this.health = this.player.getHealth();

        this.experienceProgress = this.player.experienceProgress;
        this.experienceLevel = this.player.experienceLevel;
        this.totalExperience = this.player.totalExperience;

        this.foodLevel = this.player.getHungerManager().getFoodLevel();
        this.saturationLevel = this.player.getHungerManager().getSaturationLevel();
        this.exhaustion = ((HungerManagerAccess) this.player.getHungerManager()).getExhaustion();
        this.foodTickTimer = ((HungerManagerAccess) this.player.getHungerManager()).getFoodTickTimer();

        this.world = this.player.getEntityWorld();
    }

    public void setPlayerEntityDataFromAlter() {
        for (int i = 0; i < this.getPlayerInventory().size(); ++i) {
            this.player.getInventory().setStack(i, this.getPlayerInventory().getStack(i));
        }

        for (int i = 0; i < this.getEnderChestInventory().size(); ++i) {
            this.player.getEnderChestInventory().setStack(i, this.getEnderChestInventory().getStack(i));
        }

        Vec3d alterPos = this.getPosition();
        Vec2f alterRot = this.getRotation();
        this.player.teleport(this.getWorld(), alterPos.x, alterPos.y, alterPos.z, Set.of(), alterRot.y, alterRot.x, false);

        this.player.setHealth(this.getHealth());

        this.player.experienceProgress = this.getExperienceProgress();
        this.player.experienceLevel = this.getExperienceLevel();
        this.player.totalExperience = this.getTotalExperience();

        this.player.getHungerManager().setFoodLevel(this.getFoodLevel());
        this.player.getHungerManager().setSaturationLevel(this.getSaturationLevel());
        ((HungerManagerAccess) this.player.getHungerManager()).setExhaustion(this.getExhaustion());
        ((HungerManagerAccess) this.player.getHungerManager()).setFoodTickTimer(this.getFoodTickTimer());
    }
}
