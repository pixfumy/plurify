package net.pixfumy.plurify.network.handshake;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.pixfumy.plurify.PlurifyMain;

/**
 *  Prompts the client for the c2s handshake packet that lets this server know the client has the mod loaded.
 */
public record PlurifyHandshakeS2CPacket() implements CustomPacketPayload {
    public static final Type<PlurifyHandshakeS2CPacket> ID = new Type<>(Identifier.parse(PlurifyMain.MOD_ID + ":s2c_handshake"));

    public static final StreamCodec<ByteBuf, PlurifyHandshakeS2CPacket> CODEC = StreamCodec.unit(new PlurifyHandshakeS2CPacket());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}