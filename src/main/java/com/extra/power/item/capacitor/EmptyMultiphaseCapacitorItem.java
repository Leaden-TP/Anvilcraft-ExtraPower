package com.extra.power.item.capacitor;

import com.extra.power.init.ModItems;
import dev.dubhe.anvilcraft.api.item.IChargerChargeable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EmptyMultiphaseCapacitorItem extends Item implements IChargerChargeable {
    public EmptyMultiphaseCapacitorItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack charge(ItemStack input) {
        return ModItems.MULTIPHASE_CAPACITOR.asStack(1);
    }
}
