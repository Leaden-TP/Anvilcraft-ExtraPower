package com.extra.power.integration.create;

import com.google.common.collect.ImmutableList;
import com.simibubi.create.foundation.blockEntity.SmartBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueBoxTransform;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBoard;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsBehaviour.ValueSettings;
import com.simibubi.create.foundation.blockEntity.behaviour.ValueSettingsFormatter;
import com.simibubi.create.foundation.blockEntity.behaviour.scrollValue.ScrollValueBehaviour;
import net.minecraft.network.chat.Component;
import net.minecraft.network.chat.MutableComponent;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.BlockHitResult;

/**
 * 应力发电机的力电系数滑条。
 *
 * <p>滑条数值即「力电系数」：发电机模式下决定 {@code 1 RPM} 消耗的应力，
 * 电动机模式下决定应力产出与耗电量。</p>
 */
public class StressGeneratorValueBehaviour extends ScrollValueBehaviour {

    public StressGeneratorValueBehaviour(SmartBlockEntity blockEntity, ValueBoxTransform slot) {
        super(
            Component.translatable("tooltip.anvilcraftextrapower.stress_generator.slider"),
            blockEntity,
            slot
        );
        withFormatter(value -> Integer.toString(Math.max(StressGeneratorBlockEntity.MIN_VALUE, value)));
    }

    @Override
    public ValueSettingsBoard createBoard(Player player, BlockHitResult hitResult) {
        int limit = Math.max(StressGeneratorBlockEntity.MIN_VALUE, this.max);
        return new ValueSettingsBoard(
            label,
            limit,
            Math.max(1, limit / 8),
            ImmutableList.of(Component.translatable("tooltip.anvilcraftextrapower.stress_generator.slider_row")),
            new ValueSettingsFormatter(this::formatSettings)
        );
    }

    private MutableComponent formatSettings(ValueSettings settings) {
        int value = Mth.clamp(
            settings.value(),
            StressGeneratorBlockEntity.MIN_VALUE,
            Math.max(StressGeneratorBlockEntity.MIN_VALUE, this.max)
        );
        return Component.literal(Integer.toString(value));
    }

    @Override
    public String getClipboardKey() {
        return "StressPowerCoefficient";
    }
}
