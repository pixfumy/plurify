package net.pixfumy.plurify.gui;

import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.pixfumy.plurify.Alter;
import net.pixfumy.plurify.IAltersOwner;

public class DeleteAlterGui extends SimpleGui {
    private final Alter alter;

    public DeleteAlterGui(ServerPlayerEntity player, Alter alter, AlterDetailsGui alterDetailsGui, MainAltersGui mainAltersGui) {
        super(ScreenHandlerType.GENERIC_9X6, player, true);
        this.alter = alter;
        setTitle(Text.translatable("plurify.gui.delete_alter_gui.title", this.alter.getName().getString()));

        // alter inventory
        for (int i = 0; i < 36; i++) {
            ItemStack inventoryStack = alter.getPlayerInventory().getStack(i).copy();

            inventoryStack.set(DataComponentTypes.ITEM_NAME,
                    Text.translatable("plurify.gui.alter_details_gui.inventory_slot", inventoryStack.getName().getString(), i));

            if (i < 9) {
                this.setSlot(i + 81, inventoryStack);
            } else {
                this.setSlot(i + 45, inventoryStack);
            }
        }

        // green background for YES option
        ItemStack greenGlassPane = Items.GREEN_STAINED_GLASS_PANE.getDefaultStack();
        greenGlassPane.set(DataComponentTypes.ITEM_NAME, Text.literal(""));
        for (int i = 0; i < 27; i++) {
            this.setSlot(i, greenGlassPane.copy());
        }

        // YES option
        ItemStack yesButton = Items.GREEN_DYE.getDefaultStack();

        yesButton.set(DataComponentTypes.ITEM_NAME,
                Text.translatable("plurify.gui.delete_alter_gui.confirm").setStyle(Style.EMPTY.withColor(Formatting.GREEN)));

        this.setSlot(13, yesButton, ((i, clickType, slotActionType) -> {
            ((IAltersOwner) player).plurify$removeFromAlters(alter);
            mainAltersGui.open();
        }));

        // red background for NO option
        ItemStack redGlassPane = Items.RED_STAINED_GLASS_PANE.getDefaultStack();
        redGlassPane.set(DataComponentTypes.ITEM_NAME, Text.literal(""));
        for (int i = 27; i < 54; i++) {
            this.setSlot(i, redGlassPane.copy());
        }

        // NO option
        ItemStack noButton = Items.RED_DYE.getDefaultStack();
        
        noButton.set(DataComponentTypes.ITEM_NAME,
                Text.translatable("plurify.gui.delete_alter_gui.cancel").setStyle(Style.EMPTY.withColor(Formatting.RED)));

        this.setSlot(40, noButton, ((i, clickType, slotActionType) -> {
            alterDetailsGui.open();
        }));

        // back to AlterDetailsGui
        ItemStack backArrow = Items.ARROW.getDefaultStack();
        backArrow.set(DataComponentTypes.ITEM_NAME, Text.translatable("plurify.gui.alter_details_gui.back_to_alter_details"));
        this.setSlot(53, backArrow, (i, clickType, slotActionType) -> {
            alterDetailsGui.open();
        });
    }
}
