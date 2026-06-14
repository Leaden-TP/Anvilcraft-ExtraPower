package com.extra.power.network;

import com.extra.power.api.entity.IScrollAdjustable;
import com.extra.power.init.AnvilCraftExtrapower;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import dev.anvilcraft.lib.v2.network.packet.IServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

public record MouseScrollPacket(BlockPos pos, String parameterId, float delta) implements IServerboundPacket {
    public static final Type<MouseScrollPacket> TYPE = IPacket.type(AnvilCraftExtrapower.of("mouse_scroll_pack"));
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
    public Type<MouseScrollPacket> type() {
        return TYPE;
    }

    @Override
    public void handleOnServer(Player player) {
        Level level = player.level();
        if (level.isLoaded(pos)) {
            BlockEntity be = level.getBlockEntity(pos);
            if (be instanceof IScrollAdjustable adjustable) {
                adjustable.onScrollAdjust(parameterId, delta, level, pos);
            }
        }
    }
}