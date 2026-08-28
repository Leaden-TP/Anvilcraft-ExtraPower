package com.extra.power.block.blockentity;

import com.extra.power.api.entity.IScrollAdjustable;
import com.extra.power.block.just_block.RedstoneLinkBlock;
import com.extra.power.block.just_block.RedstoneLinkColor;
import com.extra.power.init.block.ModBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

public class RedstoneLinkBlockEntity extends BlockEntity implements IScrollAdjustable {
    // 服务端与客户端共用的数据（客户端通过更新包同步）
    private ItemStack marker = ItemStack.EMPTY;
    @Nullable
    private ResourceKey<Level> boundDimension;
    @Nullable
    private BlockPos boundPos;
    /**
     * 发送端：当前输入红石信号；接收端：当前收到的信号。客户端通过更新包获得同名字段。
     */
    private int signal = 0;

    public RedstoneLinkBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public RedstoneLinkBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.REDSTONE_LINK.get(), pos, state);
    }

    public static RedstoneLinkBlockEntity createBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState blockState
    ) {
        return new RedstoneLinkBlockEntity(type, pos, blockState);
    }

    // ---------- 状态查询 ----------

    public RedstoneLinkColor getColor() {
        return getBlockState().getValue(RedstoneLinkBlock.COLOR);
    }

    public boolean isReceiveMode() {
        return getBlockState().getValue(RedstoneLinkBlock.RECEIVED);
    }

    public boolean isSender() {
        return !isReceiveMode();
    }

    /** 当前信号强度（发送端=输入，接收端=收到） */
    public int getSignal() {
        return signal;
    }

    /** 发送端对外查询用：输入信号 */
    public int getInputSignal() {
        return signal;
    }

    /** 接收端对外输出用：收到的信号 */
    public int getOutputSignal() {
        return signal;
    }

    public ItemStack getMarker() {
        return marker;
    }

    @Nullable
    public ResourceKey<Level> getBoundDimension() {
        return boundDimension;
    }

    @Nullable
    public BlockPos getBoundPos() {
        return boundPos;
    }

    // ---------- 交互 ----------

    /** 设置信号标记物品（服务端调用），蓝色模式不支持物品标记 */
    public void setMarker(ItemStack stack) {
        this.marker = stack == null ? ItemStack.EMPTY : stack.copy();
        setChanged();
        syncState();
    }

    /** 记录绑定位置（服务端调用），用于蓝色模式 */
    public void setBound(ResourceKey<Level> dimension, BlockPos pos) {
        this.boundDimension = dimension;
        this.boundPos = pos;
        setChanged();
        syncState();
    }

    /** 接收/发送模式切换后调用：更新网络注册并立即重算 */
    public void onModeChanged() {
        updateNetworkRegistration();
        if (level == null || level.isClientSide()) return;
        this.signal = isReceiveMode() ? computeReceivedSignal() : level.getBestNeighborSignal(worldPosition);
        boolean powered = this.signal > 0;
        BlockState state = level.getBlockState(worldPosition);
        if (state.getValue(RedstoneLinkBlock.POWERED) != powered) {
            level.setBlock(worldPosition, state.setValue(RedstoneLinkBlock.POWERED, powered), 3);
        }
        setChanged();
        syncState();
        level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
        level.updateNeighborsAt(worldPosition.below(), getBlockState().getBlock());
    }

    @Override
    public void onScrollAdjust(int steps) {
        if (level == null || level.isClientSide()) return;
        BlockState state = level.getBlockState(worldPosition);
        RedstoneLinkColor current = state.getValue(RedstoneLinkBlock.COLOR);
        // 蓝色模式锁定，无法滚轮调节
        if (current == RedstoneLinkColor.BLUE) return;
        RedstoneLinkColor next = current == RedstoneLinkColor.RED
                ? RedstoneLinkColor.YELLOW
                : RedstoneLinkColor.RED;
        if (next == current) return;
        level.setBlock(worldPosition, state.setValue(RedstoneLinkBlock.COLOR, next), 3);
        setChanged();
        syncState();
        level.playSound(null, worldPosition, SoundEvents.NOTE_BLOCK_HAT.value(), SoundSource.BLOCKS);
    }

    // ---------- 网络注册（仅服务端发送端） ----------

    public void updateNetworkRegistration() {
        if (level == null || level.isClientSide()) return;
        if (isSender()) {
            RedstoneLinkNetwork.addSender(this);
        } else {
            RedstoneLinkNetwork.removeSender(this);
        }
    }

    @Override
    public void onLoad() {
        super.onLoad();
        updateNetworkRegistration();
    }

    @Override
    public void setRemoved() {
        RedstoneLinkNetwork.removeSender(this);
        super.setRemoved();
    }

    @Override
    public void onChunkUnloaded() {
        RedstoneLinkNetwork.removeSender(this);
        super.onChunkUnloaded();
    }

    // ---------- 信号计算 ----------

    /**
     * 接收端扫描网络中的发送端，返回匹配的最大信号强度。
     * <p>匹配规则：颜色相同（红色全局跨纬度 / 黄色同纬度 128 格）且信号标记物品相同；
     * 蓝色发送端则只与绑定位置相同的接收端联络。
     */
    public int computeReceivedSignal() {
        if (level == null) return 0;
        ResourceKey<Level> dim = level.dimension();
        int max = 0;
        for (RedstoneLinkBlockEntity sender : RedstoneLinkNetwork.allSenders()) {
            if (matches(sender, dim)) {
                max = Math.max(max, sender.getInputSignal());
            }
        }
        return max;
    }

    private boolean matches(RedstoneLinkBlockEntity sender, ResourceKey<Level> dim) {
        if (!sender.isSender()) return false;
        RedstoneLinkColor myColor = getColor();
        RedstoneLinkColor senderColor = sender.getColor();
        // 蓝色发送端只联络标记位置的接收端（不论接收端自身颜色）
        if (senderColor == RedstoneLinkColor.BLUE) {
            return sender.getBoundPos() != null && sender.getBoundPos().equals(worldPosition)
                    && sender.getBoundDimension() != null && sender.getBoundDimension().equals(dim);
        }
        if (myColor != senderColor) return false;
        if (!ItemStack.isSameItem(marker, sender.getMarker())) return false;
        return switch (myColor) {
            case RED -> true;
            case YELLOW -> sender.getLevel() != null
                    && sender.getLevel().dimension().equals(dim)
                    && sender.getBlockPos().distSqr(worldPosition)
                    <= RedstoneLinkBlock.YELLOW_RANGE * RedstoneLinkBlock.YELLOW_RANGE;
            default -> false;
        };
    }

    // ---------- 主 tick ----------

    public static void tick(Level level, BlockPos pos, BlockState state, RedstoneLinkBlockEntity entity) {
        if (level.isClientSide()) return;
        entity.serverTick(state);
    }

    private void serverTick(BlockState state) {
        if (level == null) return;
        boolean receive = state.getValue(RedstoneLinkBlock.RECEIVED);
        int newSignal = receive ? computeReceivedSignal() : level.getBestNeighborSignal(worldPosition);
        boolean powered = newSignal > 0;
        if (state.getValue(RedstoneLinkBlock.POWERED) != powered) {
            level.setBlock(worldPosition, state.setValue(RedstoneLinkBlock.POWERED, powered), 3);
        }
        if (newSignal != signal) {
            this.signal = newSignal;
            setChanged();
            syncState();
            if (receive) {
                level.updateNeighborsAt(worldPosition, getBlockState().getBlock());
                level.updateNeighborsAt(worldPosition.below(), getBlockState().getBlock());
            }
        }
    }

    // ---------- NBT ----------

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!marker.isEmpty()) {
            tag.put("Marker", marker.save(registries));
        }
        if (boundPos != null && boundDimension != null) {
            tag.putString("BoundDim", boundDimension.location().toString());
            tag.putInt("BoundX", boundPos.getX());
            tag.putInt("BoundY", boundPos.getY());
            tag.putInt("BoundZ", boundPos.getZ());
        }
    }

    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.marker = tag.contains("Marker")
                ? ItemStack.parseOptional(registries, tag.getCompound("Marker"))
                : ItemStack.EMPTY;
        if (tag.contains("BoundX")) {
            this.boundDimension = ResourceKey.create(
                    Registries.DIMENSION, ResourceLocation.parse(tag.getString("BoundDim")));
            this.boundPos = new BlockPos(tag.getInt("BoundX"), tag.getInt("BoundY"), tag.getInt("BoundZ"));
        } else {
            this.boundDimension = null;
            this.boundPos = null;
        }
    }

    // ---------- 网络同步 ----------

    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("Signal", signal);
        if (!marker.isEmpty()) {
            tag.put("Marker", marker.save(registries));
        }
        if (boundPos != null && boundDimension != null) {
            tag.putString("BoundDim", boundDimension.location().toString());
            tag.putInt("BoundX", boundPos.getX());
            tag.putInt("BoundY", boundPos.getY());
            tag.putInt("BoundZ", boundPos.getZ());
        }
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        this.signal = tag.getInt("Signal");
        this.marker = tag.contains("Marker")
                ? ItemStack.parseOptional(registries, tag.getCompound("Marker"))
                : ItemStack.EMPTY;
        if (tag.contains("BoundX")) {
            this.boundDimension = ResourceKey.create(
                    Registries.DIMENSION, ResourceLocation.parse(tag.getString("BoundDim")));
            this.boundPos = new BlockPos(tag.getInt("BoundX"), tag.getInt("BoundY"), tag.getInt("BoundZ"));
        } else {
            this.boundDimension = null;
            this.boundPos = null;
        }
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private void syncState() {
        if (level == null || level.isClientSide()) return;
        level.sendBlockUpdated(worldPosition, getBlockState(), getBlockState(), Block.UPDATE_CLIENTS);
    }
}
