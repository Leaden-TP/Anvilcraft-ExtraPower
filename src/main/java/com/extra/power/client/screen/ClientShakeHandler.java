package com.extra.power.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;

import java.util.Random;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientShakeHandler {
    private static float shakeIntensity = 0.0f;
    private static int shakeDuration = 0;
    private static final Random RANDOM = new Random();

    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (shakeDuration <= 0 || shakeIntensity <= 0) {
            return;
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        float time = (float) (player.tickCount + event.getPartialTick());
        float currentIntensity = shakeIntensity * (shakeDuration / 40.0f);

        float yawOffset = (float) (Math.sin(time * 20) * currentIntensity * 0.5);
        float pitchOffset = (float) (Math.sin(time * 25 + 1) * currentIntensity * 0.3);
        float rollOffset = (float) (Math.cos(time * 15) * currentIntensity * 0.2);

        event.setYaw(event.getYaw() + yawOffset);
        event.setPitch(event.getPitch() + pitchOffset);
        event.setRoll(event.getRoll() + rollOffset);

        shakeDuration--;
    }

    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        shakeIntensity = 0.0f;
        shakeDuration = 0;
    }

    public static void receiveShake(float intensity, int duration) {
        shakeIntensity = intensity;
        shakeDuration = duration;
    }
}