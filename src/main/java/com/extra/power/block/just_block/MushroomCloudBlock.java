package com.extra.power.block.just_block;

import com.extra.power.init.block.ModBlockEntity;
import com.extra.power.block.blockentity.MushroomCloudBlockEntity;
import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.block.better.BetterBaseEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class MushroomCloudBlock extends BetterBaseEntityBlock {
    public MushroomCloudBlock(BlockBehaviour.Properties Properties) {
        super(Properties);
    }
    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, @NotNull BlockState state) {
        return new MushroomCloudBlockEntity(pos,state);
    }
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return simpleCodec(MushroomCloudBlock::new);
    }
    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level level, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntity.MUSHROOM_CLOUD.get(), MushroomCloudBlockEntity::tick);
    }
}
