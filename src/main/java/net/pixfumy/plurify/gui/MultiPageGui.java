package net.pixfumy.plurify.gui;

import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.elements.SimpleGuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.pixfumy.plurify.gui.icon.PlurifyIcon;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public abstract class MultiPageGui<T> extends PlurifyChestGui {

    private List<T> elements;
    protected boolean shouldRefresh;

    protected int currentPage;
    protected final SimpleGui parent;
    protected static final int PAGE_SIZE = 36; // 9 * 4, use bottom row for actions, second-to-bottom row as a divider
    protected static final int BOTTOM_LEFT_SLOT = 45;

    public MultiPageGui(ServerPlayer player, @Nullable String backToThisGuiKey) {
        this(player, backToThisGuiKey, null, null);
    }

    public MultiPageGui(ServerPlayer player, @Nullable String backToThisGuiKey, @Nullable PlurifyChestGui parent) {
        this(player, backToThisGuiKey, parent, null);
    }

    public MultiPageGui(ServerPlayer player, @Nullable String backToThisGuiKey, @Nullable PlurifyChestGui parent, @Nullable Inventory playerInventory) {
        super(player, backToThisGuiKey, parent, playerInventory);
        this.elements = getElements();
        this.parent = parent;
    }

    @Override public boolean open() {
        this.elements = getElements();
        displayCurrentPage();
        return super.open();
    }

    protected void displayCurrentPage() {
        this.clearSlots();

        boolean firstPage = this.currentPage == 0;
        boolean lastPage = elements.size() <= (this.currentPage + 1) * PAGE_SIZE;

        for (int i = 0; i < PAGE_SIZE; i++) {
            if (currentPage * PAGE_SIZE + i >= elements.size()) {
                lastPage = true;
                break;
            }
            this.setCurrentPageSlot(i, elements.get(36 * currentPage + i));
        }

        // green glass panes to separate action bar from selectable items
        ItemStack greenGlassPane = Items.STAINED_GLASS_PANE.green().getDefaultInstance();
        greenGlassPane.set(DataComponents.ITEM_NAME, Component.literal(""));
        for (int i = PAGE_SIZE; i < BOTTOM_LEFT_SLOT; i++) {
            this.setSlot(i, greenGlassPane.copy());
        }

        if (parent != null) {
            ItemStack backToParentIcon = PlurifyIcon.MENU_BACK.getAsItemStack();
            this.setSlot(BOTTOM_LEFT_SLOT + 4, backToParentIcon, this.parent::open);
        }

        SimpleGuiElement previousPageIcon;
        if (!firstPage) {
            ItemStack previousPageItemStack = PlurifyIcon.MENU_ARROW_LEFT.getAsItemStack();
            previousPageIcon = new SimpleGuiElement(previousPageItemStack, GuiElement.callback(() -> {
                this.currentPage--;
                displayCurrentPage();
            }));
        } else {
            previousPageIcon = new SimpleGuiElement(ItemStack.EMPTY, GuiElement.EMPTY_CALLBACK);
        }
        this.setSlot(BOTTOM_LEFT_SLOT, previousPageIcon);

        SimpleGuiElement nextPageIcon;
        if (!lastPage) {
            ItemStack nextPageItemStack = PlurifyIcon.MENU_ARROW_RIGHT.getAsItemStack();
            nextPageIcon = new SimpleGuiElement(nextPageItemStack, GuiElement.callback(() -> {
                this.currentPage++;
                displayCurrentPage();
            }));
        } else {
            nextPageIcon = new SimpleGuiElement(ItemStack.EMPTY, GuiElement.EMPTY_CALLBACK);
        }
        this.setSlot(BOTTOM_LEFT_SLOT + 8, nextPageIcon);
    }

    protected abstract List<T> getElements();

    protected abstract void setCurrentPageSlot(int i, T element);

    private void clearSlots() {
        for (int i = 0; i < this.size; i++) {
            this.setSlot(i, ItemStack.EMPTY);
        }
    }
}
