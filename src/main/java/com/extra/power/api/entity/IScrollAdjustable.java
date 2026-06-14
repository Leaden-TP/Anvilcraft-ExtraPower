package com.extra.power.api.entity;

import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;

/**
 * 实现此接口的方块实体可以通过滚轮调节参数
 */
public interface IScrollAdjustable {
    /**
     * 处理滚轮调节
     * @param parameterId 参数标识符，例如 "height_offset"
     * @param delta 变化量（正值增大，负值减小）
     * @param level 服务端Level
     * @param pos 方块位置
     */
    void onScrollAdjust(String parameterId, float delta, Level level, BlockPos pos);
}