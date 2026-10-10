package com.extra.power.integration.create;

import com.simibubi.create.content.kinetics.base.KineticBlockEntity;
import dev.dubhe.anvilcraft.api.anvil.IAnvilBehavior;
import dev.dubhe.anvilcraft.api.event.AnvilEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

/** 被铁砧砸击时把应力发电机转化为电动机 */
public class StressGeneratorAnvilBehavior implements IAnvilBehavior {

    @Override
    public boolean handle(
        Level level,
        BlockPos hitBlockPos,
        BlockState hitBlockState,
        float fallDistance,
        AnvilEvent.OnLand event
    ) {
        if (level.isClientSide()) return false;
        if (!(hitBlockState.getBlock() instanceof StressGeneratorBlock)) return false;
        if (StressGeneratorBlock.isMotor(hitBlockState)) return false;
        KineticBlockEntity.switchToBlockState(
            level,
            hitBlockPos,
            hitBlockState.setValue(StressGeneratorBlock.MOTOR, true)
        );
        return true;
    }
}
