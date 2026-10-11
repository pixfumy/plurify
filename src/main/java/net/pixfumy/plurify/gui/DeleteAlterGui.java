package net.pixfumy.plurify.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.pixfumy.plurify.Alter;
import net.pixfumy.plurify.IAltersOwner;

public class DeleteAlterGui extends SimpleGui {
    private final Alter alter;

    public DeleteAlterGui(ServerPlayer player, Alter alter, AlterDetailsGui alterDetailsGui, MainAltersGui mainAltersGui) {
        super(MenuType.GENERIC_9x6, player, true);
        this.alter = alter;
        setTitle(Component.translatable("plurify.gui.delete_alter_gui.title", this.alter.getName().getString()));

        // alter inventory
        for (int i = 0; i < 36; i++) {
            ItemStack inventoryStack = alter.getPlayerInventory().getItem(i).copy();

            inventoryStack.set(DataComponents.ITEM_NAME,
                    Component.translatable("plurify.gui.alter_details_gui.inventory_slot", inventoryStack.getHoverName().getString(), i));

            if (i < 9) {
                this.setSlot(i + 81, inventoryStack);
            } else {
                this.setSlot(i + 45, inventoryStack);
            }
        }

        // green background for YES option
        ItemStack greenGlassPane = Items.STAINED_GLASS_PANE.green().getDefaultInstance();
        greenGlassPane.set(DataComponents.ITEM_NAME, Component.literal(""));
        for (int i = 0; i < 27; i++) {
            this.setSlot(i, greenGlassPane.copy());
        }

        // YES option
        ItemStack yesButton = Items.DYE.green().getDefaultInstance();

        yesButton.set(DataComponents.ITEM_NAME,
                Component.translatable("plurify.gui.delete_alter_gui.confirm").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));

        this.setSlot(13, yesButton, (() -> {
            ((IAltersOwner) player).plurify$removeFromAlters(alter);
            mainAltersGui.open();
        }));

        // red background for NO option
        ItemStack redGlassPane = Items.STAINED_GLASS_PANE.red().getDefaultInstance();
        redGlassPane.set(DataComponents.ITEM_NAME, Component.literal(""));
        for (int i = 27; i < 54; i++) {
            this.setSlot(i, redGlassPane.copy());
        }

        // NO option
        ItemStack noButton = Items.DYE.red().getDefaultInstance();
        
        noButton.set(DataComponents.ITEM_NAME,
                Component.translatable("plurify.gui.delete_alter_gui.cancel").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));

        this.setSlot(40, noButton, (alterDetailsGui::open));

        // back to AlterDetailsGui
        ItemStack backArrow = Items.ARROW.getDefaultInstance();
        backArrow.set(DataComponents.ITEM_NAME, Component.translatable("plurify.gui.alter_details_gui.back_to_alter_details"));
        this.setSlot(53, backArrow, alterDetailsGui::open);
    }
}
