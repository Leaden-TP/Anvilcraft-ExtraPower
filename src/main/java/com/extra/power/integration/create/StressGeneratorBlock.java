package com.extra.power.integration.create;

import com.simibubi.create.content.kinetics.base.RotatedPillarKineticBlock;
import com.simibubi.create.foundation.block.IBE;
import dev.dubhe.anvilcraft.api.hammer.IHammerRemovable;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;

/**
 * 应力发电机。
 *
 * <p>默认是发电机：由 Create 的传动网络驱动，把应力（SU）转化为电力。
 * 被铁砧砸击后转化为电动机：消耗电力，以固定转速为传动网络提供应力。</p>
 *
 * <p>模型默认朝向为南北方向（{@code axis=z}），通过 {@code axis} 属性支持上下、南北、东西 6 个方向。</p>
 */
public class StressGeneratorBlock extends RotatedPillarKineticBlock implements IBE<StressGeneratorBlockEntity>,
    IHammerRemovable {
    /** 是否已转化为电动机 */
    public static final BooleanProperty MOTOR = BooleanProperty.create("motor");

    public StressGeneratorBlock(Properties properties) {
        super(properties);
        registerDefaultState(defaultBlockState().setValue(MOTOR, false));
    }

    public static boolean isMotor(BlockState state) {
        return state.hasProperty(MOTOR) && state.getValue(MOTOR);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        super.createBlockStateDefinition(builder.add(MOTOR));
    }

    @Override
    public Class<StressGeneratorBlockEntity> getBlockEntityClass() {
        return StressGeneratorBlockEntity.class;
    }

    @Override
    public BlockEntityType<? extends StressGeneratorBlockEntity> getBlockEntityType() {
        return StressGeneratorRegistration.blockEntityType();
    }

    @Override
    public Axis getRotationAxis(BlockState state) {
        return state.getValue(AXIS);
    }

    /** 相对的两面有裸露的传动轴 */
    @Override
    public boolean hasShaftTowards(LevelReader level, BlockPos pos, BlockState state, Direction face) {
        return face.getAxis() == state.getValue(AXIS);
    }
}
