package net.pixfumy.plurify.network.altersync;

import com.mojang.serialization.*;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.pixfumy.plurify.PlurifyMain;
import net.pixfumy.plurify.alter.PlayerAltersConfig;

public record SyncAltersS2CPacket(PlayerAltersConfig playerAltersConfig)  implements CustomPacketPayload {

    public static final Type<SyncAltersS2CPacket> ID = new Type<>(Identifier.parse(PlurifyMain.MOD_ID + ":s2c_sync_alters"));

    public static final StreamCodec<RegistryFriendlyByteBuf, SyncAltersS2CPacket> PACKET_CODEC = StreamCodec.composite(
            PlayerAltersConfig.PACKET_CODEC,
            SyncAltersS2CPacket::playerAltersConfig,
            SyncAltersS2CPacket::new
    );

    public static final Codec<SyncAltersS2CPacket> CODEC = RecordCodecBuilder.create(syncAltersS2CPacketInstance -> syncAltersS2CPacketInstance.group(
            PlayerAltersConfig.CODEC.fieldOf("playerAltersConfig").forGetter(SyncAltersS2CPacket::playerAltersConfig)
    ).apply(syncAltersS2CPacketInstance, SyncAltersS2CPacket::new));

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}