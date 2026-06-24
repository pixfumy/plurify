package net.pixfumy.plurify.gui;

import eu.pb4.sgui.api.elements.AnimatedGuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.LoreComponent;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;
import net.minecraft.util.math.Vec3d;
import net.pixfumy.plurify.Alter;
import net.pixfumy.plurify.AltersIOHelper;
import net.pixfumy.plurify.IAltersOwner;
import net.pixfumy.plurify.networking.OpenAlterSkinScreenS2CPayload;

import java.util.*;

public class AlterDetailsGui extends SimpleGui {
    private Alter alter;
    private final ItemStack keepInventoryStack;

    // array of EquipmentSlots feet, legs, chest, head
    private static final EquipmentSlot[] ARMOR_SLOTS = Arrays.stream(EquipmentSlot.values())
            .filter(equipmentSlot -> equipmentSlot.getType() == EquipmentSlot.Type.HUMANOID_ARMOR)
            .sorted(Comparator.comparingInt(EquipmentSlot::getIndex))
            .toArray(EquipmentSlot[]::new);

    public AlterDetailsGui(ServerPlayerEntity player, Alter alter, MainAltersGui mainAltersGui) {
        super(ScreenHandlerType.GENERIC_9X6, player, true);
        this.alter = alter;

        // start with a red background
        ItemStack redGlassPane = Items.RED_STAINED_GLASS_PANE.getDefaultStack();
        redGlassPane.set(DataComponentTypes.ITEM_NAME, Text.literal(""));
        for (int i = 0; i < this.getSize(); i++) {
            this.setSlot(i, redGlassPane.copy());
        }

        // main inventory
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

        // make a grid of black panes to mimic the player texture in the inventory
        ItemStack blackGlassPane = Items.BLACK_STAINED_GLASS_PANE.getDefaultStack();
        blackGlassPane.set(DataComponentTypes.ITEM_NAME, Text.literal(""));
        for (int i = 0; i < 4; i++) {
            for (int j = 0; j < 4; j++) {
                this.setSlot(9 * j + i, blackGlassPane.copy());
            }
        }

        // armour
        for (int i = 0; i < 4; i++) {
            ItemStack armourStack = alter.getPlayerInventory().getStack(36 + i).copy();

            armourStack.set(DataComponentTypes.ITEM_NAME,
                    Text.translatable("plurify.gui.alter_details_gui.armor_slot." + ARMOR_SLOTS[i].getName(),
                            armourStack.getName().getString()));

            this.setSlot(27 - i * 9, armourStack);
        }

        // offhand slot
        ItemStack offhandStack = alter.getPlayerInventory().getStack(40).copy();
        offhandStack.set(DataComponentTypes.ITEM_NAME,
                Text.translatable("plurify.gui.alter_details_gui.offhand_slot", offhandStack.getName().getString()));

        this.setSlot(31, offhandStack);

        // green glass panes to separate action bar from player inventory
        ItemStack greenGlassPane = Items.GREEN_STAINED_GLASS_PANE.getDefaultStack();
        greenGlassPane.set(DataComponentTypes.ITEM_NAME, Text.literal(""));
        for (int i = 36; i < 45; i++) {
            this.setSlot(i, greenGlassPane.copy());
        }

        // switch to alter
        ItemStack switchToPlayerStack = Items.ENDER_PEARL.getDefaultStack();
        switchToPlayerStack.set(DataComponentTypes.ITEM_NAME, Text.translatable("plurify.gui.alter_details_gui.switch_to_alter"));
        switchToPlayerStack.set(DataComponentTypes.ENCHANTMENT_GLINT_OVERRIDE, true);
        this.setSlot(11, switchToPlayerStack, ((i, clickType, slotActionType)
                -> ((IAltersOwner)player).plurify$switchToAlter(alter)));
        this.setSlot(45, switchToPlayerStack.copy(), ((i, clickType, slotActionType)
                -> ((IAltersOwner)player).plurify$switchToAlter(alter)));

        // Rename this Alter
        ItemStack renameStack = Items.OAK_SIGN.getDefaultStack();
        renameStack.set(DataComponentTypes.ITEM_NAME, Text.translatable("plurify.gui.alter_details_gui.rename_alter"));
        this.setSlot(46, renameStack, (i, clickType, slotActionType) -> {
                    new NameAlterGui(player, alter, this).open();
        });

        // Change Icon
        ItemStack changeIconStack = Items.PAINTING.getDefaultStack();
        changeIconStack.set(DataComponentTypes.ITEM_NAME, Text.translatable("plurify.gui.alter_details_gui.change_alter_icon"));
        this.setSlot(47, changeIconStack, ((i, clickType, slotActionType) ->
                new ChangeAlterIconGui(player, alter, this).open()));

        // Delete Alter
        ItemStack deleteAlterStack = Items.LAVA_BUCKET.getDefaultStack();
        deleteAlterStack.set(DataComponentTypes.ITEM_NAME, Text.translatable("plurify.gui.alter_details_gui.delete_alter"));
        if (alter.isCurrentAlter()) {
            deleteAlterStack.set(DataComponentTypes.LORE,
                    LoreComponent.DEFAULT.with(Text.translatable("plurify.gui.alter_details_gui.delete_alter.disabled")
                            .setStyle(Style.EMPTY.withColor(Formatting.RED))));
            ItemStack barrier = deleteAlterStack.copy();
            barrier.set(DataComponentTypes.ITEM_MODEL, Items.BARRIER.getDefaultStack().get(DataComponentTypes.ITEM_MODEL));

            ItemStack[] lavaBucketAndBarrier = new ItemStack[]{deleteAlterStack, barrier};
            this.setSlot(48, new AnimatedGuiElement(lavaBucketAndBarrier, 20, false,
                    ((i, clickType, slotActionType, slotGuiInterface) -> {})));
        } else {
            this.setSlot(48, deleteAlterStack, ((i, clickType, slotActionType, slotGuiInterface) ->
                    new DeleteAlterGui(player, alter, this, mainAltersGui).open()));
        }


        // Last Seen at
        ItemStack lastSeenStack = Items.COMPASS.getDefaultStack();
        lastSeenStack.set(DataComponentTypes.ITEM_NAME, Text.translatable("plurify.gui.alter_details_gui.last_seen.name"));
        Vec3d alterPos = alter.getPosition();

        LoreComponent positionComponent = LoreComponent.DEFAULT.with(
                Text.translatable("plurify.gui.alter_details_gui.last_seen.description",
                        (int) alterPos.x,
                        (int) alterPos.y,
                        (int) alterPos.z,
                        Text.translatable("plurify.gui.alter_details_gui.last_seen.dimension."
                                + alter.getWorld().getRegistryKey().getValue().getPath()).getString()
                )
        );

        lastSeenStack.set(DataComponentTypes.LORE, positionComponent);
        this.setSlot(49, lastSeenStack);

        // change skin
        ItemStack changeSkinStack = Items.PLAYER_HEAD.getDefaultStack();
        changeSkinStack.set(DataComponentTypes.ITEM_NAME, Text.translatable("plurify.gui.alter_details_gui.change_skin"));
        if (((IAltersOwner) player).plurify$isClientLoaded()) {
            this.setSlot(50, changeSkinStack, ((i, clickType, slotActionType) ->
                    ServerPlayNetworking.send(player, new OpenAlterSkinScreenS2CPayload(alter.getName().getString(), alter.getUuid().toString()))));
        } else {
            changeSkinStack.set(DataComponentTypes.LORE, LoreComponent.DEFAULT.with(
                    Text.translatable("plurify.gui.alter_details_gui.change_skin.disabled")
                            .setStyle(Style.EMPTY.withColor(Formatting.RED))
            ));
            ItemStack barrier = changeSkinStack.copy();
            barrier.set(DataComponentTypes.ITEM_MODEL, Items.BARRIER.getDefaultStack().get(DataComponentTypes.ITEM_MODEL));
            ItemStack[] playerHeadAndBarrier = new ItemStack[]{changeSkinStack, barrier};
            this.setSlot(50, new AnimatedGuiElement(playerHeadAndBarrier, 20, false,
                    ((i, clickType, slotActionType, slotGuiInterface) -> {})));
        }

        // Keep Inventory Toggle
        this.keepInventoryStack = getKeepInvIcon(alter.hasKeepInventory());
        this.setSlot(51, keepInventoryStack, (i, clickType, slotActionType) -> {
            onKeepInvToggle();
        });

        // back to MainAltersGui
        ItemStack backArrow = Items.ARROW.getDefaultStack();
        backArrow.set(DataComponentTypes.ITEM_NAME, Text.translatable("plurify.gui.main_alters_gui.back_to_main_alters"));
        this.setSlot(53, backArrow, (i, clickType, slotActionType) -> {
            mainAltersGui.open();
        });
    }

    @Override public boolean open() {
        setTitle(this.alter.getName());
        return super.open();
    }

    private ItemStack getKeepInvIcon(boolean hasKeepInv) {
        ItemStack keepInventoryStack = (hasKeepInv ? Items.GREEN_BUNDLE : Items.RED_BUNDLE).getDefaultStack();

        keepInventoryStack.set(DataComponentTypes.ITEM_NAME,
                Text.translatable("plurify.gui.alter_details_gui.keep_inventory",
                        Text.translatable("plurify.gui.alter_details_gui.keep_inventory." + hasKeepInv).getString()
                ).setStyle(Style.EMPTY.withColor(hasKeepInv ? Formatting.GREEN : Formatting.RED)));

        return keepInventoryStack;
    }

    private void onKeepInvToggle() {
        alter.setKeepInventory(!alter.hasKeepInventory());

        ItemStack keepInventoryStack = getKeepInvIcon(alter.hasKeepInventory());

        this.setSlot(51, keepInventoryStack, (i, clickType, slotActionType) -> {
            onKeepInvToggle();
        });

    }
}
