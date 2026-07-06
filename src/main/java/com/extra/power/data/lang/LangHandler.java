package com.extra.power.data.lang;

import com.extra.power.config.ModServerConfig;
import dev.anvilcraft.lib.v2.config.ConfigData;
import dev.anvilcraft.lib.v2.registrum.providers.RegistrumLangProvider;

public class LangHandler {
    /**
     * language file init
     *
     * @param provider provider
     */
    public static void init(RegistrumLangProvider provider) {
        ConfigData.readConfigClass(provider, ModServerConfig.class);

        provider.add(
                "creativetab.anvilcraftextrapower.main",
                "AnvilCraft：Extra Power");

        provider.add("pack.anvilcraftextrapower.builtin_pack",  "AnvilCraft：Extra Power Builtin ResourcePack");

        provider.add("block.anvilcraftextrapower.nuclear_collector.placement_too_close_to_another", "Too close to another Nuclear Collector");
        provider.add("tooltip.anvilcraftextrapower.nuclear_collector.title", "Nuclear Collector");
        provider.add(  "tooltip.anvilcraftextrapower.nuclear_collector.status.working", "Working normally");
        provider.add("tooltip.anvilcraftextrapower.nuclear_collector.status.too_close",  "Too close to another Nuclear Collector");
        provider.add("tooltip.anvilcraftextrapower.nuclear_collector.status.too_hot", "Overheating! Needs cooling");
        provider.add(  "tooltip.anvilcraftextrapower.nuclear_collector.status.invalid_range", "Invalid water range(Too large!)");
        provider.add(  "tooltip.anvilcraftextrapower.nuclear_collector.status.no_rod",  "No uranium rods nearby");
        provider.add(    "tooltip.anvilcraftextrapower.nuclear_collector.heat", "Heat: %d/%d");
        provider.add(  "tooltip.anvilcraftextrapower.nuclear_collector.power", "Power Output: %d.%dMW");
        provider.add(    "subtitles.anvilcraftextrapower.nuclear_explosion", "Nuclear Explosion");
        provider.add(      "death.attack.nuclear_explosion", "%1$s was reduced to anvilon in the flash");
        provider.add(     "death.attack.nuclear_explosion.player", "%1$s was reduced to anvilon in the flash");

        provider.add(  "message.anvilcraftextrapower.solar_panel_too_close", "Cannot place solar panel - another panel is too close (3x3x3 area)");
        provider.add("config.jade.plugin_anvilcraft_pigsplus.enchanted_generator", "Enchanted Generator");
    }
}
