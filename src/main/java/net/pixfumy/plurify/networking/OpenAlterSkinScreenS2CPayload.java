package net.pixfumy.plurify.networking;

import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.util.Identifier;
import net.pixfumy.plurify.PlurifyMain;

public record OpenAlterSkinScreenS2CPayload(String alterUuid, String alterName) implements CustomPayload {
    public static final CustomPayload.Id<OpenAlterSkinScreenS2CPayload> ID = new CustomPayload.Id<>(Identifier.of(PlurifyMain.MOD_ID + ":open_alter_skin"));

    public static final PacketCodec<RegistryByteBuf, OpenAlterSkinScreenS2CPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            OpenAlterSkinScreenS2CPayload::alterUuid,
            PacketCodecs.STRING,
            OpenAlterSkinScreenS2CPayload::alterName,
            OpenAlterSkinScreenS2CPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}