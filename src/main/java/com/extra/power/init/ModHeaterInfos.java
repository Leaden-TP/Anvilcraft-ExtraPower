package com.extra.power.init;

import com.extra.power.block.ModBlockEntity;
import com.extra.power.block.blockentity.BurningCoalBlockEntity;
import com.extra.power.block.blockentity.BurningMagnesiumBlockEntity;
import com.extra.power.block.just_block.BurningMagnesiumBlock;
import dev.dubhe.anvilcraft.api.heat.HeatRecorder;
import dev.dubhe.anvilcraft.api.heat.HeatTier;
import dev.dubhe.anvilcraft.api.heat.HeatTierLine;
import dev.dubhe.anvilcraft.api.heat.HeaterInfo;


import java.util.Set;

public class ModHeaterInfos {
    public static final HeaterInfo<BurningCoalBlockEntity> COAL_BURNING = HeatRecorder.registerProducerInfo(
            HeaterInfo.blockEntity(
                    (level, pos) -> level.getBlockEntity(pos, ModBlockEntity.BURNING_COAL.get()),
                    burningCoalBlock -> Set.of(burningCoalBlock.getBlockPos().above()),
                    HeatTierLine.always(HeatTier.REDHOT, 10))
    );
    public static final HeaterInfo<BurningMagnesiumBlockEntity> MAGNESIUM_BURNING = HeatRecorder.registerProducerInfo(
            HeaterInfo.blockEntity(
                    (level, pos) -> level.getBlockEntity(pos, ModBlockEntity.BURNING_MAGNESIUM.get()),
                    burningMagnesiumBlock -> Set.of(burningMagnesiumBlock.getBlockPos().above()),
                    HeatTierLine.always(HeatTier.INCANDESCENT, 10))
    );
    public static final HeaterInfo<BurningMagnesiumBlockEntity> MAGNESIUM_OVERHEATED = HeatRecorder.registerProducerInfo(
            HeaterInfo.blockEntity(
                    (level, pos) -> level.getBlockEntity(pos, ModBlockEntity.BURNING_MAGNESIUM.get()),
                    burningMagnesiumBlock -> Set.of(burningMagnesiumBlock.getBlockPos().above()),
                    HeatTierLine.always(HeatTier.OVERHEATED, 1))
    );
}
