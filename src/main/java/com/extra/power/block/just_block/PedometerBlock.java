package com.extra.power.block.just_block;

import com.extra.power.block.ModBlockEntity;
import com.extra.power.block.blockentity.PedometerBlockEntity;
import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.api.hammer.IHammerChangeable;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.nbt.CompoundTag;
import org.jetbrains.annotations.Nullable;

public class PedometerBlock extends HorizontalDirectionalBlock implements IHammerChangeable, IHammerRemovable {
    public static final IntegerProperty PROGRESS = IntegerProperty.create("progress", 0, 3);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    protected static final VoxelShape SHAPE = Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0);
    public static final MapCodec<PedometerBlock> CODEC = simpleCodec(PedometerBlock::new);

    public PedometerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(
                this.stateDefinition.any()
                        .setValue(FACING, Direction.NORTH)
                        .setValue(PROGRESS, 0)
                        .setValue(POWERED, Boolean.FALSE)
        );
    }

    @Override
    protected VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
//    @Override
//    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
//        return new PedometerBlockEntity(pos, state);
//    }
    @Override
    protected MapCodec<? extends PedometerBlock> codec() {
        return CODEC;
    }

    @Override
    public boolean canConnectRedstone(BlockState state, BlockGetter level, BlockPos pos, @Nullable Direction direction) {
        if (direction == null) return false;
        if (!(state.getBlock() instanceof PedometerBlock)) return false;
        return state.getValue(FACING).getAxis().equals(direction.getAxis());
    }

    @Override
    protected int getDirectSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getSignal(level, pos, direction);
    }

    @Override
    protected int getSignal(BlockState state, BlockGetter level, BlockPos pos, Direction direction) {
        return state.getValue(FACING) == direction && state.getValue(PROGRESS)==3 ? 15 : 0;
    }

    @Override
    public void neighborChanged(        BlockState state,
                                        Level level,
                                        BlockPos pos,
                                        Block block,
                                        @org.jspecify.annotations.Nullable Orientation orientation,
                                        boolean movedByPiston
    ) {
        boolean RP=level.hasNeighborSignal(pos);
        boolean bl = state.getValue(POWERED);
        if(!bl & RP){
            level.setBlockAndUpdate(pos, state.cycle(POWERED));
            level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.7f, 0.8f);
            PutOutResult(level,pos,state);
        }
        if(bl & !RP){
            level.setBlockAndUpdate(pos, state.cycle(POWERED));
            level.playSound(null, pos, SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.BLOCKS, 0.7f, 0.8f);
        }
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean isMoving) {
        super.onPlace(state, level, pos, oldState, isMoving);
        if (oldState.getBlock() == state.getBlock()) return;
    }

    @Override
    protected boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(FACING, context.getHorizontalDirection());
    }

    public static void PutOutResult(Level level, BlockPos pos, BlockState state) {

    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING,PROGRESS,POWERED);
    }

    @Override
    public boolean change(Player player, BlockPos blockPos, Level level, ItemStack anvilHammer) {
        return level.setBlockAndUpdate(blockPos, level.getBlockState(blockPos).cycle(FACING));
    }

    @Override
    public @Nullable Property<?> getChangeableProperty(BlockState blockState) {
        return FACING;
    }

//    @Override
//    public CompoundTag clearData(Level level, BlockPos pos) {
//        BlockEntity be = level.getBlockEntity(pos);
//        if (be == null) return new CompoundTag();
//        // 保存完整数据（不含位置元数据）
//        return be.saveWithoutMetadata(level.registryAccess());
//    }
//
//    @Override
//    public void setData(Level level, BlockPos pos, CompoundTag tag) {
//        level.getBlockEntity(pos, ModBlockEntity.PEDOMETER.get())
//                .ifPresent(be -> be.applyMoveData(level, pos, level.getBlockState(pos), tag));
//    }

}
