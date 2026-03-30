package net.pixfumy.plurify.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.component.type.ToolComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.text.TranslatableTextContent;
import net.minecraft.util.Formatting;
import net.pixfumy.plurify.Alter;
import net.pixfumy.plurify.AltersIOHelper;
import net.pixfumy.plurify.IAltersOwner;

import java.util.*;

public class MainAltersGui extends SimpleGui {
    public MainAltersGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, false);
        this.setTitle(Text.literal("Alters"));
        if (((IAltersOwner)player).plurify$getAlters().isEmpty()) {
            AltersIOHelper.loadPlayerAltersFromFile(player);
        }
        ((IAltersOwner)player).plurify$getCurrentAlter().syncToPlayer();

        ItemStack addAlterButtonItemStack = Items.EGG.getDefaultStack();
        addAlterButtonItemStack.set(DataComponentTypes.ITEM_NAME, Text.literal("Add Alter")
                .setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE)));

        this.setSlot(49, new GuiElement(addAlterButtonItemStack, (i, clickType, slotActionType, slotGuiInterface) -> {
            if (clickType == ClickType.MOUSE_LEFT) {
                Alter alter = new Alter(player);
                new NameAlterGui(player, alter, this).open();
            }
        }));

        showAlters();
    }

    @Override public boolean open() {
        showAlters();
        return super.open();
    }

    // TODO: pagination
    private void showAlters() {
        Collection<Alter> alterList = ((IAltersOwner) player).plurify$getAlters().values();
        int headIndex = 0;
        for (Alter alter : alterList) {
            ItemStack alterIcon = alter.getIcon().getDefaultStack();
            alterIcon.set(DataComponentTypes.ITEM_NAME, alter.getName());

            Style style = Style.EMPTY.withColor(Formatting.GRAY);
            LoreComponent loreComponent = LoreComponent.DEFAULT
                    .with(Text.literal("Click to switch").setStyle(style))
                    .with(Text.literal("Shift + Click to see details").setStyle(style));

            if (((IAltersOwner)player).plurify$getCurrentAlter() == alter) {
                alterIcon.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
                loreComponent = loreComponent.with(Text.literal("Currently Selected"));
            }

            alterIcon.set(DataComponentTypes.LORE, loreComponent);

            this.setSlot(headIndex, new GuiElement(alterIcon, (i, clickType, slotActionType, slotGuiInterface) -> {
                if (clickType == ClickType.MOUSE_LEFT) {
                    ((IAltersOwner) player).plurify$switchToAlter(alter);
                    showAlters();
                } else if (clickType == ClickType.MOUSE_LEFT_SHIFT) {
                    new AlterDetailsGui(player, alter, this).open();
                }
            }));
            headIndex++;
        }
    }
}
