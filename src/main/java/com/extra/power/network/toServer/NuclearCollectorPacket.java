package com.extra.power.network.toServer;

import com.extra.power.block.blockentity.NuclearCollectorBlockEntity;
import com.extra.power.init.AnvilCraftExtrapower;
import dev.anvilcraft.lib.v2.network.packet.IClientboundPacket;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.core.BlockPos;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record NuclearCollectorPacket(BlockPos pos, byte result, int heat) implements IClientboundPacket {
    public static final Type<NuclearCollectorPacket> TYPE = IPacket.type(AnvilCraftExtrapower.of("nuclear_collector_packet"));
    public static final StreamCodec<ByteBuf, NuclearCollectorPacket> STREAM_CODEC = StreamCodec.composite(
            BlockPos.STREAM_CODEC,
            NuclearCollectorPacket::pos,
            ByteBufCodecs.BYTE,
            NuclearCollectorPacket::result,
            ByteBufCodecs.VAR_INT,
            NuclearCollectorPacket::heat,
            NuclearCollectorPacket::new
    );

    @Override
    public Type<NuclearCollectorPacket> type() {
        return TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        if (player.level().getBlockEntity(pos) instanceof NuclearCollectorBlockEntity collector) {
            collector.applyClientTelemetry(result, heat);
        }
    }

    public static NuclearCollectorPacket fromCollector(NuclearCollectorBlockEntity be) {
        return new NuclearCollectorPacket(be.getBlockPos(), (byte) be.getWorkResult(), be.getHeat());
    }
}
