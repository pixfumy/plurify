package net.pixfumy.plurify.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.pixfumy.plurify.PlurifyMain;

public record PlayerSwitchAlterS2CPacket(String alterId, String alterName) implements CustomPacketPayload {
    public static final Type<PlayerSwitchAlterS2CPacket> ID = new Type<>(Identifier.parse(PlurifyMain.MOD_ID + ":player_current_alter"));

    public static final StreamCodec<ByteBuf, PlayerSwitchAlterS2CPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            PlayerSwitchAlterS2CPacket::alterId,
            ByteBufCodecs.STRING_UTF8,
            PlayerSwitchAlterS2CPacket::alterName,
            PlayerSwitchAlterS2CPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}