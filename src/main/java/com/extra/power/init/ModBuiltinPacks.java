package com.extra.power.init;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.server.packs.PackType;
import net.minecraft.server.packs.repository.Pack;
import net.minecraft.server.packs.repository.PackSource;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.event.AddPackFindersEvent;
import org.jetbrains.annotations.NotNull;

import java.util.function.UnaryOperator;

@EventBusSubscriber(modid = AnvilCraftExtrapower.MODID, bus = EventBusSubscriber.Bus.MOD)
public class ModBuiltinPacks {
    public static final PackSource BUILT_IN = PackSource.create(decorateWithSource("pack.source.builtin"), false);

    @SubscribeEvent
    public static void packSetup(@NotNull AddPackFindersEvent event) {
        event.addPackFinders(
                AnvilCraftExtrapower.of("resourcepacks/x_squareful"),
                PackType.CLIENT_RESOURCES,
                Component.translatable("pack.anvilcraftextrapower.builtin_pack"),
                com.extra.power.init.ModBuiltinPacks.BUILT_IN,
                false,
                Pack.Position.TOP
        );
    }

    @SuppressWarnings("SameParameterValue")
    private static @NotNull UnaryOperator<Component> decorateWithSource(String translationKey) {
        Component component = Component.translatable(translationKey);
        return component1 -> Component.translatable("pack.nameAndSource", component1, component).withStyle(ChatFormatting.GRAY);
    }
}
