package com.extra.power.api.tooltip.impl;

import com.extra.power.block.blockentity.RedstoneLinkBlockEntity;
import com.extra.power.block.just_block.RedstoneLinkColor;
import dev.dubhe.anvilcraft.api.tooltip.providers.ITooltipProvider;
import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.block.entity.BlockEntity;

import java.util.ArrayList;
import java.util.List;

/**
 * 为玩家提供红石中继器的红石信号强度、颜色模式、接收模式与信号标记等信息。
 */
public class RedstoneLinkTooltipProvider extends ITooltipProvider.BlockEntityTooltipProvider {

    @Override
    public boolean accepts(BlockEntity entity) {
        return entity instanceof RedstoneLinkBlockEntity;
    }

    @Override
    public List<Component> tooltip(BlockEntity entity) {
        if (!(entity instanceof RedstoneLinkBlockEntity link)) {
            return null;
        }

        List<Component> lines = new ArrayList<>();
        lines.add(Component.translatable("tooltip.anvilcraftextrapower.redstone_link.title")
                .withStyle(ChatFormatting.GOLD));

        RedstoneLinkColor color = link.getColor();
        ChatFormatting colorFormat = switch (color) {
            case RED -> ChatFormatting.RED;
            case YELLOW -> ChatFormatting.YELLOW;
            case BLUE -> ChatFormatting.AQUA;
        };
        lines.add(Component.translatable("tooltip.anvilcraftextrapower.redstone_link.color",
                        Component.translatable("tooltip.anvilcraftextrapower.redstone_link.color."
                                + color.name().toLowerCase()))
                .withStyle(colorFormat));

        lines.add(Component.translatable("tooltip.anvilcraftextrapower.redstone_link.mode",
                        Component.translatable(link.isReceiveMode()
                                ? "tooltip.anvilcraftextrapower.redstone_link.mode.receive"
                                : "tooltip.anvilcraftextrapower.redstone_link.mode.send"))
                .withStyle(ChatFormatting.GRAY));

        lines.add(Component.translatable("tooltip.anvilcraftextrapower.redstone_link.signal", link.getSignal())
                .withStyle(ChatFormatting.GRAY));

        if (color == RedstoneLinkColor.BLUE) {
            if (link.getBoundPos() != null) {
                lines.add(Component.translatable("tooltip.anvilcraftextrapower.redstone_link.bound",
                                link.getBoundPos().toShortString())
                        .withStyle(ChatFormatting.AQUA));
            } else {
                lines.add(Component.translatable("tooltip.anvilcraftextrapower.redstone_link.bound.none")
                        .withStyle(ChatFormatting.DARK_GRAY));
            }
            lines.add(Component.translatable("tooltip.anvilcraftextrapower.redstone_link.barrier")
                    .withStyle(ChatFormatting.DARK_GRAY));
        } else {
            ItemStack marker = link.getMarker();
            if (marker.isEmpty()) {
                lines.add(Component.translatable("tooltip.anvilcraftextrapower.redstone_link.marker",
                                Component.translatable("tooltip.anvilcraftextrapower.redstone_link.marker.none"))
                        .withStyle(ChatFormatting.DARK_GRAY));
            } else {
                lines.add(Component.translatable("tooltip.anvilcraftextrapower.redstone_link.marker",
                                marker.getHoverName())
                        .withStyle(ChatFormatting.GRAY));
            }
        }
        return lines;
    }

    @Override
    public int priority() {
        return 0;
    }
}
