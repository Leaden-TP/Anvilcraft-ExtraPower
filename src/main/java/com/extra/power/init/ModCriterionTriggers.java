package com.extra.power.init;

import com.extra.power.advancements.criterion.AnvilProjectionTrigger;
import com.extra.power.advancements.criterion.FrostControllerTrigger;
import com.extra.power.advancements.criterion.IgniteMagnesiumTrigger;
import com.extra.power.advancements.criterion.MagnetAttractAnvilTrigger;
import com.extra.power.advancements.criterion.MagneticDisplayStandTrigger;
import com.extra.power.advancements.criterion.NuclearCollectorTrigger;
import com.extra.power.advancements.criterion.NuclearExplosionHurtTrigger;
import com.extra.power.advancements.criterion.UraniumRodRadiationTrigger;
import net.minecraft.advancements.CriterionTrigger;
import net.minecraft.core.registries.Registries;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModCriterionTriggers {
    private static final DeferredRegister<CriterionTrigger<?>> REGISTER =
        DeferredRegister.create(Registries.TRIGGER_TYPE, AnvilCraftExtrapower.MODID);

    public static final DeferredHolder<CriterionTrigger<?>, IgniteMagnesiumTrigger> IGNITE_MAGNESIUM =
        REGISTER.register("ignite_magnesium", IgniteMagnesiumTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, UraniumRodRadiationTrigger> URANIUM_ROD_RADIATION =
        REGISTER.register("uranium_rod_radiation", UraniumRodRadiationTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, FrostControllerTrigger> FROST_CONTROLLER =
        REGISTER.register("frost_controller", FrostControllerTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, NuclearCollectorTrigger> NUCLEAR_COLLECTOR =
        REGISTER.register("nuclear_collector", NuclearCollectorTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, NuclearExplosionHurtTrigger> NUCLEAR_EXPLOSION_HURT =
        REGISTER.register("nuclear_explosion_hurt", NuclearExplosionHurtTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, MagnetAttractAnvilTrigger> MAGNET_ATTRACT_ANVIL =
        REGISTER.register("magnet_attract_anvil", MagnetAttractAnvilTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, AnvilProjectionTrigger> ANVIL_PROJECTION =
        REGISTER.register("anvil_projection", AnvilProjectionTrigger::new);

    public static final DeferredHolder<CriterionTrigger<?>, MagneticDisplayStandTrigger> MAGNETIC_DISPLAY_STAND =
        REGISTER.register("magnetic_display_stand", MagneticDisplayStandTrigger::new);

    public static void register(IEventBus eventBus) {
        REGISTER.register(eventBus);
    }
}
