package net.pixfumy.plurify.gui.icon;

import net.minecraft.SharedConstants;
import net.minecraft.network.chat.Component;
import net.minecraft.server.Bootstrap;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.ItemLore;
import org.junit.jupiter.api.Assertions;

import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

public class PlurifyIconUtilTest {

    @Test
    void testSplitTextComponentIntoLoreLines() {
        String text = ("A".repeat(54) + " ").repeat(3);
        Component textComponent = Component.literal(text);

        ItemLore multiLineItemLore = PlurifyIconUtil.splitTextComponentIntoLoreLines(textComponent);
        assertNotNull(multiLineItemLore);
        assertEquals(multiLineItemLore.lines().size(), 3);
    }

}
