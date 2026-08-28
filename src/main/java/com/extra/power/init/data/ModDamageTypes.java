package com.extra.power.init.data;

import com.extra.power.init.AnvilCraftExtrapower;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.ResourceKey;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.damagesource.DamageType;
import net.neoforged.neoforge.registries.DeferredRegister;

public class ModDamageTypes {
        public static final DeferredRegister<DamageType> DAMAGE_TYPES =
                DeferredRegister.create(Registries.DAMAGE_TYPE, AnvilCraftExtrapower.MODID);

    public static final ResourceKey<DamageType> NUCLEAR_EXPLOSION =
            ResourceKey.create(Registries.DAMAGE_TYPE, AnvilCraftExtrapower.of("nuclear_explosion"));
    static {
    }
}
