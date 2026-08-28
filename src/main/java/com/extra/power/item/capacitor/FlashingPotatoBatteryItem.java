package com.extra.power.item.capacitor;


import dev.dubhe.anvilcraft.api.item.IFullCapacitor;
import dev.dubhe.anvilcraft.item.CapacitorItem;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.inventory.ClickAction;
import net.minecraft.world.inventory.Slot;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;

import static com.extra.power.init.block.ModBlock.FLASHING_POTATO_BATTERY;
import static dev.dubhe.anvilcraft.item.IonocraftBackpackItem.addStackProvider;


public class FlashingPotatoBatteryItem extends BlockItem implements IFullCapacitor {
    public FlashingPotatoBatteryItem(Properties properties) {
        super(FLASHING_POTATO_BATTERY.get(),properties);
    }
    public static final int ENERGY = 40_000_000;


    public FlashingPotatoBatteryItem(Block block, Properties properties) {
        super(block, properties);
        addStackProvider(player -> player.getItemBySlot(EquipmentSlot.HEAD));
    }
    @Override
    public boolean canEquip(ItemStack stack, EquipmentSlot armorType, LivingEntity entity) {
        return armorType == EquipmentSlot.HEAD;
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

    @Override
    public ItemStack getEmpty(ItemStack input) {
        return new ItemStack(Items.BAKED_POTATO, 1);
    }
}
