package com.extra.power.data.advancements;

import com.extra.power.init.ModAdvancements;
import com.tterrag.registrate.providers.RegistrateAdvancementProvider;
import dev.dubhe.anvilcraft.util.Util;
import lombok.SneakyThrows;

import java.lang.reflect.Field;
import java.lang.reflect.Modifier;

public class AnvilCraftEPAdvancement {
    @SneakyThrows
    public static void init(RegistrateAdvancementProvider provider) {
        for (Field field : ModAdvancements.class.getDeclaredFields()) {
            int modifiers = field.getModifiers();
            if (Modifier.isPublic(modifiers) && Modifier.isStatic(modifiers) && Modifier.isFinal(modifiers)) {
                provider.accept(Util.cast(field.get(null)));
            }
        }
    }
}
