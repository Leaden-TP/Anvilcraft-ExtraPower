package com.extra.power.network.toServer;

import com.extra.power.block.blockentity.NuclearCollectorBlockEntity;
import com.extra.power.init.AnvilCraftExtrapower;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import dev.anvilcraft.lib.v2.network.packet.IServerboundPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.Collections;
import java.util.Map;
import java.util.WeakHashMap;

public record NuclearCollectorRequestPacket(BlockPos pos) implements IServerboundPacket {
    private static final int MIN_REQUEST_INTERVAL = 5;
    private static final double MAX_REQUEST_DISTANCE_SQUARED = 64.0;
    private static final Map<ServerPlayer, Integer> LAST_REQUEST_TICKS =
            Collections.synchronizedMap(new WeakHashMap<>());

    public static final Type<NuclearCollectorRequestPacket> TYPE =
            IPacket.type(AnvilCraftExtrapower.of("nuclear_collector_request"));
    public static final StreamCodec<ByteBuf, NuclearCollectorRequestPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            NuclearCollectorRequestPacket::pos,
            NuclearCollectorRequestPacket::new
    );

    @Override
    public Type<NuclearCollectorRequestPacket> type() {
        return TYPE;
    }

    @Override
    public void handleOnServer(Player player) {
        if (!(player instanceof ServerPlayer serverPlayer)) return;
        Level level = serverPlayer.level();
        if (!level.isLoaded(pos)) return;
        if (serverPlayer.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5)
                > MAX_REQUEST_DISTANCE_SQUARED) {
            return;
        }

        if (level.getBlockEntity(pos) instanceof NuclearCollectorBlockEntity collector) {
            int playerTick = serverPlayer.tickCount;
            Integer previousTick = LAST_REQUEST_TICKS.get(serverPlayer);
            if (previousTick != null && playerTick - previousTick < MIN_REQUEST_INTERVAL) return;
            LAST_REQUEST_TICKS.put(serverPlayer, playerTick);
            PacketDistributor.sendToPlayer(serverPlayer, NuclearCollectorPacket.fromCollector(collector));
        }
    }
}
