package com.extra.power.block.just_block;

import com.extra.power.block.ModBlockEntity;
import com.extra.power.block.blockentity.BurningMagnesiumBlockEntity;
import com.extra.power.block.blockentity.MushroomCloudBlockEntity;
import com.mojang.serialization.MapCodec;
import dev.dubhe.anvilcraft.block.better.BetterBaseEntityBlock;
import net.minecraft.core.BlockPos;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public class MushroomCloudBlock extends BetterBaseEntityBlock {
    public MushroomCloudBlock(Properties Properties) {
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
