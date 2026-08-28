package com.extra.power.event;

import com.extra.power.init.ModItems;
import com.extra.power.init.block.ModBlockEntity;
import com.extra.power.init.AnvilCraftExtrapower;
import com.extra.power.item.AutomaticCrossbowItem;
import dev.dubhe.anvilcraft.api.energy.ItemFEStorage;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.List;

@EventBusSubscriber(modid = AnvilCraftExtrapower.MODID)
public class CapabilitiesEventListener {
    @SubscribeEvent
    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        List.of(ModBlockEntity. CRATE.get())
                .forEach(type -> event.registerBlockEntity(
                        Capabilities.ItemHandler.BLOCK,
                        type,
                        (be, side) -> be.getItemHandler()
                )
        );
        List.of(ModBlockEntity. MAGNETIC_DISPLAY_STAND.get())
                .forEach(type -> event.registerBlockEntity(
                                Capabilities.ItemHandler.BLOCK,
                                type,
                                (be, side) -> be.getItemHandler()
                        )
                );
        event.registerItem(
                Capabilities.EnergyStorage.ITEM,
                (stack, ctx) -> new ItemFEStorage(stack, AutomaticCrossbowItem.MAX_ENERGY),
                ModItems.AUTOMATIC_CROSSBOW.get()
        );

    }
}