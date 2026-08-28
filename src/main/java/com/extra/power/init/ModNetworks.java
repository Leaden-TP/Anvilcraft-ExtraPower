package com.extra.power.init;

import com.extra.power.client.screen.ClientFlashHandler;
import com.extra.power.client.screen.ClientShakeHandler;
import com.extra.power.network.toClient.FlashPayload;
import com.extra.power.network.toClient.ShakePayload;
import com.extra.power.network.toServer.MouseScrollPacket;
import com.extra.power.network.toServer.NuclearCollectorPacket;
import com.extra.power.network.toServer.NuclearCollectorRequestPacket;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

public class ModNetworks {
    public static void init(PayloadRegistrar registrar) {
        registrar.playToClient(
                FlashPayload.TYPE,
                FlashPayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        ClientFlashHandler.receiveFlash(payload.intensity(), payload.duration());
                    });
                }
        );
        registrar.playToClient(
                ShakePayload.TYPE,
                ShakePayload.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        ClientShakeHandler.receiveShake(payload.strength(), payload.duration());
                    });
                }
        );
        registrar.playToClient(
                NuclearCollectorPacket.TYPE,
                NuclearCollectorPacket.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        payload.handleOnClient(context.player());
                    });
                }
        );


        registrar.playToServer(
                NuclearCollectorRequestPacket.TYPE,
                NuclearCollectorRequestPacket.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> {
                        payload.handleOnServer(context.player());
                    });
                }
        );
        registrar.playToServer(
                MouseScrollPacket.TYPE,
                MouseScrollPacket.STREAM_CODEC,
                (payload, context) -> {
                    context.enqueueWork(() -> payload.handleOnServer(context.player()));
                }
        );
    }
}
