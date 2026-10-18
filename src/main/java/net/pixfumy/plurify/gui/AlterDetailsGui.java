package net.pixfumy.plurify.gui;

import eu.pb4.sgui.api.elements.AnimatedGuiElement;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.DyeColor;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.DyedItemColor;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.phys.Vec3;
import net.pixfumy.plurify.Alter;
import net.pixfumy.plurify.AltersIOHelper;
import net.pixfumy.plurify.IAltersOwner;
import net.pixfumy.plurify.gui.icon.PlurifyIcon;
import net.pixfumy.plurify.gui.icon.PlurifyIconUtil;
import net.pixfumy.plurify.networking.OpenAlterSkinScreenS2CPayload;

import java.util.*;

public class AlterDetailsGui extends PlurifyChestGui {
    private static final int ARMOUR_HELMET_SLOT = 0;
    private static final int RENAME_ALTER_SLOT = 1;
    private static final int CHANGE_ICON_SLOT = 3;
    private static final int LAST_SEEN_SLOT = 8;
    private static final int ALTER_ICON_SLOT = 11;
    private static final int KEEP_INV_SLOT = 15;
    private static final int ENDER_CHEST_SLOT = 25;
    private static final int CHANGE_SKIN_SLOT = 28;
    private static final int DELETE_ALTER_SLOT = 30;
    private static final int OFFHAND_STACK_SLOT = 31;
    private static final int SWITCH_TO_ALTER_SLOT = 45;

    private Alter alter;

    // array of EquipmentSlots feet, legs, chest, head
    private static final EquipmentSlot[] ARMOR_SLOTS = Arrays.stream(EquipmentSlot.values())
            .filter(equipmentSlot -> equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
            .sorted(Comparator.comparingInt(EquipmentSlot::getId))
            .toArray(EquipmentSlot[]::new);

    @Override
    public boolean open() {
        setTitle(alter.getName());
        return super.open();
    }

    public AlterDetailsGui(ServerPlayer player, Alter alter, MainAltersGui mainAltersGui) {
        super(player, "plurify.gui.alter_details_gui.back_to_alter_details", mainAltersGui, alter.getPlayerInventory());

        this.alter = alter;

        // start with a red background
        ItemStack redGlassPane = PlurifyIconUtil.getItemStackWithNoTooltip(Items.STAINED_GLASS_PANE.red());
        for (int i = 0; i < 36; i++) {
            this.setSlot(i, redGlassPane.copy());
        }

        // make a grid of black panes to mimic the player texture in the inventory
        ItemStack blackGlassPane = PlurifyIconUtil.getItemStackWithNoTooltip(Items.STAINED_GLASS_PANE.black());
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                this.setSlot(9 * j + i, blackGlassPane.copy());
            }
        }

        // make a 4x4 grid of brown panes to mimic the crafting texture
        ItemStack brownGlassPane = PlurifyIconUtil.getItemStackWithNoTooltip(Items.STAINED_GLASS_PANE.brown());
        for (int i : List.of(15, 16, 24, 25)) {
            this.setSlot(i, brownGlassPane.copy());
        }

        // armour
        for (int i = 0; i < 4; i++) {
            ItemStack armourStack = alter.getPlayerInventory().getItem(36 + i).copy();

            armourStack.set(DataComponents.ITEM_NAME,
                    Component.translatable("plurify.gui.alter_details_gui.armor_slot." + ARMOR_SLOTS[i].getName(),
                            armourStack.getHoverName().getString()));

            this.setSlot(ARMOUR_HELMET_SLOT + 27 - i * 9, armourStack);
        }

        // alter icon
        ItemStack alterIconStack = alter.getIcon();
        alterIconStack.set(DataComponents.ITEM_NAME, Component.translatable("plurify.gui.alter_details_gui.selected_icon"));
        this.setSlot(ALTER_ICON_SLOT, alterIconStack);

        // Last Seen at
        ItemStack lastSeenStack = PlurifyIcon.MENU_LOCATION.getAsItemStack();
        lastSeenStack.set(DataComponents.ITEM_NAME, Component.translatable("plurify.gui.alter_details_gui.last_seen.name"));
        Vec3 alterPos = alter.getPosition();
        ServerLevel alterWorld = alter.getLevel();

        ItemLore positionComponent = ItemLore.EMPTY.withLineAdded(
                Component.translatable("plurify.gui.alter_details_gui.last_seen.description",
                        (int) alterPos.x,
                        (int) alterPos.y,
                        (int) alterPos.z,
                        Component.translatable("plurify.gui.alter_details_gui.last_seen.dimension."
                                + alterWorld.dimension().identifier().getPath()
                        )
                )
        );

        lastSeenStack.set(DataComponents.LORE, positionComponent);
        this.setSlot(LAST_SEEN_SLOT, lastSeenStack);

        // offhand slot
        ItemStack offhandStack = alter.getPlayerInventory().getItem(40).copy();
        offhandStack.set(DataComponents.ITEM_NAME,
                Component.translatable("plurify.gui.alter_details_gui.offhand_slot", offhandStack.getHoverName().getString()));

        this.setSlot(OFFHAND_STACK_SLOT, offhandStack);

        // green glass panes to separate action bar from player inventory
        ItemStack greenGlassPane = PlurifyIconUtil.getItemStackWithNoTooltip(Items.STAINED_GLASS_PANE.green());
        for (int i = 36; i < 45; i++) {
            this.setSlot(i, greenGlassPane.copy());
        }

