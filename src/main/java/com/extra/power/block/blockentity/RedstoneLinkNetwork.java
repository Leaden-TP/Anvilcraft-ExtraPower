package com.extra.power.block.blockentity;

import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

import java.util.Collection;
import java.util.Collections;
import java.util.HashMap;
import java.util.Map;

/**
 * 红石中继器的网络注册表（服务端）。
 * <p>仅登记处于发送模式（received=false）的方块实体，供接收模式的方块在 tick 时查询，
 * 从而实现红色全局跨纬度、黄色 128 格范围以及蓝色定点联络。
 */
public final class RedstoneLinkNetwork {
    private static final Map<ResourceKey<Level>, Map<BlockPos, RedstoneLinkBlockEntity>> SENDERS = new HashMap<>();

    private RedstoneLinkNetwork() {
    }

    public static void addSender(RedstoneLinkBlockEntity entity) {
        if (entity.getLevel() == null || entity.getLevel().isClientSide()) return;
        SENDERS.computeIfAbsent(entity.getLevel().dimension(), k -> new HashMap<>())
                .put(entity.getBlockPos(), entity);
    }

    public static void removeSender(RedstoneLinkBlockEntity entity) {
        if (entity.getLevel() == null || entity.getLevel().isClientSide()) return;
        Map<BlockPos, RedstoneLinkBlockEntity> map = SENDERS.get(entity.getLevel().dimension());
        if (map == null) return;
        map.remove(entity.getBlockPos());
        if (map.isEmpty()) SENDERS.remove(entity.getLevel().dimension());
    }

    public static Collection<RedstoneLinkBlockEntity> allSenders() {
        return SENDERS.values().stream()
                .flatMap(map -> map.values().stream())
                .toList();
    }

    /** 服务器停止时清空，避免跨会话残留 */
    public static void clear() {
        SENDERS.clear();
    }

    /** 仅供调试/清理使用 */
    public static Map<ResourceKey<Level>, Map<BlockPos, RedstoneLinkBlockEntity>> sendersView() {
        return Collections.unmodifiableMap(SENDERS);
    }
}
