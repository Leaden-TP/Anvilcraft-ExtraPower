package com.extra.power.block.just_block;

import com.extra.power.block.ModBlock;
import com.extra.power.block.ModBlockEntity;
import com.extra.power.block.blockentity.BurningCoalBlockEntity;
import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.block.better.BetterBaseEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;


public class BurningCoalBlock extends BetterBaseEntityBlock {
    public BurningCoalBlock(BlockBehaviour.Properties Properties) {
        super(Properties);
    }
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, @NotNull BlockState state) {
        return new BurningCoalBlockEntity(pos,state);
    }

@Nullable
@Override
public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
    return createTickerHelper(type, ModBlockEntity.BURNING_COAL.get(), BurningCoalBlockEntity::tick);
}

    @Override
    protected MapCodec<BurningCoalBlock> codec() {
        return simpleCodec(BurningCoalBlock::new);
    }

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (state.is(ModBlock.BURNING_COAL_BLOCK.get())
                && !entity.isSteppingCarefully()
                && entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().hotFloor(), 4.0F);
        }
        super.stepOn(level, pos, state, entity);
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