        // switch to alter
        ItemStack switchToPlayerStack = PlurifyIcon.MENU_SWITCH.getAsItemStack();
        switchToPlayerStack.set(DataComponents.ITEM_NAME, Component.translatable("plurify.gui.alter_details_gui.switch_to_alter"));
        switchToPlayerStack.set(DataComponents.ENCHANTMENT_GLINT_OVERRIDE, true);
        this.setSlot(SWITCH_TO_ALTER_SLOT, switchToPlayerStack.copy(), () -> ((IAltersOwner)player).plurify$switchToAlter(alter));

        // Rename this Alter
        ItemStack renameStack = PlurifyIcon.MENU_RENAME.getAsItemStack();
        renameStack.set(DataComponents.ITEM_NAME, Component.translatable("plurify.gui.alter_details_gui.rename_alter"));
        this.setSlot(RENAME_ALTER_SLOT, renameStack, () -> new NameAlterGui(player, alter, this).open());

        // Change Icon
        ItemStack changeIconStack = PlurifyIcon.MENU_CHOOSE_ICON.getAsItemStack();
        changeIconStack.set(DataComponents.ITEM_NAME, Component.translatable("plurify.gui.alter_details_gui.change_alter_icon"));
        this.setSlot(CHANGE_ICON_SLOT, changeIconStack, () -> new ChangeAlterIconGui(player, alter, this).open());

        // Delete Alter
        ItemStack deleteAlterStack = PlurifyIcon.MENU_DELETE.getAsItemStack();
        deleteAlterStack.set(DataComponents.ITEM_NAME, Component.translatable("plurify.gui.alter_details_gui.delete_alter"));
        if (alter.isCurrentAlter()) {
            deleteAlterStack.set(DataComponents.LORE,
                    ItemLore.EMPTY.withLineAdded(Component.translatable("plurify.gui.alter_details_gui.delete_alter.disabled")
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))));
            ItemStack barrier = deleteAlterStack.copy();
            barrier.set(DataComponents.ITEM_MODEL, Items.BARRIER.getDefaultInstance().get(DataComponents.ITEM_MODEL));

            ItemStack[] lavaBucketAndBarrier = new ItemStack[]{deleteAlterStack, barrier};
            this.setSlot(DELETE_ALTER_SLOT, new AnimatedGuiElement(lavaBucketAndBarrier, 20, false, GuiElement.EMPTY_CALLBACK));
        } else {
            this.setSlot(DELETE_ALTER_SLOT, deleteAlterStack, (() ->
                    new DeleteAlterGui(player, alter, this, mainAltersGui).open()));
        }

        // change skin
        ItemStack changeSkinStack = PlurifyIcon.MENU_CHANGE_SKIN.getAsItemStack();
        changeSkinStack.set(DataComponents.ITEM_NAME, Component.translatable("plurify.gui.alter_details_gui.change_skin"));
        if (((IAltersOwner) player).plurify$isClientLoaded()) {
            this.setSlot(CHANGE_SKIN_SLOT, changeSkinStack, () ->
                    {
                        this.close();
                        ServerPlayNetworking.send(player, new OpenAlterSkinScreenS2CPayload(alter.getName().getString(), alter.getUuid().toString()));
                    });
        } else {
            changeSkinStack.set(DataComponents.LORE, ItemLore.EMPTY.withLineAdded(
                    Component.translatable("plurify.gui.alter_details_gui.change_skin.disabled")
                            .setStyle(Style.EMPTY.withColor(ChatFormatting.RED))
            ));
            ItemStack barrier = changeSkinStack.copy();
            barrier.set(DataComponents.ITEM_MODEL, Items.BARRIER.getDefaultInstance().get(DataComponents.ITEM_MODEL));
            ItemStack[] playerHeadAndBarrier = new ItemStack[]{changeSkinStack, barrier};
            this.setSlot(CHANGE_SKIN_SLOT, new AnimatedGuiElement(playerHeadAndBarrier, 20, false, GuiElement.EMPTY_CALLBACK));
        }

        ItemStack enderChestStack = PlurifyIcon.MENU_ENDER_CHEST.getAsItemStack();
        enderChestStack.set(DataComponents.ITEM_NAME, Component.translatable("plurify.gui.alter_details_gui.view_ender_chest"));
        this.setSlot(ENDER_CHEST_SLOT, enderChestStack, () -> {
            new ViewEnderChestGui(player, alter, this).open();
        });

        // Keep Inventory Toggle
        ItemStack keepInventoryStack = getKeepInvIcon(alter.hasKeepInventory());
        this.setSlot(KEEP_INV_SLOT, keepInventoryStack, this::onKeepInvToggle);

    }

    private ItemStack getKeepInvIcon(boolean hasKeepInv) {
        ItemStack keepInvOnStack = PlurifyIcon.MENU_KEEP_INV_ON.getAsItemStack();
        ItemStack keepInvOffStack = PlurifyIcon.MENU_KEEP_INV_OFF.getAsItemStack();

        ItemStack keepInventoryStack = hasKeepInv ? keepInvOnStack : keepInvOffStack;

        keepInventoryStack.set(DataComponents.ITEM_NAME,
                Component.translatable("plurify.gui.alter_details_gui.keep_inventory",
                        Component.translatable("plurify.gui.alter_details_gui.keep_inventory." + hasKeepInv).getString()
                ).setStyle(Style.EMPTY.withColor(hasKeepInv ? ChatFormatting.GREEN : ChatFormatting.RED)));

        return keepInventoryStack;
    }

    private void onKeepInvToggle() {
        alter.setKeepInventory(!alter.hasKeepInventory());

        ItemStack keepInventoryStack = getKeepInvIcon(alter.hasKeepInventory());

        this.setSlot(KEEP_INV_SLOT, keepInventoryStack, this::onKeepInvToggle);

    }
}
