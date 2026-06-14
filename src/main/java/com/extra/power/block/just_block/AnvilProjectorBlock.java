package com.extra.power.block.just_block;

import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import dev.dubhe.anvilcraft.block.multipart.SimpleMultiPartBlock;
import dev.dubhe.anvilcraft.entity.FallingSpectralBlockEntity;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

public class AnvilProjectorBlock extends Block implements IHammerRemovable {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final BooleanProperty COLD_DOWN = BooleanProperty.create("cold_down");;
    public AnvilProjectorBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any().setValue(FACING, Direction.NORTH)
                        .setValue(COLD_DOWN, false)
        );
    }
    @Override
    protected VoxelShape getVisualShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }
    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }
    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(FACING, context.getHorizontalDirection())
                .setValue(COLD_DOWN, false);
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING,COLD_DOWN);
    }
    public void getHit(Level level,BlockState state, BlockPos pos,float falling_D) {
        if (!level.isClientSide()) {
            BlockPos pos2 = pos.relative(level.getBlockState(pos).getValue(FACING).getOpposite(), 1);
            if (falling_D <= 1) return;
            level.setBlock(pos, state.setValue(COLD_DOWN, true), 3);
            for (int i = 1; i <= Math.min(falling_D, 16); i++) {
                BlockPos pos1 = pos.relative(level.getBlockState(pos).getValue(FACING), i);
                if (!level.getBlockState(pos1).canBeReplaced()) break;
                if (level.getBlockState(pos2).canBeReplaced() || level.getBlockState(pos2).getBlock() instanceof SimpleMultiPartBlock) {
                    FallingSpectralBlockEntity.fall(
                            level,
                            pos1,
                            ModBlocks.SPECTRAL_ANVIL.getDefaultState(),
                            false,
                            true
                    );
                } else
                    FallingSpectralBlockEntity.fall(
                            level,
                            pos1,
                            level.getBlockState(pos2),
                            false,
                            true
                    );
            }
            level.scheduleTick(pos, this, 20);
        }
    }

    @Override
    public void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        level.setBlock(pos, state.setValue(COLD_DOWN,false), 3);
    }
    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
}
