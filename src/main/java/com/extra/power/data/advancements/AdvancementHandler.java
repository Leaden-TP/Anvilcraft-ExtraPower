package com.extra.power.data.advancements;

import com.tterrag.registrate.providers.RegistrateAdvancementProvider;


public class AdvancementHandler {
    public static void init(RegistrateAdvancementProvider provider) {
        AnvilCraftEPAdvancement.init(provider);
    }
}
