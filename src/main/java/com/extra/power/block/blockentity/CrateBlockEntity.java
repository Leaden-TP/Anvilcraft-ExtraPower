package com.extra.power.block.blockentity;

import com.extra.power.block.ModBlockEntity;
import dev.dubhe.anvilcraft.api.itemhandler.FilteredItemStackHandler;
import dev.dubhe.anvilcraft.api.itemhandler.IItemResourceHandlerHolder;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.NonNullList;
import net.minecraft.core.Vec3i;
import net.minecraft.network.chat.Component;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.Container;
import net.minecraft.world.ContainerHelper;
import net.minecraft.world.entity.ContainerUser;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.inventory.ChestMenu;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BarrelBlock;
import net.minecraft.world.level.block.entity.*;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.neoforged.neoforge.transfer.ResourceHandler;
import net.neoforged.neoforge.transfer.item.ItemResource;

import java.util.List;

public class CrateBlockEntity extends RandomizableContainerBlockEntity implements IItemResourceHandlerHolder {
    private static final Component DEFAULT_NAME = Component.translatable("block.anvilcraftextrapower.crate_ui");
    private NonNullList<ItemStack> items;
    private final ContainerOpenersCounter openersCounter;
    private final FilteredItemStackHandler itemHandler = new FilteredItemStackHandler(54);

    public CrateBlockEntity(BlockPos worldPosition, BlockState blockState) {
        super(ModBlockEntity.CRATE.get(), worldPosition, blockState);
        this.items = NonNullList.withSize(54, ItemStack.EMPTY);
        this.openersCounter = new ContainerOpenersCounter() {

            protected void onOpen(Level level, BlockPos pos, BlockState state) {
                CrateBlockEntity.this.playSound(state, SoundEvents.BARREL_OPEN);
                CrateBlockEntity.this.updateBlockState(state, true);
            }

            protected void onClose(Level level, BlockPos pos, BlockState state) {
                CrateBlockEntity.this.playSound(state, SoundEvents.BARREL_CLOSE);
                CrateBlockEntity.this.updateBlockState(state, false);
            }

            protected void openerCountChanged(Level level, BlockPos pos, BlockState blockState, int previous, int current) {
            }

            public boolean isOwnContainer(Player player) {
                if (player.containerMenu instanceof ChestMenu) {
                    Container container = ((ChestMenu)player.containerMenu).getContainer();
                    return container == CrateBlockEntity.this;
                } else {
                    return false;
                }
            }
        };
    }



    public static CrateBlockEntity createBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState blockState
    ) {
        return new CrateBlockEntity( pos, blockState);
    }

    protected void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        if (!this.trySaveLootTable(output)) {
            ContainerHelper.saveAllItems(output, this.items);
        }

    }

    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.items = NonNullList.withSize(this.getContainerSize(), ItemStack.EMPTY);
        if (!this.tryLoadLootTable(input)) {
            ContainerHelper.loadAllItems(input, this.items);
        }

    }

    public int getContainerSize() {
        return 54;
    }

    protected NonNullList<ItemStack> getItems() {
        return this.items;
    }

    protected void setItems(NonNullList<ItemStack> items) {
        this.items = items;
    }

    protected Component getDefaultName() {
        return DEFAULT_NAME;
    }

    protected AbstractContainerMenu createMenu(int containerId, Inventory inventory) {
        return ChestMenu.sixRows(containerId, inventory, this);
    }

    public void startOpen(ContainerUser containerUser) {
        if (!this.remove && !containerUser.getLivingEntity().isSpectator()) {
            this.openersCounter.incrementOpeners(containerUser.getLivingEntity(), this.getLevel(), this.getBlockPos(), this.getBlockState(), containerUser.getContainerInteractionRange());
        }

    }

    public void stopOpen(ContainerUser containerUser) {
        if (!this.remove && !containerUser.getLivingEntity().isSpectator()) {
            this.openersCounter.decrementOpeners(containerUser.getLivingEntity(), this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    public List<ContainerUser> getEntitiesWithContainerOpen() {
        return this.openersCounter.getEntitiesWithContainerOpen(this.getLevel(), this.getBlockPos());
    }

    public void recheckOpen() {
        if (!this.remove) {
            this.openersCounter.recheckOpeners(this.getLevel(), this.getBlockPos(), this.getBlockState());
        }

    }

    private void updateBlockState(BlockState state, boolean isOpen) {
        this.level.setBlock(this.getBlockPos(), (BlockState)state.setValue(BarrelBlock.OPEN, isOpen), 3);
    }

    private void playSound(BlockState state, SoundEvent event) {
        Vec3i direction = ((Direction)state.getValue(BarrelBlock.FACING)).getUnitVec3i();
        double x = (double)this.worldPosition.getX() + (double)0.5F + (double)direction.getX() / (double)2.0F;
        double y = (double)this.worldPosition.getY() + (double)0.5F + (double)direction.getY() / (double)2.0F;
        double z = (double)this.worldPosition.getZ() + (double)0.5F + (double)direction.getZ() / (double)2.0F;
        this.level.playSound((Entity)null, x, y, z, event, SoundSource.BLOCKS, 0.5F, this.level.getRandom().nextFloat() * 0.1F + 0.9F);
    }

    @Override
    public ResourceHandler<ItemResource> getItemHandler() {
        return itemHandler;
    }
}