package net.pixfumy.plurify.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.pixfumy.plurify.Alter;
import net.pixfumy.plurify.AltersIOHelper;

import java.util.List;

public class ChangeAlterIconGui extends SimpleGui {
    private final Alter alter;
    List<Item> ITEMS_FOR_ICONS = BuiltInRegistries.ITEM.stream().filter(
            item -> item instanceof DyeItem
    ).toList(); // TODO: pagination

    public ChangeAlterIconGui(ServerPlayer player, Alter alter, AlterDetailsGui alterDetailsGui) {
        super(MenuType.GENERIC_9x6, player, true);
        this.alter = alter;
        setTitle(Component.translatable("plurify.gui.change_alter_icon_gui.title", alter.getName().getString()));

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

        for (int i = 0; i < ITEMS_FOR_ICONS.size(); i++) {
            Item icon = ITEMS_FOR_ICONS.get(i);
            this.setSlot(i, icon.getDefaultInstance(), (() -> {
                alter.setIcon(icon);
                AltersIOHelper.writePlayerAltersToFile(player);
            }));
        }

        // back to AlterDetailsGui
        ItemStack backArrow = Items.ARROW.getDefaultInstance();
        backArrow.set(DataComponents.ITEM_NAME, Component.translatable("plurify.gui.alter_details_gui.back_to_alter_details"));
        this.setSlot(53, backArrow, alterDetailsGui::open);
    }
}
