package com.extra.power.event;

import com.extra.power.block.ModBlockEntity;
import com.extra.power.init.AnvilCraftExtrapower;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.capabilities.Capabilities;
import net.neoforged.neoforge.capabilities.RegisterCapabilitiesEvent;

import java.util.List;

@EventBusSubscriber(modid = AnvilCraftExtrapower.MODID)
public class CapabilitiesEventListener {
    @SubscribeEvent
    public static void registerCapabilities(final RegisterCapabilitiesEvent event) {
        List.of(
                ModBlockEntity.CRATE.get(),
                ModBlockEntity.MAGNETIC_DISPLAY_STAND.get()
        ).forEach(type -> event.registerBlockEntity(
                        Capabilities.Item.BLOCK,
                        type,
                        (be, side) -> be.getItemHandler()
                )
        );

    }
}