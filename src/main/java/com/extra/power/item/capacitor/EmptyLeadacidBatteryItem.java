package com.extra.power.item.capacitor;

import com.extra.power.init.ModItems;
import dev.dubhe.anvilcraft.api.item.IChargerChargeable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class EmptyLeadacidBatteryItem extends Item implements IChargerChargeable {
    public EmptyLeadacidBatteryItem(Properties properties) {
        super(properties);
    }

    @Override
    public ItemStack charge(ItemStack input) {
        return ModItems.LEAD_ACID_BATTERY.asStack(1);
    }
}
