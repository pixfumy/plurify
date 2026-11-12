package net.pixfumy.plurify.network.skinchange;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.pixfumy.plurify.PlurifyMain;

public record CloseAlterSkinScreenC2SPacket(String alterUuid) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<CloseAlterSkinScreenC2SPacket> ID = new CustomPacketPayload.Type<>(Identifier.parse(PlurifyMain.MOD_ID + ":close_alter_skin"));

    public static final StreamCodec<RegistryFriendlyByteBuf, CloseAlterSkinScreenC2SPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            CloseAlterSkinScreenC2SPacket::alterUuid,
            CloseAlterSkinScreenC2SPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}