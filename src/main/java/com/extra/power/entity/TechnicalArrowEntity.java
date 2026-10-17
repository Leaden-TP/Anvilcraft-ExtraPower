package com.extra.power.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class TechnicalArrowEntity extends Arrow {

    /** 落地后存活时间（tick）：2 秒 = 40 tick */
    private static final int GROUND_LIFETIME = 40;

    /** 已经落地的 tick 数 */
    private int groundTicks = 0;

    public TechnicalArrowEntity(EntityType<? extends TechnicalArrowEntity> type, Level level) {
        super(type, level);
    }

    @Override
    protected void defineSynchedData(SynchedEntityData.Builder builder) {
        super.defineSynchedData(builder);
    }

    @Override
    public ItemStack getPickupItem() {
        return ItemStack.EMPTY; // 无物品形式
    }

    @Override
    protected void onHitEntity(EntityHitResult result) {
        Entity target = result.getEntity();
        if (target instanceof LivingEntity living) {
            living.invulnerableTime = 0;
        }
        super.onHitEntity(result);
    }

    @Override
    public void tick() {
        super.tick();

        // super.tick() 里可能已经因为各种原因（拾取、超时、命中伤害等）把实体移除
        if (this.isRemoved()) {
            return;
        }

        if (this.inGround) {
            // 落地计时：每 tick 累加，达到 40 tick（2 秒）即消失
            if (++this.groundTicks >= GROUND_LIFETIME) {
                this.discard();
            }
        } else {
            // 重新起飞（落地方块被破坏等）则重置计时
            this.groundTicks = 0;
        }
    }
}