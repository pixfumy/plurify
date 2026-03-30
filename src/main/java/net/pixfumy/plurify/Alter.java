package net.pixfumy.plurify;

import net.minecraft.entity.EntityEquipment;
import net.minecraft.entity.player.PlayerInventory;
import net.minecraft.item.Item;
import net.minecraft.item.Items;
import net.minecraft.registry.RegistryKey;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.text.Text;
import net.minecraft.util.Colors;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec2f;
import net.minecraft.util.math.Vec3d;
import net.minecraft.world.World;
import net.minecraft.world.dimension.DimensionTypes;

import java.awt.*;
import java.util.UUID;

public class Alter {
    private Text name;
    private ServerPlayerEntity player;
    private PlayerInventory playerInventory;
    private Vec3d position;
    private Vec2f rotation;

    private ServerWorld world;

    private Item icon;

    public Alter(ServerPlayerEntity player) {
        this(player.getName().copy(), player);
    }

    public Alter(Text name, ServerPlayerEntity player) {
        while (((IAltersOwner) player).plurify$getAlters().get(name.getString()) != null) {
            name = Text.literal(UUID.randomUUID().toString().substring(0, 8));
        }
        this.name = name;
        this.player = player;
        this.playerInventory = new PlayerInventory(player, new EntityEquipment());
        this.position = new Vec3d(player.getEntityWorld().getServer().getOverworld().getSpawnPoint().getPos());
        this.rotation = player.getRotationClient();
        this.world = player.getEntityWorld();
        this.icon = Items.MAGENTA_DYE;
    }

    public Text getName() {
        return this.name;
    }

    public void setName(Text name) {
        this.name = name;
    }

    public PlayerInventory getPlayerInventory() {
        return this.playerInventory;
    }

    public void setPlayerInventory(PlayerInventory playerInventory) {
        this.playerInventory = playerInventory;
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

    public ServerWorld getWorld() {
        return world;
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

    public void syncToPlayer() {
        for (int i = 0; i < this.player.getInventory().size(); i++) {
            this.playerInventory.setStack(i, this.player.getInventory().getStack(i));
        }

        this.position = this.player.getEntityPos();
        this.rotation = this.player.getRotationClient();
        this.world = this.player.getEntityWorld();
    }
}
