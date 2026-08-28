package com.extra.power.util;

import com.extra.power.block.blockentity.NuclearCollectorBlockEntity;
import com.extra.power.block.just_block.FrostControllerBlock;
import com.extra.power.block.just_block.NuclearBombBlock;
import com.extra.power.block.just_block.UraniumRodBlock;
import com.extra.power.config.ModServerConfig;
import com.extra.power.init.block.ModBlock;
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
import net.minecraft.world.level.material.FluidState;

import java.util.HashMap;
import java.util.Map;

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
        final int[] waterAbsorbed = {0};
        final int[] outlimit = {0};
        int half = ModServerConfig.nuclearCollector.theMaximumWaterSurfaceArea / 2;
        int maxSteps = ModServerConfig.nuclearCollector.theMaximumWaterSurfaceArea + 3;
        int maxNodes = maxSteps * maxSteps;

        BlockPos.breadthFirstTraversal(startPos, maxSteps, maxNodes,
                (currentPos, queue) -> {
                    for (Direction direction : Direction.Plane.HORIZONTAL) {
                        BlockPos neighborPos = currentPos.relative(direction);
                        if (neighborPos.getY() == startPos.getY() &&
                                !ifOutLimet(neighborPos, startPos, half + 1)) {
                            queue.accept(neighborPos);
                        }
                    }
                },
                (currentPos) -> {
                    if (currentPos.equals(startPos)) {
                        return true;
                    }
                    if (currentPos.getY() != startPos.getY()) {
                        return false;
                    }
                    BlockState blockstate = level.getBlockState(currentPos);
                    Block block = blockstate.getBlock();

                    // 处理水源或冰
                    Integer multiplier = MULTIPLICATION.get(block);
                    if (multiplier != null) {
                        waterAbsorbed[0] += multiplier;
                        if (ifOutLimet(currentPos, startPos, half)) outlimit[0] += 1;
                        if (clean) {
                            if (block == Blocks.ICE || block == Blocks.FROSTED_ICE) {
                                if (level.dimension().equals(Level.NETHER)) level.setBlock(currentPos, Blocks.AIR.defaultBlockState(), 3);
                                else level.setBlock(currentPos, Blocks.WATER.defaultBlockState(), 3);
                            } else if (block == Blocks.PACKED_ICE) {
                                level.setBlock(currentPos, Blocks.ICE.defaultBlockState(), 3);
                            } else if (block == Blocks.BLUE_ICE) {
                                level.setBlock(currentPos, Blocks.PACKED_ICE.defaultBlockState(), 3);
                            } else if (block == Blocks.WATER) {
                                FluidState fluidState = blockstate.getFluidState();
                                if (fluidState.isSource()) {
                                    level.setBlock(currentPos, Blocks.AIR.defaultBlockState(), 3);
                                } else {
                                    waterAbsorbed[0] -= multiplier; // 非水源不计数
                                }
                            }
                        }
                        return true;
                    }

                    // 处理可拾取的水方块（如炼药锅）—— 修改就在这里
                    if (block instanceof BucketPickup) {
                        BucketPickup bucketpickup = (BucketPickup) block;
                        boolean hasWater = !blockstate.getFluidState().isEmpty(); // 判断是否真的有水

                        if (hasWater) {
                            if (clean) {
                                bucketpickup.pickupBlock(null, level, currentPos, blockstate);
                            }
                            waterAbsorbed[0]++;
                            if (ifOutLimet(currentPos, startPos, half)) {
                                outlimit[0] += 1;
                            }
                        }
                        return true;
                    }

                    // 处理水生植物
                    if (blockstate.is(Blocks.KELP) ||
                            blockstate.is(Blocks.KELP_PLANT) ||
                            blockstate.is(Blocks.SEAGRASS) ||
                            blockstate.is(Blocks.TALL_SEAGRASS)) {
                        BlockEntity blockentity = blockstate.hasBlockEntity() ? level.getBlockEntity(currentPos) : null;
                        if (clean) {
                            dropResources(blockstate, level, currentPos, blockentity);
                            level.setBlock(currentPos, Blocks.AIR.defaultBlockState(), 3);
                        }
                        if (ifOutLimet(currentPos, startPos, half)) outlimit[0] += 1;
                        return true;
                    }
                    return false;
                });
        if (outlimit[0] > 0) return -outlimit[0];
        return waterAbsorbed[0];
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
                        effective_rod += 5;
                    }
                }
            }
        }
        return effective_rod;
    }
    public static int checkRod_for_power(Level level, BlockPos pos) {
        int effective_rod = 0;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int i = -2; i <= 2; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -2; k <= 2; k++) {
                    mpos.set(pos).move(i, j, k);
                    if (level.isOutsideBuildHeight(mpos)) continue;
                    BlockState blockState = level.getBlockState(mpos);
                    if (blockState.getBlock() instanceof UraniumRodBlock && blockState.getValue(HALF) == Vertical3PartHalf.MID) {
                        effective_rod += 1;
                    }
                    if (blockState.getBlock() instanceof NuclearBombBlock || blockState.getBlock() == ModBlocks.PLUTONIUM_BLOCK.get()) {
                        effective_rod += 1;
                    }
                }
            }
        }
        return effective_rod;
    }
    public static int check_for_cold(Level level, BlockPos pos) {
        int controller = 0;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int i = -2; i <= 2; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -2; k <= 2; k++) {
                    mpos.set(pos).move(i, j, k);
                    if (level.isOutsideBuildHeight(mpos)) continue;
                    BlockState blockState = level.getBlockState(mpos);
                    if (blockState.getBlock() instanceof FrostControllerBlock && blockState.getValue(FrostControllerBlock.HALF) == Vertical3PartHalf.MID
                            &&  blockState.getValue(FrostControllerBlock.ACTIVE)) {
                        controller+=1;
                    }
                }
            }
        }
        return controller;
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
