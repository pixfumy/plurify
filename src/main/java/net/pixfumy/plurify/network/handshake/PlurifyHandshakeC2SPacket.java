package net.pixfumy.plurify.network.handshake;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.pixfumy.plurify.PlurifyMain;

/**
 * Notifies the server that this client has the plurify mod loaded.
 */
public record PlurifyHandshakeC2SPacket() implements CustomPacketPayload {
    public static final Type<PlurifyHandshakeC2SPacket> ID = new Type<>(Identifier.parse(PlurifyMain.MOD_ID + ":c2s_handshake"));

    public static final StreamCodec<ByteBuf, PlurifyHandshakeC2SPacket> CODEC = StreamCodec.unit(new PlurifyHandshakeC2SPacket());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}