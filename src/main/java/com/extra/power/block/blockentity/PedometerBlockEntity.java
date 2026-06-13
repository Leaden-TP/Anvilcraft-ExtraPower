package com.extra.power.block.blockentity;


import dev.dubhe.anvilcraft.api.item.IDiskCloneable;
import dev.dubhe.anvilcraft.block.PulseGeneratorBlock;
import dev.dubhe.anvilcraft.block.entity.PulseGeneratorBlockEntity;
import dev.dubhe.anvilcraft.init.ModMenuTypes;
import dev.dubhe.anvilcraft.inventory.PulseGeneratorMenu;
import dev.dubhe.anvilcraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.MenuProvider;
import net.minecraft.world.entity.player.Inventory;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.AbstractContainerMenu;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import static com.extra.power.block.ModBlockEntity.PEDOMETER;


public class PedometerBlockEntity extends BlockEntity implements MenuProvider, IDiskCloneable {
    public PedometerBlockEntity(BlockPos pos, BlockState state) {
        super(PEDOMETER.get(), pos, state);
    }
    public static PedometerBlockEntity createBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState blockState
    ) {
        return new PedometerBlockEntity(pos, blockState);
    }

    public PedometerBlockEntity readDataNbt(CompoundTag data) {
        return this;
    }
    public CompoundTag constructDataNbt() {
        CompoundTag data = new CompoundTag();
        return data;
    }
    @Override
    public void storeDiskData(CompoundTag tag) {
        tag.put("Data", this.constructDataNbt());
    }
    @Override
    public void applyDiskData(CompoundTag data) {
        this.readDataNbt(data.getCompound("Data"));
    }
    @Override
    public Component getDisplayName() {
        return Component.translatable("block.anvilcraft.pulse_generator");
    }
    @Override
    public @Nullable AbstractContainerMenu createMenu(int containerId, Inventory inventory, Player player) {
        if (player.isSpectator()) return null;
        if (player.level().getBlockEntity(getBlockPos()) instanceof PulseGeneratorBlockEntity blockEntity) {
            return new PulseGeneratorMenu(ModMenuTypes.PULSE_GENERATOR.get(), containerId, inventory, blockEntity);
        }
        return null;
    }
}
