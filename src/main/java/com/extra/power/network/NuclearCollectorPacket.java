package com.extra.power.network;

import com.extra.power.block.blockentity.NuclearCollectorBlockEntity;
import com.extra.power.init.AnvilCraftExtrapower;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import dev.anvilcraft.lib.v2.network.packet.ISensitiveBiPacket;
import dev.anvilcraft.lib.v2.util.Util;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;
import net.neoforged.neoforge.network.PacketDistributor;

public record NuclearCollectorPacket(BlockPos pos, int result, int heat, int power) implements ISensitiveBiPacket {
    public static final Type<NuclearCollectorPacket> TYPE = IPacket.type(AnvilCraftExtrapower.of("nuclear_collector_packet"));
    public static final StreamCodec<RegistryFriendlyByteBuf, NuclearCollectorPacket> STREAM_CODEC =
            StreamCodec.ofMember(NuclearCollectorPacket::encode, NuclearCollectorPacket::new);

    private NuclearCollectorPacket(RegistryFriendlyByteBuf buf) {
        this(buf.readBlockPos(), buf.readInt(), buf.readInt(), buf.readInt());
    }

    private void encode(RegistryFriendlyByteBuf buf) {
        buf.writeBlockPos(pos);
        buf.writeInt(result);
        buf.writeInt(heat);
        buf.writeInt(power);
    }

    @Override
    public Type<NuclearCollectorPacket> type() {
        return TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        if (Minecraft.getInstance().level != null &&
                Minecraft.getInstance().level.getBlockEntity(pos) instanceof NuclearCollectorBlockEntity be) {
            be.setResult(result);
            be.setClientHeat(heat);
            be.setPower(power);
        }
    }

    @Override
    public void handleOnServer(Player player) {
        if (player.level().getBlockEntity(pos) instanceof NuclearCollectorBlockEntity be) {
            // 将最新的状态同步回发起者
            PacketDistributor.sendToPlayer(Util.cast(player), fromCollector(be));
        }
    }

    public static NuclearCollectorPacket fromCollector(NuclearCollectorBlockEntity be) {
        return new NuclearCollectorPacket(be.getBlockPos(), be.getWorkResult(), be.getHeat(), be.getOutputPower());
    }
}