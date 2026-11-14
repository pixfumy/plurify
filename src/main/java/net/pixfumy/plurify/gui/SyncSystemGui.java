package net.pixfumy.plurify.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.SimpleGuiElement;
import eu.pb4.sgui.api.gui.SignGui;
import eu.pb4.sgui.api.gui.SimpleGui;
import eu.pb4.sgui.impl.virtual.sign.VirtualSignBlockEntity;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.util.Mth;
import net.minecraft.world.inventory.MenuType;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.block.Blocks;
import net.pixfumy.plurify.alter.IAltersOwner;
import net.pixfumy.plurify.alter.PlayerAltersConfig;
import net.pixfumy.plurify.gui.icon.PlurifyIcon;
import net.pixfumy.plurify.gui.icon.PlurifyIconUtil;
import net.pixfumy.plurify.network.altersync.RequestAltersSyncS2CPacket;
import net.pixfumy.plurify.network.altersync.SyncAltersS2CPacket;
import net.pixfumy.plurify.pluralkit.PluralKitImporter;

import javax.xml.crypto.Data;
import java.util.Locale;

public class SyncSystemGui extends PlurifyChestGui {

    public SyncSystemGui(ServerPlayer player) {
        super(player);
    }

    public SyncSystemGui(ServerPlayer player, MainAltersGui mainAltersGui) {
        super(player, null, mainAltersGui);

        this.setTitle(Component.translatable("plurify.gui.sync_system_gui.title"));

        // green glass panes to separate action bar from selectable items
        ItemStack greenGlassPane = PlurifyIconUtil.getItemStackWithNoTooltip(Items.STAINED_GLASS_PANE.green());
        for (int i = 36; i < 45; i++) {
            this.setSlot(i, greenGlassPane.copy());
        }

        // some decor since this isn't a very busy screen
        int[] orangeGlassPaneSlots = new int[]{3, 4, 5, 12, 14, 21, 23, 30, 31, 32};
        ItemStack orangeGlassPane = PlurifyIconUtil.getItemStackWithNoTooltip(Items.STAINED_GLASS_PANE.orange());
        for (int i : orangeGlassPaneSlots) {
            this.setSlot(i, orangeGlassPane.copy());
        }

        int[] cyanGlassPaneSlots = new int[]{2, 11, 20, 19, 18, 33, 24, 15, 16, 17};
        ItemStack cyanGlassPane = PlurifyIconUtil.getItemStackWithNoTooltip(Items.STAINED_GLASS_PANE.cyan());
        for (int i : cyanGlassPaneSlots) {
            this.setSlot(i, cyanGlassPane.copy());
        }

        int[] blueGlassPaneSlots = new int[]{1, 9, 10, 34, 22, 25, 26};
        ItemStack blueGlassPane = PlurifyIconUtil.getItemStackWithNoTooltip(Items.STAINED_GLASS_PANE.blue());
        for (int i : blueGlassPaneSlots) {
            this.setSlot(i, blueGlassPane.copy());
        }

        int[] lightBlueGlassPaneSlots = new int[]{27, 28, 29, 8, 7, 6};
        ItemStack lightBlueGlassPane = PlurifyIconUtil.getItemStackWithNoTooltip(Items.STAINED_GLASS_PANE.lightBlue());
        for (int i : lightBlueGlassPaneSlots) {
            this.setSlot(i, lightBlueGlassPane.copy());
        }

        // import alters from PluralKit
        ItemStack importAltersFromPK = PlurifyIcon.MENU_PLURAL_KIT.getAsItemStack();
        importAltersFromPK.set(DataComponents.ITEM_NAME, Component.translatable("plurify.gui.sync_system_gui.import_system_from_pk"));
        this.setSlot(13, new SimpleGuiElement(importAltersFromPK, (i, clickType, slotActionType, slotGuiInterface) -> {
            if (clickType == ClickType.MOUSE_LEFT) {
                new SignGui(player) {
                    @Override
                    public boolean open() {
                        this.setLine(1, Component.literal("^^^^^^"));
                        this.setLine(2, Component.translatable("plurify.gui.sync_system_gui.enter_pk_id.line1"));
                        this.setLine(3, Component.translatable("plurify.gui.sync_system_gui.enter_pk_id.line2"));
                        return super.open();
                    }
                    @Override
                    public void close(boolean alreadyClosed) {
                        PluralKitImporter.importAltersFromPluralKit(player, this.getLine(0).getString().trim().toLowerCase());
                        super.close(alreadyClosed);
                    }
                }.open();
            }
        }));

        if (((IAltersOwner) player).plurify$isClientLoaded() && FabricLoader.getInstance().getEnvironmentType() == EnvType.SERVER) {

            // import alter from single player
            ItemStack importAltersFromSingleplayer = PlurifyIcon.MENU_IMPORT_ALTERS.getAsItemStack();
            importAltersFromSingleplayer.set(DataComponents.ITEM_NAME, Component.translatable("plurify.gui.sync_system_gui.import_system_from_sp"));

            this.setSlot(0, new SimpleGuiElement(importAltersFromSingleplayer, (i, clickType, slotActionType, slotGuiInterface) -> {
                if (clickType == ClickType.MOUSE_LEFT) {
                    ServerPlayNetworking.send(player, new RequestAltersSyncS2CPacket());
                    this.close(); // closes this GUI but the receiver reopens the MainAltersGUI upon receiving the SyncAltersC2SPacket to refresh the screen
                }
            }));

            // export alters to single player
            ItemStack syncAltersToSinglePlayer = PlurifyIcon.MENU_EXPORT_ALTERS.getAsItemStack();
            syncAltersToSinglePlayer.set(DataComponents.ITEM_NAME, Component.translatable("plurify.gui.sync_system_gui.export_system_to_sp"));

            this.setSlot(35, new SimpleGuiElement(syncAltersToSinglePlayer, (i, clickType, slotActionType, slotGuiInterface) -> {
                if (clickType == ClickType.MOUSE_LEFT) {
                    ServerPlayNetworking.send(player, new SyncAltersS2CPacket(new PlayerAltersConfig(player)));
                }
            }));
        }
    }
}
