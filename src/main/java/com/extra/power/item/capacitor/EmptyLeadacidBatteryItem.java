package com.extra.power.item.capacitor;

import com.extra.power.init.ModItems;
import dev.dubhe.anvilcraft.api.item.IEmptyCapacitor;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;

public class EmptyLeadacidBatteryItem extends Item implements IEmptyCapacitor {
    public EmptyLeadacidBatteryItem(Properties properties) {
        super(properties);
    }
    @Override
    public ItemStack getFull(ItemStack input) {
        return ModItems.LEAD_ACID_BATTERY.asStack(1);
    }
}
