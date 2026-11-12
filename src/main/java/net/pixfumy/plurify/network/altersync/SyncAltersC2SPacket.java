package net.pixfumy.plurify.network.altersync;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.pixfumy.plurify.PlurifyMain;
import net.pixfumy.plurify.alter.PlayerAltersConfig;

public record SyncAltersC2SPacket(PlayerAltersConfig playerAltersConfig) implements CustomPacketPayload {

    public static final Type<SyncAltersC2SPacket> ID = new Type<>(Identifier.parse(PlurifyMain.MOD_ID + ":c2s_sync_alters"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAltersC2SPacket> PACKET_CODEC = StreamCodec.composite(
            PlayerAltersConfig.PACKET_CODEC,
            SyncAltersC2SPacket::playerAltersConfig,
            SyncAltersC2SPacket::new
    );

    public static final Codec<SyncAltersC2SPacket> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            PlayerAltersConfig.CODEC.fieldOf("playerAltersConfig").forGetter(SyncAltersC2SPacket::playerAltersConfig)
    ).apply(instance, SyncAltersC2SPacket::new));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}