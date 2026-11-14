package net.pixfumy.plurify.gui.icon;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.TooltipDisplay;
import org.jetbrains.annotations.Nullable;

import java.util.*;
import java.util.concurrent.ThreadLocalRandom;
import java.util.stream.Collectors;

/**
 * Util class for PlurifyItems, mainly in interacting with ItemStacks
 */
public class PlurifyIconUtil {
    public static boolean isPlurifyItem(ItemStack itemStack, PlurifyIcon plurifyIcon) {
        if (itemStack.getItem() == plurifyIcon.item) {
            CustomModelData customModelDataComponent = itemStack.get(DataComponents.CUSTOM_MODEL_DATA);
            if (customModelDataComponent == null) {
                return false;
            }
            if (customModelDataComponent.getString(0) == null) {
                return false;
            }
            return customModelDataComponent.getString(0).equals(plurifyIcon.id);
        }
        return false;
    }

    public static boolean isAnyPlurifyItem(ItemStack itemStack) {
        return Arrays.stream(PlurifyIcon.values()).anyMatch(plurifyIcon -> isPlurifyItem(itemStack, plurifyIcon));
    }

    public static PlurifyIcon getAsPlurifyItem(ItemStack itemStack) {
        Optional<PlurifyIcon> optionalPlurifyIcon = Arrays.stream(PlurifyIcon.values()).filter(plurifyItem -> isPlurifyItem(itemStack, plurifyItem))
                .findFirst();

        return optionalPlurifyIcon.orElse(null);
    }

    public static ItemStack getItemStackWithNoTooltip(Item item) {
        ItemStack itemStack = item.getDefaultInstance();
        itemStack.set(DataComponents.TOOLTIP_DISPLAY, new TooltipDisplay(true, ReferenceSortedSets.emptySet()));
        return itemStack;
    }

    public static PlurifyIcon getPlurifyIconById(String id) {
        return Arrays.stream(PlurifyIcon.values()).filter(plurifyIcon -> plurifyIcon.id.equals(id)).findFirst().get();
    }

    public static PlurifyIcon getRandomAlterIcon() {
        List<PlurifyIcon> alterIcons = Arrays.stream(PlurifyIcon.values())
                .filter(plurifyIcon -> !plurifyIcon.id.startsWith("plurify:menu/"))
                .toList();

        return alterIcons.get(ThreadLocalRandom.current().nextInt(alterIcons.size()));
    }

    public static boolean arePlurifyIconsEqual(ItemStack itemStack1, ItemStack itemStack2) {
        return getAsPlurifyItem(itemStack1) == getAsPlurifyItem(itemStack2);
    }

}
