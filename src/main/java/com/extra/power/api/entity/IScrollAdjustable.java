package com.extra.power.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * 实现此接口的方块实体可以通过滚轮调节参数
 */
public interface IScrollAdjustable {
    /**
     * 处理滚轮调节
     * steps 为滚轮调节的步数，正数表示向上滚动，负数表示向下滚动
     */
    void onScrollAdjust(int steps);
}