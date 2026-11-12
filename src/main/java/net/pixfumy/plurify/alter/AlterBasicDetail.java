package net.pixfumy.plurify.alter;

import com.mojang.serialization.Codec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import net.minecraft.core.UUIDUtil;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.pixfumy.plurify.gui.icon.PlurifyIcon;

import java.util.UUID;

public record AlterBasicDetail(UUID uuid, String name, PlurifyIcon icon) {

    private AlterBasicDetail(String uuidAsString, String name, PlurifyIcon icon) {
        this(UUID.fromString(uuidAsString), name, icon);
    }

    public static final StreamCodec<RegistryFriendlyByteBuf, AlterBasicDetail> PACKET_CODEC = StreamCodec.composite(
            UUIDUtil.STREAM_CODEC,
            AlterBasicDetail::uuid,
            ByteBufCodecs.STRING_UTF8,
            AlterBasicDetail::name,
            PlurifyIcon.PACKET_CODEC,
            AlterBasicDetail::icon,
            AlterBasicDetail::new
    );

    public static final Codec<AlterBasicDetail> CODEC = RecordCodecBuilder.create(instance -> instance.group(
            Codec.STRING.fieldOf("uuid").forGetter(alterBasicDetail -> alterBasicDetail.uuid.toString()),
            Codec.STRING.fieldOf("name").forGetter(AlterBasicDetail::name),
            PlurifyIcon.CODEC.fieldOf("icon").forGetter(AlterBasicDetail::icon))
            .apply(instance, AlterBasicDetail::new)
    );

}
