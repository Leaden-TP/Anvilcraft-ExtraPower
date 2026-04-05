package com.extra.power.network;

import com.extra.power.block.blockentity.NuclearCollectorBlockEntity;
import com.extra.power.init.AnvilCraftExtrapower;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.minecraft.server.level.ServerPlayer;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.network.handling.DirectionalPayloadHandler;
import net.neoforged.neoforge.network.handling.IPayloadContext;
import net.neoforged.neoforge.network.handling.IPayloadHandler;
import org.jetbrains.annotations.NotNull;

public class NuclearCollectorPacket implements CustomPacketPayload {
    public static final Type<NuclearCollectorPacket> TYPE =
            new Type<>(AnvilCraftExtrapower.of("nuclear_collector_irradiation_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NuclearCollectorPacket> STREAM_CODEC =
            StreamCodec.ofMember(NuclearCollectorPacket::encode, NuclearCollectorPacket::new);
    public static final IPayloadHandler<NuclearCollectorPacket> HANDLER = new DirectionalPayloadHandler<>(
            NuclearCollectorPacket::clientHandler, NuclearCollectorPacket::serverHandler);

    private final int result;
    private final BlockPos blockPos;
    private final int heat;
    private final int power;

    public NuclearCollectorPacket(int result, BlockPos blockPos, int heat, int power) {
        this.result = result;
        this.blockPos = blockPos;
        this.heat = heat;
        this.power = power;
    }

    public NuclearCollectorPacket(RegistryFriendlyByteBuf buf) {
        this.result = buf.readInt();
        this.blockPos = buf.readBlockPos();
        this.heat = buf.readInt();
        this.power = buf.readInt();
    }

    public void encode(@NotNull RegistryFriendlyByteBuf buf) {
        buf.writeInt(result);
        buf.writeBlockPos(blockPos);
        buf.writeInt(heat);
        buf.writeInt(power);
    }

    @Override
    public Type<? extends CustomPacketPayload> type() {
        return TYPE;
    }

    public static void clientHandler(NuclearCollectorPacket data, IPayloadContext context) {
        context.enqueueWork(() -> {
            if (Minecraft.getInstance().level != null && Minecraft.getInstance().level.getBlockEntity(data.blockPos)
                    instanceof NuclearCollectorBlockEntity  nuclearCollector) {
                // 更新客户端数据
                nuclearCollector.setResult(data.result);
                nuclearCollector.setClientHeat(data.heat);
                nuclearCollector.setPower(data.power);
            }
        });
    }
    public static NuclearCollectorPacket fromCollector(NuclearCollectorBlockEntity collector) {
        return new NuclearCollectorPacket(
                collector.getWorkResult(),
                collector.getBlockPos(),
                collector.getHeat(),
                collector.getOutputPower()
        );
    }

    public static void serverHandler(NuclearCollectorPacket data, IPayloadContext context) {
        ServerPlayer player = (ServerPlayer) context.player();
        context.enqueueWork(() -> {
            if (player.level().getBlockEntity(data.blockPos) instanceof NuclearCollectorBlockEntity nuclearCollectorBlockEntity) {
                var pack = new NuclearCollectorPacket(nuclearCollectorBlockEntity.getWorkResult(),data.blockPos,
                        nuclearCollectorBlockEntity.getHeat(), nuclearCollectorBlockEntity.getOutputPower());
                PacketDistributor.sendToPlayer(player, pack);
            }
        });
    }
}
