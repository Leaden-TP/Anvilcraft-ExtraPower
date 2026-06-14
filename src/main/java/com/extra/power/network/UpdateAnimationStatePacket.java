package com.extra.power.network;

import com.extra.power.block.blockentity.MagneticDisplayStandBlockEntity;
import com.extra.power.init.AnvilCraftExtrapower;
import dev.anvilcraft.lib.v2.network.packet.IClientboundPacket;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import net.minecraft.core.BlockPos;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import org.jetbrains.annotations.NotNull;

import java.util.ArrayList;
import java.util.List;

public record UpdateAnimationStatePacket(List<Double> actionState, BlockPos pos) implements IClientboundPacket {

    public static final Type<UpdateAnimationStatePacket> TYPE = IPacket.type(AnvilCraftExtrapower.of("update_animation_state"));

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

    @Override
    public @NotNull Type<? extends IClientboundPacket> type() {
        return TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        if (player.level().isClientSide()) {
            var level = player.level();
            if (level.getBlockEntity(pos) instanceof MagneticDisplayStandBlockEntity be) {
                be.updateActionState(actionState);
            }
        }
    }
}