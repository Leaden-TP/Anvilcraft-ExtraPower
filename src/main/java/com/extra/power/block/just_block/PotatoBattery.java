package com.extra.power.block.just_block;

import dev.dubhe.anvilcraft.block.heatable.HeatableBlock;
import dev.dubhe.anvilcraft.block.heatable.NormalBlock;
import dev.dubhe.anvilcraft.init.block.ModBlockTags;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseFireBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;


public class PotatoBattery extends Block {
    private static final VoxelShape BASE = Block.box(4.5, 0.0, 6, 11.5, 4, 10.0);

    public PotatoBattery(BlockBehaviour.Properties pProperties) {
        super(pProperties);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = world.getBlockState(belowPos);
        return belowState.isFaceSturdy(world, belowPos, Direction.UP);
    }

    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block sourceBlock,
                              @NotNull BlockPos sourcePos, boolean isMoving) {
        if (!level.isClientSide) {
            BlockPos belowPos = pos.below();
            BlockState belowState = level.getBlockState(belowPos);

            if (!belowState.isFaceSturdy(level, belowPos, Direction.UP)) {
                level.destroyBlock(pos, true);
            }
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide) {
            BlockPos belowPos = pos.below();
            BlockState belowState = level.getBlockState(belowPos);

            if (!belowState.isFaceSturdy(level, belowPos, Direction.UP)) {
                level.destroyBlock(pos, true);
            }
            else if (belowState.getBlock() instanceof BaseFireBlock
                    || belowState.is(Blocks.LAVA)
                    || ((belowState.is(ModBlockTags.REDHOT_BLOCKS) || belowState.is(ModBlockTags.INCANDESCENT_BLOCKS)
                    || belowState.is(ModBlockTags.OVERHEATED_BLOCKS)) && !(belowState.getBlock() instanceof NormalBlock))){
                level.destroyBlock(pos, false);
                ItemEntity bakedPotato = new ItemEntity(level, pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5,
                        new ItemStack(Items.BAKED_POTATO));
                level.addFreshEntity(bakedPotato);
            }
        }
    }

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BASE;
    }
}
