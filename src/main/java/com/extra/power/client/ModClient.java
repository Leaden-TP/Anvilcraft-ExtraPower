package com.extra.power.client;

import com.extra.power.init.AnvilCraftExtrapower;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;

@Mod(value = AnvilCraftExtrapower.MODID, dist = Dist.CLIENT)
public class ModClient {
    public ModClient(IEventBus modBus, ModContainer container) {
    }
}
