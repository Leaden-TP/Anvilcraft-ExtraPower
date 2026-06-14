package com.extra.power.anvil;

import com.extra.power.block.just_block.AnvilProjectorBlock;
import dev.dubhe.anvilcraft.api.anvil.IAnvilBehavior;
import dev.dubhe.anvilcraft.api.event.AnvilEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;

public class HitAnvilProjectorBehavior implements IAnvilBehavior {

    @Override
    public boolean handle(ServerLevel level, BlockPos hitBlockPos, BlockState hitBlockState, double fallDistance, AnvilEvent.OnLand event) {
        if (!(level instanceof ServerLevel serverLevel)) return false;

        AnvilProjectorBlock block = (AnvilProjectorBlock) hitBlockState.getBlock();
        block.getHit(level,hitBlockState ,hitBlockPos, (float) fallDistance);
        return true;
    }
}
