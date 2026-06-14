package com.extra.power.block.just_block;

import com.extra.power.block.ModBlockEntity;
import com.extra.power.block.blockentity.MagneticDisplayStandBlockEntity;
import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import dev.dubhe.anvilcraft.api.power.IPowerComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResult;
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
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.transaction.Transaction;
import org.jetbrains.annotations.Nullable;

public class MagneticDisplayStandBlock extends BaseEntityBlock implements IHammerRemovable {
    public static final BooleanProperty OVERLOAD = IPowerComponent.OVERLOAD;
    private static final VoxelShape BASE = Shapes.or(
            Block.box(0, 0, 0, 16.0, 3.0, 16.0),
            Block.box(0, 13, 0, 16, 15, 16),
            Block.box(2, 15, 2, 14, 16, 14)
    );

    public MagneticDisplayStandBlock(Properties properties) {
        super(properties);
        this.registerDefaultState(this.stateDefinition.any().setValue(OVERLOAD, true));
    }

    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(OVERLOAD, true);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(MagneticDisplayStandBlock::new);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos blockPos, BlockState blockState) {
        return new MagneticDisplayStandBlockEntity(blockPos, blockState);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        if (level.isClientSide()) return null;
        return createTickerHelper(
                type, ModBlockEntity.MAGNETIC_DISPLAY_STAND.get(),
                (level1, pos, state1, entity) -> entity.tick(level1, pos, state1, entity)
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OVERLOAD);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected InteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (level.isClientSide()) {
            return InteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof MagneticDisplayStandBlockEntity displayStand)) {
            return InteractionResult.PASS;
        }

        ItemStack handStack = player.getItemInHand(hand);
        ItemStack displayItem = displayStand.getItemstack();

        try (Transaction transaction = Transaction.openRoot()) {
            if (handStack.isEmpty()) {
                // 空手 - 取出物品
                if (!displayItem.isEmpty()) {
                    // 先尝试放入背包
                    if (!player.getInventory().add(displayItem)) {
                        // 背包满了则掉落在地上
                        displayStand.dropItemStack(displayItem);
                    }
                    // 清空展示架槽位
                    displayStand.getItemHandler().set(0, ItemResource.EMPTY, 0);
                    transaction.commit();
                    return InteractionResult.SUCCESS;
                }
            } else {
                // 手中有物品
                if (displayItem.isEmpty()) {
                    // 放入新物品（只放1个）
                    ItemStack toPlace = handStack.copyWithCount(1);
                    displayStand.getItemHandler().set(0, ItemResource.of(toPlace), 1);
                    handStack.shrink(1);
                    transaction.commit();
                    return InteractionResult.SUCCESS;
                } else {
                    // 替换现有物品
                    ItemStack toPlace = handStack.copyWithCount(1);
                    ItemStack oldItem = displayItem.copy();
                    displayStand.getItemHandler().set(0, ItemResource.of(toPlace), 1);
                    handStack.shrink(1);
                    // 将旧物品放入背包，满则掉落
                    if (!player.getInventory().add(oldItem)) {
                        displayStand.dropItemStack(oldItem);
                    }
                    transaction.commit();
                    return InteractionResult.SUCCESS;
                }
            }
        } catch (Exception e) {
            // 事务自动回滚，不处理
        }

        return InteractionResult.PASS;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return BASE;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel level, BlockPos pos, boolean movedByPiston) {
        if (!state.is(state.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MagneticDisplayStandBlockEntity displayStand) {
                displayStand.preRemoveSideEffects(pos, state);
            }
            level.removeBlockEntity(pos);
        }
        super.affectNeighborsAfterRemoval(state, level,pos, movedByPiston);
    }
}