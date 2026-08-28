package com.extra.power.block.just_block;

import com.extra.power.init.block.ModBlockEntity;
import com.extra.power.block.blockentity.MagneticDisplayStandBlockEntity;
import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import dev.dubhe.anvilcraft.api.power.IPowerComponent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.ItemInteractionResult;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MagneticDisplayStandBlock extends BaseEntityBlock implements IHammerRemovable {
    public static BooleanProperty OVERLOAD = IPowerComponent.OVERLOAD;
    public static  BooleanProperty RP = BooleanProperty.create("rp");
    private static final VoxelShape BASE = Shapes.or(Block.box(0, 0, 0, 16.0, 3.0, 16.0)
            ,Block.box(0,13,0,16,15,16),Block.box(2,15,2,14,16,14));
    public MagneticDisplayStandBlock(BlockBehaviour.Properties Properties) {
        super(Properties);
        this.registerDefaultState(this.stateDefinition.any()
                .setValue(OVERLOAD, true).setValue(RP, false));
    }
    @Override
    @Nullable
    public BlockState getStateForPlacement(BlockPlaceContext context) {
        return this.defaultBlockState().setValue(OVERLOAD, true).setValue(RP, false);
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
        return createTickerHelper(
                type, ModBlockEntity.MAGNETIC_DISPLAY_STAND.get(),
                level.isClientSide()
                        ? MagneticDisplayStandBlockEntity::clientTick
                        : MagneticDisplayStandBlockEntity::serverTick
        );
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(OVERLOAD).add(RP);
    }

    @Override
    public RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected ItemInteractionResult useItemOn(
            ItemStack stack,
            BlockState state,
            Level level,
            BlockPos pos,
            Player player,
            InteractionHand hand,
            BlockHitResult hit
    ) {
        if (level.isClientSide) {
            return ItemInteractionResult.SUCCESS;
        }

        BlockEntity blockEntity = level.getBlockEntity(pos);
        if (!(blockEntity instanceof MagneticDisplayStandBlockEntity displayStand)) {
            return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        }

        ItemStack handStack = player.getItemInHand(hand);
        ItemStack displayItem = displayStand.getItemstack();
        if (displayStand.isLocked())return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
        if (handStack.isEmpty()) {
            // 空手 - 取出物品
            if (!displayItem.isEmpty()) {
                player.getInventory().placeItemBackInInventory(displayItem);
                displayStand.setItem(0, ItemStack.EMPTY);
                return ItemInteractionResult.SUCCESS;
            }
        } else {
            // 手中有物品 - 放入或替换
            if (displayItem.isEmpty()) {
                // 放入新物品
                ItemStack toPlace = handStack.copyWithCount(1);
                displayStand.setItem(0, toPlace);
                handStack.shrink(1);
            } else {
                // 替换现有物品
                ItemStack toPlace = handStack.copyWithCount(1);
                ItemStack oldItem = displayItem.copy();
                displayStand.setItem(0, toPlace);
                handStack.shrink(1);
                // 将旧物品放入玩家背包或掉落
                player.getInventory().placeItemBackInInventory(oldItem);
            }
            return ItemInteractionResult.SUCCESS;
        }

        return ItemInteractionResult.PASS_TO_DEFAULT_BLOCK_INTERACTION;
    }
    @Override
    public VoxelShape getShape(BlockState state, BlockGetter level, BlockPos pos, CollisionContext context) {
        return  BASE;
    }

    @Override
    protected void onRemove(BlockState state, Level level, BlockPos pos, BlockState newState, boolean movedByPiston) {
        // 在移除方块实体之前获取物品
        if (!state.is(newState.getBlock())) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MagneticDisplayStandBlockEntity displayStand) {
                ItemStack stack = displayStand.getItemstack();
                if (!stack.isEmpty()) {
                    // 掉落物品
                    displayStand.dropItemStack(stack);
                    // 清空展示架中的物品
                    displayStand.setItem(0, ItemStack.EMPTY);
                }
            }
        }

        super.onRemove(state, level, pos, newState, movedByPiston);
    }
    @Override
    protected void neighborChanged(BlockState state, Level level, BlockPos pos, Block sourceBlock, BlockPos sourcePos, boolean movedByPiston) {
        if (level.isClientSide()) return;
        Boolean Rp = level.hasNeighborSignal(pos);
        if (state.getValue(RP) != Rp)level.setBlock(pos,state.setValue(RP, Rp), 3);
    }
    @Override
    public void stepOn(Level level, @NotNull BlockPos pos, @NotNull BlockState state, @NotNull Entity entity) {
        if (entity instanceof ItemEntity itemEntity && !level.isClientSide()) {
            BlockEntity blockEntity = level.getBlockEntity(pos);
            if (blockEntity instanceof MagneticDisplayStandBlockEntity displayStand) {
                ItemStack itemStack = itemEntity.getItem();
                if (itemStack.is(Items.HONEYCOMB) && !displayStand.isLocked()) {
                    displayStand.LockIt();
                    level.levelEvent(null, 3003, pos, 0);
                    itemStack.shrink(1);
                }
            }
            super.stepOn(level, pos, state, entity);
        }
    }
}
