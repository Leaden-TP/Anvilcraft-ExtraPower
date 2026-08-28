package com.extra.power.client.event;

import com.extra.power.api.entity.IScrollAdjustable;
import com.extra.power.block.blockentity.NuclearCollectorBlockEntity;
import com.extra.power.network.toServer.MouseScrollPacket;
import com.extra.power.network.toServer.NuclearCollectorRequestPacket;
import net.minecraft.client.Minecraft;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.InputEvent;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;

@EventBusSubscriber(value = Dist.CLIENT)
public class ClientEventHandler {
    private static final int COLLECTOR_REQUEST_INTERVAL = 20;
    private static BlockPos pendingScrollPos;
    private static int pendingScrollSteps;
    private static BlockPos lastCollectorPos;
    private static long nextCollectorRequestTick;
    private static ResourceKey<Level> clientDimension;

    @SubscribeEvent
    public static void onMouseScroll(InputEvent.MouseScrollingEvent event) {
        Minecraft mc = Minecraft.getInstance();
        Player player = mc.player;
        if (player == null || mc.level == null) return;
        updateDimension(mc.level);

        // 条件：空手、潜行
        if (!player.isShiftKeyDown()) return;
        ItemStack mainHand = player.getMainHandItem();
        if (!mainHand.isEmpty()) return;

        HitResult hit = mc.hitResult;
        if (!(hit instanceof BlockHitResult blockHit)) return;

        // 检查指向的方块实体是否实现了 IScrollAdjustable
        var be = mc.level.getBlockEntity(blockHit.getBlockPos());
        if (!(be instanceof IScrollAdjustable)) return;

        int step = event.getScrollDeltaY() > 0 ? 1 : -1;
        BlockPos pos = blockHit.getBlockPos();
        if (!pos.equals(pendingScrollPos)) {
            pendingScrollPos = pos.immutable();
            pendingScrollSteps = 0;
        }
        pendingScrollSteps = Mth.clamp(pendingScrollSteps + step, -24, 24);
        ((IScrollAdjustable) be).onScrollAdjust(step);
        event.setCanceled(true);
    }

    @SubscribeEvent
    public static void onClientTick(ClientTickEvent.Post event) {
        Minecraft mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) {
            clearPendingState();
            return;
        }
        updateDimension(mc.level);

        if (pendingScrollPos != null && pendingScrollSteps != 0) {
            PacketDistributor.sendToServer(
                    new MouseScrollPacket(pendingScrollPos, (byte) pendingScrollSteps)
            );
            pendingScrollPos = null;
            pendingScrollSteps = 0;
        }

        if (mc.options.hideGui || mc.screen != null
                || !(mc.hitResult instanceof BlockHitResult blockHit)
                || !(mc.level.getBlockEntity(blockHit.getBlockPos()) instanceof NuclearCollectorBlockEntity)) {
            lastCollectorPos = null;
            return;
        }

        BlockPos collectorPos = blockHit.getBlockPos();
        long gameTime = mc.level.getGameTime();
        if (!collectorPos.equals(lastCollectorPos) || gameTime >= nextCollectorRequestTick) {
            PacketDistributor.sendToServer(new NuclearCollectorRequestPacket(collectorPos));
            lastCollectorPos = collectorPos.immutable();
            nextCollectorRequestTick = gameTime + COLLECTOR_REQUEST_INTERVAL;
        }
    }

    private static void clearPendingState() {
        pendingScrollPos = null;
        pendingScrollSteps = 0;
        lastCollectorPos = null;
        nextCollectorRequestTick = 0;
        clientDimension = null;
    }

    private static void updateDimension(Level level) {
        ResourceKey<Level> dimension = level.dimension();
        if (dimension.equals(clientDimension)) return;
        clearPendingState();
        clientDimension = dimension;
    }
}
