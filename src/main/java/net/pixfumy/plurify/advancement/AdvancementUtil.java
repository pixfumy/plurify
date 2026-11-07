package net.pixfumy.plurify.advancement;

import com.google.gson.JsonElement;
import com.mojang.serialization.DataResult;
import com.mojang.serialization.JsonOps;
import net.minecraft.advancements.Advancement;
import net.minecraft.server.PlayerAdvancements;
import org.jetbrains.annotations.NotNull;

public class AdvancementUtil {
    public static JsonElement encodeAdvancementsToJson(@NotNull  PlayerAdvancements.Data playerAdvancementsData) {
        return PlayerAdvancements.Data.CODEC.encodeStart(JsonOps.INSTANCE, playerAdvancementsData).getOrThrow();
    }

    public static PlayerAdvancements.Data decodeAdvancementsFromJson(@NotNull JsonElement playerAdvancementsJson) {
        return PlayerAdvancements.Data.CODEC.decode(JsonOps.INSTANCE, playerAdvancementsJson).getOrThrow().getFirst();
    }

    public static PlayerAdvancements.Data deepCopyAdvancementsData(PlayerAdvancements.Data playerAdvancementsData) {
        return decodeAdvancementsFromJson(encodeAdvancementsToJson(playerAdvancementsData));
    }
}
