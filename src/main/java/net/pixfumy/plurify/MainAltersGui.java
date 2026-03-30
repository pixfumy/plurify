package net.pixfumy.plurify;

import eu.pb4.sgui.api.ClickType;
import eu.pb4.sgui.api.elements.GuiElement;
import eu.pb4.sgui.api.gui.SimpleGui;
import net.minecraft.component.DataComponentTypes;
import net.minecraft.component.type.ProfileComponent;
import net.minecraft.item.ItemStack;
import net.minecraft.item.Items;
import net.minecraft.screen.ScreenHandlerType;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.text.Style;
import net.minecraft.text.Text;
import net.minecraft.util.Formatting;

import javax.xml.crypto.Data;
import java.util.List;

public class MainAltersGui extends SimpleGui {
    public MainAltersGui(ServerPlayerEntity player) {
        super(ScreenHandlerType.GENERIC_9X6, player, true);

        List<Alter> alterList = ((IAltersOwner) player).getAlters();
        int headIndex = 0;
        for (Alter alter : alterList) {
            ItemStack alterHead = Items.PLAYER_HEAD.getDefaultStack();
            alterHead.set(DataComponentTypes.ITEM_NAME, alter.getName());
            alterHead.set(DataComponentTypes.PROFILE, ProfileComponent.ofStatic(player.getGameProfile()));

            this.setSlot(headIndex, new GuiElement(alterHead, (i, clickType, slotActionType, slotGuiInterface) -> {
                if (clickType == ClickType.MOUSE_LEFT) {
                    System.out.println(alter.getName());
                }
            }));
            headIndex++;
        }

        ItemStack addAlterButtonItemStack = Items.DRAGON_EGG.getDefaultStack();
        addAlterButtonItemStack.set(DataComponentTypes.ITEM_NAME, Text.literal("Add Alter")
                .setStyle(Style.EMPTY.withColor(Formatting.LIGHT_PURPLE)));

        this.setSlot(49, new GuiElement(addAlterButtonItemStack, (i, clickType, slotActionType, slotGuiInterface) -> {
            if (clickType == ClickType.MOUSE_LEFT) {
                System.out.println("ouch");
            }
        }));
    }
}
