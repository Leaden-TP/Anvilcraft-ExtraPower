package com.extra.power.init;

import com.extra.power.client.screen.ClientFlashHandler;
import com.extra.power.client.screen.ClientShakeHandler;
import com.extra.power.network.*;
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
                    UpdateAnimationStatePacket.TYPE,
                    UpdateAnimationStatePacket.STREAM_CODEC,
                    UpdateAnimationStatePacket::handle
            );
        registrar.playToServer(
                MouseScrollPacket.TYPE,
                MouseScrollPacket.STREAM_CODEC,
                MouseScrollPacket::handle
        );
        }
    }
