package com.extra.power.client.screen;

import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.RenderPipelines;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientFlashHandler {
    private static float flashIntensity = 0.0f;
    private static int flashDuration = 0;
    private static int totalFlashDuration = 0;

    public static void receiveFlash(float intensity, int duration) {
        if (intensity > flashIntensity) flashIntensity = intensity;
        if (duration > flashDuration) {
            flashDuration = duration;
            totalFlashDuration = duration;
        }
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Pre event) {
        if (flashDuration > 0) {
            flashDuration--;
            if (flashDuration <= 0) {
                flashIntensity = 0.0f;
                totalFlashDuration = 0;
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (flashDuration <= 0 || flashIntensity <= 0) return;

        float alpha = flashIntensity * (flashDuration / (float) totalFlashDuration);
        if (alpha <= 0) return;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        event.getGuiGraphics().fill(RenderPipelines.GUI, 0, 0, screenWidth, screenHeight,
                ((int)(alpha * 255) << 24) | 0x00FFFFFF);
    }

    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        flashIntensity = 0.0f;
        flashDuration = 0;
        totalFlashDuration = 0;
    }
}