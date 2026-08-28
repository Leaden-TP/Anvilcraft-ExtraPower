package com.extra.power.block.just_block;

import com.extra.power.block.blockentity.RedstoneLinkBlockEntity;
import com.extra.power.init.block.ModBlockEntity;
import com.extra.power.item.RedstoneLinkItem;
import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
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
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

/**
 * 红石中继器（仿机械动力 Redstone Link）。
 * <p>碰撞箱 16*16*3；方块状态：是否红石激活（powered）、颜色模式（color: r/y/b）、是否接收模式（received）。
 * <p>右键记录手中物品为信号标记（空手重置），潜行右键切换接收模式，滚轮切换颜色（红色全局跨纬度 / 黄色 128 格 / 蓝色绑定）。
 */
public class RedstoneLinkBlock extends BaseEntityBlock implements IHammerRemovable {
    public static final EnumProperty<RedstoneLinkColor> COLOR = EnumProperty.create("color", RedstoneLinkColor.class);
    public static final BooleanProperty POWERED = BlockStateProperties.POWERED;
    public static final BooleanProperty RECEIVED = BooleanProperty.create("received");

    /** 黄色模式收发范围 */
    public static final int YELLOW_RANGE = 128;

    private static final VoxelShape SHAPE = Block.box(0, 0, 0, 16, 4, 16);

    public RedstoneLinkBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(COLOR, RedstoneLinkColor.YELLOW)
                .setValue(POWERED, false)
                .setValue(RECEIVED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(RedstoneLinkBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new RedstoneLinkBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntity.REDSTONE_LINK.get(), RedstoneLinkBlockEntity::tick);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(COLOR, POWERED, RECEIVED);
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState();
    }

    @Override
    public void setPlacedBy(Level level, BlockPos pos, BlockState state, @Nullable LivingEntity placer, ItemStack stack) {
        super.setPlacedBy(level, pos, state, placer, stack);
        if (level.isClientSide()) return;
        ResourceKey<Level> boundDim = RedstoneLinkItem.getBoundDimension(stack);
        BlockPos boundPos = RedstoneLinkItem.getBoundPos(stack);
        if (boundDim != null && boundPos != null
                && level.getBlockEntity(pos) instanceof RedstoneLinkBlockEntity be) {
            // 携带标记位置的 redstone link 放置后自动进入蓝色模式（颜色锁定）
            be.setBound(boundDim, boundPos);
            level.setBlock(pos, state.setValue(COLOR, RedstoneLinkColor.BLUE), 3);
        }
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return SHAPE;
    }

    // ---------- 红石输出 ----------

    @Override
    public boolean isSignalSource(BlockState state) {
        return true;
    }

    @Override
    public int getSignal(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        if (!state.getValue(RECEIVED)) return 0;
        if (level.getBlockEntity(pos) instanceof RedstoneLinkBlockEntity be) {
            return be.getOutputSignal();
        }
        return 0;
    }

    @Override
    public int getDirectSignal(BlockState state, @NotNull BlockGetter level, @NotNull BlockPos pos, @NotNull Direction direction) {
        return getSignal(state, level, pos, direction);
    }

    // ---------- 交互 ----------

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hitResult
    ) {
        if (level.isClientSide()) {
            // 客户端镜像：手持 redstone link 且目标不是接收模式时，允许继续放置方块
            if (!player.isShiftKeyDown()
                    && stack.getItem() instanceof RedstoneLinkItem
                    && !state.getValue(RECEIVED)) {
                return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
            }
            return ItemInteractionResult.SUCCESS;
        }
        return handleInteraction(state, level, pos, player, stack);
    }

    @Override
    protected InteractionResult useWithoutItem(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            BlockHitResult hitResult
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }
        ItemInteractionResult result = handleInteraction(state, level, pos, player, ItemStack.EMPTY);
        return result == ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION
                ? InteractionResult.PASS
                : result.result();
    }

    private ItemInteractionResult handleInteraction(
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            ItemStack stack
    ) {
        if (!(level.getBlockEntity(pos) instanceof RedstoneLinkBlockEntity be)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 潜行右键：切换接收模式
        if (player.isShiftKeyDown()) {
            boolean receive = !state.getValue(RECEIVED);
            level.setBlock(pos, state.setValue(RECEIVED, receive), 3);
            be.onModeChanged();
            player.displayClientMessage(Component.translatable(receive
                    ? "message.anvilcraftextrapower.redstone_link.mode_receive"
                    : "message.anvilcraftextrapower.redstone_link.mode_send"), true);
            return ItemInteractionResult.SUCCESS;
        }

        // 手持 redstone link 物品：目标为接收模式时记录其位置（模仿铁砧工艺定日镜的磁盘记录）
        if (stack.getItem() instanceof RedstoneLinkItem) {
            if (state.getValue(RECEIVED)) {
                RedstoneLinkItem.writeBoundData(stack, level.dimension(), pos);
                player.displayClientMessage(Component.translatable(
                        "message.anvilcraftextrapower.redstone_link.bind_success", pos.toShortString()), true);
                return ItemInteractionResult.SUCCESS;
            }
            // 非接收模式：允许正常放置
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        // 空手：重置信号标记
        if (stack.isEmpty()) {
            if (!be.getMarker().isEmpty()) {
                be.setMarker(ItemStack.EMPTY);
                player.displayClientMessage(Component.translatable(
                        "message.anvilcraftextrapower.redstone_link.marker_reset"), true);
            }
            return ItemInteractionResult.SUCCESS;
        }

        // 手持其他物品：记录为信号标记（蓝色模式不支持物品标记）
        if (state.getValue(COLOR) == RedstoneLinkColor.BLUE) {
            player.displayClientMessage(Component.translatable(
                    "message.anvilcraftextrapower.redstone_link.marker_unsupported_blue"), true);
            return ItemInteractionResult.SUCCESS;
        }
        be.setMarker(stack.copyWithCount(1));
        player.displayClientMessage(Component.translatable(
                "message.anvilcraftextrapower.redstone_link.marker_set", stack.getHoverName()), true);
        return ItemInteractionResult.SUCCESS;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        super.onRemove(state, level, pos, newState, movedByPiston);
        if (!state.is(newState.getBlock()) && state.getValue(RECEIVED)) {
            level.updateNeighborsAt(pos, this);
            level.updateNeighborsAt(pos.below(), this);
        }
    }
}
