package com.extra.power.client.event;

import com.extra.power.init.AnvilCraftExtrapower;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ModelEvent;

@EventBusSubscriber(value = Dist.CLIENT, bus = EventBusSubscriber.Bus.MOD, modid = AnvilCraftExtrapower.MODID)
public class RegisterAdditionalEventListener {
    /**
     * 注册模型
     */
    @SubscribeEvent
    public static void registerModels(ModelEvent.RegisterAdditional event) {
        event.register(ModelResourceLocation.standalone(AnvilCraftExtrapower.of("block/nuclear_collector_head")));
        event.register(ModelResourceLocation.standalone(AnvilCraftExtrapower.of("block/nuclear_collector_head_overheated")));
    }
}
