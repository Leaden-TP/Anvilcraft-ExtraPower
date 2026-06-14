package com.extra.power.init;

import net.minecraft.core.registries.Registries;
import net.minecraft.sounds.SoundEvent;
import net.neoforged.neoforge.registries.DeferredRegister;

import java.util.function.Supplier;

public class ModSounds {
    public static final DeferredRegister<SoundEvent> SOUNDS =
            DeferredRegister.create(Registries.SOUND_EVENT, AnvilCraftExtrapower.MODID);

    public static final Supplier<SoundEvent> NUCLEAR_EXPLOSION =
            registerSound("nuclear_explosion");

    private static Supplier<SoundEvent> registerSound(String name) {
        return SOUNDS.register(name, () -> SoundEvent.createVariableRangeEvent(AnvilCraftExtrapower.of(name)));
    }
}
