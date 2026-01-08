package com.extra.power.item.capacitor;

import dev.dubhe.anvilcraft.api.item.IChargerDischargeable;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.extra.power.block.ModBlock.FLASHING_POTATO_BATTERY;


public class FlashingPotatoBatteryItem extends BlockItem implements IChargerDischargeable {
    public FlashingPotatoBatteryItem(Properties properties) {
        super(FLASHING_POTATO_BATTERY.get(),properties);
    }

    @Override
    public ItemStack discharge(ItemStack input) {
        return new ItemStack(Items.BAKED_POTATO, 1);
    }
}
