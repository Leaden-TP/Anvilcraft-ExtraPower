package com.extra.power.entity;


import com.extra.power.init.ModEntities;
import net.minecraft.network.syncher.EntityDataAccessor;
import net.minecraft.network.syncher.EntityDataSerializers;
import net.minecraft.network.syncher.SynchedEntityData;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.projectile.AbstractArrow;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;


import net.minecraft.world.phys.EntityHitResult;



public class TechnicalArrowEntity extends Arrow {


    public TechnicalArrowEntity(EntityType<? extends AbstractArrow> type, Level level) {
        super((EntityType<? extends Arrow>) type, level);
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

}
