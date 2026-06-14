package com.extra.power.block.blockentity;

import com.extra.power.api.entity.IScrollAdjustable;
import com.extra.power.block.ModBlockEntity;
import com.extra.power.network.UpdateAnimationStatePacket;
import dev.dubhe.anvilcraft.api.IHasDisplayItem;
import dev.dubhe.anvilcraft.api.itemhandler.FilteredItemStackHandler;
import dev.dubhe.anvilcraft.api.itemhandler.IItemResourceHandlerHolder;
import dev.dubhe.anvilcraft.api.power.IPowerConsumer;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import dev.dubhe.anvilcraft.network.UpdateDisplayItemPacket;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.network.PacketDistributor;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;
import net.neoforged.neoforge.transfer.item.ItemStacksResourceHandler;
import net.neoforged.neoforge.transfer.transaction.TransactionContext;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MagneticDisplayStandBlockEntity extends BlockEntity
        implements IPowerConsumer, IHasDisplayItem, IScrollAdjustable, IItemResourceHandlerHolder {

    private float userHeightOffset = 0.0f;
    private static final float MIN_HEIGHT_OFFSET = -0.5f;
    private static final float MAX_HEIGHT_OFFSET = 6.0f;
    private static final int POWER = 8;
    private static final int SYNC_INTERVAL = 40;
    private int action_t = 0;
    private int syncTimer = 0;
    private boolean loading = false;
    @Getter
    private List<Double> action_state = new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
    @Getter
    private PowerGrid grid;

    @Getter
    private ItemStack displayItemStack = ItemStack.EMPTY;
    private ItemStack lastSyncedStack = ItemStack.EMPTY;

    // 单格物品容器，只允许存放一个物品
    @Getter
    private final ItemStacksResourceHandler itemHandler = new ItemStacksResourceHandler(1) {

        @Override
        public int insert(int slot, ItemResource resource, int amount, TransactionContext transaction) {
            if (slot != 0) return 0;
            if (!isValid(slot, resource)) return 0;
            // 只允许插入一个物品，且槽位必须为空
            if (!getResource(0).isEmpty()) return 0;
            int toInsert = Math.min(amount, 1);
            return super.insert(slot, resource, toInsert, transaction);
        }

        @Override
        public int extract(int slot, ItemResource resource, int amount, TransactionContext transaction) {
            if (slot != 0) return 0;
            return super.extract(slot, resource, amount, transaction);
        }

        @Override
        public boolean isValid(int slot, ItemResource resource) {
            return slot == 0 && EnchantmentHelper.canStoreEnchantments(resource.toStack());
        }

        @Override
        protected void onContentsChanged(int slot, ItemStack previousContents) {
            super.onContentsChanged(slot, previousContents);
            if (level != null && !level.isClientSide()) {
                setChanged();
                updateDisplayItemStack();       // 更新显示用物品
                syncDisplayItemImmediately();   // 立即同步到客户端
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
            }
        }
    };

    public static MagneticDisplayStandBlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new MagneticDisplayStandBlockEntity(type, pos, blockState);
    }

    public MagneticDisplayStandBlockEntity(BlockPos pos, BlockState blockState) {
        this(ModBlockEntity.MAGNETIC_DISPLAY_STAND.get(), pos, blockState);
    }

    public MagneticDisplayStandBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    // ---------- Tick ----------
    public void tick(Level level, BlockPos pos, BlockState state, MagneticDisplayStandBlockEntity entity) {
        if (getDisplayItemStack().isEmpty()) {
            entity.action_state = new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));
        }
        List<Float> target_state = Arrays.asList(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        if (entity.loading && getDisplayItemStack().getItem() instanceof BlockItem) {
            target_state = Arrays.asList(0.0f, 0.5f + entity.userHeightOffset, 0.0f, 0.0f, (float) level.getGameTime() % 360, 0.0f);
        } else if (entity.loading) {
            target_state = Arrays.asList(0.0f, 0.5f + entity.userHeightOffset, 0.125f, -90.0f, (float) level.getGameTime() % 360, 0.0f);
        }
        for (int i = 0; i < entity.action_state.size(); i++) {
            double current = entity.action_state.get(i);
            double target = target_state.get(i).doubleValue();
            double distance = Math.abs(current - target);

            if (distance <= 0.03) {
                entity.action_state.set(i, target);
                continue;
            }

            double step = Math.clamp(distance / 10, 0.01, distance);
            if (current < target) {
                entity.action_state.set(i, current + step);
            } else {
                entity.action_state.set(i, current - step);
            }
        }

        // 修复动画同步：loading 始终等于未过载状态 (假设 OVERLOAD 为 true 时过载，需要停止动画)
        if (!level.isClientSide() && entity.action_t % 3 == 0) {
            entity.loading = !state.getValue(OVERLOAD);
            entity.action_t = 0;
        }

        if (!level.isClientSide()) {
            this.flushState(level, pos);
            entity.action_t++;
            entity.syncTimer++;
            if (entity.syncTimer >= SYNC_INTERVAL) {
                entity.syncTimer = 0;
                entity.syncDisplayItemPeriodically();
            }
        }
        entity.syncAnimationState();
    }

    // ---------- 网络同步（服务端 → 客户端） ----------
    private void syncAnimationState() {
        if (level == null || level.isClientSide()) return;
        PacketDistributor.sendToPlayersTrackingChunk(
                (ServerLevel) level,
                level.getChunk(getBlockPos()).getPos(),
                new UpdateAnimationStatePacket(new ArrayList<>(this.action_state), getBlockPos())
        );
    }

    public void updateActionState(List<Double> newState) {
        if (level != null && level.isClientSide()) {
            for (int i = 0; i < Math.min(action_state.size(), newState.size()); i++) {
                action_state.set(i, newState.get(i));
            }
        }
    }

    private void syncDisplayItemImmediately() {
        if (level == null || level.isClientSide()) return;

        ItemStack currentStack = getDisplayItemStackForRender();

        if (!ItemStack.matches(currentStack, lastSyncedStack)) {
            displayItemStack = currentStack.copy();
            lastSyncedStack = currentStack.copy();

            PacketDistributor.sendToPlayersTrackingChunk(
                    (ServerLevel) level,
                    level.getChunk(getBlockPos()).getPos(),
                    new UpdateDisplayItemPacket(displayItemStack, getPos())
            );

            setChanged();
        }
    }

    private void syncDisplayItemPeriodically() {
        if (level == null || level.isClientSide()) return;

        ItemStack currentStack = getDisplayItemStackForRender();

        if (!ItemStack.matches(currentStack, lastSyncedStack)) {
            syncDisplayItemImmediately();
        } else {
            displayItemStack = currentStack.copy();
            PacketDistributor.sendToPlayersTrackingChunk(
                    (ServerLevel) level,
                    level.getChunk(getBlockPos()).getPos(),
                    new UpdateDisplayItemPacket(displayItemStack, getPos())
            );
        }
    }

    // ---------- 物品操作 ----------
    public ItemStack getItemstack() {
        return itemHandler.getResource(0).toStack(itemHandler.getAmountAsInt(0));
    }

    private ItemStack getDisplayItemStackForRender() {
        if (itemHandler.getResource(0).isEmpty()) return ItemStack.EMPTY;
        return itemHandler.getResource(0).toStack(itemHandler.getAmountAsInt(0));
    }

    private void updateDisplayItemStack() {
        ItemStack newDisplayStack = getDisplayItemStackForRender();
        if (!ItemStack.matches(displayItemStack, newDisplayStack)) {
            displayItemStack = newDisplayStack.copy();
        }
    }

    @Override
    public void updateDisplayItem(ItemStack stack) {
        this.displayItemStack = stack == null ? ItemStack.EMPTY : stack.copy();
        if (level != null && level.isClientSide()) {
            this.lastSyncedStack = this.displayItemStack.copy();
        }
    }

    // ---------- 生命周期 ----------
    @Override
    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putBoolean("load", loading);
        itemHandler.serialize(output.child("Inventory"));
        output.putFloat("UserHeightOffset", userHeightOffset);
        var animationComp = output.child("AnimationState");
        for (int i = 0; i < action_state.size(); i++) {
            animationComp.putDouble("ActionState_" + i, action_state.get(i));
        }
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        loading = input.getBooleanOr("load", false);   // 修复：实际赋值
        itemHandler.deserialize(input.childOrEmpty("Inventory"));
        userHeightOffset = input.getFloatOr("UserHeightOffset", 0.0f);
        userHeightOffset = (float) Math.clamp(userHeightOffset, MIN_HEIGHT_OFFSET, MAX_HEIGHT_OFFSET);

        var animationComp = input.childOrEmpty("AnimationState");
        for (int i = 0; i < action_state.size(); i++) {
            action_state.set(i, animationComp.getDoubleOr("ActionState_" + i, 0.0));
        }
        updateDisplayItemStack();
        // 服务端加载后确保立即同步物品，防止退出重进后物品丢失
        if (level != null && !level.isClientSide()) {
            syncDisplayItemImmediately();
        }
    }

    @Override
    public void preRemoveSideEffects(BlockPos pos, BlockState state) {
        ItemStack stack = getItemstack();
        if (!stack.isEmpty()) {
            dropItemStack(stack);
            itemHandler.set(0, ItemResource.EMPTY, 0);
        }
    }

    public void dropItemStack(ItemStack stack) {
        if (!stack.isEmpty() && level != null) {
            net.minecraft.world.entity.item.ItemEntity itemEntity = new net.minecraft.world.entity.item.ItemEntity(
                    level,
                    worldPosition.getX() + 0.5,
                    worldPosition.getY() + 1.0,
                    worldPosition.getZ() + 0.5,
                    stack
            );
            itemEntity.setDefaultPickUpDelay();
            level.addFreshEntity(itemEntity);
        }
    }

    // ---------- 辅助方法 ----------

    @Nullable
    public Level getCurrentLevel() {
        return level;
    }

    public BlockPos getPos() {
        return this.getBlockPos();
    }

    @Override
    public int getInputPower() {
        return POWER;
    }

    @Override
    public void setGrid(@Nullable PowerGrid grid) {
        this.grid = grid;
    }

    @Override
    public void onScrollAdjust(String parameterId, float delta, Level level, BlockPos pos) {
        if ("height_offset".equals(parameterId)) {
            float newOffset = userHeightOffset + delta * 0.25f;
            newOffset = (float) Math.clamp(newOffset, MIN_HEIGHT_OFFSET, MAX_HEIGHT_OFFSET);
            if (Math.abs(newOffset - userHeightOffset) > 1e-5) {
                userHeightOffset = newOffset;
                setChanged();
                if (level != null && !level.isClientSide()) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
                    level.playSound(null, pos, SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.RECORDS);
                }
            }
        }
    }
}