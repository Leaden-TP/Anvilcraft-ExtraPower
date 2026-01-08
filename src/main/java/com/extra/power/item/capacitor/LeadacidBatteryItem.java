package com.extra.power.item.capacitor;

import com.extra.power.init.ModItems;
import dev.dubhe.anvilcraft.api.item.IChargerDischargeable;

import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class LeadacidBatteryItem extends Item implements IChargerDischargeable {

    public LeadacidBatteryItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack discharge(ItemStack input) {
        return ModItems.LEAD_ACID_BATTERY_EMPTY.asStack(1);
    }

}
