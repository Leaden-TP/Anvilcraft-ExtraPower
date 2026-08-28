package com.extra.power.item.capacitor;

import com.extra.power.init.ModItems;
import dev.dubhe.anvilcraft.api.item.IEmptyCapacitor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EmptyMultiphaseCapacitorItem extends Item implements IEmptyCapacitor {
    public EmptyMultiphaseCapacitorItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack getFull(ItemStack input) {
        return ModItems.MULTIPHASE_CAPACITOR.asStack(1);
    }
}
