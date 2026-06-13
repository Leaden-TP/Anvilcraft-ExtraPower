
// ClientShakeHandler.java
package com.extra.power.client.screen;

import com.extra.power.network.ShakePayload;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.ViewportEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;

import java.util.Random;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientShakeHandler {
    private static float shakeIntensity = 0.0f;
    private static int shakeDuration = 0;
    private static final Random RANDOM = new Random();

    // 1. 注册网络包接收器
    public static void register(final RegisterPayloadHandlersEvent event) {
        final PayloadRegistrar registrar = event.registrar("1");
        registrar.playToClient(
                ShakePayload.TYPE,
                ShakePayload.STREAM_CODEC,
                (payload, context) -> {
                    // 收到数据包后，设置震动参数
                    shakeIntensity = payload.strength();
                    shakeDuration = payload.duration();
                }
        );
    }
    // 2. 摄像机设置事件：在这里修改视角实现震动
    @SubscribeEvent
    public static void onCameraSetup(ViewportEvent.ComputeCameraAngles event) {
        if (shakeDuration <= 0 || shakeIntensity <= 0) {
            return; // 没有震动需求
        }

        LocalPlayer player = Minecraft.getInstance().player;
        if (player == null) return;

        // 计算当前这一帧的震动偏移量
        float time = (float) (player.tickCount + event.getPartialTick());
        float currentIntensity = shakeIntensity * (shakeDuration / 40.0f); // 强度随时间衰减

        // 使用正弦波在三个轴上产生随机摇晃
        float yawOffset = (float) (Math.sin(time * 20) * currentIntensity * 0.5);
        float pitchOffset = (float) (Math.sin(time * 25 + 1) * currentIntensity * 0.3);
        float rollOffset = (float) (Math.cos(time * 15) * currentIntensity * 0.2); // 滚动效果

        // 将偏移量应用到摄像机角度上
        event.setYaw(event.getYaw() + yawOffset);
        event.setPitch(event.getPitch() + pitchOffset);
        event.setRoll(event.getRoll() + rollOffset);

        // 减少持续时间
        shakeDuration--;
    }

    // 3. 玩家登出或切换世界时重置震动，避免效果残留
    @SubscribeEvent
    public static void onPlayerLogout(ClientPlayerNetworkEvent.LoggingOut event) {
        shakeIntensity = 0.0f;
        shakeDuration = 0;
    }
    public static void receiveShake(float intensity, int duration) {
        // 直接设置震动参数
        shakeIntensity = intensity;
        shakeDuration = duration;
    }
}
