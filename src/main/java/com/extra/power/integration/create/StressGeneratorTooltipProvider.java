package com.extra.power.integration.create;

import dev.dubhe.anvilcraft.api.tooltip.providers.ITooltipProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/** 为玩家提供应力发电机的模式、力电系数、转速与应力／电力关系等信息 */
public class StressGeneratorTooltipProvider extends ITooltipProvider.BlockEntityTooltipProvider {
    private static final String KEY = "tooltip.anvilcraftextrapower.stress_generator.";

    @Override
    public boolean accepts(BlockEntity entity) {
        return entity instanceof StressGeneratorBlockEntity;
    }

    @Override
    public List<Component> tooltip(BlockEntity entity) {
        if (!(entity instanceof StressGeneratorBlockEntity generator)) return null;

        boolean motor = generator.isMotor();
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable(KEY + "title").withStyle(ChatFormatting.GOLD));

        lines.add(Component.translatable(
            KEY + "mode",
            Component.translatable(KEY + "mode." + (motor ? "motor" : "generator"))
        ).withStyle(motor ? ChatFormatting.AQUA : ChatFormatting.GREEN));

        lines.add(Component.translatable(KEY + "coefficient", generator.ratioValue())
            .withStyle(ChatFormatting.YELLOW));

        lines.add(Component.translatable(
            KEY + "speed",
            StressGeneratorBlockEntity.formatNumber(Math.abs(generator.getSpeed()))
        ).withStyle(ChatFormatting.GRAY));

        if (motor) {
            lines.add(Component.translatable(
                KEY + "stress.provided",
                StressGeneratorBlockEntity.formatNumber(generator.providedStress())
            ).withStyle(ChatFormatting.AQUA));
            lines.add(Component.translatable(KEY + "power.consuming", generator.consumedPower())
                .withStyle(ChatFormatting.RED));
        } else {
            lines.add(Component.translatable(
                KEY + "ratio",
                StressGeneratorBlockEntity.formatNumber(generator.stressPerRpm())
            ).withStyle(ChatFormatting.YELLOW));
            lines.add(Component.translatable(
                KEY + "stress.consumed",
                StressGeneratorBlockEntity.formatNumber(generator.consumedStress())
            ).withStyle(ChatFormatting.AQUA));
            lines.add(Component.translatable(KEY + "power.generating", generator.generatedPower())
                .withStyle(ChatFormatting.GREEN));
        }

        return lines;
    }

    @Override
    public int priority() {
        return 0;
    }
}
