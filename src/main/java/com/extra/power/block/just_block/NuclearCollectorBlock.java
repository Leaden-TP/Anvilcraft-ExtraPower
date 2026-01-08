package com.extra.power.block.just_block;

import com.extra.power.block.ModBlockEntity;
import com.extra.power.block.blockentity.NuclearCollectorBlockEntity;
import com.extra.power.block.blockentity.SolarPanelBlockEntity;
import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import dev.dubhe.anvilcraft.block.better.BetterBaseEntityBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

import java.util.Optional;


public class NuclearCollectorBlock extends BetterBaseEntityBlock implements IHammerRemovable {
    public static VoxelShape SHAPE = Block.box(0, 0, 0, 16, 4, 16);
    public static BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static BooleanProperty OVERHEATED = BooleanProperty.create("overheated");

    public NuclearCollectorBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(OVERHEATED, false));
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
        builder.add(POWERED).add(OVERHEATED);
    }

    public static void activate(Level level, BlockPos pos, BlockState state,NuclearCollectorBlock block) {
        level.setBlockAndUpdate(pos, state.setValue(POWERED, true));
        block.updateNeighbours(level, pos);
        level.scheduleTick(pos,block , 2);
    }

    @Override
    protected void tick(BlockState state, ServerLevel level, BlockPos pos, RandomSource random) {
        if (!state.getValue(POWERED)) return;
        level.setBlockAndUpdate(pos, state.setValue(POWERED, false));
        this.updateNeighbours(level, pos);
    }

    private void updateNeighbours(Level level, BlockPos pos) {
        level.updateNeighborsAt(pos, this);
        level.updateNeighborsAt(pos.below(), this);
    }

    @Override
    protected void onPlace(BlockState state, Level level, BlockPos pos, BlockState oldState, boolean movedByPiston) {
        if (level.isClientSide() || state.is(oldState.getBlock())) return;
        if (state.getValue(POWERED) && !level.getBlockTicks().hasScheduledTick(pos, this)) {
            level.setBlock(pos, state.setValue(POWERED, false), 18);
        }
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock()) && state.getValue(POWERED)) {
            this.updateNeighbours(level, pos);
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext context) {
        if (NuclearCollectorBlockEntity.isAnotherCollectorNearby(context.getLevel(), context.getClickedPos())) {
            Optional.ofNullable(context.getPlayer()).ifPresent(player -> player.displayClientMessage(
                    Component.translatable("block.anvilcraftextrapower.nuclear_collector.placement_too_close_to_another")
                            .withStyle(ChatFormatting.RED), true));
        }
        return super.getStateForPlacement(context);
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

    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }
}
