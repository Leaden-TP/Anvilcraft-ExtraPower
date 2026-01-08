package com.extra.power.block.just_block;

import com.extra.power.block.ModBlockEntity;
import com.extra.power.block.blockentity.SolarPanelBlockEntity;
import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import dev.dubhe.anvilcraft.api.power.IPowerComponent;
import dev.dubhe.anvilcraft.block.better.BetterBaseEntityBlock;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SolarPanelBlock extends BetterBaseEntityBlock implements IHammerRemovable {
    private static final VoxelShape BASE = Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 16.0);
    public static BooleanProperty OVERLOAD = IPowerComponent.OVERLOAD;
    public static BooleanProperty ACTIVE = BooleanProperty.create("active");

    public SolarPanelBlock(Properties pProperties) {
        super(pProperties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(OVERLOAD, false)
                .setValue(ACTIVE, false));
    }

    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(SolarPanelBlock::new);
    }

    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState()
                .setValue(OVERLOAD, false)
                .setValue(ACTIVE, false);
    }

    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new SolarPanelBlockEntity(blockPos, blockState);
    }

    @Override
    public boolean canSurvive(BlockState state, LevelReader world, BlockPos pos) {
        BlockPos belowPos = pos.below();
        BlockState belowState = world.getBlockState(belowPos);
        // 检查下方方块是否在Y轴正方向上面是坚固的（完整的）
        return belowState.isFaceSturdy(world, belowPos, Direction.UP);
    }
    @Override
    public void neighborChanged(@NotNull BlockState state, @NotNull Level level, @NotNull BlockPos pos, @NotNull Block sourceBlock,
                                @NotNull BlockPos sourcePos, boolean isMoving) {
        if (!level.isClientSide) {
            BlockPos belowPos = pos.below();
            BlockState belowState = level.getBlockState(belowPos);
            if (!belowState.isFaceSturdy(level, belowPos, Direction.UP)) {
                level.destroyBlock(pos,true);
            }
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntity.SOLAR_PANEL.get(), SolarPanelBlockEntity::tick);
    }

    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OVERLOAD, ACTIVE);
    }

    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BASE;
    }
}

