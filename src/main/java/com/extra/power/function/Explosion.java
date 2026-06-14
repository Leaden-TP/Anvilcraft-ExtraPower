package com.extra.power.function;

import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.ExplosionDamageCalculator;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;

public class Explosion {
    public static void explosion(Level level, BlockPos pos, float r) {
        if (level.isClientSide()) {
            return;
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
        level.explode(null,
                null,
                new ExplosionDamageCalculator() {
                    @Override
                    public boolean shouldDamageEntity(net.minecraft.world.level.Explosion explosion, Entity entity) {
                        return true;
                    }
                },
                pos.getX(),
                pos.getY(),
                pos.getZ(),
                r,
                true,
                Level.ExplosionInteraction.BLOCK);
    }
}
