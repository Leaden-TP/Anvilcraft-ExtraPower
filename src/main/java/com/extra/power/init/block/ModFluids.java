package com.extra.power.init.block;


import com.extra.power.fluid.NoWorldFluid;
import com.extra.power.init.AnvilCraftExtrapower;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.level.material.Fluid;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import net.neoforged.neoforge.fluids.FluidType;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.neoforged.neoforge.registries.NeoForgeRegistries;

import java.util.function.Supplier;

public class ModFluids {
    public static void register(IEventBus eventBus) {
        FLUIDS.register(eventBus);
        FLUID_TYPES.register(eventBus);
    }
    private static final DeferredRegister<Fluid> FLUIDS = DeferredRegister.create(Registries.FLUID, AnvilCraftExtrapower.MODID);
    private static final DeferredRegister<FluidType> FLUID_TYPES = DeferredRegister.create(NeoForgeRegistries.FLUID_TYPES, AnvilCraftExtrapower.MODID);

    public static final DeferredHolder<FluidType, FluidType> NUTRIENT_LIQUID_TYPE = FLUID_TYPES.register("nutrient_liquid", () -> new FluidType(FluidType.Properties.create()));

    public static final DeferredHolder<Fluid, NoWorldFluid> NUTRIENT_LIQUID = registerNoWorldFluid("nutrient_liquid",NUTRIENT_LIQUID_TYPE::value, () -> Items.AIR);

    private static DeferredHolder<Fluid, NoWorldFluid> registerNoWorldFluid(String name, Supplier<FluidType> fluidType, Supplier<? extends Item> bucket) {
        DeferredHolder<Fluid, NoWorldFluid> holder = DeferredHolder.create(Registries.FLUID, AnvilCraftExtrapower.of(name));
        BaseFlowingFluid.Properties properties = new BaseFlowingFluid.Properties(fluidType, holder::value, holder::value).bucket(bucket);
        FLUIDS.register(name, () -> new NoWorldFluid(properties));
        return holder;
    }
}
