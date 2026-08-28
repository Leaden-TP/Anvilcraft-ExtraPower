package com.extra.power.client;

import com.extra.power.init.AnvilCraftExtrapower;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(value = Dist.CLIENT, modid = AnvilCraftExtrapower.MODID)
public class RegisterAdditionalEventListener {
    /**
     * 注册模型
     */
    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(AnvilCraftExtrapower.of("block/nuclear_collector_head")));
        event.register(ModelResourceLocation.standalone(AnvilCraftExtrapower.of("block/nuclear_collector_head_overheated")));
        event.register(ModelResourceLocation.standalone(AnvilCraftExtrapower.of("block/mushroom_cloud_head")));
        event.register(ModelResourceLocation.standalone(AnvilCraftExtrapower.of("block/mushroom_cloud_bottom")));
        event.register(ModelResourceLocation.standalone(AnvilCraftExtrapower.of("block/mushroom_cloud_top_side")));
        event.register(ModelResourceLocation.standalone(AnvilCraftExtrapower.of("block/mushroom_epicenter")));
        event.register(ModelResourceLocation.standalone(AnvilCraftExtrapower.of("block/nuclear_bomb_circle")));
        event.register(ModelResourceLocation.standalone(AnvilCraftExtrapower.of("block/frost_controller_core")));
        event.register(ModelResourceLocation.standalone(AnvilCraftExtrapower.of("block/solar_panel_head")));
        event.register(ModelResourceLocation.standalone(AnvilCraftExtrapower.of("block/solar_panel_head_sunflower")));
        event.register(ModelResourceLocation.standalone(AnvilCraftExtrapower.of("block/solar_panel_head_closing")));
        event.register(ModelResourceLocation.standalone(AnvilCraftExtrapower.of("block/solar_panel_head_sunflower_closing")));
        event.register(ModelResourceLocation.standalone(AnvilCraftExtrapower.of("block/enchanted_generator_head")));
        event.register(ModelResourceLocation.standalone(AnvilCraftExtrapower.of("block/wind_turbine")));
        event.register(ModelResourceLocation.standalone(AnvilCraftExtrapower.of("entity/arrow")));
    }
}
