package com.extra.power.util;

import com.extra.power.init.ModCriterionTriggers;
import dev.dubhe.anvilcraft.util.PlayerUtil;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.Level;

/**
 * 本模组成就触发器的集中调用入口。
 * 方块相关的触发条件按铁砧工艺的做法广播给附近玩家，直接由玩家造成的条件则当场判定该玩家。
 */
public class ModAdvancementTriggers {
    public static void igniteMagnesium(ServerPlayer player) {
        ModCriterionTriggers.IGNITE_MAGNESIUM.get().trigger(player);
    }

    public static void uraniumRodRadiation(ServerPlayer player) {
        ModCriterionTriggers.URANIUM_ROD_RADIATION.get().trigger(player);
    }

    public static void nuclearExplosionHurt(ServerPlayer player) {
        ModCriterionTriggers.NUCLEAR_EXPLOSION_HURT.get().trigger(player);
    }

    public static void magneticDisplayStand(ServerPlayer player) {
        ModCriterionTriggers.MAGNETIC_DISPLAY_STAND.get().trigger(player);
    }

    public static void frostController(Level level, BlockPos pos) {
        if (!level.isClientSide) {
            for (ServerPlayer player : PlayerUtil.searchPlayerByPos(level, pos, 7)) {
                ModCriterionTriggers.FROST_CONTROLLER.get().trigger(player);
            }
        }
    }

    public static void nuclearCollector(Level level, BlockPos pos, int rods) {
        if (!level.isClientSide) {
            for (ServerPlayer player : PlayerUtil.searchPlayerByPos(level, pos, 7)) {
                ModCriterionTriggers.NUCLEAR_COLLECTOR.get().trigger(player, rods);
            }
        }
    }

    public static void magnetAttractAnvil(Level level, BlockPos pos, int distance) {
        if (!level.isClientSide) {
            for (ServerPlayer player : PlayerUtil.searchPlayerByPos(level, pos, 10)) {
                ModCriterionTriggers.MAGNET_ATTRACT_ANVIL.get().trigger(player, distance);
            }
        }
    }

    public static void anvilProjection(Level level, BlockPos pos, int blocks) {
        if (!level.isClientSide) {
            for (ServerPlayer player : PlayerUtil.searchPlayerByPos(level, pos, 16)) {
                ModCriterionTriggers.ANVIL_PROJECTION.get().trigger(player, blocks);
            }
        }
    }
}
