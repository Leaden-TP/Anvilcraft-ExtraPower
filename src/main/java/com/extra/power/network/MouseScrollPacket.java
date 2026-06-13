package com.extra.power.network;


import com.extra.power.api.entity.IScrollAdjustable;
import com.extra.power.block.blockentity.MagneticDisplayStandBlockEntity;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;


public record MouseScrollPacket(BlockPos pos, String parameterId, float delta) implements CustomPacketPayload {
    public static final CustomPacketPayload.Type<MouseScrollPacket> TYPE =
            new CustomPacketPayload.Type<>(ResourceLocation.fromNamespaceAndPath("extra_power", "mouse_scroll"));

    public static final StreamCodec<ByteBuf, MouseScrollPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            MouseScrollPacket::pos,
            ByteBufCodecs.STRING_UTF8,
            MouseScrollPacket::parameterId,
            ByteBufCodecs.FLOAT,
            MouseScrollPacket::delta,
            MouseScrollPacket::new
    );

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void handle(MouseScrollPacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (context.player() instanceof ServerPlayer serverPlayer) {
                Level level = serverPlayer.level();
                if (level.isLoaded(packet.pos)) {
                    BlockEntity be = level.getBlockEntity(packet.pos);
                    if (be instanceof IScrollAdjustable adjustable) {
                        adjustable.onScrollAdjust(packet.parameterId, packet.delta, level, packet.pos);
                    }
                }
            }
        });
    }
}
