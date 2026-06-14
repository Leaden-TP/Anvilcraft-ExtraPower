package com.extra.power.block.just_block;

import com.extra.power.block.ModBlock;
import com.extra.power.block.ModBlockEntity;
import com.extra.power.block.blockentity.BurningMagnesiumBlockEntity;
import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.block.better.BetterBaseEntityBlock;
import dev.dubhe.anvilcraft.init.block.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.*;
import net.minecraft.world.level.block.*;
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

import static com.extra.power.block.just_block.LightBlock.BRIGHTNESS;

public class BurningMagnesiumBlock extends BetterBaseEntityBlock {
    public static final BooleanProperty OVERHEATED = BooleanProperty.create("overheated");
    public static final BooleanProperty ToBoom = BooleanProperty.create("toboom");
    public BurningMagnesiumBlock (Properties Properties) {
        super(Properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(OVERHEATED, false).setValue(ToBoom,false));
    }
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, @NotNull BlockState state) {
        return new BurningMagnesiumBlockEntity(pos,state);
    }
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntity.BURNING_MAGNESIUM.get(), BurningMagnesiumBlockEntity::tick);
    }

    @Override
    protected MapCodec<BurningMagnesiumBlock> codec() {
        return simpleCodec(BurningMagnesiumBlock::new);
    }
    public static void explosion(Level level, BlockPos pos, float r) {
    if (level.isClientSide()) {
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

    @Override
    public void stepOn(Level level, BlockPos pos, BlockState state, Entity entity) {
        if (state.is(ModBlock.BURNING_MAGNESIUM_BLOCK.get())
                && !entity.isSteppingCarefully()
                && entity instanceof LivingEntity) {
            entity.hurt(level.damageSources().hotFloor(), 10.0F);
        }
        super.stepOn(level, pos, state, entity);
    }
    @Override
    protected BlockState updateShape(
            BlockState state,
            LevelReader level,
            ScheduledTickAccess ticks,
            BlockPos pos,
            Direction direction,
            BlockPos neighbourPos,
            BlockState neighbour,
            RandomSource random
    ) {
        if (!level.isClientSide()) {
            BlockState block = level.getBlockState(pos.relative(direction));
            BlockState block_over = level.getBlockState(pos.above());
            Level level1 = (Level) level;
            if (block.is(Blocks.WATER)) {
                this.removeWaterBreadthFirstSearch((Level) level, pos);
                    explosion((Level) level,pos,3);
            }
            if (block_over.is(ModBlockTags.OVERHEATED_BLOCKS)) {
                level1.setBlock(pos, state.setValue(OVERHEATED,true),1);
            }
            if (!block_over.is(ModBlockTags.OVERHEATED_BLOCKS) & state.getValue(OVERHEATED)) {
                level1.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
                level1.playSound(null,pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS,
                        0.7F, 1.0F);
            }
            if (block.is(Blocks.AIR)) {
                level1.setBlock(pos.relative(direction),
                        ModBlock.LIGHT.get().defaultBlockState().setValue(BRIGHTNESS, 0), 11);
            }
            if (block.is(ModBlock.LIGHT.get())) {
                if (block.getValue(BRIGHTNESS) >0)
                    level1.setBlock(pos.relative(direction),
                            ModBlock.LIGHT.get().defaultBlockState().setValue(BRIGHTNESS, 0), 11);
            }
        }
        return super.updateShape(state, level, ticks, pos, direction, neighbourPos, neighbour, random);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide()) {
            BlockState block_over = level.getBlockState(pos.above());
            for (Direction direction : Direction.values()) {
                BlockState block = level.getBlockState(pos.relative(direction));
                if (block.is(Blocks.WATER)) {
                    this.removeWaterBreadthFirstSearch(level, pos);
                    explosion(level,pos,3);
                }
                if (block.is(Blocks.AIR)) {
                    level.setBlock(pos.relative(direction),
                            ModBlock.LIGHT.get().defaultBlockState().setValue(BRIGHTNESS, 0), 11);
                }
                if (block.is(ModBlock.LIGHT.get())) {
                    if (block.getValue(BRIGHTNESS) >0)
                    level.setBlock(pos.relative(direction),
                            ModBlock.LIGHT.get().defaultBlockState().setValue(BRIGHTNESS, 0), 11);
                }
            }
            if (block_over.is(ModBlockTags.OVERHEATED_BLOCKS)) {
                level.setBlock(pos, state.setValue(OVERHEATED,true),1);
            }
        }
    }
    @Override
    public void wasExploded(ServerLevel level, BlockPos pos, Explosion explosion) {
        if (level.isClientSide()) {
           return;
        }
        level.setBlock(pos, ModBlock.BURNING_MAGNESIUM_BLOCK.get().defaultBlockState().setValue(ToBoom,true),1);
    }
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OVERHEATED,ToBoom);
    }
    private boolean removeWaterBreadthFirstSearch(Level level, BlockPos startPos) {
        BlockState spongeState = level.getBlockState(startPos);
        return BlockPos.breadthFirstTraversal(startPos, 6, 65, (pos, consumer) -> {
            for (Direction direction : Direction.values()) {
                consumer.accept(pos.relative(direction));
            }
        }, pos -> {
            if (pos.equals(startPos)) {
                return BlockPos.TraversalNodeStatus.ACCEPT;
            } else {
                BlockState state = level.getBlockState(pos);
                FluidState fluidState = level.getFluidState(pos);
                if (!spongeState.canBeHydrated(level, startPos, fluidState, pos)) {
                    return BlockPos.TraversalNodeStatus.SKIP;
                } else if (state.getBlock() instanceof BucketPickup bucketPickup && !bucketPickup.pickupBlock(null, level, pos, state).isEmpty()) {
                    return BlockPos.TraversalNodeStatus.ACCEPT;
                } else {
                    if (state.getBlock() instanceof LiquidBlock) {
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    } else {
                        if (!state.is(Blocks.KELP) && !state.is(Blocks.KELP_PLANT) && !state.is(Blocks.SEAGRASS) && !state.is(Blocks.TALL_SEAGRASS)) {
                            return BlockPos.TraversalNodeStatus.SKIP;
                        }

                        BlockEntity blockEntity = state.hasBlockEntity() ? level.getBlockEntity(pos) : null;
                        dropResources(state, level, pos, blockEntity);
                        level.setBlock(pos, Blocks.AIR.defaultBlockState(), 3);
                    }

                    return BlockPos.TraversalNodeStatus.ACCEPT;
                }
            }
        }) > 1;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}

