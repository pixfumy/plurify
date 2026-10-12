package net.pixfumy.plurify.gui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.SimpleGuiElement;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.DyeItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.pixfumy.plurify.Alter;
import net.pixfumy.plurify.AltersIOHelper;
import net.pixfumy.plurify.gui.icon.PlurifyIcon;
import net.pixfumy.plurify.gui.icon.PlurifyIconUtil;

import java.util.ArrayList;
import java.util.List;

public class ChangeAlterIconGui extends MultiPageGui<ItemStack> {
    private final Alter alter;
    public static final List<ItemStack> ITEMS_FOR_ICONS = new ArrayList<ItemStack>() {{
        for (PlurifyIcon plurifyIcon : PlurifyIcon.values()) {
            if (plurifyIcon.id.contains("avatar")) {
                this.add(plurifyIcon.getAsItemStack());
            }
        }
    }};

    public ChangeAlterIconGui(ServerPlayer player, Alter alter, AlterDetailsGui alterDetailsGui) {
        super(player, null, alterDetailsGui, alter.getPlayerInventory());
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
    }

    @Override
    protected List<ItemStack> getElements() {
        return ITEMS_FOR_ICONS;
    }

    @Override
    protected void setCurrentPageSlot(int i, ItemStack itemStack) {
        ItemStack itemStackCopy = itemStack.copy();
        if (PlurifyIconUtil.getAsPlurifyItem(itemStackCopy) == PlurifyIconUtil.getAsPlurifyItem(alter.getIcon())) {
            itemStackCopy.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        }
        GuiElement alterAvatarIcon = new SimpleGuiElement(itemStackCopy, GuiElement.callback(() -> {
            alter.setIcon(itemStackCopy);
            AltersIOHelper.writePlayerAltersToFile(player);
        }));
        this.setSlot(i, alterAvatarIcon);
    }
}
