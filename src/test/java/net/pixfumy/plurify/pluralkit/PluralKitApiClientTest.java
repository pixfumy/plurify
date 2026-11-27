package net.pixfumy.plurify.pluralkit;

import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerPlayer;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.function.Executable;
import org.mockito.Mockito;

import java.io.IOException;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

class PluralKitApiClientTest {

    private static Component fakeGameChat;

    @BeforeAll
    static void beforeAll() {
        fakeGameChat = null;
    }

    private ServerPlayer getMockPlayer() {
        ServerPlayer player = mock(ServerPlayer.class);
        when(player.getScoreboardName()).thenReturn("TestPlayer");
        when(player.getUUID()).thenReturn(any());
        return player;
    }

    @Test
    void testRequestSystemMembersFromPluralKit() {
        try {
            String json = new String(getClass().getResourceAsStream("/PluralKitResponse.json").readAllBytes());
        } catch (IOException e) {
            fail();
        }
    }

}