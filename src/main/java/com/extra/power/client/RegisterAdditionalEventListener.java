package com.extra.power.client;

import com.extra.power.client.renderer.blockentity.*;
import com.extra.power.init.AnvilCraftExtrapower;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;
import net.neoforged.neoforge.client.model.standalone.SimpleUnbakedStandaloneModel;

@EventBusSubscriber(value = Dist.CLIENT, modid = AnvilCraftExtrapower.MODID)
public class RegisterAdditionalEventListener {

    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterStandalone event) {

        // NuclearCollector
        event.register(
                NuclearCollectorRenderer.MODEL,
                SimpleUnbakedStandaloneModel.blockStateModel(AnvilCraftExtrapower.of("block/nuclear_collector_head"))
        );
        event.register(
                NuclearCollectorRenderer.OVERHEATED_MODEL,
                SimpleUnbakedStandaloneModel.blockStateModel(AnvilCraftExtrapower.of("block/nuclear_collector_head_overheated"))
        );

        // FrostController
        event.register(
                FrostControllerRenderer.CUBE,
                SimpleUnbakedStandaloneModel.blockStateModel(AnvilCraftExtrapower.of("block/frost_controller_core"))
        );

        // SolarPanel
        event.register(
                SolarPanelRenderer.HEAD,
                SimpleUnbakedStandaloneModel.blockStateModel(AnvilCraftExtrapower.of("block/solar_panel_head"))
        );
        event.register(
                SolarPanelRenderer.HEAD_SUNFLOWER,
                SimpleUnbakedStandaloneModel.blockStateModel(AnvilCraftExtrapower.of("block/solar_panel_head_sunflower"))
        );
        event.register(
                SolarPanelRenderer.HEAD_CLOSING,
                SimpleUnbakedStandaloneModel.blockStateModel(AnvilCraftExtrapower.of("block/solar_panel_head_closing"))
        );
        event.register(
                SolarPanelRenderer.HEAD_SUNFLOWER_CLOSING,
                SimpleUnbakedStandaloneModel.blockStateModel(AnvilCraftExtrapower.of("block/solar_panel_head_sunflower_closing"))
        );

        // MushroomCloud
        event.register(
                MushroomCloudRenderer.HEAD_MODEL,
                SimpleUnbakedStandaloneModel.blockStateModel(AnvilCraftExtrapower.of("block/mushroom_cloud"))
        );
        event.register(
                MushroomCloudRenderer.BOTTOM_MODEL,
                SimpleUnbakedStandaloneModel.blockStateModel(AnvilCraftExtrapower.of("block/mushroom_cloud_bottom"))
        );
        event.register(
                MushroomCloudRenderer.TOP_SIDE_MODEL,
                SimpleUnbakedStandaloneModel.blockStateModel(AnvilCraftExtrapower.of("block/mushroom_cloud_top_side"))
        );
        event.register(
                MushroomCloudRenderer.EPICENTER_MODEL,
                SimpleUnbakedStandaloneModel.blockStateModel(AnvilCraftExtrapower.of("block/mushroom_epicenter"))
        );
        event.register(
                MushroomCloudRenderer.CIRCLE_MODEL,
                SimpleUnbakedStandaloneModel.blockStateModel(AnvilCraftExtrapower.of("block/nuclear_bomb_circle"))
        );
    }
}