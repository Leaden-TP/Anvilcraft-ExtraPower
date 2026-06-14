package com.extra.power.network;

import com.extra.power.client.screen.ClientFlashHandler;
import com.extra.power.init.AnvilCraftExtrapower;
import dev.anvilcraft.lib.v2.network.packet.IClientboundPacket;
import dev.anvilcraft.lib.v2.network.packet.IPacket;
import io.netty.buffer.ByteBuf;
import net.minecraft.network.codec.ByteBufCodecs;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.world.entity.player.Player;

public record FlashPayload(float intensity, int duration) implements IClientboundPacket {
    public static final Type<FlashPayload> TYPE = IPacket.type(AnvilCraftExtrapower.of("flash"));
    public static final StreamCodec<ByteBuf, FlashPayload> STREAM_CODEC = StreamCodec.composite(
            ByteBufCodecs.FLOAT,
            FlashPayload::intensity,
            ByteBufCodecs.INT,
            FlashPayload::duration,
            FlashPayload::new
    );

    @Override
    public Type<FlashPayload> type() {
        return TYPE;
    }

    @Override
    public void handleOnClient(Player player) {
        ClientFlashHandler.receiveFlash(intensity, duration);
    }
}