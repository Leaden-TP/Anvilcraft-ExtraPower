package com.extra.power.block.just_block;

import net.minecraft.util.StringRepresentable;

/**
 * 红石中继器的颜色模式。
 * <p>红色：全局收发，范围无限且可跨纬度。
 * <p>黄色：128 格内收发。
 * <p>蓝色：特殊模式，绑定一个接收模式的 redstone link 的位置，只与该位置的方块联络，
 * 且颜色被锁定，无法通过滚轮调节。
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
