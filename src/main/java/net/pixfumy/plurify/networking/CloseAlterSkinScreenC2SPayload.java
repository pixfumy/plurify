package net.pixfumy.plurify.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.pixfumy.plurify.PlurifyMain;

public record CloseAlterSkinScreenC2SPayload(String alterUuid) implements CustomPayload {
    public static final CustomPayload.Id<CloseAlterSkinScreenC2SPayload> ID = new CustomPayload.Id<>(Identifier.of(PlurifyMain.MOD_ID + ":close_alter_skin"));

    public static final PacketCodec<RegistryByteBuf, CloseAlterSkinScreenC2SPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            CloseAlterSkinScreenC2SPayload::alterUuid,
            CloseAlterSkinScreenC2SPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}