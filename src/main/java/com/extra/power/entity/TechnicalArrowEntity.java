package com.extra.power.entity;

import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.EntityHitResult;

public class TechnicalArrowEntity extends Arrow {

    /** 落地后存活时间（tick）：2 秒 = 40 tick */
    private static final int GROUND_LIFETIME = 40;

    /** 光灵箭造成的发光效果持续时间（tick），与原版 SpectralArrow 一致 */
    private static final int GLOWING_DURATION = 200;

    /** 已经落地的 tick 数 */
    private int groundTicks = 0;

    public TechnicalArrowEntity(EntityType<? extends TechnicalArrowEntity> type, Level level) {
        super(type, level);
    }

    /**
     * 以装填的弹药栈构造。
     *
     * <p>弹药栈会写入 {@code pickupItemStack}，而 {@link Arrow} 的药水效果正是从这个栈里读的
     * （{@link Arrow#doPostHurtEffects}），所以药水箭（{@code TIPPED_ARROW}）的效果可以正常生效；
     * 同时 {@code Arrow#setPickupItemStack} 会顺带刷新药水颜色，粒子也会对。
     *
     * <p>注意：{@code AbstractArrow} 中带 {@code EntityType} 参数的构造器在 {@link Arrow} 里没有对外开放
     * （{@code Arrow} 的构造器把实体类型写死为 {@code EntityType.ARROW}），所以这里用
     * {@code super(type, level)} + 受保护的 setter 组合出等价初始化。
     *
     * @param ammoStack 被装填的弹药（箭矢）
     */
    public TechnicalArrowEntity(
            EntityType<? extends TechnicalArrowEntity> type,
            LivingEntity owner,
            Level level,
            ItemStack ammoStack
    ) {
        super(type, level);
        this.setPos(owner.getX(), owner.getEyeY() - 0.1F, owner.getZ());
        this.setOwner(owner);
        this.setPickupItemStack(ammoStack.copyWithCount(1));
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
    protected void doPostHurtEffects(LivingEntity living) {
        super.doPostHurtEffects(living);
        // 光灵箭的原版行为由单独的 SpectralArrow 实体提供，本实体统一承载所有箭矢，需要在此补齐。
        if (this.getPickupItemStackOrigin().is(Items.SPECTRAL_ARROW)) {
            living.addEffect(new MobEffectInstance(MobEffects.GLOWING, GLOWING_DURATION, 0), this.getEffectSource());
        }
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