package com.extra.power.block.just_block;

import com.extra.power.block.ModBlock;
import com.extra.power.block.ModBlockEntity;
import com.extra.power.block.blockentity.BurningMagnesiumBlockEntity;
import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.block.better.BetterBaseEntityBlock;
import dev.dubhe.anvilcraft.init.block.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
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
import java.util.Optional;

import static com.extra.power.block.just_block.Light.BRIGHTNESS;

public class BurningMagnesiumBlock extends BetterBaseEntityBlock {
    public static final BooleanProperty OVERHEATED = BooleanProperty.create("overheated");
    public static final BooleanProperty ToBoom = BooleanProperty.create("toboom");
    public BurningMagnesiumBlock (BlockBehaviour.Properties Properties) {
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
    protected BlockState updateShape(BlockState state, Direction direction, BlockState neighborState, LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (!level.isClientSide()) {
            BlockState block = level.getBlockState(pos.relative(direction));
            BlockState block_over = level.getBlockState(pos.above());
            if (block.is(Blocks.WATER)) {
                this.removeWaterBreadthFirstSearch((Level) level, pos);
                    explosion((Level) level,pos,3);
            }
            if (block_over.is(ModBlockTags.OVERHEATED_BLOCKS)) {
                level.setBlock(pos, state.setValue(OVERHEATED,true),1);
            }
            if (!block_over.is(ModBlockTags.OVERHEATED_BLOCKS) & state.getValue(OVERHEATED)) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
                level.playSound(null,pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS,
                        0.7F, 1.0F);
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
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
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
    public void wasExploded(Level level, BlockPos pos, Explosion explosion) {
        if (level.isClientSide) {
           return;
        }
        level.setBlock(pos, ModBlock.BURNING_MAGNESIUM_BLOCK.get().defaultBlockState().setValue(ToBoom,true),1);
    }
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OVERHEATED,ToBoom);
    }
    private boolean removeWaterBreadthFirstSearch(Level level, BlockPos pos) {
        BlockState spongeState = level.getBlockState(pos);
        return BlockPos.breadthFirstTraversal(pos, 6, 65, (p_277519_, p_277492_) -> {
            for(Direction direction : Direction.values()) {
                p_277492_.accept(p_277519_.relative(direction));
            }

        }, (p_294069_) -> {
            if (p_294069_.equals(pos)) {
                return true;
            } else {
                BlockState blockstate = level.getBlockState(p_294069_);
                FluidState fluidstate = level.getFluidState(p_294069_);
                if (!spongeState.canBeHydrated(level, pos, fluidstate, p_294069_)) {
                    return false;
                } else {
                    Block patt0$temp = blockstate.getBlock();
                    if (patt0$temp instanceof BucketPickup) {
                        BucketPickup bucketpickup = (BucketPickup)patt0$temp;
                        if (!bucketpickup.pickupBlock((Player)null, level, p_294069_, blockstate).isEmpty()) {
                            return true;
                        }
                    }

                    if (blockstate.getBlock() instanceof LiquidBlock) {
                        level.setBlock(p_294069_, Blocks.AIR.defaultBlockState(), 3);
                    } else {
                        if (!blockstate.is(Blocks.KELP) && !blockstate.is(Blocks.KELP_PLANT) && !blockstate.is(Blocks.SEAGRASS) && !blockstate.is(Blocks.TALL_SEAGRASS)) {
                            return false;
                        }

                        BlockEntity blockentity = blockstate.hasBlockEntity() ? level.getBlockEntity(p_294069_) : null;
                        dropResources(blockstate, level, p_294069_, blockentity);
                        level.setBlock(p_294069_, Blocks.AIR.defaultBlockState(), 3);
                    }

                    return true;
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

