package com.extra.power.data.lang;

import dev.anvilcraft.lib.v2.registrum.providers.RegistrumLangProvider;

public class AdvancementLang {
    @SuppressWarnings("checkstyle:LineLength")
    public static void init(RegistrumLangProvider provider) {
        // region root
        provider.add("advancements.anvilcraftextrapower.root.title", "AnvilCraft: Extra Power");
        provider.add(
            "advancements.anvilcraftextrapower.root.description",
            "Explore more possibilities of AnvilCraft"
        );
        // endregion

        // region 火力发电线
        provider.add("advancements.anvilcraftextrapower.burning_coal.title", "Thermal Power");
        provider.add(
            "advancements.anvilcraftextrapower.burning_coal.description",
            "Go heat something up with a burning coal block"
        );

        provider.add("advancements.anvilcraftextrapower.ignite_magnesium.title", "Blinding Light");
        provider.add(
            "advancements.anvilcraftextrapower.ignite_magnesium.description",
            "Ignite a magnesium block with flint and steel; its violent burning reaches incandescent heat, or keeps things overheated"
        );
        // endregion

        // region 太阳能线
        provider.add("advancements.anvilcraftextrapower.solar_panel.title", "Sunbathing");
        provider.add(
            "advancements.anvilcraftextrapower.solar_panel.description",
            "Let a solar panel soak up the sun"
        );

        provider.add("advancements.anvilcraftextrapower.knowledge_is_power.title", "Knowledge Is Power");
        provider.add(
            "advancements.anvilcraftextrapower.knowledge_is_power.description",
            "Craft an enchanted generator"
        );
        // endregion

        // region 核能线
        provider.add("advancements.anvilcraftextrapower.nuclear_collector.title", "Nuclear Utilization");
        provider.add(
            "advancements.anvilcraftextrapower.nuclear_collector.description",
            "Craft a nuclear collector"
        );

        provider.add(
            "advancements.anvilcraftextrapower.uranium_rod_radiation.title",
            "Why Does My Mouth Taste Like Metal"
        );
        provider.add(
            "advancements.anvilcraftextrapower.uranium_rod_radiation.description",
            "Get irradiated by an out-of-control uranium rod"
        );

        provider.add("advancements.anvilcraftextrapower.frost_controller.title", "Take Control of It");
        provider.add(
            "advancements.anvilcraftextrapower.frost_controller.description",
            "Place a frost controller near a uranium rod to cut down its heat generation"
        );

        provider.add(
            "advancements.anvilcraftextrapower.nuclear_power_plant.title",
            "Nuclear Power, Piece of Cake"
        );
        provider.add(
            "advancements.anvilcraftextrapower.nuclear_power_plant.description",
            "Build a nuclear power plant with 24 uranium rods"
        );
        // endregion

        // region 核弹线
        provider.add("advancements.anvilcraftextrapower.nuclear_bomb.title", "Demon Core");
        provider.add(
            "advancements.anvilcraftextrapower.nuclear_bomb.description",
            "Craft a nuclear bomb"
        );

        provider.add("advancements.anvilcraftextrapower.nuclear_explosion.title", "Oh No, Oh No");
        provider.add(
            "advancements.anvilcraftextrapower.nuclear_explosion.description",
            "Take nuclear explosion damage while watching the \"fireworks\""
        );
        // endregion

        // region 自动弩线
        provider.add("advancements.anvilcraftextrapower.automatic_crossbow.title", "Hedgehog Wholesaler");
        provider.add(
            "advancements.anvilcraftextrapower.automatic_crossbow.description",
            "Craft an automatic crossbow, then turn your enemies into sieves"
        );
        // endregion

        // region 土豆电池线
        provider.add("advancements.anvilcraftextrapower.potato_battery.title", "Potato Power");
        provider.add(
            "advancements.anvilcraftextrapower.potato_battery.description",
            "Craft a potato battery and drive your power grid with it"
        );

        provider.add("advancements.anvilcraftextrapower.flashing_potato_battery.title", "Utterly Thunderous");
        provider.add(
            "advancements.anvilcraftextrapower.flashing_potato_battery.description",
            "Strike a potato battery with lightning to get a flashing potato battery"
        );
        // endregion

        // region 红石收发器线
        provider.add(
            "advancements.anvilcraftextrapower.wireless_redstone.title",
            "Magnetism, Electricity, Magnetism, Electricity..."
        );
        provider.add(
            "advancements.anvilcraftextrapower.wireless_redstone.description",
            "Craft a wireless redstone transceiver and enjoy the convenience of wireless"
        );

        provider.add("advancements.anvilcraftextrapower.strong_magnetism.title", "Powerful Magnetism");
        provider.add(
            "advancements.anvilcraftextrapower.strong_magnetism.description",
            "Use an electromagnet to pull an anvil from more than 10 blocks away"
        );

        provider.add("advancements.anvilcraftextrapower.anvil_projection.title", "Shadow Clone Technique");
        provider.add(
            "advancements.anvilcraftextrapower.anvil_projection.description",
            "Smash an anvil projector with an anvil and create a 16-block-long illusion"
        );

        provider.add(
            "advancements.anvilcraftextrapower.display_stand_on_display_stand.title",
            "Magnetic Display Stand Displays Magnetic Display Stand"
        );
        provider.add(
            "advancements.anvilcraftextrapower.display_stand_on_display_stand.description",
            "Use a magnetic display stand to display a magnetic display stand"
        );
        // endregion
    }
}
