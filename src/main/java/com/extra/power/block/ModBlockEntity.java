package com.extra.power.block;

import com.extra.power.block.blockentity.BurningMagnesiumBlockEntity;
import com.extra.power.init.AnvilCraftExtrapower;

import com.extra.power.block.blockentity.BurningCoalBlockEntity;
import com.extra.power.block.blockentity.SolarPanelBlockEntity;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;
import static com.extra.power.block.ModBlock.*;

public class ModBlockEntity {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITYS =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AnvilCraftExtrapower.MODID);

    public static final Supplier<BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL= BLOCK_ENTITYS.register(SOLAR_PANEL_BLOCK_ID,
            () -> BlockEntityType.Builder.of(SolarPanelBlockEntity::new, ModBlock.SOLAR_PANEL.get()).build(null));
    public static final Supplier<BlockEntityType<BurningCoalBlockEntity>> BURNING_COAL= BLOCK_ENTITYS.register(BURNING_COAL_BLOCK_ID,
            () -> BlockEntityType.Builder.of(BurningCoalBlockEntity::new, ModBlock.BURNING_COAL_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<BurningMagnesiumBlockEntity>> BURNING_MAGNESIUM= BLOCK_ENTITYS.register(BURNING_MAGNESIUM_BLOCK_ID,
            () -> BlockEntityType.Builder.of(BurningMagnesiumBlockEntity::new, ModBlock.BURNING_MAGNESIUM_BLOCK.get()).build(null));
}
