package net.pixfumy.plurify.networking;

import com.mojang.serialization.Codec;
import net.minecraft.network.RegistryByteBuf;
import net.minecraft.network.codec.PacketCodec;
import net.minecraft.network.codec.PacketCodecs;
import net.minecraft.network.packet.CustomPayload;
import net.minecraft.text.Text;
import net.minecraft.util.Identifier;

public record OpenAlterSkinScreenPayload(String alterName) implements CustomPayload {
    public static final CustomPayload.Id<OpenAlterSkinScreenPayload> ID = new CustomPayload.Id<>(Identifier.of("open_alter_skin"));

    public static final PacketCodec<RegistryByteBuf, OpenAlterSkinScreenPayload> CODEC = PacketCodec.tuple(
            PacketCodecs.STRING,
            OpenAlterSkinScreenPayload::alterName,
            OpenAlterSkinScreenPayload::new
    );

    @Override
    public CustomPayload.Id<? extends CustomPayload> getId() {
        return ID;
    }
}