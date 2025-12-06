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
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.ContainerOpenersCounter;
import net.minecraft.world.level.block.entity.RandomizableContainerBlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class CrateBlockEntity extends RandomizableContainerBlockEntity {//BarrelBlockEntity
        private NonNullList<ItemStack> items = NonNullList.withSize(54, ItemStack.EMPTY);
        private final ContainerOpenersCounter openersCounter = new ContainerOpenersCounter() {
            @Override
            protected void onOpen(Level p_155062_, BlockPos p_155063_, BlockState p_155064_) {
                CrateBlockEntity.this.playSound(p_155064_, SoundEvents.BARREL_OPEN);
                CrateBlockEntity.this.updateBlockState(p_155064_, true);
            }

            @Override
            protected void onClose(Level p_155072_, BlockPos p_155073_, BlockState p_155074_) {
                CrateBlockEntity.this.playSound(p_155074_, SoundEvents.BARREL_CLOSE);
                CrateBlockEntity.this.updateBlockState(p_155074_, false);
            }

            @Override
            protected void openerCountChanged(Level p_155066_, BlockPos p_155067_, BlockState p_155068_, int p_155069_, int p_155070_) {
            }

            @Override
            protected boolean isOwnContainer(Player p_155060_) {
                if (p_155060_.containerMenu instanceof ChestMenu) {
                    Container container = ((ChestMenu)p_155060_.containerMenu).getContainer();
                    return container == CrateBlockEntity.this;
                } else {
                    return false;
                }
            }
        };

        public CrateBlockEntity(BlockPos pos, BlockState blockState) {
            super(ModBlockEntity.CRATE.get(),pos, blockState);
        }

        @Override
        protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
            super.saveAdditional(tag, registries);
            if (!this.trySaveLootTable(tag)) {
                ContainerHelper.saveAllItems(tag, this.items, registries);
            }
        }

        @Override
        protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
            super.loadAdditional(tag, registries);
            this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
            if (!this.tryLoadLootTable(tag)) {
                ContainerHelper.loadAllItems(tag, this.items, registries);
            }
        }

        @Override
        public int getMaxStackSize() {
            return 64;
        }

        @Override
        public int getContainerSize() {
            return 54;
        }

        @Override
        protected NonNullList<ItemStack> getItems() {
            return this.items;
        }

        @Override
        protected void setItems(NonNullList<ItemStack> items) {
            this.items = items;
        }

        @Override
        protected Component getDefaultName() {
            return Component.translatable("block.anvilcraftextrapower.crate_ui");
        }

        @Override
        protected AbstractContainerMenu createMenu(int id, Inventory player) {
            return ChestMenu.sixRows(id, player, this);
        }

        @Override
        public void startOpen(Player player) {
            if (!this.remove && !player.isSpectator()) {
                this.openersCounter.incrementOpeners(player, this.getLevel(), this.getBlockPos(), this.getBlockState());
            }
        }

        @Override
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
            this.level.setBlock(this.getBlockPos(), state.setValue(BarrelBlock.OPEN, Boolean.valueOf(open)), 3);
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
            return true;
        }

        @Override
        public boolean canTakeItem(Container target, int slot, ItemStack stack) {
            return true;
        }
    }

