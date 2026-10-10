package com.extra.power.integration.create;

import com.extra.power.config.ModServerConfig;
import com.simibubi.create.content.kinetics.base.GeneratingKineticBlockEntity;
import com.simibubi.create.foundation.blockEntity.behaviour.BlockEntityBehaviour;
import dev.dubhe.anvilcraft.api.power.IPowerConsumer;
import dev.dubhe.anvilcraft.api.power.IPowerProducer;
import dev.dubhe.anvilcraft.api.power.PowerComponentType;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.AxisDirection;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import java.util.Locale;

import static com.extra.power.init.AnvilCraftExtrapower.CONFIG;

/**
 * 应力发电机。
 *
 * <p>滑条数值是「力电系数」，用于调节机械（应力）与电力之间的换算关系。</p>
 *
 * <p>发电机模式（默认）：被 Create 的传动网络带动，按系数消耗应力（SU）并转化为电力，<br>
 * {@code 1 RPM = 系数 * stressPerRpmMultiplier SU}（默认 {@code 系数 * 16 SU}），<br>
 * {@code 发电量 = 消耗应力 / stressPerKilowatt kW}（默认每 kW 消耗 8 SU）。</p>
 *
 * <p>电动机模式（被铁砧砸击后）：以固定转速（默认 16 RPM）为传动网络提供应力，<br>
 * {@code 耗电量 = 系数 * motorPowerPerValue kW}，{@code 应力产出 = 系数 * motorSuPerValue SU}（默认 {@code 系数 * 8}）。</p>
 */
