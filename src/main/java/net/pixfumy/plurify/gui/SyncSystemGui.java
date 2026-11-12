package net.pixfumy.plurify.gui;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.SimpleGuiElement;
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking;
import net.minecraft.ChatFormatting;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.Style;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.pixfumy.plurify.alter.PlayerAltersConfig;
import net.pixfumy.plurify.gui.icon.PlurifyIcon;
import net.pixfumy.plurify.gui.icon.PlurifyIconUtil;
import net.pixfumy.plurify.network.altersync.RequestAltersSyncS2CPacket;
import net.pixfumy.plurify.network.altersync.SyncAltersS2CPacket;

public class SyncSystemGui extends PlurifyChestGui {

    public SyncSystemGui(ServerPlayer player) {
        super(player);
    }

    public SyncSystemGui(ServerPlayer player, MainAltersGui mainAltersGui) {
        super(player, null, mainAltersGui);

        this.setTitle(Component.translatable("plurify.gui.sync_system_gui.title"));

        ItemStack importAltersFromSingleplayer = PlurifyIcon.MENU_IMPORT_ALTERS.getAsItemStack();
        importAltersFromSingleplayer.set(DataComponents.ITEM_NAME, Component.translatable("plurify.gui.sync_system_gui.import_system_from_sp"));

        // green glass panes to separate action bar from selectable items
        ItemStack greenGlassPane = PlurifyIconUtil.getItemStackWithNoTooltip(Items.STAINED_GLASS_PANE.green());
        for (int i = 36; i < 45; i++) {
            this.setSlot(i, greenGlassPane.copy());
        }

        this.setSlot(0, new SimpleGuiElement(importAltersFromSingleplayer, (i, clickType, slotActionType, slotGuiInterface) -> {
            if (clickType == ClickType.MOUSE_LEFT) {
                ServerPlayNetworking.send(player, new RequestAltersSyncS2CPacket());
                this.close();
            }
        }));

        ItemStack syncAltersToSinglePlayer = PlurifyIcon.MENU_EXPORT_ALTERS.getAsItemStack();
        syncAltersToSinglePlayer.set(DataComponents.ITEM_NAME, Component.translatable("plurify.gui.sync_system_gui.export_system_to_sp"));

        this.setSlot(35, new SimpleGuiElement(syncAltersToSinglePlayer, (i, clickType, slotActionType, slotGuiInterface) -> {
            if (clickType == ClickType.MOUSE_LEFT) {
                ServerPlayNetworking.send(player, new SyncAltersS2CPacket(new PlayerAltersConfig(player)));
            }
        }));
    }
}
