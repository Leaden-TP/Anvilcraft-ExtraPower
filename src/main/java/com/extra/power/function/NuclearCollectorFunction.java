package com.extra.power.function;

import com.extra.power.block.blockentity.NuclearCollectorBlockEntity;
import com.extra.power.block.just_block.NuclearBombBlock;
import com.extra.power.block.just_block.UraniumRodBlock;
import com.extra.power.config.ModServerConfig;
import dev.dubhe.anvilcraft.block.state.Vertical3PartHalf;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.BucketPickup;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicInteger;

import static com.extra.power.block.just_block.UraniumRodBlock.*;
import static net.minecraft.world.level.block.Block.dropResources;

public class NuclearCollectorFunction {
    public static final Map<Block, Integer> MULTIPLICATION = new HashMap<>();

    static {
        MULTIPLICATION.put(Blocks.WATER, 1);
        MULTIPLICATION.put(Blocks.ICE, 2);
        MULTIPLICATION.put(Blocks.PACKED_ICE, 2);
        MULTIPLICATION.put(Blocks.BLUE_ICE, 2);
        MULTIPLICATION.put(Blocks.FROSTED_ICE, 1);
    }
    public static boolean ifOutLimet(BlockPos pos ,BlockPos startPos ,int half){
        return Math.abs(pos.getX() - startPos.getX()) > half ||
                Math.abs(pos.getZ() - startPos.getZ()) > half;
    }
    public static int checkWater(Level level, BlockPos pos, int y, boolean clean) {
        BlockPos startPos = pos.above(y);
        int half = ModServerConfig.nuclearCollector.theMaximumWaterSurfaceArea / 2;
        int maxSteps = ModServerConfig.nuclearCollector.theMaximumWaterSurfaceArea + 3;
        int maxNodes = maxSteps * maxSteps;

        AtomicInteger waterAbsorbed = new AtomicInteger(0);
        AtomicInteger outLimit = new AtomicInteger(0);

        BlockPos.breadthFirstTraversal(startPos, maxSteps, maxNodes,
                (currentPos, queue) -> {
                    for (Direction direction : Direction.Plane.HORIZONTAL) {
                        BlockPos neighbor = currentPos.relative(direction);
                        if (neighbor.getY() == startPos.getY() && !ifOutLimet(neighbor, startPos, half + 1)) {
                            queue.accept(neighbor);
                        }
                    }
                },
                (currentPos) -> {
                    if (currentPos.equals(startPos)) {
                        return BlockPos.TraversalNodeStatus.ACCEPT;
                    }
                    BlockState blockState = level.getBlockState(currentPos);
                    Block block = blockState.getBlock();

                    Integer multiplier = MULTIPLICATION.get(block);
                    if (multiplier != null) {
                        waterAbsorbed.addAndGet(multiplier);
                        if (ifOutLimet(currentPos, startPos, half)) {
                            outLimit.incrementAndGet();
                        }
                        if (clean) {
                            if (block == Blocks.ICE || block == Blocks.FROSTED_ICE) {
                                if (level.dimension().equals(Level.NETHER))
                                    level.setBlock(currentPos, Blocks.AIR.defaultBlockState(), 3);
                                else
                                    level.setBlock(currentPos, Blocks.WATER.defaultBlockState(), 3);
                            } else if (block == Blocks.PACKED_ICE) {
                                level.setBlock(currentPos, Blocks.ICE.defaultBlockState(), 3);
                            } else if (block == Blocks.BLUE_ICE) {
                                level.setBlock(currentPos, Blocks.PACKED_ICE.defaultBlockState(), 3);
                            } else if (block == Blocks.WATER) {
                                if (blockState.getFluidState().isSource()) {
                                    level.setBlock(currentPos, Blocks.AIR.defaultBlockState(), 3);
                                }
                            }
                        }
                        return BlockPos.TraversalNodeStatus.ACCEPT;
                    }

                    if (block instanceof BucketPickup) {
                        BucketPickup bucketPickup = (BucketPickup) block;
                        if (!bucketPickup.pickupBlock(null, level, currentPos, blockState).isEmpty()) {
                            waterAbsorbed.incrementAndGet();
                            if (ifOutLimet(currentPos, startPos, half)) {
                                outLimit.incrementAndGet();
                            }
                            return BlockPos.TraversalNodeStatus.ACCEPT;
                        }
                    }

                    if (blockState.is(Blocks.KELP) || blockState.is(Blocks.KELP_PLANT) ||
                            blockState.is(Blocks.SEAGRASS) || blockState.is(Blocks.TALL_SEAGRASS)) {
                        if (clean) {
                            BlockEntity blockEntity = blockState.hasBlockEntity() ? level.getBlockEntity(currentPos) : null;
                            dropResources(blockState, level, currentPos, blockEntity);
                            level.setBlock(currentPos, Blocks.AIR.defaultBlockState(), 3);
                        }
                        if (ifOutLimet(currentPos, startPos, half)) {
                            outLimit.incrementAndGet();
                        }
                        return BlockPos.TraversalNodeStatus.ACCEPT;
                    }

                    return BlockPos.TraversalNodeStatus.SKIP;
                }
        );

        if (outLimit.get() > 0) {
            return -outLimit.get();
        }
        return waterAbsorbed.get();
    }
    public static int checkRod(Level level, BlockPos pos, NuclearCollectorBlockEntity entity,Boolean control) {
        int effective_rod = 0;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int i = -2; i <= 2; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -2; k <= 2; k++) {
                    mpos.set(pos).move(i, j, k);
                    if (level.isOutsideBuildHeight(mpos)) continue;
                    BlockState blockState = level.getBlockState(mpos);
                    if (blockState.getBlock() instanceof UraniumRodBlock && blockState.getValue(HALF) == Vertical3PartHalf.MID) {
                        if (blockState.getValue(ACTIVE) == 0) {
                            level.setBlock(mpos, blockState.setValue(ACTIVE, 5), 11);
                        }
                        if (control) {
                            if (blockState.getValue(UNDER_CONTROL) == false)
                                level.setBlock(mpos, blockState.setValue(UNDER_CONTROL, true), 11);
                        } else {
                            level.setBlock(mpos, blockState.setValue(UNDER_CONTROL, false), 11);
                        }
                        if (blockState.getValue(ACTIVE) > 1) {
                            effective_rod += blockState.getValue(ACTIVE);
                        }
                    }
                    if (blockState.getBlock() instanceof NuclearBombBlock || blockState.getBlock() == ModBlocks.PLUTONIUM_BLOCK.get()) {
                        effective_rod += 1;
                    }
                }
            }
        }
        return effective_rod;
    }
    //查找冷却基准点
    public static int findPoint(Level level, BlockPos pos) {
        for (int j = 1; j <= 360; j++) {
            BlockPos mpos = pos.above(j);
            if (level.isOutsideBuildHeight(mpos)) return j-1;
            BlockState blockState = level.getBlockState(mpos);
            if (!blockState.is(Blocks.WATER)) {
                if (blockState.is(Blocks.ICE)||blockState.is(Blocks.PACKED_ICE)
                        ||blockState.is(Blocks.BLUE_ICE)||blockState.is(Blocks.FROSTED_ICE)) {
                    return j;
                }
                return j-1;
            }
        }
        return 20;
    }
}
