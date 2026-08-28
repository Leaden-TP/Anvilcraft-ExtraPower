package com.extra.power.fluid;

import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.neoforged.neoforge.fluids.BaseFlowingFluid;
import org.jetbrains.annotations.NotNull;

public class NoWorldFluid extends BaseFlowingFluid {
    public NoWorldFluid(Properties properties) {
        super(properties);
    }

    @Override
    public boolean isSource(@NotNull FluidState state) {
        return false;
    }

    @Override
    public int getAmount(@NotNull FluidState state) {
        return 0;
    }

    @Override
    protected @NotNull BlockState createLegacyBlock(@NotNull FluidState state) {
        return Blocks.AIR.defaultBlockState();
    }
}
