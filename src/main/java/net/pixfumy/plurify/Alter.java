package net.pixfumy.plurify;

import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntityEquipment;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.PlayerEnderChestContainer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.gamerules.GameRules;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.pixfumy.plurify.gui.ChangeAlterIconGui;
import net.pixfumy.plurify.mixin.access.HungerManagerAccess;

import java.util.*;

public class Alter {
    private UUID uuid;
    private Component name;

    private ServerPlayer player;
    private Inventory playerInventory;
    private PlayerEnderChestContainer enderChestInventory;

    private ServerLevel level;
    private Vec3 position;
    private Vec2 rotation;

    private ServerPlayer.RespawnConfig respawn;

    private float health = 20;

    private float experienceProgress = 0.0F;
    private int experienceLevel;
    private int totalExperience;

    private int foodLevel = 20;
    private float saturationLevel = 5.0F;
    private float exhaustion;
    private int foodTickTimer;

    private boolean keepInventory;

    private ServerLevel world;

    private ItemStack icon;

    public Alter(ServerPlayer player, boolean copyEntityDataFromPlayer) {
        this(null, player.getName().copy(), player, copyEntityDataFromPlayer);
    }

    public Alter(UUID uuid, Component name, ServerPlayer player, boolean copyEntityDataFromPlayer) {
        this.uuid = uuid == null ? UUID.randomUUID() : uuid;
        this.name = name;
        this.player = player;
        this.playerInventory = new Inventory(player, new EntityEquipment());
        this.enderChestInventory = new PlayerEnderChestContainer();

        if (copyEntityDataFromPlayer) {
            setAlterEntityDataFromPlayer();
            this.keepInventory = player.level().getGameRules().get(GameRules.KEEP_INVENTORY);
        } else {
            this.level = player.level().getServer().overworld();
            BlockPos spawnPoint = BlockPos.containing(TeleportTransition.createDefault(player, TeleportTransition.DO_NOTHING).position());
            this.position = new Vec3(spawnPoint.getX(), spawnPoint.getY(), spawnPoint.getZ());
            this.rotation = new Vec2(0, 0);
        }

        this.icon = ChangeAlterIconGui.ITEMS_FOR_ICONS.get(new Random().nextInt(ChangeAlterIconGui.ITEMS_FOR_ICONS.size()));
    }

    public UUID getUuid() {
        return uuid;
    }

    public Component getName() {
        return this.name;
    }

    public void setName(Component name) {
        this.name = name;
    }

    public ServerPlayer getPlayer() {
        return player;
    }

    public void setPlayer(ServerPlayer player) {
        this.player = player;
    }

    public Inventory getPlayerInventory() {
        return this.playerInventory;
    }

    public void setPlayerInventory(Inventory playerInventory) {
        this.playerInventory = playerInventory;
    }


    public PlayerEnderChestContainer getEnderChestInventory() {
        return enderChestInventory;
    }

    public void setEnderChestInventory(PlayerEnderChestContainer enderChestInventory) {
        this.enderChestInventory = enderChestInventory;
    }

    public ServerLevel getLevel() {
        return level;
    }

    public void setLevel(ServerLevel level) {
        this.level = level;
    }

    public Vec3 getPosition() {
        return position;
    }

    public void setPosition(Vec3 position) {
        this.position = position;
    }

    public Vec2 getRotation() {
        return rotation;
    }

    public void setRotation(Vec2 rotation) {
        this.rotation = rotation;
    }

    public ServerPlayer.RespawnConfig getRespawn() {
        return respawn;
    }

    public void setRespawn(ServerPlayer.RespawnConfig respawn) {
        this.respawn = respawn;
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

    public boolean hasKeepInventory() {
        return keepInventory;
    }

    public void setKeepInventory(boolean keepInventory) {
        this.keepInventory = keepInventory;
    }

    public ItemStack getIcon() {
        return icon.copy();
    }

    public void setIcon(ItemStack icon) {
        this.icon = icon;
    }

    public boolean isCurrentAlter() {
        return ((IAltersOwner) player).plurify$getCurrentAlter() == this;
    }

    public void setAlterEntityDataFromPlayer() {
        for (int i = 0; i < this.player.getInventory().getContainerSize(); i++) {
            this.playerInventory.setItem(i, this.player.getInventory().getItem(i));
        }

        for (int i = 0; i < this.player.getEnderChestInventory().getContainerSize(); i++) {
            this.enderChestInventory.setItem(i, this.player.getEnderChestInventory().getItem(i));
        }

        this.level = player.level();
        this.position = player.position();

        this.rotation = this.player.getRotationVector();

        this.respawn = player.getRespawnConfig();

        this.health = this.player.getHealth();

        this.experienceProgress = this.player.experienceProgress;
        this.experienceLevel = this.player.experienceLevel;
        this.totalExperience = this.player.totalExperience;

        this.foodLevel = this.player.getFoodData().getFoodLevel();
        this.saturationLevel = this.player.getFoodData().getSaturationLevel();
        this.exhaustion = ((HungerManagerAccess) this.player.getFoodData()).getExhaustionLevel();
        this.foodTickTimer = ((HungerManagerAccess) this.player.getFoodData()).getTickTimer();
    }

    public void setPlayerEntityDataFromAlter() {
        for (int i = 0; i < this.getPlayerInventory().getContainerSize(); ++i) {
            this.player.getInventory().setItem(i, this.getPlayerInventory().getItem(i));
        }

        for (int i = 0; i < this.getEnderChestInventory().getContainerSize(); ++i) {
            this.player.getEnderChestInventory().setItem(i, this.getEnderChestInventory().getItem(i));
        }

        Vec3 alterPos = this.getPosition();
        Vec2 alterRot = this.getRotation();

        this.player.teleportTo(this.level,
                alterPos.x,
                alterPos.y,
                alterPos.z,
                Set.of(),
                alterRot.y,
                alterRot.x,
                false
        );

        this.player.setRespawnPosition(this.respawn, false);

        this.player.setHealth(this.getHealth());

        this.player.experienceProgress = this.getExperienceProgress();
        this.player.experienceLevel = this.getExperienceLevel();
        this.player.totalExperience = this.getTotalExperience();

        this.player.getFoodData().setFoodLevel(this.getFoodLevel());
        this.player.getFoodData().setSaturation(this.getSaturationLevel());
        ((HungerManagerAccess) this.player.getFoodData()).setExhaustionLevel(this.getExhaustion());
        ((HungerManagerAccess) this.player.getFoodData()).setTickTimer(this.getFoodTickTimer());
    }
}
