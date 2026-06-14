package com.extra.power.block.just_block;


import com.extra.power.block.ModBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;

public class LightBlock extends Block {
    public static final IntegerProperty BRIGHTNESS = IntegerProperty.create("brightness", 0, 5);

    public LightBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(BRIGHTNESS, 0));
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty(); // 返回空形状表示没有碰撞体积
    }
    @Override
    public VoxelShape getVisualShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Shapes.empty();
    }
    @Override
    protected boolean propagatesSkylightDown(BlockState state) {
        return true;
    }
    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return true; // 允许被任意方块替换
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(BRIGHTNESS);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (!level.isClientSide()) {
            this.lightBlock(state, level, pos);
        }
    }
    @Override
    public void neighborChanged(        BlockState state,
                                        Level level,
                                        BlockPos pos,
                                        Block block1,
                                        @org.jspecify.annotations.Nullable Orientation orientation,
                                        boolean movedByPiston
    ) {
        boolean flag = false;
        if (!level.isClientSide()) {
            for (Direction direction : Direction.values()) {
                BlockState block = level.getBlockState(pos.relative(direction));
                if (block.is(ModBlock.LIGHT)) {
                    if (block.getValue(BRIGHTNESS) < state.getValue(BRIGHTNESS)) {
                        flag = true;
                    }
                }
                if (block.is(ModBlock.BURNING_MAGNESIUM_BLOCK)) {
                    flag = true;
                }
            }
            if (!flag) {
                level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
            } else {
                this.lightBlock(state, level, pos);
            }
        }
    }

    public void lightBlock(BlockState state, Level level, BlockPos pos) {
        if (level.isClientSide()) { return; }
        if (state.getValue(BRIGHTNESS) == 5) { return; }
        for (Direction direction : Direction.values()) {
            BlockState block = level.getBlockState(pos.relative(direction));
            if (block.is(Blocks.AIR)) {
                level.setBlock(pos.relative(direction),
                        ModBlock.LIGHT.get().defaultBlockState().setValue(BRIGHTNESS, state.getValue(BRIGHTNESS) + 1), 11);
            }
            if (block.is(ModBlock.LIGHT)) {
                if (block.getValue(BRIGHTNESS) > state.getValue(BRIGHTNESS) + 1)
                    level.setBlock(pos.relative(direction),
                            ModBlock.LIGHT.get().defaultBlockState().setValue(BRIGHTNESS, state.getValue(BRIGHTNESS) + 1), 11);
            }
        }
    }
}
