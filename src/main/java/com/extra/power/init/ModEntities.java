package com.extra.power.init;

import com.extra.power.client.entity.TechnicalArrowRender;
import com.extra.power.entity.TechnicalArrowEntity;
import dev.anvilcraft.lib.v2.registrum.util.entry.EntityEntry;
import net.minecraft.core.registries.Registries;
import net.neoforged.neoforge.registries.DeferredRegister;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;


public class ModEntities {
    public static final DeferredRegister<EntityType<?>> ENTITY_TYPES =
            DeferredRegister.create(Registries.ENTITY_TYPE, AnvilCraftExtrapower.MODID);


    public static final EntityEntry<? extends TechnicalArrowEntity> TECHNICAL_ARROW = AnvilCraftExtrapower.REGISTRATE
            .<TechnicalArrowEntity>entity("technical_arrow", TechnicalArrowEntity::new, MobCategory.MISC)
            .properties(it -> it.
                    sized(0.5F, 0.5F)
                    .clientTrackingRange(4)
                    .updateInterval(20))
            .renderer(() -> TechnicalArrowRender::new)
            .register();

    public static void register() {
    }
}
