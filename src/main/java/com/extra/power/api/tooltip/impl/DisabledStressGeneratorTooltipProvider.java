package com.extra.power.api.tooltip.impl;

import com.extra.power.block.just_block.DisabledStressGeneratorBlock;
import dev.dubhe.anvilcraft.api.tooltip.providers.ITooltipProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.core.BlockPos;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

import java.util.ArrayList;
import java.util.List;

/** 未安装 Create 时提示应力发电机不可用 */
public class DisabledStressGeneratorTooltipProvider extends ITooltipProvider.BlockTooltipProvider {

    @Override
    public boolean accepts(Level level, BlockPos pos, BlockState value) {
        return value.getBlock() instanceof DisabledStressGeneratorBlock;
    }

    @Override
    public List<Component> tooltip(Level level, BlockPos pos, BlockState value) {
        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("tooltip.anvilcraftextrapower.stress_generator.title")
            .withStyle(ChatFormatting.GOLD));
        lines.add(Component.translatable("tooltip.anvilcraftextrapower.stress_generator.missing_create")
            .withStyle(ChatFormatting.RED));
        return lines;
    }

    @Override
    public int priority() {
        return 0;
    }
}
