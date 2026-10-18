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
import net.pixfumy.plurify.gui.icon.PlurifyIcon;
import net.pixfumy.plurify.gui.icon.PlurifyIconUtil;

public class DeleteAlterGui extends PlurifyChestGui {
    private final Alter alter;

    public DeleteAlterGui(ServerPlayer player, Alter alter, AlterDetailsGui alterDetailsGui, MainAltersGui mainAltersGui) {
        super(player, null, alterDetailsGui, alter.getPlayerInventory());
        this.alter = alter;
        setTitle(Component.translatable("plurify.gui.delete_alter_gui.title", this.alter.getName().getString()));

        // green background for YES option
        ItemStack greenGlassPane = PlurifyIconUtil.getItemStackWithNoTooltip(Items.STAINED_GLASS_PANE.green());
        for (int i = 0; i < 27; i++) {
            this.setSlot(i, greenGlassPane.copy());
        }

        // YES option
        ItemStack yesButton = PlurifyIcon.MENU_DELETE.getAsItemStack();

        yesButton.set(DataComponents.ITEM_NAME,
                Component.translatable("plurify.gui.delete_alter_gui.confirm").setStyle(Style.EMPTY.withColor(ChatFormatting.GREEN)));

        this.setSlot(13, yesButton, (() -> {
            ((IAltersOwner) player).plurify$removeFromAlters(alter);
            mainAltersGui.open();
        }));

        // red background for NO option
        ItemStack redGlassPane = PlurifyIconUtil.getItemStackWithNoTooltip(Items.STAINED_GLASS_PANE.red());
        for (int i = 27; i < 54; i++) {
            this.setSlot(i, redGlassPane.copy());
        }

        // NO option
        ItemStack noButton = PlurifyIcon.MENU_BACK.getAsItemStack();
        
        noButton.set(DataComponents.ITEM_NAME,
                Component.translatable("plurify.gui.delete_alter_gui.cancel").setStyle(Style.EMPTY.withColor(ChatFormatting.RED)));

        this.setSlot(40, noButton, (alterDetailsGui::open));
    }
}
