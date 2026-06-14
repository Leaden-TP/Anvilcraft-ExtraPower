package com.extra.power.block.just_block;

import com.extra.power.block.blockentity.UraniumRodBlockEntity;
import dev.dubhe.anvilcraft.api.IHasMultiBlock;
import dev.dubhe.anvilcraft.api.block.IEmberBlock;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import dev.dubhe.anvilcraft.block.multipart.SimpleMultiPartBlock;
import dev.dubhe.anvilcraft.block.state.Vertical3PartHalf;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
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
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.Property;
import net.minecraft.world.level.pathfinder.PathComputationType;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class UraniumRodBlock extends SimpleMultiPartBlock<Vertical3PartHalf>
        implements IHammerRemovable,IHasMultiBlock , IEmberBlock, EntityBlock {
    private final double waterAbsorptionChance;
    public static final VoxelShape URANIUM_ROD_BOTTOM =
            Shapes.or(Block.box(1, 0, 1, 15, 3, 15), Block.box(4, 3, 4, 12, 16, 12));
    public static final VoxelShape URANIUM_ROD_MID = Shapes.or(Block.box(4, 0, 4, 12, 16, 12));
    public static final VoxelShape URANIUM_ROD_TOP = Shapes.or(Block.box(1, 13, 1, 15, 16, 15), Block.box(4, 0, 4, 12, 13, 12));
    public static final EnumProperty<Vertical3PartHalf> HALF = EnumProperty.create("half", Vertical3PartHalf.class);
    public static final IntegerProperty ACTIVE = IntegerProperty.create("active", 0, 5);
    public static BooleanProperty UNDER_CONTROL = BooleanProperty.create("under_control");
    public UraniumRodBlock(Properties properties,double waterAbsorptionChance) {
        super(properties);
        this.waterAbsorptionChance = waterAbsorptionChance;
        this.registerDefaultState(this.stateDefinition
                .any()
                .setValue(HALF, Vertical3PartHalf.BOTTOM)
                .setValue(ACTIVE, 0)
                .setValue(UNDER_CONTROL, false));
    }

    @Override
    public Property<Vertical3PartHalf> getPart() {
        return UraniumRodBlock.HALF;
    }

    @Override
    public Vertical3PartHalf[] getParts() {
        return Vertical3PartHalf.values();
    }

    @Override
    protected void createBlockStateDefinition(@NotNull StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(HALF).add(ACTIVE).add(UNDER_CONTROL);
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
        if (state.getValue(HALF) == Vertical3PartHalf.BOTTOM) return URANIUM_ROD_BOTTOM;
        if (state.getValue(HALF) == Vertical3PartHalf.MID) return URANIUM_ROD_MID;
        if (state.getValue(HALF) == Vertical3PartHalf.TOP) return URANIUM_ROD_TOP;
        return super.getShape(state, level, pos, context);
    }

    @Override
    public BlockState placedState(Vertical3PartHalf part, BlockState state) {
        return super.placedState(part, state).setValue(ACTIVE, 0);
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
        return this.defaultBlockState().setValue(HALF, Vertical3PartHalf.BOTTOM).setValue(ACTIVE, 0);
    }
    @Override
    public void onPlace(@NotNull Level level, BlockPos pos, BlockState state) {
        if (level.isClientSide()) return;
        if (state.getValue(HALF) == Vertical3PartHalf.MID && state.getValue(ACTIVE)!=0) {
            level.setBlock(pos.above(), state.setValue(ACTIVE, state.getValue(ACTIVE)), 11);
            level.setBlock(pos.below(), state.setValue(ACTIVE, state.getValue(ACTIVE)), 11);
        }
    }
    @Override
    public void onRemove(@NotNull Level level, BlockPos pos, BlockState state) {
    }

    @Override
    public boolean isRandomlyTicking(BlockState state) {
        return true;
    }

    @Override
    public void randomTick(
            BlockState state,
            ServerLevel level,
            BlockPos pos,
            RandomSource random
    ) {
        if (random.nextDouble() <= waterAbsorptionChance) {
            tryAbsorbWater(level, pos);
        }
    }
    @Getter
    @Setter
    private BlockState checkBlockState;

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new UraniumRodBlockEntity(blockPos, blockState);
    }
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(
            Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return (level1, pos, state1, entity) -> {
            if (entity instanceof UraniumRodBlockEntity entity1) entity1.tick(level, pos, state, entity1);
        };
    }
    @Override
    public boolean isMainPart(BlockState state) {
        return state==this.defaultBlockState().setValue(HALF, Vertical3PartHalf.MID);
    }
}
