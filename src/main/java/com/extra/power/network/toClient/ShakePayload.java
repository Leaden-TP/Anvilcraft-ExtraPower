package com.extra.power.network.toClient;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;


public record ShakePayload(float strength, int duration) implements CustomPacketPayload {

    // 数据包唯一标识，客户端与服务端必须一致
    public static final CustomPacketPayload.Type<ShakePayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("extra_power", "shake"));

    // 编解码器：读写网络缓冲
    public static final StreamCodec<FriendlyByteBuf, ShakePayload> STREAM_CODEC =
            StreamCodec.composite(
                    StreamCodec.of(FriendlyByteBuf::writeFloat, FriendlyByteBuf::readFloat), // 强度
                    ShakePayload::strength,
                    StreamCodec.of(FriendlyByteBuf::writeInt, FriendlyByteBuf::readInt),   // 持续时间
                    ShakePayload::duration,
                    ShakePayload::new
            );

    @NotNull
    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}