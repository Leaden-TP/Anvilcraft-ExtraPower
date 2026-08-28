package com.extra.power.block.blockentity;

import com.extra.power.api.entity.IScrollAdjustable;
import com.extra.power.init.block.ModBlockEntity;
import dev.dubhe.anvilcraft.api.IHasDisplayItem;
import dev.dubhe.anvilcraft.api.itemhandler.FilteredItemStackHandler;
import dev.dubhe.anvilcraft.api.itemhandler.IItemHandlerHolder;
import dev.dubhe.anvilcraft.api.power.IPowerConsumer;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.enchantment.EnchantmentHelper;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.neoforged.neoforge.items.IItemHandler;
import org.jetbrains.annotations.Nullable;

import static com.extra.power.block.just_block.MagneticDisplayStandBlock.RP;

public class MagneticDisplayStandBlockEntity extends BlockEntity
        implements IPowerConsumer, IHasDisplayItem, IScrollAdjustable, IItemHandlerHolder {

    private static final float MIN_HEIGHT_OFFSET = 0.0f;
    private static final float MAX_HEIGHT_OFFSET = 6.0f;
    private static final float CLIENT_LERP_FACTOR = 0.18f;
    private static final float ROTATION_SPEED = 2.0f;
    private static final float POSITION_EPSILON = 0.005f;
    private static final float ROTATION_EPSILON = 0.25f;
    private static final float SPEED_EPSILON = 0.005f;
    private static final float POSE_START_HEIGHT_RATIO = 0.2f;
    private static final float LOWERING_START_ROTATION_OFFSET = 65.0f;
    private static final float CUBE_PROGRESS_PER_TICK = 0.1f;
    private static final int POWER = 8;

    private float userHeightOffset = 0.5f;
    private boolean locked = false;
    private float clientYOffset;
    private float previousClientYOffset;
    private float clientZOffset;
    private float previousClientZOffset;
    private float clientRotationX;
    private float previousClientRotationX;
    private float clientRotationY;
    private float previousClientRotationY;
    private float clientRotationSpeed;
    private float clientCubeProgress;
    private float previousClientCubeProgress;
    private boolean clientCubeAnimationInitialized;
    private ClientAnimationPhase clientAnimationPhase = ClientAnimationPhase.RESTING;

    @Getter
    private PowerGrid grid;

    @Getter
    private ItemStack displayItemStack = ItemStack.EMPTY;


    private final FilteredItemStackHandler itemHandler = new FilteredItemStackHandler(1) {

        @Override
        public ItemStack insertItem(int slot, ItemStack stack, boolean simulate) {
            if (slot != 0) return stack;
            // 只允许在槽位为空时插入一个物品
            if (!getStackInSlot(0).isEmpty()) return stack;
            // 检查物品有效性
            if (!isItemValid(slot, stack)) return stack;
            // 只插入一个
            ItemStack one = stack.copyWithCount(1);
            ItemStack result = super.insertItem(slot, one, simulate);
            if (result.isEmpty()) {
                // 插入成功，返回剩余（原数量-1）
                if (stack.getCount() > 1) {
                    return stack.copyWithCount(stack.getCount() - 1);
                } else {
                    return ItemStack.EMPTY;
                }
            } else {
                return stack;
            }
        }

        @Override
        public boolean isItemValid(int slot, ItemStack stack) {
            return slot == 0 && EnchantmentHelper.canStoreEnchantments(stack);
        }

        @Override
        public ItemStack extractItem(int slot, int amount, boolean simulate) {
            return slot == 0 ? super.extractItem(0, amount, simulate) : ItemStack.EMPTY;
        }

        @Override
        protected void onContentsChanged(int slot) {
            super.onContentsChanged(slot);
            if (level != null && !level.isClientSide()) {
                updateDisplayItemStack();
                syncState();
            }
        }
    };

    // ---------- 构造方法 ----------
    public static MagneticDisplayStandBlockEntity createBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new MagneticDisplayStandBlockEntity(type, pos, blockState);
    }

    public MagneticDisplayStandBlockEntity(BlockPos pos, BlockState blockState) {
        this(ModBlockEntity.MAGNETIC_DISPLAY_STAND.get(), pos, blockState);
    }

    public MagneticDisplayStandBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    // ---------- 静态 tick 方法 ----------
    public static void serverTick(Level level, BlockPos pos, BlockState state, MagneticDisplayStandBlockEntity entity) {
        entity.flushState(level, pos);
    }

    public static void clientTick(Level level, BlockPos pos, BlockState state, MagneticDisplayStandBlockEntity entity) {
        float cubeTarget = isCubeEnergized(state) ? 1.0f : 0.0f;
        if (entity.clientCubeAnimationInitialized) {
            entity.previousClientCubeProgress = entity.clientCubeProgress;
            entity.clientCubeProgress = moveTowards(entity.clientCubeProgress, cubeTarget, CUBE_PROGRESS_PER_TICK);
        } else {
            entity.clientCubeProgress = cubeTarget;
            entity.previousClientCubeProgress = cubeTarget;
            entity.clientCubeAnimationInitialized = true;
        }

        entity.previousClientYOffset = entity.clientYOffset;
        entity.previousClientZOffset = entity.clientZOffset;
        entity.previousClientRotationX = entity.clientRotationX;
        entity.previousClientRotationY = entity.clientRotationY;

        int redstonePower = level.getBestNeighborSignal(pos);
        boolean running = !state.getValue(OVERLOAD)
                && redstonePower < 15
                && !entity.displayItemStack.isEmpty();
        boolean blockItem = entity.displayItemStack.getItem() instanceof BlockItem;
        float activeY = entity.userHeightOffset * (15 - redstonePower) / 15.0f;

        if (running) {
            if (entity.clientAnimationPhase == ClientAnimationPhase.RESTING
                    || entity.clientAnimationPhase == ClientAnimationPhase.RETURNING_TO_DEFAULT
                    || entity.clientAnimationPhase == ClientAnimationPhase.LOWERING) {
                entity.clientAnimationPhase = ClientAnimationPhase.RAISING;
            }
        } else if (entity.clientAnimationPhase == ClientAnimationPhase.RAISING
                || entity.clientAnimationPhase == ClientAnimationPhase.ACTIVE) {
            entity.clientAnimationPhase = ClientAnimationPhase.RETURNING_TO_DEFAULT;
        }

        float targetY = switch (entity.clientAnimationPhase) {
            case RAISING, ACTIVE -> activeY;
            case RETURNING_TO_DEFAULT -> entity.clientYOffset;
            case LOWERING, RESTING -> 0.0f;
        };
        boolean activePose = entity.clientAnimationPhase == ClientAnimationPhase.ACTIVE;
        float targetZ = activePose && !blockItem ? 0.125f : 0.0f;
        float targetRotationX = activePose && !blockItem ? -90.0f : 0.0f;
        float targetRotationSpeed = activePose ? ROTATION_SPEED : 0.0f;

        entity.clientYOffset = smoothTowards(entity.clientYOffset, targetY, POSITION_EPSILON);
        entity.clientZOffset = smoothTowards(entity.clientZOffset, targetZ, POSITION_EPSILON);
        entity.clientRotationX = smoothTowards(entity.clientRotationX, targetRotationX, ROTATION_EPSILON);
        entity.clientRotationSpeed = smoothTowards(entity.clientRotationSpeed, targetRotationSpeed, SPEED_EPSILON);

        if (activePose) {
            entity.clientRotationY += entity.clientRotationSpeed;
        } else {
            entity.clientRotationY = smoothAngleTowards(entity.clientRotationY, 0.0f);
        }
        entity.wrapClientRotation();

        if (entity.clientAnimationPhase == ClientAnimationPhase.RAISING && entity.hasRaisedEnough(activeY)) {
            entity.clientAnimationPhase = ClientAnimationPhase.ACTIVE;
        } else if (entity.clientAnimationPhase == ClientAnimationPhase.RETURNING_TO_DEFAULT
                && Math.abs(entity.clientRotationX) <= LOWERING_START_ROTATION_OFFSET) {
            entity.clientAnimationPhase = ClientAnimationPhase.LOWERING;
        } else if (entity.clientAnimationPhase == ClientAnimationPhase.LOWERING
                && near(entity.clientYOffset, 0.0f, POSITION_EPSILON)
                && entity.hasDefaultPose()) {
            entity.resetClientAnimation();
        }
    }

    // ---------- 锁定与交互 ----------
    public boolean isLocked() {
        return this.locked;
    }

    public void LockIt() {
        if (this.locked) return;
        this.locked = true;
        syncState();
    }

    @Override
    public void onScrollAdjust(int steps) {
        if (this.locked) return;
        float newOffset = Mth.clamp(userHeightOffset + steps * 0.25f, MIN_HEIGHT_OFFSET, MAX_HEIGHT_OFFSET);
        if (Math.abs(newOffset - userHeightOffset) <= 1e-5) return;

        userHeightOffset = newOffset;
        if (level != null && !level.isClientSide()) {
            syncState();
            level.playSound(null, worldPosition, SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.RECORDS);
        }
    }

    // ---------- 物品操作 ----------
    public ItemStack getItemstack() {
        return itemHandler.getStackInSlot(0);
    }

    private ItemStack getDisplayItemStackForRender() {
        return itemHandler.getStackInSlot(0);
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
    }

    // 兼容旧版 use 方法可能调用的 setItem
    public void setItem(int slot, ItemStack stack) {
        if (slot == 0) {
            itemHandler.setStackInSlot(0, stack);
            if (level != null && !level.isClientSide()) {
                updateDisplayItemStack();
                syncState();
            }
        }
    }

    // ---------- 生命周期与 NBT ----------
    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putBoolean("locked", locked);
        tag.put("Inventory", itemHandler.serializeNBT(registries));
        tag.putFloat("UserHeightOffset", userHeightOffset);
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        locked = tag.getBoolean("locked");
        itemHandler.deserializeNBT(registries, tag.getCompound("Inventory"));
        userHeightOffset = tag.getFloat("UserHeightOffset");
        updateDisplayItemStack();
    }

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        return saveWithoutMetadata(registries);
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
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

    // ---------- 能源接口 ----------
    @Override
    public int getInputPower() {
        return POWER;
    }

    @Override
    public @Nullable Level getCurrentLevel() {
        return level;
    }

    @Override
    public BlockPos getPos() {
        return this.getBlockPos();
    }

    @Override
    public void setGrid(@Nullable PowerGrid grid) {
        this.grid = grid;
    }

    // ---------- 客户端动画取值 ----------
    public float getClientYOffset(float partialTick) {
        return Mth.lerp(partialTick, previousClientYOffset, clientYOffset);
    }

    public float getClientZOffset(float partialTick) {
        return Mth.lerp(partialTick, previousClientZOffset, clientZOffset);
    }

    public float getClientRotationX(float partialTick) {
        return Mth.lerp(partialTick, previousClientRotationX, clientRotationX);
    }

    public float getClientRotationY(float partialTick) {
        return Mth.lerp(partialTick, previousClientRotationY, clientRotationY);
    }

    public float getClientCubeProgress(float partialTick) {
        if (!clientCubeAnimationInitialized) {
            return isCubeEnergized(getBlockState()) ? 1.0f : 0.0f;
        }
        float progress = Mth.clamp(
                Mth.lerp(partialTick, previousClientCubeProgress, clientCubeProgress),
                0.0f, 1.0f
        );
        return progress * progress * (3.0f - 2.0f * progress);
    }

    public static boolean isCubeEnergized(BlockState state) {
        return !state.getValue(OVERLOAD) && !state.getValue(RP);
    }

    // ---------- 内部辅助 ----------
    private void syncState() {
        setChanged();
        if (level == null || level.isClientSide()) return;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }

    private void wrapClientRotation() {
        float turns = (float) Math.floor(clientRotationY / 360.0f);
        if (turns == 0.0f) return;
        float offset = turns * 360.0f;
        clientRotationY -= offset;
        previousClientRotationY -= offset;
    }

    private boolean hasDefaultPose() {
        return near(clientZOffset, 0.0f, POSITION_EPSILON)
                && near(clientRotationX, 0.0f, ROTATION_EPSILON)
                && Math.abs(Mth.wrapDegrees(clientRotationY)) <= ROTATION_EPSILON
                && near(clientRotationSpeed, 0.0f, SPEED_EPSILON);
    }

    private boolean hasRaisedEnough(float targetY) {
        if (targetY <= POSITION_EPSILON) return true;
        return clientYOffset >= targetY * POSE_START_HEIGHT_RATIO;
    }

    private void resetClientAnimation() {
        clientYOffset = 0.0f;
        clientZOffset = 0.0f;
        clientRotationX = 0.0f;
        clientRotationY = 0.0f;
        clientRotationSpeed = 0.0f;
        clientAnimationPhase = ClientAnimationPhase.RESTING;
    }

    private static float smoothTowards(float current, float target, float epsilon) {
        float next = Mth.lerp(CLIENT_LERP_FACTOR, current, target);
        return near(next, target, epsilon) ? target : next;
    }

    private static float smoothAngleTowards(float current, float target) {
        float delta = Mth.wrapDegrees(target - current);
        if (Math.abs(delta) <= ROTATION_EPSILON) return current + delta;
        return current + delta * CLIENT_LERP_FACTOR;
    }

    private static float moveTowards(float current, float target, float maxDelta) {
        if (current < target) return Math.min(current + maxDelta, target);
        if (current > target) return Math.max(current - maxDelta, target);
        return target;
    }

    private static boolean near(float value, float target, float epsilon) {
        return Math.abs(value - target) <= epsilon;
    }

    private enum ClientAnimationPhase {
        RESTING,
        RAISING,
        ACTIVE,
        RETURNING_TO_DEFAULT,
        LOWERING
    }

    // IItemHandlerHolder 必须实现的方法
    @Override
    public IItemHandler getItemHandler() {
        return itemHandler;
    }
}