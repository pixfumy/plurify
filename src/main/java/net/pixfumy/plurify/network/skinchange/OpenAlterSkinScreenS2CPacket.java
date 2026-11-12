package net.pixfumy.plurify.network.skinchange;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.Identifier;
import net.pixfumy.plurify.PlurifyMain;

public record OpenAlterSkinScreenS2CPacket(String alterUuid, String alterName) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<OpenAlterSkinScreenS2CPacket> ID = new CustomPacketPayload.Type<>(Identifier.parse(PlurifyMain.MOD_ID + ":open_alter_skin"));

    public static final StreamCodec<RegistryFriendlyByteBuf, OpenAlterSkinScreenS2CPacket> CODEC = StreamCodec.composite(
            ByteBufCodecs.STRING_UTF8,
            OpenAlterSkinScreenS2CPacket::alterUuid,
            ByteBufCodecs.STRING_UTF8,
            OpenAlterSkinScreenS2CPacket::alterName,
            OpenAlterSkinScreenS2CPacket::new
    );

    @Override
    public CustomPacketPayload.Type<? extends CustomPacketPayload> type() {
        return ID;
    }
}