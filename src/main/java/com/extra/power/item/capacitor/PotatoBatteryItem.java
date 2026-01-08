package com.extra.power.item.capacitor;

import dev.dubhe.anvilcraft.api.item.IChargerDischargeable;
import dev.dubhe.anvilcraft.init.item.ModItems;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;

import static com.extra.power.block.ModBlock.POTATO_BATTERY;

public class PotatoBatteryItem extends BlockItem implements IChargerDischargeable {
    public PotatoBatteryItem(Properties properties) {
        super(POTATO_BATTERY.get(), properties);}
    @Override
    public ItemStack discharge(ItemStack input) {
        return new ItemStack(Items.BAKED_POTATO, 1);
    }
}
