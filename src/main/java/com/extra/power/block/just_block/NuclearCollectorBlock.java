package com.extra.power.block.just_block;

import com.extra.power.init.block.ModBlock;
import com.extra.power.init.block.ModBlockEntity;
import com.extra.power.block.blockentity.NuclearCollectorBlockEntity;
import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import dev.dubhe.anvilcraft.block.better.BetterBaseEntityBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelAccessor;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;

import static com.extra.power.block.blockentity.NuclearCollectorBlockEntity.isAnotherCollectorNearby;
import static com.extra.power.util.NuclearCollectorFunction.checkRod;

public class NuclearCollectorBlock extends BetterBaseEntityBlock implements IHammerRemovable, SimpleWaterloggedBlock { // ① 实现接口
    public static VoxelShape SHAPE = Block.box(0, 0, 0, 16, 4, 16);
    public static BooleanProperty OVERHEATED = BooleanProperty.create("overheated");
    // ② 声明含水属性
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    public NuclearCollectorBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(OVERHEATED, false)
                .setValue(WATERLOGGED, false)); // ③ 默认含水为 false
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(NuclearCollectorBlock::new);
    }

    @Override
    public BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new NuclearCollectorBlockEntity(blockPos, blockState);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OVERHEATED, WATERLOGGED); // ④ 注册含水属性
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(OVERHEATED)) return;
        level.setBlockAndUpdate(pos, state.setValue(OVERHEATED, false));
        this.updateNeighbours(level, pos);
    }

    private void updateNeighbours(Level level, BlockPos pos) {
        level.updateNeighborsAt(pos, this);
        level.updateNeighborsAt(pos.below(), this);
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock()) && state.getValue(OVERHEATED)) {
            this.updateNeighbours(level, pos);
        }
        if(!isAnotherCollectorNearby(level, pos)) checkRod(level, pos, new NuclearCollectorBlockEntity(pos, state), false);
        if (state.getValue(OVERHEATED))
            level.setBlockAndUpdate(pos, ModBlock.MUSHROOM_CLOUD.get().defaultBlockState());
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        if (isAnotherCollectorNearby(context.getLevel(), context.getClickedPos())) {
            Optional.ofNullable(context.getPlayer()).ifPresent(player -> player.displayClientMessage(
                    Component.translatable("block.anvilcraftextrapower.nuclear_collector.placement_too_close_to_another")
                            .withStyle(ChatFormatting.RED), true));
        }
        // ⑤ 放置时检测位置是否有水，并设置含水状态
        FluidState fluidState = context.getLevel().getFluidState(context.getClickedPos());
        boolean waterlogged = fluidState.getType() == Fluids.WATER;
        return super.getStateForPlacement(context)
                .setValue(WATERLOGGED, waterlogged);
    }

    // ⑥ 重写 getFluidState
    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    // ⑦ 重写 updateShape，处理含水状态更新
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

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntity.NUCLEAR_COLLECTOR.get(), NuclearCollectorBlockEntity::tick);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    @Override
    public int getSignal(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        return state.getValue(OVERHEATED) ? 15 : 0;
    }
}