package com.extra.power.item.capacitor;

import com.extra.power.init.ModItems;
import dev.dubhe.anvilcraft.api.item.IFullCapacitor;
import dev.dubhe.anvilcraft.item.CapacitorItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class MultiphaseCapacitorItem extends Item implements IFullCapacitor {
    public static final int ENERGY = 80_000_000;


    public MultiphaseCapacitorItem(Properties properties) {
        super(properties);
    }
    @Override
    public ItemStack getEmpty(ItemStack input) {
        return ModItems.MULTIPHASE_CAPACITOR_EMPTY.asStack(1);
    }
    @Override
    public boolean overrideStackedOnOther(ItemStack stack, Slot slot, ClickAction clickAction, Player player) {
        return IFullCapacitor.tryForceChargeTarget(this, stack, slot, clickAction, player);
    }

    @Override
    public void inventoryTick(ItemStack stack, Level level, Entity entity, int slotId, boolean isSelected) {
        super.inventoryTick(stack, level, entity, slotId, isSelected);
        if (!(entity instanceof Player player)) {
            return;
        }
        IFullCapacitor.super.inventoryTick(stack, player);
    }

    @Override
    public int getEnergyStored(ItemStack stack) {
        return ENERGY;
    }

}
