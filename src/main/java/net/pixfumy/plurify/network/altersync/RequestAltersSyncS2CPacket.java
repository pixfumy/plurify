package net.pixfumy.plurify.network.altersync;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.pixfumy.plurify.PlurifyMain;

public record RequestAltersSyncS2CPacket() implements CustomPacketPayload {
    public static final Type<RequestAltersSyncS2CPacket> ID = new Type<>(Identifier.parse(PlurifyMain.MOD_ID + ":s2c_request_sync_alters"));

    public static final StreamCodec<ByteBuf, RequestAltersSyncS2CPacket> CODEC = StreamCodec.unit(new RequestAltersSyncS2CPacket());

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}