package com.extra.power.network;

import com.extra.power.block.blockentity.MagneticDisplayStandBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record UpdateAnimationStatePacket(List<Double> actionState, BlockPos pos) implements CustomPacketPayload {

    public static final Type<UpdateAnimationStatePacket> TYPE = new Type<>(ResourceLocation.fromNamespaceAndPath("extra_power", "update_animation_state"));

    @Override
    public @NotNull Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static final StreamCodec<FriendlyByteBuf, UpdateAnimationStatePacket> STREAM_CODEC = StreamCodec.of(
        UpdateAnimationStatePacket::encode,
        UpdateAnimationStatePacket::decode
    );

    private static void encode(FriendlyByteBuf buf, UpdateAnimationStatePacket packet) {
        buf.writeBlockPos(packet.pos);
        buf.writeInt(packet.actionState.size());
        for (Double value : packet.actionState) {
            buf.writeDouble(value);
        }
    }

    private static UpdateAnimationStatePacket decode(FriendlyByteBuf buf) {
        BlockPos pos = buf.readBlockPos();
        int size = buf.readInt();
        List<Double> actionState = new ArrayList<>(size);
        for (int i = 0; i < size; i++) {
            actionState.add(buf.readDouble());
        }
        return new UpdateAnimationStatePacket(actionState, pos);
    }

    public static void handle(UpdateAnimationStatePacket packet, IPayloadContext context) {
        context.enqueueWork(() -> {
            Level level = context.player().level();
            if (level.isClientSide() && level.getBlockEntity(packet.pos) instanceof MagneticDisplayStandBlockEntity be) {
                be.updateActionState(packet.actionState);
            }
        });
    }
}
