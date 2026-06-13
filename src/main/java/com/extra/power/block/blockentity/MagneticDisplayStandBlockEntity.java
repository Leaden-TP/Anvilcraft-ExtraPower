package com.extra.power.block.blockentity;

import com.extra.power.api.entity.IScrollAdjustable;
import com.extra.power.block.ModBlockEntity;
import com.extra.power.network.UpdateAnimationStatePacket;
import dev.dubhe.anvilcraft.api.IHasDisplayItem;
import dev.dubhe.anvilcraft.api.itemhandler.FilteredItemStackHandler;
import dev.dubhe.anvilcraft.api.itemhandler.IItemHandlerHolder;
import dev.dubhe.anvilcraft.api.power.IPowerConsumer;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import dev.dubhe.anvilcraft.network.UpdateDisplayItemPacket;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;

public class MagneticDisplayStandBlockEntity extends BlockEntity implements IPowerConsumer, IItemHandlerHolder, IHasDisplayItem, IScrollAdjustable {
    private float userHeightOffset = 0.0f;   // 玩家调节的高度偏移
    private static final float MIN_HEIGHT_OFFSET = -0.5f;
    private static final float MAX_HEIGHT_OFFSET = 6.0f;
    private static final int POWER = 8;
    private static final int SYNC_INTERVAL = 40; // 2秒（20 tick/秒 * 2秒）
    private int action_t = 0;
    private int syncTimer = 0;
    @Getter
    private List<Double> action_state = new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0)); // 前三个为xyz 后三个为对应的旋转
    private boolean loading = false;
    @Getter
    private PowerGrid grid;
    @Getter
    private ItemStack displayItemStack = ItemStack.EMPTY;
    private ItemStack lastSyncedStack = ItemStack.EMPTY;

    @Getter
    private final FilteredItemStackHandler itemHandler = new FilteredItemStackHandler(1) {

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot == 0 && itemHandler.getStackInSlot(0).isEmpty()) {
                ItemStack original = stack.copy();
                original.shrink(1);
                if (original.isEmpty()) {
                    return super.insertItem(slot, stack.copyWithCount(1), simulate);
                } else {
                    ItemStack left = super.insertItem(slot, stack.copyWithCount(1), simulate);
                    return stack.copyWithCount(stack.getCount() - 1 + left.getCount());
                }
            } else {
                return stack;
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return EnchantmentHelper.canStoreEnchantments(stack);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 0 ? super.extractItem(0, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if (level != null && !level.isClientSide) {
                setChanged();
                // 物品变化时立即同步
                syncDisplayItemImmediately();
                level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
            }
        }
    };

    public static MagneticDisplayStandBlockEntity createBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState blockState
    ) {
        return new MagneticDisplayStandBlockEntity(type, pos, blockState);
    }

    public MagneticDisplayStandBlockEntity(BlockPos pos, BlockState blockState) {
        this(ModBlockEntity.MAGNETIC_DISPLAY_STAND.get(), pos, blockState);
    }

    public MagneticDisplayStandBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public void tick(Level level, BlockPos pos, BlockState state, MagneticDisplayStandBlockEntity entity) {
        if (getDisplayItemStack().isEmpty()){entity.action_state=new ArrayList<>(Arrays.asList(0.0, 0.0, 0.0, 0.0, 0.0, 0.0));}
        List<Float> target_state = Arrays.asList(0.0f, 0.0f, 0.0f, 0.0f, 0.0f, 0.0f);
        if (entity.loading && getDisplayItemStack().getItem() instanceof BlockItem) target_state = Arrays.asList(0.0f, 0.5f+entity.userHeightOffset, 0.0f, 0.0f, (float)level.getGameTime()%360, 0.0f);
        else if (entity.loading) target_state = Arrays.asList(0.0f, 0.5f+entity.userHeightOffset, 0.125f, -90.0f, (float)level.getGameTime()%360, 0.0f);
        for (int i = 0; i < entity.action_state.size(); i++) {
            double current = entity.action_state.get(i);
            double target = target_state.get(i).doubleValue();
            double distance = Math.abs(current - target);

            if (distance <= 0.03) {
                entity.action_state.set(i, target);
                continue;
            }

            // 使用更平滑的插值
          double step = Math.min(distance, Math.max(0.01, distance / 10));
            if (current < target) {
                entity.action_state.set(i, current + step);
            } else {
                entity.action_state.set(i, current - step);
            }
        }

            if (!level.isClientSide && entity.action_t % 3 == 0) {
            if (!state.getValue(OVERLOAD) != entity.loading)
            entity.loading = !state.getValue(OVERLOAD);
            action_t = 0;}

        // 定期同步检查（每2秒）
        if (!level.isClientSide) {
            this.flushState(level, pos);
            entity.action_t++;
            entity.syncTimer++;
            if (entity.syncTimer >= SYNC_INTERVAL) {
                entity.syncTimer = 0;
                entity.syncDisplayItemPeriodically();
            }
            entity.syncAnimationState();
        }
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (itemHandler != null) {
            tag.put("Inventory", itemHandler.serializeNBT(registries));
        }
        // 修复：安全保存最后同步的物品状态
        CompoundTag lastSyncedTag = new CompoundTag();
        if (lastSyncedStack != null && !lastSyncedStack.isEmpty()) {
            lastSyncedStack.save(registries, lastSyncedTag);
        }
        tag.put("LastSyncedStack", lastSyncedTag);
        if (action_state != null) {
            CompoundTag animationTag = new CompoundTag();
            for (int i = 0; i < action_state.size(); i++) {
                animationTag.putDouble("ActionState_" + i, action_state.get(i));
            }
            tag.put("AnimationState", animationTag);
        }
        tag.putFloat("UserHeightOffset", userHeightOffset);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        // 加载最后同步的物品状态
        if (tag.contains("LastSyncedStack")) {
            lastSyncedStack = ItemStack.parse(registries, tag.getCompound("LastSyncedStack")).orElse(ItemStack.EMPTY);
        }
        updateDisplayItemStack();

        // 加载动画状态
        if (tag.contains("AnimationState")) {
            CompoundTag animationTag = tag.getCompound("AnimationState");
            for (int i = 0; i < action_state.size(); i++) {
                if (animationTag.contains("ActionState_" + i)) {
                    action_state.set(i, animationTag.getDouble("ActionState_" + i));
                }
            }
        }
        userHeightOffset = tag.getFloat("UserHeightOffset");
        // 限制范围
        userHeightOffset = (float) Math.clamp(userHeightOffset, MIN_HEIGHT_OFFSET, MAX_HEIGHT_OFFSET);
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

    @Nullable
    public Level getCurrentLevel() {
        return level;
    }

    public BlockPos getPos() {
        return this.getBlockPos();
    }

    @Override
    public int getInputPower() {
        return MagneticDisplayStandBlockEntity.POWER;
    }

    @Override
    public void setGrid(@Nullable PowerGrid grid) {
        this.grid = grid;
    }

    /**
     * 同步动画状态到客户端
     */
    private void syncAnimationState() {
        if (level == null || level.isClientSide) return;

        PacketDistributor.sendToPlayersTrackingChunk(
            (ServerLevel) level,
            level.getChunk(getBlockPos()).getPos(),
            new UpdateAnimationStatePacket(new ArrayList<>(action_state), getBlockPos())
        );
    }

    /**
     * 更新动画状态（从网络包调用）
     */
    public void updateActionState(List<Double> newState) {
        if (level != null && level.isClientSide) {
            // 只在客户端更新
            for (int i = 0; i < Math.min(action_state.size(), newState.size()); i++) {
                action_state.set(i, newState.get(i));
            }
        }
    }

    /**
     * 立即同步显示物品（物品变化时调用）
     */
    private void syncDisplayItemImmediately() {
        if (level == null || level.isClientSide) return;

        ItemStack currentStack = getDisplayItemStackForRender();

        // 检查物品是否真的发生了变化
        if (!ItemStack.matches(currentStack, lastSyncedStack)) {
            displayItemStack = currentStack.copy();
            lastSyncedStack = currentStack.copy();

            // 发送同步包
            PacketDistributor.sendToPlayersTrackingChunk(
                    (ServerLevel) level,
                    level.getChunk(getBlockPos()).getPos(),
                    new UpdateDisplayItemPacket(displayItemStack, getPos())
            );

            setChanged();
        }
    }

    private void syncDisplayItemPeriodically() {
        if (level == null || level.isClientSide) return;

        ItemStack currentStack = getDisplayItemStackForRender();

        // 即使物品没有变化，也定期同步以确保客户端状态一致
        if (!ItemStack.matches(currentStack, lastSyncedStack)) {
            // 如果物品变化了，使用立即同步逻辑
            syncDisplayItemImmediately();
        } else {
            // 物品没有变化，但仍然发送同步包以确保客户端状态一致
            displayItemStack = currentStack.copy();

            PacketDistributor.sendToPlayersTrackingChunk(
                    (ServerLevel) level,
                    level.getChunk(getBlockPos()).getPos(),
                    new UpdateDisplayItemPacket(displayItemStack, getPos())
            );
        }
    }
    @Override
    public void onScrollAdjust(String parameterId, float delta, Level level, BlockPos pos) {
        if ("height_offset".equals(parameterId)) {
            float newOffset = userHeightOffset + delta*0.25f;
            newOffset = (float) Math.clamp(newOffset, MIN_HEIGHT_OFFSET, MAX_HEIGHT_OFFSET);
            if (Math.abs(newOffset - userHeightOffset) > 1e-5) {
                userHeightOffset = newOffset;
                setChanged();
                if (level != null && !level.isClientSide) {
                    level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), 3);
                    level.playSound(null, pos, SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.RECORDS);
                }
            }
        }
        // 未来可扩展其他参数
    }
    /**
     * 更新本地显示物品（不触发网络同步）
     */
    private void updateDisplayItemStack() {
        ItemStack newDisplayStack = getDisplayItemStackForRender();
        if (!ItemStack.matches(displayItemStack, newDisplayStack)) {
            displayItemStack = newDisplayStack.copy();
        }
    }

    private ItemStack getDisplayItemStackForRender() {
        // 只有一个槽位，直接返回槽位0的物品
        return itemHandler.getStackInSlot(0);
    }

    @Override
    public void updateDisplayItem(ItemStack stack) {
        this.displayItemStack = stack;
        // 客户端接收到同步后，更新最后同步状态
        if (level != null && level.isClientSide) {
            this.lastSyncedStack = stack.copy();
        }
    }

    // 以下方法用于兼容 Block 中的 use 方法
    public ItemStack getItemstack() {
        return itemHandler.getStackInSlot(0);
    }

    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            itemHandler.setStackInSlot(0, stack);
            // 手动设置物品时也触发立即同步
            if (level != null && !level.isClientSide) {
                syncDisplayItemImmediately();
            }
            setChanged();
        }
    }
}
