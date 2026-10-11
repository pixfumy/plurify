package net.pixfumy.plurify.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.SimpleGuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import net.pixfumy.plurify.Alter;
import net.pixfumy.plurify.AltersIOHelper;
import net.pixfumy.plurify.IAltersOwner;

import java.util.*;

public class MainAltersGui extends SimpleGui {

    private static final int EGG_INDEX = 49;

    public MainAltersGui(ServerPlayer player) {
        super(MenuType.GENERIC_9x6, player, false);
        this.setTitle(Component.translatable("plurify.gui.main_alters_gui.title"));
        if (((IAltersOwner)player).plurify$getAlters().isEmpty()) {
            AltersIOHelper.loadPlayerAltersFromFile(player);
        }

        ((IAltersOwner) player).plurify$getCurrentAlter().setAlterEntityDataFromPlayer();

        ItemStack addAlterButtonItemStack = Items.EGG.getDefaultInstance();
        addAlterButtonItemStack.set(DataComponents.ITEM_NAME, Component.translatable("plurify.gui.main_alters_gui.add_alter")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE)));

        this.setSlot(EGG_INDEX, new SimpleGuiElement(addAlterButtonItemStack, (i, clickType, slotActionType, slotGuiInterface) -> {
            if (clickType == ClickType.MOUSE_LEFT) {
                Alter alter = new Alter(player, false);
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
        clearAlterSlots();
        Collection<Alter> alterList = ((IAltersOwner) player).plurify$getAlters().values();
        int headIndex = 0;
        for (Alter alter : alterList) {
            ItemStack alterIcon = alter.getIcon().getDefaultInstance();
            alterIcon.set(DataComponents.ITEM_NAME, alter.getName());

            Style style = Style.EMPTY.withColor(ChatFormatting.GRAY);
            ItemLore loreComponent = ItemLore.EMPTY
                    .withLineAdded(Component.translatable("plurify.gui.main_alters_gui.switch_to_alter").setStyle(style))
                    .withLineAdded(Component.translatable("plurify.gui.main_alters_gui.go_to_alter_details").setStyle(style));

            if (((IAltersOwner)player).plurify$getCurrentAlter() == alter) {
                alterIcon.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
                loreComponent = loreComponent.withLineAdded(Component.translatable("plurify.gui.main_alters_gui.currently_selected"));
            }

            alterIcon.set(DataComponents.LORE, loreComponent);

            this.setSlot(headIndex, new SimpleGuiElement(alterIcon, (i, clickType, slotActionType, slotGuiInterface) -> {
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

    private void clearAlterSlots() {
        for (int i = 0; i < this.size && i != EGG_INDEX; i++) {
            this.setSlot(i, ItemStack.EMPTY);
        }
    }
}
