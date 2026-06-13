package com.extra.power.integration.jade.provider;

import com.extra.power.block.blockentity.NuclearCollectorBlockEntity;
import com.extra.power.config.ModServerConfig;
import com.extra.power.init.AnvilCraftExtrapower;
import net.minecraft.ChatFormatting;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import snownee.jade.api.BlockAccessor;
import snownee.jade.api.IBlockComponentProvider;
import snownee.jade.api.IServerDataProvider;
import snownee.jade.api.ITooltip;
import snownee.jade.api.config.IPluginConfig;

public enum NuclearCollectorProvider implements IBlockComponentProvider, IServerDataProvider<BlockAccessor> {
    INSTANCE;

    @Override
    public void appendTooltip(ITooltip tooltip, BlockAccessor accessor, IPluginConfig config) {
        CompoundTag serverData = accessor.getServerData();
        if (serverData.contains("heat")) {
            tooltip.add(Component.translatable(
                    "tooltip.anvilcraftextrapower.nuclear_collector.heat",
                    serverData.getInt("heat"),serverData.getInt("max_heat")));
        }
        if (serverData.contains("result")) {
            switch (serverData.getInt("result")) {
                case 0 ->
                        tooltip.add(Component.translatable("tooltip.anvilcraftextrapower.nuclear_collector.status.working")
                                .withStyle(ChatFormatting.GREEN));
                case 1 ->
                        tooltip.add(Component.translatable("tooltip.anvilcraftextrapower.nuclear_collector.status.too_close")
                                .withStyle(ChatFormatting.RED));
                case 2 ->
                        tooltip.add(Component.translatable("tooltip.anvilcraftextrapower.nuclear_collector.status.too_hot")
                                .withStyle(ChatFormatting.RED));
                case 3 ->
                        tooltip.add(Component.translatable("tooltip.anvilcraftextrapower.nuclear_collector.status.no_rod")
                                .withStyle(ChatFormatting.RED));
                case 4 ->
                        tooltip.add(Component.translatable("tooltip.anvilcraftextrapower.nuclear_collector.status.invalid_range")
                                .withStyle(ChatFormatting.RED));
            }
        }
    }

    @Override
    public void appendServerData(CompoundTag tag, BlockAccessor accessor) {
        if (accessor.getBlockEntity() instanceof NuclearCollectorBlockEntity entity) {
            tag.putInt("heat", entity.getHeat());
            tag.putInt("max_heat", ModServerConfig.nuclearCollector.baseHeatLimit);
            tag.putInt("result", entity.getWorkResult());
        }
    }

    @Override
    public ResourceLocation getUid() {
        return AnvilCraftExtrapower.of("nuclear_collector");
    }
}
