package net.pixfumy.plurify.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.DyeItem;
import net.minecraft.item.Item;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.registry.Registries;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Text;
import net.pixfumy.plurify.Alter;
import net.pixfumy.plurify.AltersIOHelper;

import java.util.List;

public class ChangeAlterIconGui extends SimpleGui {
    private final Alter alter;
    List<Item> ITEMS_FOR_ICONS = Registries.ITEM.stream().filter(
            item -> item instanceof DyeItem
    ).toList(); // TODO: pagination

    public ChangeAlterIconGui(ServerPlayerEntity player, Alter alter, AlterDetailsGui alterDetailsGui) {
        super(ScreenHandlerType.GENERIC_9X6, player, true);
        this.alter = alter;
        setTitle(Text.literal("Change Icon for " + alter.getName().getString()));

        // alter inventory
        for (int i = 0; i < 36; i++) {
            ItemStack inventoryStack = alter.getPlayerInventory().getStack(i).copy();
            inventoryStack.set(DataComponentTypes.ITEM_NAME, Text.literal(inventoryStack.getName().getString() + " (Slot " + i + ")"));
            if (i < 9) {
                this.setSlot(i + 81, inventoryStack);
            } else {
                this.setSlot(i + 45, inventoryStack);
            }
        }

        for (int i = 0; i < ITEMS_FOR_ICONS.size(); i++) {
            Item icon = ITEMS_FOR_ICONS.get(i);
            this.setSlot(i, icon.getDefaultStack(), ((i1, clickType, slotActionType) -> {
                alter.setIcon(icon);
                AltersIOHelper.writePlayerAltersToFile(player);
            }));
        }

        // back to AlterDetailsGui
        ItemStack backArrow = Items.ARROW.getDefaultStack();
        backArrow.set(DataComponentTypes.ITEM_NAME, Text.literal("Back to Alter Details"));
        this.setSlot(53, backArrow, (i, clickType, slotActionType) -> {
            alterDetailsGui.open();
        });
    }
}
