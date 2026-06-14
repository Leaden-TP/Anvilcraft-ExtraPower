package com.extra.power.block.blockentity;

import com.extra.power.block.just_block.FrostControllerBlock;
import com.extra.power.block.just_block.UraniumRodBlock;
import dev.dubhe.anvilcraft.api.tooltip.providers.IHasAffectRange;
import dev.dubhe.anvilcraft.block.state.Vertical3PartHalf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import java.util.ArrayList;
import java.util.List;

import static com.extra.power.block.ModBlockEntity.FROST_CONTROLLER;


public class FrostControllerBlockEntity extends BlockEntity implements IHasAffectRange {
    private int tickCounter = 0;
    public FrostControllerBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public static FrostControllerBlockEntity createBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState blockState
    ) {
        return new FrostControllerBlockEntity(type, pos, blockState);
    }
    public FrostControllerBlockEntity(BlockPos pos, BlockState state) {
        super(FROST_CONTROLLER.get(), pos, state);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, FrostControllerBlockEntity entity) {
        if (level.isClientSide()) return;
        entity.tickCounter++;
        if (entity.tickCounter >= 60) {
            if (state.getValue(FrostControllerBlock.HALF) != Vertical3PartHalf.MID || !state.getValue(FrostControllerBlock.ACTIVE)) {
                return;
            }
            entity.tickCounter = 0;
            if(checkRod(level,pos)>0)return;
            int radius = 2;
            List<BlockPos> waterPositions = new ArrayList<>();
            for (int x = -radius - 1; x <= radius + 1; x++) {
                for (int z = -radius - 1; z <= radius + 1; z++) {
                    for (int y = -radius; y <= radius; y++) {
                        BlockPos targetPos = pos.offset(x, y, z);
                        if (level.isOutsideBuildHeight(targetPos)) continue;
                        BlockState targetState = level.getBlockState(targetPos);

                        if (targetState.getBlock() == Blocks.WATER) {
                            waterPositions.add(targetPos);
                        }
                    }
                }
            }
            if (!waterPositions.isEmpty()) {
                BlockPos selectedPos = waterPositions.get(level.getRandom().nextInt(waterPositions.size()));
                level.setBlock(selectedPos, Blocks.ICE.defaultBlockState(), 3);
            }

        }

    }
    public static int checkRod(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        int rod = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    mpos.set(pos).move(i, j, k);
                    if (level.isOutsideBuildHeight(mpos)) continue;
                    BlockState blockState = level.getBlockState(mpos);
                    if (blockState.getBlock() instanceof UraniumRodBlock && blockState.getValue(UraniumRodBlock.HALF) == Vertical3PartHalf.MID) {
                        rod+=1;
                    }
                }
            }
        }
        return rod;
    }
    @Override
    public AABB shape() {
        return AABB.ofSize(getBlockPos().getCenter(), 3, 3, 3);
    }
}