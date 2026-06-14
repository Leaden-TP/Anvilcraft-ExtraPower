package com.extra.power.block.just_block;

import com.extra.power.block.blockentity.FrostControllerBlockEntity;
import dev.dubhe.anvilcraft.api.IHasMultiBlock;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import dev.dubhe.anvilcraft.block.multipart.SimpleMultiPartBlock;
import dev.dubhe.anvilcraft.block.state.Vertical3PartHalf;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import static net.minecraft.world.phys.shapes.Shapes.empty;

public class FrostControllerBlock extends SimpleMultiPartBlock<Vertical3PartHalf>
        implements IHammerRemovable, IHasMultiBlock,  EntityBlock {
    public static final VoxelShape BOTTOM = Shapes.or(
            Block.box(1, 0, 1, 15, 4, 15), Block.box(4, 4, 4, 12, 12, 12));
    public static final VoxelShape TOP = Shapes.or(
            Block.box(1, 12, 1, 15, 16, 15), Block.box(4, 4, 4, 12, 12, 12));
    public static final VoxelShape MID = Shapes.or(
            Block.box(4, 4, 4, 12, 12, 12));
    public static final EnumProperty<Vertical3PartHalf> HALF = EnumProperty.create("half", Vertical3PartHalf.class);
    public static  BooleanProperty ACTIVE = BooleanProperty.create("active");
    public static  BooleanProperty RP = BooleanProperty.create("rp");
    public FrostControllerBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition
                .any()
                .setValue(HALF, Vertical3PartHalf.BOTTOM)
                .setValue(ACTIVE, false)
                .setValue(RP, false));
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
        builder.add(HALF).add(ACTIVE).add(RP);
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
        return super.placedState(part, state).setValue(ACTIVE, true);
    }
    @Override
    public BlockState playerWillDestroy(
            Level level, BlockPos pos, BlockState state, Player player) {
        if (level.isClientSide()) return state;
        onRemove(level, pos, state);
        super.playerWillDestroy(level, pos, state, player);
        return state;
    }
    @Override
    @Nullable
    public BlockState getPlacementState(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(HALF, Vertical3PartHalf.BOTTOM).setValue(ACTIVE, false);
    }
    @Override
    public void onPlace(@NotNull Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;
    }
    @Override
    public void onRemove(@NotNull Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;
    }
    @Override
    public void neighborChanged(        BlockState state,
                                        Level level,
                                        BlockPos pos,
                                        Block block,
                                        @org.jspecify.annotations.Nullable Orientation orientation,
                                        boolean movedByPiston
    ) {
        if (level.isClientSide()) return;
        Boolean Rp = level.hasNeighborSignal(pos);
            if (state.getValue(HALF) == Vertical3PartHalf.BOTTOM) {
                if (state.getValue(RP) != Rp)level.setBlock(pos,state.setValue(RP, Rp), 3);
            }
            else if (state.getValue(HALF) == Vertical3PartHalf.TOP) {
                if (state.getValue(RP) != Rp) level.setBlock(pos, state.setValue(RP, Rp), 3);
            }
            else if (state.getValue(HALF) == Vertical3PartHalf.MID) {
                if(!(level.getBlockState(pos.above()).getBlock() instanceof FrostControllerBlock
                        && level.getBlockState(pos.below()).getBlock() instanceof FrostControllerBlock))return;
                Boolean U_D=level.getBlockState(pos.above()).getValue(RP)
                        ||level.getBlockState(pos.below()).getValue(RP);
                    if (state.getValue(ACTIVE) != !U_D)level.setBlock(pos,state.setValue(ACTIVE, !U_D), 3);
            }
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
        if (level.isClientSide()) return null;
        return (level1, pos, state1, entity) -> {
            if (entity instanceof FrostControllerBlockEntity entity1) entity1.tick(level, pos, state, entity1);
        };
    }
}
