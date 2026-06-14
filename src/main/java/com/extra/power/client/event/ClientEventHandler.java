package com.extra.power.client.event;

import com.extra.power.api.entity.IScrollAdjustable;
import com.extra.power.network.MouseScrollPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.network.ClientPacketDistributor;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEventHandler {
    private static final float STEP = 1f; // 每次滚轮调整的步长

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;

        // 条件：空手、潜行
        if (!player.isShiftKeyDown()) return;
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) return;

        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) return;

        // 检查指向的方块实体是否实现了 IScrollAdjustable
        var be = mc.level.getBlockEntity(blockHit.getBlockPos());
        if (!(be instanceof IScrollAdjustable)) return;

        float delta = event.getScrollDeltaY() > 0 ? STEP : -STEP;
        // 发送通用滚轮调节包，参数ID为 "height_offset"
        ClientPacketDistributor.sendToServer(new MouseScrollPacket(blockHit.getBlockPos(), "height_offset", delta));
        event.setCanceled(true);
    }
}