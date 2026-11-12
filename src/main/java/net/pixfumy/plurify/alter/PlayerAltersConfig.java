package net.pixfumy.plurify.alter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.pixfumy.plurify.gui.icon.PlurifyIconUtil;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public record PlayerAltersConfig(UUID playerUuid, String inGameName, UUID currentAlterUuid, List<AlterBasicDetail> alters) {

    public PlayerAltersConfig(String playerUuidAsString, String inGameName, String currentAlterUuidAsString, List<AlterBasicDetail> alters) {
        this(UUID.fromString(playerUuidAsString), inGameName, UUID.fromString(currentAlterUuidAsString), alters);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, PlayerAltersConfig> PACKET_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            PlayerAltersConfig::playerUuid,
            ByteBufCodecs.STRING_UTF8,
            PlayerAltersConfig::inGameName,
            UUIDUtil.STREAM_CODEC,
            PlayerAltersConfig::currentAlterUuid,
            ByteBufCodecs.collection(
                    i -> new ArrayList<>(),
                    AlterBasicDetail.PACKET_CODEC
            ),
            PlayerAltersConfig::alters,
            PlayerAltersConfig::new
    );

    public static final Codec<PlayerAltersConfig> CODEC = RecordCodecBuilder.create(instance -> instance.group(
                    Codec.STRING.fieldOf("uuid").forGetter(playerAltersConfig -> playerAltersConfig.playerUuid.toString()),
                    Codec.STRING.fieldOf("inGameName").forGetter(PlayerAltersConfig::inGameName),
                    Codec.STRING.fieldOf("currentAlterUuid").forGetter(packet -> packet.currentAlterUuid.toString()),
                    Codec.list(AlterBasicDetail.CODEC).fieldOf("alters").forGetter(packet -> packet.alters))
            .apply(instance, PlayerAltersConfig::new)
    );

    public PlayerAltersConfig(ServerPlayer player) {
        this(
                player.getUUID(),
                player.getScoreboardName(),
                ((IAltersOwner) player).plurify$getCurrentAlter().getUuid(),
                ((IAltersOwner) player).plurify$getAlters().values().stream().map(
                        alter -> new AlterBasicDetail(
                                alter.getUuid(),
                                alter.getName().getString(),
                                PlurifyIconUtil.getAsPlurifyItem(alter.getIcon()
                                ))
                ).toList()
        );
    }
}
