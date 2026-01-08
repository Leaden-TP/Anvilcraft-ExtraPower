package com.extra.power.item.capacitor;

import com.extra.power.init.ModItems;
import dev.dubhe.anvilcraft.api.item.IChargerDischargeable;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

public class MultiphaseCapacitorItem extends Item implements IChargerDischargeable {

    public MultiphaseCapacitorItem(Properties properties) {
        super(properties);
    }
    @Override
    public ItemStack discharge(ItemStack input) {
        return ModItems.MULTIPHASE_CAPACITOR_EMPTY.asStack(1);
    }

}
