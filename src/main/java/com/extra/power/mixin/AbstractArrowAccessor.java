package com.extra.power.mixin;

import net.minecraft.world.entity.projectile.AbstractArrow;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * {@code AbstractArrow#setPierceLevel(byte)} 是 private 的，写入的是同步数据 {@code PIERCE_LEVEL}。
 * 这里用 Invoker 取得调用入口，让本模组的箭矢也能设置穿透等级，从而复用原版的穿透逻辑。
 */
@Mixin(AbstractArrow.class)
public interface AbstractArrowAccessor {
    @Invoker("setPierceLevel")
    void anvilcraftextrapower$setPierceLevel(byte pierceLevel);
}
