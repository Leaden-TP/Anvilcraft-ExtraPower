package com.extra.power.init;

import dev.dubhe.anvilcraft.init.item.ModItemGroups;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.CreativeModeTab;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;


import static com.extra.power.init.AnvilCraftExtrapower.MODID;
import static dev.dubhe.anvilcraft.init.item.ModItems.CAPACITOR;


public class ModCreativeModeTab {
    public static final DeferredRegister<CreativeModeTab> CREATIVE_MODE_TABS = DeferredRegister.create(Registries.CREATIVE_MODE_TAB, MODID);
    public static final DeferredHolder<CreativeModeTab, CreativeModeTab> MOD_TAB = CREATIVE_MODE_TABS.register("anvilcraftextrapower_tab",
            () -> CreativeModeTab.builder()
            .title(Component.translatable("creativetab.anvilcraftextrapower.main"))
            .withTabsBefore(ModItemGroups.ANVILCRAFT_BUILDING_BLOCKS.getKey())
            .icon(() -> CAPACITOR.get().getDefaultInstance())
            .displayItems((parameters, output) -> {
            }).build());
}
