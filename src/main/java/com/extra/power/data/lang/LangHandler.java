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
        provider.add("block.anvilcraftextrapower.crate_ui", "Crate");

        provider.add(
                "block.anvilcraftextrapower.enchanted_generator.placement_too_close_to_another",
                "Too close to another enchanted generator"
        );
        provider.add(
                "entity.anvilcraftextrapower.technical_arrow",
                "Special Arrow"
        );

        provider.add("message.anvilcraftextrapower.redstone_link.marker_set", "Signal marker set: %s");
        provider.add("message.anvilcraftextrapower.redstone_link.marker_reset", "Signal marker cleared");
        provider.add("message.anvilcraftextrapower.redstone_link.marker_unsupported_blue", "Item markers are not supported in blue mode");
        provider.add("message.anvilcraftextrapower.redstone_link.bind_success", "Position recorded: %s");
        provider.add("message.anvilcraftextrapower.redstone_link.mode_receive", "Receive mode: ON");
        provider.add("message.anvilcraftextrapower.redstone_link.mode_send", "Receive mode: OFF");
        provider.add("item.anvilcraftextrapower.redstone_link.bound", "Bound position: %s");
        provider.add("tooltip.anvilcraftextrapower.redstone_link.title", "Redstone Link");
        provider.add("tooltip.anvilcraftextrapower.redstone_link.color", "Color: %s");
        provider.add("tooltip.anvilcraftextrapower.redstone_link.color.red", "Red (Global)");
        provider.add("tooltip.anvilcraftextrapower.redstone_link.color.yellow", "Yellow (128 blocks)");
        provider.add("tooltip.anvilcraftextrapower.redstone_link.color.blue", "Blue (Bound)");
        provider.add("tooltip.anvilcraftextrapower.redstone_link.mode", "Mode: %s");
        provider.add("tooltip.anvilcraftextrapower.redstone_link.mode.send", "Transmitting");
        provider.add("tooltip.anvilcraftextrapower.redstone_link.mode.receive", "Receiving");
        provider.add("tooltip.anvilcraftextrapower.redstone_link.signal", "Signal: %d/15");
        provider.add("tooltip.anvilcraftextrapower.redstone_link.marker", "Marker: %s");
        provider.add("tooltip.anvilcraftextrapower.redstone_link.marker.none", "None");
        provider.add("tooltip.anvilcraftextrapower.redstone_link.bound", "Bound to: %s");
        provider.add("tooltip.anvilcraftextrapower.redstone_link.bound.none", "Not bound");
        provider.add("tooltip.anvilcraftextrapower.redstone_link.barrier", "Item markers are not supported in blue mode");
    }
}
