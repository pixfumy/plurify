package net.pixfumy.plurify.gui;

import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.pixfumy.plurify.Alter;
import net.pixfumy.plurify.gui.icon.PlurifyIconUtil;

public class ViewEnderChestGui extends PlurifyChestGui {

    public ViewEnderChestGui(ServerPlayer player, Alter alter, AlterDetailsGui alterDetailsGui) {
        super(player, null, alterDetailsGui, alter.getPlayerInventory());

        setTitle(Component.translatable("plurify.gui.view_ender_chest_gui.title", alter.getName()));

        // green glass panes to separate action bar from selectable items
        ItemStack yellowGlassPane = PlurifyIconUtil.getItemStackWithNoTooltip(Items.STAINED_GLASS_PANE.yellow());
        ItemStack purpleGlassPane = PlurifyIconUtil.getItemStackWithNoTooltip(Items.STAINED_GLASS_PANE.purple());
        for (int i = 27; i < 45; i++) {
            this.setSlot(i, i % 2 == 0 ? yellowGlassPane : purpleGlassPane);
        }

        for (int i = 0; i < alter.getEnderChestInventory().getContainerSize(); i++) {
            this.setSlot(i, alter.getEnderChestInventory().getItem(i));
        }
    }
}
