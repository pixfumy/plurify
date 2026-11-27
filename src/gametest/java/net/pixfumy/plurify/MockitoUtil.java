package net.pixfumy.plurify;

import com.google.gson.JsonElement;
import com.google.gson.JsonParser;
import com.mojang.datafixers.util.Pair;
import com.mojang.serialization.JsonOps;
import net.minecraft.world.entity.player.Player;
import net.pixfumy.plurify.alter.PlayerAltersConfig;
import net.pixfumy.plurify.fileio.AltersIOHelper;
import org.mockito.MockedStatic;

import java.io.IOException;
import java.io.InputStream;
import java.util.Optional;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.CALLS_REAL_METHODS;
import static org.mockito.Mockito.mockStatic;

public class MockitoUtil {
    public static void runWithMocks(Runnable runnable) {
        try (MockedStatic<AltersIOHelper> mockAltersIOHelper = mockStatic(AltersIOHelper.class, CALLS_REAL_METHODS)) {
            InputStream alterDataInputStream = MockitoUtil.class.getResourceAsStream("/testRejoinWorld/testRejoinWorld-alter_data.json");
            JsonElement altersConfigJson = JsonParser.parseString(
                    new String(Optional.ofNullable(alterDataInputStream).map(inputStream -> {
                        try {
                            return inputStream.readAllBytes();
                        } catch (IOException e) {
                            return new byte[]{};
                        }
                    }).orElse(new byte[]{}))
            );

            mockAltersIOHelper.when(() -> AltersIOHelper.loadPlayerAltersConfigFromFile((Player) any())).thenReturn(
                    PlayerAltersConfig.CODEC.decode(JsonOps.INSTANCE, altersConfigJson).mapOrElse(
                            Pair::getFirst,
                            pairError -> null
                    )
            );

            // configures the specified method to do nothing
            // mockAltersIOHelper.when(() -> AltersIOHelper.loadPlayerAltersFromFile(any())).thenAnswer(invocation -> null);

            //TODO : USE argThat to match alter uuids in getAlterAdvancementsFile etc.
            runnable.run();
        }
    }
}
