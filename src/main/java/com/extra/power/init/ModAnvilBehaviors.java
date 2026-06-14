package com.extra.power.init;


import com.extra.power.anvil.HitAnvilProjectorBehavior;
import com.extra.power.block.ModBlock;
import dev.dubhe.anvilcraft.api.event.AnvilBehaviorRegisterEvent;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;

@EventBusSubscriber(modid = AnvilCraftExtrapower.MODID)
public class ModAnvilBehaviors {
    @SubscribeEvent
    public static void register(AnvilBehaviorRegisterEvent event) {
        event.registerBehavior(ModBlock.ANVIL_PROJECTOR.get(), new HitAnvilProjectorBehavior());
    }
}
