package com.extra.power.init;

import com.extra.power.client.screen.ClientFlashHandler;
import com.extra.power.client.screen.ClientShakeHandler;
import com.extra.power.network.FlashPayload;
import com.extra.power.network.NuclearCollectorPacket;
import com.extra.power.network.ShakePayload;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworks {
    public static void init(PayloadRegistrar registrar) {
        registrar.playBidirectional(
                NuclearCollectorPacket.TYPE,
                NuclearCollectorPacket.STREAM_CODEC,
                NuclearCollectorPacket.HANDLER
        );
        registrar.playToClient(
                FlashPayload.TYPE,
                FlashPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        ClientFlashHandler.receiveFlash(payload.intensity(), payload.duration());
                    });
                }
        );
    }
}
