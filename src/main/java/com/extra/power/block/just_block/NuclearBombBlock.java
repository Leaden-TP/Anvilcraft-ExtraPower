package com.extra.power.block.just_block;

import com.extra.power.block.ModBlock;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import dev.dubhe.anvilcraft.block.better.BetterAnvilBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

import java.util.Properties;

public class NuclearBombBlock extends BetterAnvilBlock implements IHammerRemovable {
    private static final VoxelShape BASE = Shapes.or(Block.box(0, 0, 0, 16.0, 6.0, 16.0)
    ,Block.box(2,6,2,14,7,14),Block.box(4,7,4,12,9,12),Block.box(2,9,2,14,10,14),
            Block.box(0,10,0,16,16,16));
    public NuclearBombBlock(Properties properties) {
        super(properties);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return  BASE;
    }

    protected void falling(FallingBlockEntity entity) {
        entity.setHurtsEntities(8.0F, 80);
    }

    public void onLand(Level level, BlockPos pos, BlockState state, BlockState replaceableState, FallingBlockEntity fallingBlock) {
        if (!fallingBlock.isSilent()) {
            level.levelEvent(1031, pos, 0);
        }
    }
    public void onBrokenAfterFall(Level level, BlockPos pos, FallingBlockEntity fallingBlock) {
        if (!fallingBlock.isSilent()) {
            level.setBlock(pos,ModBlock.MUSHROOM_CLOUD.getDefaultState(),3);
            level.levelEvent(1029, pos, 0);
        }
    }
}
