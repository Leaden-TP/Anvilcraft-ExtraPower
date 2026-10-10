package com.extra.power.block.just_block;

import net.minecraft.util.StringRepresentable;

/**
 * 红石中继器的颜色模式：红色全局收发且范围无限、可跨纬度，黄色 128 格内收发。
 * 蓝色为特殊模式，绑定一个接收模式的 redstone link 的位置并只与该位置的方块联络，颜色被锁定且无法通过滚轮调节。
 */
public enum RedstoneLinkColor implements StringRepresentable {
    RED,
    YELLOW,
    BLUE;

    @Override
    public String getSerializedName() {
        // 与模型文件命名一致：颜色开头字母（red -> r）
        return switch (this) {
            case RED -> "r";
            case YELLOW -> "y";
            case BLUE -> "b";
        };
    }
}
