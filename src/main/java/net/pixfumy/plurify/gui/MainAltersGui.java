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
import net.pixfumy.plurify.gui.icon.PlurifyIcon;
import net.pixfumy.plurify.gui.icon.PlurifyIconUtil;

import java.util.*;

public class MainAltersGui extends MultiPageGui<Alter> {

    private static final int MENU_ADD_ALTER_SLOT = 49;

    public MainAltersGui(ServerPlayer player) {
        super(player, "plurify.gui.main_alters_gui.back_to_main_alters");
        this.setTitle(Component.translatable("plurify.gui.main_alters_gui.title"));

        ((IAltersOwner) player).plurify$getCurrentAlter().setAlterEntityDataFromPlayer();
    }

    @Override
    protected List<Alter> getElements() {
        if (((IAltersOwner)player).plurify$getAlters().isEmpty()) {
            AltersIOHelper.loadPlayerAltersFromFile(player);
        }
        return ((IAltersOwner) player).plurify$getAlters().values().stream().toList();
    }

    @Override
    protected void setCurrentPageSlot(int i, Alter alter) {
        ItemStack alterIcon = alter.getIcon();
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

        SimpleGuiElement alterGuiElement = new SimpleGuiElement(alterIcon, GuiElement.callback(clickType -> {
            if (clickType == ClickType.MOUSE_LEFT) {
                ((IAltersOwner) player).plurify$switchToAlter(alter);
                displayCurrentPage();
            } else if (clickType == ClickType.MOUSE_LEFT_SHIFT) {
                new AlterDetailsGui(player, alter, this).open();
            }
        }));

        this.setSlot(i, alterGuiElement);
    }

    @Override
    protected void displayCurrentPage() {
        super.displayCurrentPage();
        ItemStack addAlterButtonItemStack = PlurifyIcon.MENU_ADD_ALTER.getAsItemStack();
        addAlterButtonItemStack.set(DataComponents.ITEM_NAME, Component.translatable("plurify.gui.main_alters_gui.add_alter")
                .setStyle(Style.EMPTY.withColor(ChatFormatting.LIGHT_PURPLE)));

        this.setSlot(MENU_ADD_ALTER_SLOT, new SimpleGuiElement(addAlterButtonItemStack, (i, clickType, slotActionType, slotGuiInterface) -> {
            if (clickType == ClickType.MOUSE_LEFT) {
                Alter alter = new Alter(player, false);
                new NameAlterGui(player, alter, this).open();
            }
        }));
    }
}
