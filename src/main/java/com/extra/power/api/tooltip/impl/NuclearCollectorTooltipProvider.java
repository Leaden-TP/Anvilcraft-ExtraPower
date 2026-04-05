package com.extra.power.api.tooltip.impl;

import com.extra.power.block.blockentity.NuclearCollectorBlockEntity;
import com.extra.power.config.ModServerConfig;
import dev.dubhe.anvilcraft.api.tooltip.providers.ITooltipProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.level.block.entity.BlockEntity;


import java.util.ArrayList;
import java.util.List;

public class NuclearCollectorTooltipProvider extends ITooltipProvider.BlockEntityTooltipProvider {

    @Override
    public boolean accepts(BlockEntity entity) {
        return entity instanceof NuclearCollectorBlockEntity;
    }

    @Override
    public List<Component> tooltip(BlockEntity entity) {
        if (!(entity instanceof NuclearCollectorBlockEntity collector)) {
            return null;
        }

        List<Component> lines = new ArrayList<>();

        // 添加基础信息
        lines.add(Component.translatable("tooltip.anvilcraftextrapower.nuclear_collector.title")
                .withStyle(ChatFormatting.GOLD));

        // 添加状态信息
        switch (collector.getWorkResult()) {
            case 0 ->
                lines.add(Component.translatable("tooltip.anvilcraftextrapower.nuclear_collector.status.working")
                        .withStyle(ChatFormatting.GREEN));
            case 1 ->
                lines.add(Component.translatable("tooltip.anvilcraftextrapower.nuclear_collector.status.too_close")
                        .withStyle(ChatFormatting.RED));
            case 2 ->
                lines.add(Component.translatable("tooltip.anvilcraftextrapower.nuclear_collector.status.too_hot")
                        .withStyle(ChatFormatting.RED));
            case 3 ->
                lines.add(Component.translatable("tooltip.anvilcraftextrapower.nuclear_collector.status.no_rod")
                        .withStyle(ChatFormatting.RED));
            case 4 ->
                    lines.add(Component.translatable("tooltip.anvilcraftextrapower.nuclear_collector.status.invalid_range")
                            .withStyle(ChatFormatting.RED));
        }
        int displayHeat = collector.getClientHeat();
        int displayPower_int = collector.getOutputPower()/1000;
        int displayPower_float = (collector.getOutputPower()-displayPower_int*1000)/100 ;
        // 添加详细信息
        if(collector.getWorkResult()==2){
        lines.add(Component.translatable("tooltip.anvilcraftextrapower.nuclear_collector.heat",
                displayHeat,
                ModServerConfig.nuclearCollector.baseHeatLimit)
                .withStyle(ChatFormatting.RED));
        }else {
            lines.add(Component.translatable("tooltip.anvilcraftextrapower.nuclear_collector.heat",
                            displayHeat,
                            ModServerConfig.nuclearCollector.baseHeatLimit)
                    .withStyle(ChatFormatting.GRAY));
        }

        lines.add(Component.translatable("tooltip.anvilcraftextrapower.nuclear_collector.power",
                displayPower_int,displayPower_float)
                .withStyle(ChatFormatting.GRAY));

        return lines;
    }

    @Override
    public int priority() {
        return 0; // 设置较高的优先级
    }
}
