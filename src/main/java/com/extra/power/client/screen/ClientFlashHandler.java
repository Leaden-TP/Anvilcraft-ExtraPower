package com.extra.power.client.screen;


import net.minecraft.client.Minecraft;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.client.event.RenderGuiLayerEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientFlashHandler {
    private static float flashIntensity = 0.0f;
    private static int flashDuration = 0;
    private static int totalFlashDuration = 0; // 用于计算alpha比例

    public static void receiveFlash(float intensity, int duration) {
        // 如果已有更强或更长的闪光，取最大值
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
            }
        }
    }

    @SubscribeEvent
    public static void onRenderGuiLayer(RenderGuiLayerEvent.Pre event) {
        if (flashDuration <= 0 || flashIntensity <= 0) {
            return;
        }

        // 计算当前alpha：强度 * 剩余时间比例（线性衰减）
        float alpha = flashIntensity * (flashDuration / (float) totalFlashDuration);
        if (alpha <= 0) return;

        Minecraft mc = Minecraft.getInstance();
        int screenWidth = mc.getWindow().getGuiScaledWidth();
        int screenHeight = mc.getWindow().getGuiScaledHeight();

        // 启用混合，绘制白色全屏矩形
        com.mojang.blaze3d.systems.RenderSystem.enableBlend();
        com.mojang.blaze3d.systems.RenderSystem.defaultBlendFunc();
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, alpha);

        // 使用GuiGraphics绘制矩形（ARGB格式）
        event.getGuiGraphics().fill(0, 0, screenWidth, screenHeight,
                (int) (alpha * 255) << 24 | 0x00FFFFFF); // 白色，alpha从高字节

        // 重置颜色
        com.mojang.blaze3d.systems.RenderSystem.setShaderColor(1.0f, 1.0f, 1.0f, 1.0f);
        com.mojang.blaze3d.systems.RenderSystem.disableBlend();
    }

    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        flashIntensity = 0.0f;
        flashDuration = 0;
        totalFlashDuration = 0;
    }
}
