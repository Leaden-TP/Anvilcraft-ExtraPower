package com.extra.power.init;

import com.extra.power.advancements.criterion.NuclearCollectorTrigger;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.advancements.criterion.HeatCollectorTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCriterionTriggers {
    public static final DeferredRegister<CriterionTrigger<?>> TRIGGER_TYPES =
            DeferredRegister.create(Registries.TRIGGER_TYPE, AnvilCraftExtrapower.MODID);
    public static final DeferredHolder<CriterionTrigger<?>, NuclearCollectorTrigger> NUCLEAR_COLLECTOR_COLLECT = TRIGGER_TYPES.register(
            "nuclear_collector_collect",
            NuclearCollectorTrigger::new
    );
}
