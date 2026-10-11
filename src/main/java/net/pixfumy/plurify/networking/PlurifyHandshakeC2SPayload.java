package net.pixfumy.plurify.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.pixfumy.plurify.PlurifyMain;

/**
 * Notifies the server that this client has the plurify mod loaded.
 */
public record PlurifyHandshakeC2SPayload() implements CustomPacketPayload {
    public static final Type<PlurifyHandshakeC2SPayload> ID = new Type<>(Identifier.parse(PlurifyMain.MOD_ID + ":c2s_handshake"));

    public static final StreamCodec<ByteBuf, PlurifyHandshakeC2SPayload> CODEC = StreamCodec.ofMember(
            (value, buf) -> {},
            (buf -> new PlurifyHandshakeC2SPayload())
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}