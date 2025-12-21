package com.extra.power.block.blockentity;

import com.extra.power.block.ModBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.items.ItemStackHandler;
import net.neoforged.neoforge.items.wrapper.InvWrapper;
import org.jetbrains.annotations.NotNull;

public class CrateBlockEntity extends RandomizableContainerBlockEntity implements IItemHandler {
    private NonNullList<ItemStack> items = NonNullList.withSize(54, ItemStack.EMPTY);
    private final InvWrapper itemHandler = new InvWrapper(this);
    private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
        protected void onOpen(Level level, BlockPos pos, BlockState state) {
            CrateBlockEntity.this.playSound(state, SoundEvents.BARREL_OPEN);
            CrateBlockEntity.this.updateBlockState(state, true);
        }

        protected void onClose(Level level, BlockPos pos, BlockState state) {
            CrateBlockEntity.this.playSound(state, SoundEvents.BARREL_CLOSE);
            CrateBlockEntity.this.updateBlockState(state, false);
        }

        protected void openerCountChanged(Level level, BlockPos pos, BlockState state, int oldCount, int newCount) {
        }

        protected boolean isOwnContainer(Player player) {
            if (player.containerMenu instanceof ChestMenu) {
                Container container = ((ChestMenu)player.containerMenu).getContainer();
                return container == CrateBlockEntity.this;
            } else {
                return false;
            }
        }
    };

    public CrateBlockEntity(BlockPos pos, BlockState blockState) {
        super(ModBlockEntity.CRATE.get(), pos, blockState);
    }

    // IItemHandler implementation
    @Override
    public int getSlots() {
        return this.getContainerSize();
    }

    @Override
    public @NotNull ItemStack getStackInSlot(int slot) {
        return this.getItem(slot);
    }

    @Override
    public @NotNull ItemStack insertItem(int slot, @NotNull ItemStack stack, boolean simulate) {
        if (!canPlaceItem(slot, stack)) {
            return stack;
        }

        if (!simulate) {
            ItemStack existing = this.getItem(slot);
            if (existing.isEmpty()) {
                this.setItem(slot, stack.copy());
                return ItemStack.EMPTY;
            } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                int maxStackSize = Math.min(this.getMaxStackSize(), existing.getMaxStackSize());
                int space = maxStackSize - existing.getCount();
                if (space > 0) {
                    int toAdd = Math.min(space, stack.getCount());
                    existing.grow(toAdd);
                    return stack.copyWithCount(stack.getCount() - toAdd);
                }
            }
            return stack;
        } else {
            ItemStack existing = this.getItem(slot);
            if (existing.isEmpty()) {
                return ItemStack.EMPTY;
            } else if (ItemStack.isSameItemSameComponents(existing, stack)) {
                int maxStackSize = Math.min(this.getMaxStackSize(), existing.getMaxStackSize());
                int space = maxStackSize - existing.getCount();
                if (space > 0) {
                    int toAdd = Math.min(space, stack.getCount());
                    return stack.copyWithCount(stack.getCount() - toAdd);
                }
            }
            return stack;
        }
    }

    @Override
    public @NotNull ItemStack extractItem(int slot, int amount, boolean simulate) {
        if (amount == 0) {
            return ItemStack.EMPTY;
        }

        ItemStack existing = this.getItem(slot);
        if (existing.isEmpty()) {
            return ItemStack.EMPTY;
        }

        int toExtract = Math.min(amount, existing.getMaxStackSize());
        if (existing.getCount() <= toExtract) {
            if (!simulate) {
                this.setItem(slot, ItemStack.EMPTY);
            }
            return existing.copy();
        } else {
            if (!simulate) {
                this.setItem(slot, existing.copyWithCount(existing.getCount() - toExtract));
            }
            return existing.copyWithCount(toExtract);
        }
    }

    @Override
    public int getSlotLimit(int slot) {
        return this.getMaxStackSize();
    }

    @Override
    public boolean isItemValid(int slot, @NotNull ItemStack stack) {
        return canPlaceItem(slot, stack);
    }

    // Rest of the original methods remain the same
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        if (!this.trySaveLootTable(tag)) {
            ContainerHelper.saveAllItems(tag, this.items, registries);
        }
    }

    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(tag)) {
            ContainerHelper.loadAllItems(tag, this.items, registries);
        }
    }

    public int getContainerSize() {
        return 54;
    }

    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    public IItemHandler getItemHandler() {
        return itemHandler;
    }

    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    protected Component getDefaultName() {
        return Component.translatable("block.anvilcraftextrapower.crate_ui");
    }

    protected AbstractContainerMenu createMenu(int id, Inventory player) {
        return ChestMenu.sixRows(id, player, this);
    }

    public void startOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public void stopOpen(Player player) {
        if (!this.remove && !player.isSpectator()) {
            this.openersCounter.decrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }
    }

    void updateBlockState(BlockState state, boolean open) {
        this.level.setBlock(this.getBlockPos(), state.setValue(BarrelBlock.OPEN, open), 3);
    }

    void playSound(BlockState state, SoundEvent sound) {
        Vec3i vec3i = state.getValue(BarrelBlock.FACING).getNormal();
        double d0 = (double)this.worldPosition.getX() + 0.5 + (double)vec3i.getX() / 2.0;
        double d1 = (double)this.worldPosition.getY() + 0.5 + (double)vec3i.getY() / 2.0;
        double d2 = (double)this.worldPosition.getZ() + 0.5 + (double)vec3i.getZ() / 2.0;
        this.level.playSound(null, d0, d1, d2, sound, SoundSource.BLOCKS, 0.5F, this.level.random.nextFloat() * 0.1F + 0.9F);
    }

    @Override
    public boolean canPlaceItem(int slot, ItemStack stack) {
        return stack.getItem() instanceof BlockItem || super.canPlaceItem(slot, stack);
    }
}

