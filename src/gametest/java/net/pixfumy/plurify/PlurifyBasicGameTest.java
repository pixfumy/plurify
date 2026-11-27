package net.pixfumy.plurify;

import java.io.IOException;
import java.io.InputStream;
import java.lang.reflect.InvocationTargetException;
import java.lang.reflect.Method;
import java.util.Optional;
import java.util.UUID;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.authlib.GameProfile;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.entity.player.Player;

import net.fabricmc.fabric.api.gametest.v1.GameTest;
import net.fabricmc.fabric.api.gametest.v1.CustomTestMethodInvoker;
import net.pixfumy.plurify.alter.IAltersOwner;
import net.pixfumy.plurify.alter.PlayerAltersConfig;
import net.pixfumy.plurify.fileio.AltersIOHelper;
import org.mockito.MockedStatic;
import org.mockito.Mockito;

import static org.mockito.Mockito.*;

public class PlurifyBasicGameTest implements CustomTestMethodInvoker {

//    @GameTest
//    public void testBasicAlterSwitchScenario(GameTestHelper context) {
//        ServerLevel level = context.getLevel();
//        MinecraftServer server = level.getServer();
//        ServerPlayer player = context.getLevel().getServer().getPlayerList().getPlayerByName("test-mock-player");
//        PlayerList playerList = context.getLevel().getServer().getPlayerList();
//
//        AdvancementHolder acquireHardwareAdvancement = server.getAdvancements().get(Identifier.parse("story/smelt_iron"));
//
//        context.assertTrue(player != null, "Player should be non-null");
//        context.assertValueEqual(((IAltersOwner) player).plurify$getAlters().size(), 1, "Player should have 1 alter");
//
//        player.getInventory().setItem(0, Items.FLINT_AND_STEEL.getDefaultInstance());
//        player.teleportTo(server.getLevel(Level.NETHER), 1, 1, 1, Set.of(), 0, 1, false);
//        player.getAdvancements().award(acquireHardwareAdvancement, "iron");
//        player.awardStat(Stats.JUMP, 10);
//
//        Alter alter1 = ((IAltersOwner) player).plurify$getCurrentAlter();
//
//        Alter alter2 = new Alter(
//                UUID.fromString("f31e75a6-58b1-4538-8c8d-858f29de7790"),
//                Component.literal("test-mock-alter"),
//                player,
//                false
//        );
//
//        ((IAltersOwner) player).plurify$addToAlters(alter2);
//        ((IAltersOwner) player).plurify$switchToAlter(alter2);
//
//        context.assertFalse(player.level().dimension().equals(Level.NETHER), "Alter2 is no longer in the nether.");
//        context.assertValueEqual(player.getInventory().getItem(0), ItemStack.EMPTY, "Alter2 inventory slot 0 is empty");
//        context.assertFalse(player.getAdvancements().getOrStartProgress(acquireHardwareAdvancement).isDone(), "Alter2 does not have acquire hardware");
//        context.assertFalse(player.getStats().getValue(Stats.CUSTOM.get(Stats.JUMP)) == 10, "Alter 2 Jump stat is 0");
//
//        player.teleportTo(context.getLevel().getServer().getLevel(Level.END), -1, -1, -1, Set.of(), 1, 0, false);
//
//        playerList.remove(player);
//        context.assertValueEqual(playerList.getPlayerCount(), 0, "Player List should have 0 players.");
//
//        context.succeed();
//    }

    @GameTest
    public void testJoinWorld(GameTestHelper context) {
        createPlayerAndPutInWorld(context);
        context.succeed();
    }

    @GameTest
    public void testRejoinWorld(GameTestHelper context) {
        createPlayerAndPutInWorld(context);
        ServerPlayer player = context.getLevel().getServer().getPlayerList().getPlayerByName("test-mock-player");
        context.assertValueEqual(((IAltersOwner) player).plurify$getAlters().size(), 2, "Player should have 2 alters");
        context.succeed();

//        Alter alter1 = ((IAltersOwner) player).plurify$getAlters();
//        ((IAltersOwner) player).plurify$switchToAlter(alter1);
//
//        context.assertValueEqual(player.level().dimension(), Level.NETHER, "Alter1 is back in the nether.");
//        context.assertValueEqual(player.getInventory().getItem(0).getItem(), Items.FLINT_AND_STEEL, "Alter1 inventory slot is Flint&Steel");
//        context.assertTrue(player.getAdvancements().getOrStartProgress(acquireHardwareAdvancement).isDone(), "Alter1 has acquire hardware advancement");
//        context.assertValueEqual(player.getStats().getValue(Stats.CUSTOM.get(Stats.JUMP)), 10, "Alter 1 Jump stat is 10");

    }


    @Override
    public void invokeTestMethod(GameTestHelper context, Method method) throws ReflectiveOperationException {
        MockitoUtil.runWithMocks(() -> {
            try {
                method.invoke(this, context);
            } catch (Exception e) {
                context.fail(e.getMessage());
            }
        });
    }

    private ServerPlayer createPlayerAndPutInWorld(GameTestHelper context) {
        CommonListenerCookie cookie = CommonListenerCookie.createInitial(
                new GameProfile(UUID.fromString("4e9f73cf-14c6-4842-98f4-0500f547e585"), "test-mock-player"), false);

        ServerPlayer player = new ServerPlayer(context.getLevel().getServer(), context.getLevel(), cookie.gameProfile(), cookie.clientInformation());

        Connection connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        context.getLevel().getServer().getPlayerList().placeNewPlayer(connection, player, cookie);

        return player;
    }

}