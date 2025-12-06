package com.extra.power.block.just_block;

import com.extra.power.block.ModBlock;
import com.extra.power.block.ModBlockEntity;
import com.extra.power.block.blockentity.BurningCoalBlockEntity;
import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.block.better.BetterBaseEntityBlock;
import dev.dubhe.anvilcraft.init.block.ModBlockTags;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.pathfinder.PathComputationType;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;



public class BurningCoalBlock extends BetterBaseEntityBlock {
    public static final BooleanProperty ToBoom = BooleanProperty.create("toboom");
    public BurningCoalBlock(BlockBehaviour.Properties Properties) {
        super(Properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(ToBoom, false));
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, @NotNull BlockState state) {
        return new BurningCoalBlockEntity(pos, state);
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

    public static void explosion(Level level, BlockPos pos, float r) {
        if (level.isClientSide) {
            return;
        }
        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
        level.explode(null,
                null,
                new ExplosionDamageCalculator() {
                    @Override
                    public boolean shouldDamageEntity(Explosion explosion, Entity entity) {
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

    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (level.isClientSide) {
            return;
        }
        level.setBlock(pos, ModBlock.BURNING_COAL_BLOCK.get().defaultBlockState().setValue(ToBoom,true),1);
    }
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ToBoom);
    }
}
