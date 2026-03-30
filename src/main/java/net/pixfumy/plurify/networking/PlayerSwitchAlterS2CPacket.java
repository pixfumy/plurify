package net.pixfumy.plurify.networking;

import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.pixfumy.plurify.PlurifyMain;

public record PlayerSwitchAlterS2CPacket(String alterId, String alterName) implements CustomPayload {
    public static final Id<PlayerSwitchAlterS2CPacket> ID = new Id<>(Identifier.of(PlurifyMain.MOD_ID + ":player_current_alter"));

    public static final PacketCodec<ByteBuf, PlayerSwitchAlterS2CPacket> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            PlayerSwitchAlterS2CPacket::alterId,
            PacketCodecs.STRING,
            PlayerSwitchAlterS2CPacket::alterName,
            PlayerSwitchAlterS2CPacket::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}