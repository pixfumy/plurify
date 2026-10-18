package net.pixfumy.plurify.gui.icon;

import it.unimi.dsi.fastutil.objects.ReferenceSortedSets;
import net.minecraft.core.component.DataComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.component.ItemLore;
import net.minecraft.world.item.component.TooltipDisplay;

import javax.xml.crypto.Data;
import java.util.List;

public enum PlurifyIcon {
    MENU_ADD_ALTER(Items.CARROT_ON_A_STICK, "plurify:menu/add_alter"),
    MENU_ARROW_LEFT(Items.CARROT_ON_A_STICK, "plurify:menu/arrow_left"),
    MENU_ARROW_RIGHT(Items.CARROT_ON_A_STICK, "plurify:menu/arrow_right"),
    MENU_BACK(Items.CARROT_ON_A_STICK, "plurify:menu/back"),
    MENU_CHANGE_SKIN(Items.CARROT_ON_A_STICK, "plurify:menu/change_skin"),
    MENU_CHOOSE_ICON(Items.CARROT_ON_A_STICK, "plurify:menu/choose_icon"),
    MENU_DELETE(Items.CARROT_ON_A_STICK, "plurify:menu/delete"),
    MENU_ENDER_CHEST(Items.CARROT_ON_A_STICK, "plurify:menu/ender_chest"),
    MENU_KEEP_INV_OFF(Items.CARROT_ON_A_STICK, "plurify:menu/keep_inv_off"),
    MENU_KEEP_INV_ON(Items.CARROT_ON_A_STICK, "plurify:menu/keep_inv_on"),
    MENU_LOCATION(Items.CARROT_ON_A_STICK, "plurify:menu/location"),
    MENU_PAGE(Items.GUNPOWDER, "plurify:menu/page"),
    MENU_RENAME(Items.CARROT_ON_A_STICK, "plurify:menu/rename"),
    MENU_SWITCH(Items.CARROT_ON_A_STICK, "plurify:menu/switch"),
    AVATAR_BLACK_CAT(Items.CARROT_ON_A_STICK, "plurify:avatar/cat/black"),
    AVATAR_BRITISH_SHORTHAIR_CAT(Items.CARROT_ON_A_STICK, "plurify:avatar/cat/british_shorthair"),
    AVATAR_CALICO_CAT(Items.CARROT_ON_A_STICK, "plurify:avatar/cat/calico"),
    AVATAR_JELLIE_CAT(Items.CARROT_ON_A_STICK, "plurify:avatar/cat/jellie"),
    AVATAR_PERSIAN_CAT(Items.CARROT_ON_A_STICK, "plurify:avatar/cat/persian"),
    AVATAR_RAGDOLL_CAT(Items.CARROT_ON_A_STICK, "plurify:avatar/cat/ragdoll"),
    AVATAR_RED_CAT(Items.CARROT_ON_A_STICK, "plurify:avatar/cat/red"),
    AVATAR_SIAMESE_CAT(Items.CARROT_ON_A_STICK, "plurify:avatar/cat/siamese"),
    AVATAR_TABBY_CAT(Items.CARROT_ON_A_STICK, "plurify:avatar/cat/tabby"),
    AVATAR_TUXEDO_CAT(Items.CARROT_ON_A_STICK, "plurify:avatar/cat/tuxedo"),
    AVATAR_WHITE_CAT(Items.CARROT_ON_A_STICK, "plurify:avatar/cat/white"),
    AVATAR_ASHEN_DOG(Items.CARROT_ON_A_STICK, "plurify:avatar/dog/ashen_dog"),
    AVATAR_BLACK_DOG(Items.CARROT_ON_A_STICK, "plurify:avatar/dog/black_dog"),
    AVATAR_CHESTNUT_DOG(Items.CARROT_ON_A_STICK, "plurify:avatar/dog/chestnut_dog"),
    AVATAR_PALE_DOG(Items.CARROT_ON_A_STICK, "plurify:avatar/dog/pale_dog"),
    AVATAR_RUSTY_DOG(Items.CARROT_ON_A_STICK, "plurify:avatar/dog/rusty_dog"),
    AVATAR_SNOWY_DOG(Items.CARROT_ON_A_STICK, "plurify:avatar/dog/snowy_dog"),
    AVATAR_SPOTTED_DOG(Items.CARROT_ON_A_STICK, "plurify:avatar/dog/spotted_dog"),
    AVATAR_STRIPED_DOG(Items.CARROT_ON_A_STICK, "plurify:avatar/dog/striped_dog"),
    AVATAR_WOODS_DOG(Items.CARROT_ON_A_STICK, "plurify:avatar/dog/woods_dog"),
    AVATAR_COPPER_AXE(Items.CARROT_ON_A_STICK, "plurify:avatar/axe/copper"),
    AVATAR_DIAMOND_AXE(Items.CARROT_ON_A_STICK, "plurify:avatar/axe/diamond"),
    AVATAR_GOLDEN_AXE(Items.CARROT_ON_A_STICK, "plurify:avatar/axe/golden"),
    AVATAR_IRON_AXE(Items.CARROT_ON_A_STICK, "plurify:avatar/axe/iron"),
    AVATAR_NETHERITE_AXE(Items.CARROT_ON_A_STICK, "plurify:avatar/axe/netherite"),
    AVATAR_STONE_AXE(Items.CARROT_ON_A_STICK, "plurify:avatar/axe/stone"),
    AVATAR_WOODEN_AXE(Items.CARROT_ON_A_STICK, "plurify:avatar/axe/wooden"),
    AVATAR_COPPER_PICKAXE(Items.CARROT_ON_A_STICK, "plurify:avatar/pickaxe/copper"),
    AVATAR_DIAMOND_PICKAXE(Items.CARROT_ON_A_STICK, "plurify:avatar/pickaxe/diamond"),
    AVATAR_GOLDEN_PICKAXE(Items.CARROT_ON_A_STICK, "plurify:avatar/pickaxe/golden"),
    AVATAR_IRON_PICKAXE(Items.CARROT_ON_A_STICK, "plurify:avatar/pickaxe/iron"),
    AVATAR_NETHERITE_PICKAXE(Items.CARROT_ON_A_STICK, "plurify:avatar/pickaxe/netherite"),
    AVATAR_STONE_PICKAXE(Items.CARROT_ON_A_STICK, "plurify:avatar/pickaxe/stone"),
    AVATAR_WOODEN_PICKAXE(Items.CARROT_ON_A_STICK, "plurify:avatar/pickaxe/wooden"),
    AVATAR_COPPER_SWORD(Items.CARROT_ON_A_STICK, "plurify:avatar/sword/copper"),
    AVATAR_DIAMOND_SWORD(Items.CARROT_ON_A_STICK, "plurify:avatar/sword/diamond"),
    AVATAR_GOLDEN_SWORD(Items.CARROT_ON_A_STICK, "plurify:avatar/sword/golden"),
    AVATAR_IRON_SWORD(Items.CARROT_ON_A_STICK, "plurify:avatar/sword/iron"),
    AVATAR_NETHERITE_SWORD(Items.CARROT_ON_A_STICK, "plurify:avatar/sword/netherite"),
    AVATAR_STONE_SWORD(Items.CARROT_ON_A_STICK, "plurify:avatar/sword/stone"),
    AVATAR_WOODEN_SWORD(Items.CARROT_ON_A_STICK, "plurify:avatar/sword/wooden"),
    AVATAR_COPPER_SHOVEL(Items.CARROT_ON_A_STICK, "plurify:avatar/shovel/copper"),
    AVATAR_DIAMOND_SHOVEL(Items.CARROT_ON_A_STICK, "plurify:avatar/shovel/diamond"),
    AVATAR_GOLDEN_SHOVEL(Items.CARROT_ON_A_STICK, "plurify:avatar/shovel/golden"),
    AVATAR_IRON_SHOVEL(Items.CARROT_ON_A_STICK, "plurify:avatar/shovel/iron"),
    AVATAR_NETHERITE_SHOVEL(Items.CARROT_ON_A_STICK, "plurify:avatar/shovel/netherite"),
    AVATAR_STONE_SHOVEL(Items.CARROT_ON_A_STICK, "plurify:avatar/shovel/stone"),
    AVATAR_WOODEN_SHOVEL(Items.CARROT_ON_A_STICK, "plurify:avatar/shovel/wooden"),
    AVATAR_BLACK_CANDY(Items.CARROT_ON_A_STICK, "plurify:avatar/candy/black"),
    AVATAR_BLUE_CANDY(Items.CARROT_ON_A_STICK, "plurify:avatar/candy/blue"),
    AVATAR_BROWN_CANDY(Items.CARROT_ON_A_STICK, "plurify:avatar/candy/brown"),
    AVATAR_CYAN_CANDY(Items.CARROT_ON_A_STICK, "plurify:avatar/candy/cyan"),
    AVATAR_GRAY_CANDY(Items.CARROT_ON_A_STICK, "plurify:avatar/candy/gray"),
    AVATAR_GREEN_CANDY(Items.CARROT_ON_A_STICK, "plurify:avatar/candy/green"),
    AVATAR_LIGHT_BLUE_CANDY(Items.CARROT_ON_A_STICK, "plurify:avatar/candy/light_blue"),
    AVATAR_LIGHT_GRAY_CANDY(Items.CARROT_ON_A_STICK, "plurify:avatar/candy/light_gray"),
    AVATAR_LIME_CANDY(Items.CARROT_ON_A_STICK, "plurify:avatar/candy/lime"),
    AVATAR_MAGENTA_CANDY(Items.CARROT_ON_A_STICK, "plurify:avatar/candy/magenta"),
    AVATAR_ORANGE_CANDY(Items.CARROT_ON_A_STICK, "plurify:avatar/candy/orange"),
    AVATAR_PINK_CANDY(Items.CARROT_ON_A_STICK, "plurify:avatar/candy/pink"),
    AVATAR_PURPLE_CANDY(Items.CARROT_ON_A_STICK, "plurify:avatar/candy/purple"),
    AVATAR_RED_CANDY(Items.CARROT_ON_A_STICK, "plurify:avatar/candy/red"),
    AVATAR_WHITE_CANDY(Items.CARROT_ON_A_STICK, "plurify:avatar/candy/white"),
    AVATAR_YELLOW_CANDY(Items.CARROT_ON_A_STICK, "plurify:avatar/candy/yellow"),
    AVATAR_BLACK_HEART(Items.CARROT_ON_A_STICK, "plurify:avatar/heart/black"),
    AVATAR_BLUE_HEART(Items.CARROT_ON_A_STICK, "plurify:avatar/heart/blue"),
    AVATAR_BROWN_HEART(Items.CARROT_ON_A_STICK, "plurify:avatar/heart/brown"),
    AVATAR_CYAN_HEART(Items.CARROT_ON_A_STICK, "plurify:avatar/heart/cyan"),
    AVATAR_GRAY_HEART(Items.CARROT_ON_A_STICK, "plurify:avatar/heart/gray"),
    AVATAR_GREEN_HEART(Items.CARROT_ON_A_STICK, "plurify:avatar/heart/green"),
    AVATAR_LIGHT_BLUE_HEART(Items.CARROT_ON_A_STICK, "plurify:avatar/heart/light_blue"),
    AVATAR_LIGHT_GRAY_HEART(Items.CARROT_ON_A_STICK, "plurify:avatar/heart/light_gray"),
    AVATAR_LIME_HEART(Items.CARROT_ON_A_STICK, "plurify:avatar/heart/lime"),
    AVATAR_MAGENTA_HEART(Items.CARROT_ON_A_STICK, "plurify:avatar/heart/magenta"),
    AVATAR_ORANGE_HEART(Items.CARROT_ON_A_STICK, "plurify:avatar/heart/orange"),
    AVATAR_PINK_HEART(Items.CARROT_ON_A_STICK, "plurify:avatar/heart/pink"),
    AVATAR_PURPLE_HEART(Items.CARROT_ON_A_STICK, "plurify:avatar/heart/purple"),
    AVATAR_RED_HEART(Items.CARROT_ON_A_STICK, "plurify:avatar/heart/red"),
    AVATAR_WHITE_HEART(Items.CARROT_ON_A_STICK, "plurify:avatar/heart/white"),
    AVATAR_YELLOW_HEART(Items.CARROT_ON_A_STICK, "plurify:avatar/heart/yellow"),
    AVATAR_BLACK_STAR(Items.CARROT_ON_A_STICK, "plurify:avatar/star/black"),
    AVATAR_BLUE_STAR(Items.CARROT_ON_A_STICK, "plurify:avatar/star/blue"),
    AVATAR_BROWN_STAR(Items.CARROT_ON_A_STICK, "plurify:avatar/star/brown"),
    AVATAR_CYAN_STAR(Items.CARROT_ON_A_STICK, "plurify:avatar/star/cyan"),
    AVATAR_GRAY_STAR(Items.CARROT_ON_A_STICK, "plurify:avatar/star/gray"),
    AVATAR_GREEN_STAR(Items.CARROT_ON_A_STICK, "plurify:avatar/star/green"),
    AVATAR_LIGHT_BLUE_STAR(Items.CARROT_ON_A_STICK, "plurify:avatar/star/light_blue"),
    AVATAR_LIGHT_GRAY_STAR(Items.CARROT_ON_A_STICK, "plurify:avatar/star/light_gray"),
    AVATAR_LIME_STAR(Items.CARROT_ON_A_STICK, "plurify:avatar/star/lime"),
    AVATAR_MAGENTA_STAR(Items.CARROT_ON_A_STICK, "plurify:avatar/star/magenta"),
    AVATAR_ORANGE_STAR(Items.CARROT_ON_A_STICK, "plurify:avatar/star/orange"),
    AVATAR_PINK_STAR(Items.CARROT_ON_A_STICK, "plurify:avatar/star/pink"),
    AVATAR_PURPLE_STAR(Items.CARROT_ON_A_STICK, "plurify:avatar/star/purple"),
    AVATAR_RED_STAR(Items.CARROT_ON_A_STICK, "plurify:avatar/star/red"),
    AVATAR_WHITE_STAR(Items.CARROT_ON_A_STICK, "plurify:avatar/star/white"),
    AVATAR_YELLOW_STAR(Items.CARROT_ON_A_STICK, "plurify:avatar/star/yellow");

    public final Item item;
    public final String id;

    PlurifyIcon(Item item, String id) {
        this.item = item;
        this.id = id;
    }

    public ItemStack getAsItemStack() {
        ItemStack itemStack = this.item.getDefaultInstance();
        itemStack.set(DataComponents.ITEM_NAME, Component.empty());
        itemStack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(List.of(), List.of(), List.of(this.id), List.of()));
        return itemStack;
    }
}
