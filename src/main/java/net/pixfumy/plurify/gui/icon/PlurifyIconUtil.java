package net.pixfumy.plurify.gui.icon;

import com.mojang.authlib.GameProfile;
import com.mojang.authlib.minecraft.MinecraftProfileTextures;
import net.minecraft.core.component.DataComponents;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.component.CustomModelData;
import org.jetbrains.annotations.Nullable;

import java.util.*;

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

    public static PlurifyIcon getPlurifyIconById(String id) {
        return Arrays.stream(PlurifyIcon.values()).filter(plurifyIcon -> plurifyIcon.id.equals(id)).findFirst().get();
    }

    public static boolean arePlurifyIconsEqual(ItemStack itemStack1, ItemStack itemStack2) {
        return getAsPlurifyItem(itemStack1) == getAsPlurifyItem(itemStack2);
    }

}