public class StressGeneratorBlockEntity extends GeneratingKineticBlockEntity implements IPowerProducer,
    IPowerConsumer {
    /** 滑条允许的最小数值 */
    public static final int MIN_VALUE = 1;

    @Setter
    @Getter
    private PowerGrid grid;
    /** 电动机是否已通电：电网不工作时电动机不输出转动 */
    private boolean motorPowered;
    private StressGeneratorValueBehaviour ratio;

    public StressGeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static StressGeneratorBlockEntity createBlockEntity(
        BlockEntityType<?> type,
        BlockPos pos,
        BlockState state
    ) {
        return new StressGeneratorBlockEntity(type, pos, state);
    }

    // region 配置

    public static ModServerConfig.StressGenerator config() {
        return CONFIG.stressGenerator;
    }

    /** 发电机模式下 1 RPM 消耗的应力系数（默认 16，即 1 RPM = 力电系数 * 16 SU） */
    public static int stressPerRpmMultiplier() {
        return Math.max(1, config().stressPerRpmMultiplier);
    }

    /** 电动机模式下每个力电系数的应力产出 */
    public static int motorSuPerValue() {
        return Math.max(0, config().motorSuPerValue);
    }

    /** 发电机模式下每产出 1 kW 消耗的应力（发电量 = 消耗应力 / 该值） */
    public static int stressPerKilowatt() {
        return Math.max(1, config().stressPerKilowatt);
    }

    /** 电动机模式下每个力电系数的耗电量 */
    public static int motorPowerPerValue() {
        return Math.max(0, config().motorPowerPerValue);
    }

    /** 电动机的固定转速 */
    public static int motorRpm() {
        return Math.max(0, config().motorRpm);
    }

    /** 滑条数值上限 */
    public static int maxValue() {
        return Math.max(MIN_VALUE, config().maxValue);
    }

    /** 新放置的应力发电机的力电系数 */
    public static int defaultValue() {
        return Mth.clamp(config().defaultValue, MIN_VALUE, maxValue());
    }

    // endregion

    // region 行为

    @Override
    public void addBehaviours(List<BlockEntityBehaviour> behaviours) {
        super.addBehaviours(behaviours);
        ratio = new StressGeneratorValueBehaviour(this, new StressGeneratorValueBox());
        ratio.between(MIN_VALUE, maxValue());
        ratio.setValue(defaultValue());
        ratio.withCallback($ -> onValueChanged());
        behaviours.add(ratio);
    }

    @Override
    public void tick() {
        super.tick();
        if (this.level == null || this.level.isClientSide()) return;
        refreshMotorState();
    }

    /** 电网状态变化时重新计算电动机的输出 */
    private void refreshMotorState() {
        boolean powered = isMotor() && isGridWorking();
        if (powered == this.motorPowered) return;
        this.motorPowered = powered;
        updateGeneratedRotation();
    }

    /** 滑条数值变化后刷新应力与电力输出 */
    private void onValueChanged() {
        if (this.level == null || this.level.isClientSide()) return;
        if (isMotor()) {
            updateGeneratedRotation();
        } else if (hasNetwork()) {
            getOrCreateNetwork().updateStressFor(this, calculateStressApplied());
        }
        if (this.grid != null) this.grid.markChanged();
    }

    // endregion

    // region 应力

    public boolean isMotor() {
        return StressGeneratorBlock.isMotor(getBlockState());
    }

    /** 力电系数（至少为 1） */
    public int ratioValue() {
        if (this.ratio == null) return defaultValue();
        return Mth.clamp(this.ratio.getValue(), MIN_VALUE, maxValue());
    }

    /** 发电机模式：每 RPM 消耗的应力（力电系数 * 系数） */
    public float stressPerRpm() {
        return ratioValue() * (float) stressPerRpmMultiplier();
    }

    /** 发电机模式：当前转速下消耗的应力 */
    public float consumedStress() {
        return Math.abs(getSpeed()) * stressPerRpm();
    }

    /** 电动机模式：力电系数对应的应力产出 */
    public float providedStress() {
        return ratioValue() * (float) motorSuPerValue();
    }

    /** 发电机模式：当前发电量（kW = 消耗应力 / 每 kW 发电量消耗 SU） */
    public int generatedPower() {
        return Math.round(consumedStress() / stressPerKilowatt());
    }

    /** 电动机模式：耗电量（kW） */
    public int consumedPower() {
        return Math.round(ratioValue() * (float) motorPowerPerValue());
    }

    @Override
    public float calculateStressApplied() {
        float impact = isMotor() ? 0 : stressPerRpm();
        this.lastStressApplied = impact;
        return impact;
    }

    @Override
    public float calculateAddedStressCapacity() {
        float stress = isMotor() ? providedStress() : 0;
        float speed = Math.abs(getGeneratedSpeed());
        float capacity = stress > 0 && speed > 0 ? stress / speed : 0;
        this.lastCapacityProvided = capacity;
        return capacity;
    }

    @Override
    public float getGeneratedSpeed() {
        if (!isMotor() || !this.motorPowered) return 0;
        int rpm = motorRpm();
        if (rpm == 0) return 0;
        Direction direction = Direction.get(
            AxisDirection.POSITIVE,
            getBlockState().getValue(StressGeneratorBlock.AXIS)
        );
        return convertToDirection(rpm, direction);
    }

    // endregion

    // region 电力

    @Override
    public int getOutputPower() {
        return isMotor() ? 0 : generatedPower();
    }

    @Override
    public int getInputPower() {
        return isMotor() ? consumedPower() : 0;
    }

    @Override
    public PowerComponentType getComponentType() {
        return isMotor() ? PowerComponentType.CONSUMER : PowerComponentType.PRODUCER;
    }

    @Override
    public @Nullable Level getCurrentLevel() {
        return this.level;
    }

    @Override
    public BlockPos getPos() {
        return this.getBlockPos();
    }

    // endregion

    // region 显示

    /** 把浮点数格式化为便于阅读的文本 */
    public static String formatNumber(float value) {
        float absolute = Math.abs(value);
        if (absolute - Math.round(absolute) < 1.0E-4f) return Integer.toString(Math.round(value));
        return String.format(Locale.ROOT, "%.2f", value);
    }

    // endregion
}
