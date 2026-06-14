package com.extra.power.network;

import com.extra.power.client.screen.ClientShakeHandler;
import com.extra.power.init.AnvilCraftExtrapower;
import dev.anvilcraft.lib.v2.network.packet.IClientboundPacket;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record ShakePayload(float strength, int duration) implements IClientboundPacket {
    public static final Type<ShakePayload> TYPE = IPacket.type(AnvilCraftExtrapower.of("shake"));
    public static final StreamCodec<ByteBuf, ShakePayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            ShakePayload::strength,
            ByteBufCodecs.INT,
            ShakePayload::duration,
            ShakePayload::new
    );

    @Override
    public Type<ShakePayload> type() {
        return TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        ClientShakeHandler.receiveShake(strength, duration);
    }
}