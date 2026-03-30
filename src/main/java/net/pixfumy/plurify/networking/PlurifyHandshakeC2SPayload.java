package net.pixfumy.plurify.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.pixfumy.plurify.PlurifyMain;

/**
 * Notifies the server that this client has the plurify mod loaded.
 */
public record PlurifyHandshakeC2SPayload() implements CustomPayload {
    public static final Id<PlurifyHandshakeC2SPayload> ID = new Id<>(Identifier.of(PlurifyMain.MOD_ID + ":c2s_handshake"));

    public static final PacketCodec<ByteBuf, PlurifyHandshakeC2SPayload> CODEC = PacketCodec.of(
            (value, buf) -> {},
            (buf -> new PlurifyHandshakeC2SPayload())
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}