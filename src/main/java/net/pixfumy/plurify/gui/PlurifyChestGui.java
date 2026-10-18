package net.pixfumy.plurify.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.pixfumy.plurify.gui.icon.PlurifyIcon;
import org.jetbrains.annotations.Nullable;

public class PlurifyChestGui extends SimpleGui {

    protected final String backToThisGuiKey;

    public PlurifyChestGui(ServerPlayer player) {
        this(player, null, null, null);
    }

    public PlurifyChestGui(ServerPlayer player, @Nullable String backToThisGuiKey) {
        this(player, backToThisGuiKey, null, null);
    }

    public PlurifyChestGui(ServerPlayer player, @Nullable String backToThisGuiKey, @Nullable PlurifyChestGui parent) {
        this(player, backToThisGuiKey, parent, null);
    }

    public PlurifyChestGui(ServerPlayer player, @Nullable String backToThisGuiKey,@Nullable PlurifyChestGui parent, @Nullable Inventory playerInventory) {
        super(MenuType.GENERIC_9x6, player, playerInventory != null);
        this.backToThisGuiKey = backToThisGuiKey;

        if (parent != null) {
            // back to parent
            ItemStack backArrow = PlurifyIcon.MENU_BACK.getAsItemStack();
            backArrow.set(DataComponents.ITEM_NAME, Component.translatable(parent.backToThisGuiKey));
            this.setSlot(49, backArrow, parent::open);
        }

        if (playerInventory != null) {
            for (int i = 0; i < 36; i++) {
                ItemStack inventoryStack = playerInventory.getItem(i).copy();

                inventoryStack.set(DataComponents.ITEM_NAME,
                        Component.translatable("plurify.gui.alter_details_gui.inventory_slot", inventoryStack.getHoverName().getString(), i));

                if (i < 9) {
                    this.setSlot(i + 81, inventoryStack);
                } else {
                    this.setSlot(i + 45, inventoryStack);
                }
            }
        }
    }

}
