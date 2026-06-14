package com.extra.power.block.just_block;

import com.extra.power.block.ModBlockEntity;
import com.extra.power.block.blockentity.SolarPanelBlockEntity;
import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import dev.dubhe.anvilcraft.api.power.IPowerComponent;
import dev.dubhe.anvilcraft.block.better.BetterBaseEntityBlock;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
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
import net.minecraft.world.level.redstone.Orientation;
import net.minecraft.world.phys.shapes.BooleanOp;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;
import java.util.stream.Stream;

@ParametersAreNonnullByDefault
public class SolarPanelBlock extends BetterBaseEntityBlock implements IHammerRemovable {
    public static final VoxelShape SHAPE =
            Stream.of(
                    Block.box(0, 0, 0, 16, 4, 16),
                    Block.box(4, 4, 4, 12, 6, 12),
                    Block.box(6, 6, 6, 10, 22, 10)
            ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();

    public static final VoxelShape COLLISION_SHAPE =
            Stream.of(
                    Block.box(0, 0, 0, 16, 4, 16),
                    Block.box(4, 4, 4, 12, 6, 12)
            ).reduce((v1, v2) -> Shapes.join(v1, v2, BooleanOp.OR)).get();
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
        Level level = context.getLevel();
        BlockPos pos = context.getClickedPos();

        // 检查3x3x3范围内是否有其他太阳能板
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (x == 0 && y == 0 && z == 0) continue;

                    BlockPos checkPos = pos.offset(x, y, z);
                    BlockState checkState = level.getBlockState(checkPos);
                    if (checkState.getBlock() instanceof SolarPanelBlock) {
                        if (!level.isClientSide()) {
                            context.getPlayer().sendOverlayMessage(
                                    Component.translatable("message.anvilcraftextrapower.solar_panel_too_close")
                                            .withStyle(ChatFormatting.RED)
                            );
                        }
                        return null; // 阻止放置
                    }
                }
            }
        }

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
        return belowState.isFaceSturdy(world, belowPos, Direction.UP);
    }

    @Override
    public void neighborChanged(        BlockState state,
                                        Level level,
                                        BlockPos pos,
                                        Block block,
                                        @org.jspecify.annotations.Nullable Orientation orientation,
                                        boolean movedByPiston
    ) {
        if (!level.isClientSide()) {
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
    @Override
    public VoxelShape getShape(
            BlockState state,
            BlockGetter level,
            BlockPos pos,
            CollisionContext context) {
        return SHAPE;
    }
    protected VoxelShape getCollisionShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return COLLISION_SHAPE;
    }
}

