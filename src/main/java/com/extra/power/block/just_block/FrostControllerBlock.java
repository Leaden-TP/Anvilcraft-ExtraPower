package com.extra.power.block.just_block;

import com.extra.power.block.blockentity.FrostControllerBlockEntity;
import dev.dubhe.anvilcraft.api.IHasMultiBlock;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import dev.dubhe.anvilcraft.block.multipart.SimpleMultiPartBlock;
import dev.dubhe.anvilcraft.block.state.Vertical3PartHalf;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.phys.shapes.Shapes.empty;

public class FrostControllerBlock extends SimpleMultiPartBlock<Vertical3PartHalf>
        implements IHammerRemovable, IHasMultiBlock, EntityBlock, SimpleWaterloggedBlock { // ① 实现接口

    public static final VoxelShape BOTTOM = Shapes.or(
            Block.box(1, 0, 1, 15, 4, 15), Block.box(4, 4, 4, 12, 12, 12));
    public static final VoxelShape TOP = Shapes.or(
            Block.box(1, 12, 1, 15, 16, 15), Block.box(4, 4, 4, 12, 12, 12));
    public static final VoxelShape MID = Shapes.or(
            Block.box(4, 4, 4, 12, 12, 12));
    public static final EnumProperty<Vertical3PartHalf> HALF = EnumProperty.create("half", Vertical3PartHalf.class);
    public static final BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static final BooleanProperty RP = BooleanProperty.create("rp");
    // ② 声明含水属性
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public FrostControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition
                .any()
                .setValue(HALF, Vertical3PartHalf.BOTTOM)
                .setValue(ACTIVE, false)
                .setValue(RP, false)
                .setValue(WATERLOGGED, false)); // ③ 默认含水为 false
    }

    @Override
    public Property<Vertical3PartHalf> getPart() {
        return FrostControllerBlock.HALF;
    }

    @Override
    public Vertical3PartHalf[] getParts() {
        return Vertical3PartHalf.values();
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF, ACTIVE, RP, WATERLOGGED); // ④ 注册含水属性
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected boolean isPathfindable(BlockState state, PathComputationType pathComputationType) {
        return false;
    }

    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context) {
        if (state.getValue(HALF) == Vertical3PartHalf.BOTTOM) return BOTTOM;
        if (state.getValue(HALF) == Vertical3PartHalf.MID) return MID;
        if (state.getValue(HALF) == Vertical3PartHalf.TOP) return TOP;
        return super.getShape(state, level, pos, context);
    }

    @Override
    public BlockState placedState(Vertical3PartHalf part, BlockState state) {
        // ⑤ 其他部分放置时继承含水状态（但实际可能由 updateShape 自动修正）
        return super.placedState(part, state).setValue(ACTIVE, true);
    }

    @Override
    public BlockState playerWillDestroy(
            Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide) return state;
        onRemove(level, pos, state);
        super.playerWillDestroy(level, pos, state, player);
        return state;
    }

    @Override
    @Nullable
    public BlockState getPlacementState(BlockPlaceContext context) {
        // ⑥ 放置时检测位置是否有水，并设置含水状态
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        boolean waterlogged = fluidState.getType() == Fluids.WATER;
        return this.defaultBlockState()
                .setValue(HALF, Vertical3PartHalf.BOTTOM)
                .setValue(ACTIVE, false)
                .setValue(WATERLOGGED, waterlogged);
    }

    @Override
    public void onPlace(@NotNull Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
    }

    @Override
    public void onRemove(@NotNull Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide) return;
    }

    @Override
    public void neighborChanged(
            BlockState state,
            Level level,
            BlockPos pos,
            Block neighborBlock,
            BlockPos neighborPos,
            boolean movedByPiston) {
        if (level.isClientSide) return;
        Boolean Rp = level.hasNeighborSignal(pos);
        if (state.getValue(HALF) == Vertical3PartHalf.BOTTOM) {
            if (state.getValue(RP) != Rp) level.setBlock(pos, state.setValue(RP, Rp), 3);
        } else if (state.getValue(HALF) == Vertical3PartHalf.TOP) {
            if (state.getValue(RP) != Rp) level.setBlock(pos, state.setValue(RP, Rp), 3);
        } else if (state.getValue(HALF) == Vertical3PartHalf.MID) {
            if (!(level.getBlockState(pos.above()).getBlock() instanceof FrostControllerBlock
                    && level.getBlockState(pos.below()).getBlock() instanceof FrostControllerBlock))
                return;
            Boolean U_D = level.getBlockState(pos.above()).getValue(RP)
                    || level.getBlockState(pos.below()).getValue(RP);
            if (state.getValue(ACTIVE) != !U_D)
                level.setBlock(pos, state.setValue(ACTIVE, !U_D), 3);
        }
    }

    // ⑦ 重写 getFluidState
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    // ⑧ 重写 updateShape，处理含水状态更新
    @Override
    public BlockState updateShape(BlockState state, Direction direction, BlockState neighborState,
                                  LevelAccessor level, BlockPos pos, BlockPos neighborPos) {
        if (state.getValue(WATERLOGGED)) {
            level.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(level));
        }
        // 检测当前位置是否有水，更新含水属性
        boolean waterPresent = level.getFluidState(pos).getType() == Fluids.WATER;
        if (state.getValue(WATERLOGGED) != waterPresent) {
            return state.setValue(WATERLOGGED, waterPresent);
        }
        return super.updateShape(state, direction, neighborState, level, pos, neighborPos);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        if (blockState.getValue(HALF) == Vertical3PartHalf.MID) {
            return new FrostControllerBlockEntity(blockPos, blockState);
        }
        return null;
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return false;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        return (level1, pos, state1, entity) -> {
            if (!(entity instanceof FrostControllerBlockEntity controller)) return;
            if (level1.isClientSide()) {
                FrostControllerBlockEntity.clientTick(level1, pos, state1, controller);
            } else {
                FrostControllerBlockEntity.serverTick(level1, pos, state1, controller);
            }
        };
    }
}