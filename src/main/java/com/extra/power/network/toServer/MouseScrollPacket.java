package com.extra.power.network.toServer;


import com.extra.power.api.entity.IScrollAdjustable;
import com.extra.power.init.AnvilCraftExtrapower;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import dev.anvilcraft.lib.v2.network.packet.IServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;


public record MouseScrollPacket(BlockPos pos, byte steps) implements IServerboundPacket {
    private static final double MAX_ADJUST_DISTANCE_SQUARED = 64.0;
    private static final Map<Player, Integer> LAST_ADJUST_TICKS = Collections.synchronizedMap(new WeakHashMap<>());

    public static final Type<MouseScrollPacket> TYPE = IPacket.type(AnvilCraftExtrapower.of("mouse_scroll_pack"));
    public static final StreamCodec<ByteBuf, MouseScrollPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            MouseScrollPacket::pos,
            ByteBufCodecs.BYTE,
            MouseScrollPacket::steps,
            MouseScrollPacket::new
    );

    @Override
    public Type<MouseScrollPacket> type() {
        return TYPE;
    }

    @Override
    public void handleOnServer(Player player) {
        Level level = player.level();
        if (!level.isLoaded(pos) || steps == 0) return;
        if (!player.isShiftKeyDown() || !player.getMainHandItem().isEmpty()) return;
        if (player.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                > MAX_ADJUST_DISTANCE_SQUARED) return;

        BlockEntity be = level.getBlockEntity(pos);
        if (be instanceof IScrollAdjustable adjustable) {
            int playerTick = player.tickCount;
            Integer previousTick = LAST_ADJUST_TICKS.get(player);
            if (previousTick != null && previousTick == playerTick) return;
            LAST_ADJUST_TICKS.put(player, playerTick);
            adjustable.onScrollAdjust(Mth.clamp((int) steps, -24, 24));
        }
    }
}
