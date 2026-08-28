
package com.extra.power.network.toClient;

import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import org.jetbrains.annotations.NotNull;

public record FlashPayload(float intensity, int duration) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<FlashPayload> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("extra_power", "flash"));
    public static final StreamCodec<FriendlyByteBuf, FlashPayload> STREAM_CODEC =
            StreamCodec.composite(
                    StreamCodec.of(FriendlyByteBuf::writeFloat, FriendlyByteBuf::readFloat),
                    FlashPayload::intensity,
                    StreamCodec.of(FriendlyByteBuf::writeInt, FriendlyByteBuf::readInt),
                    FlashPayload::duration,
                    FlashPayload::new
            );

    @Override
    @NotNull
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }
}
