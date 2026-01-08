package com.extra.power.block;

import com.extra.power.block.blockentity.*;
import com.extra.power.client.renderer.blockentity.NuclearCollectorRenderer;
import com.extra.power.init.AnvilCraftExtrapower;

import com.tterrag.registrate.util.entry.BlockEntityEntry;
import net.minecraft.core.registries.Registries;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.neoforged.neoforge.registries.DeferredRegister;
import java.util.function.Supplier;
import static com.extra.power.block.ModBlock.*;
import static com.extra.power.init.AnvilCraftExtrapower.REGISTRATE;

public class ModBlockEntity {
    public static final DeferredRegister<BlockEntityType<?>> BLOCK_ENTITYS =
            DeferredRegister.create(Registries.BLOCK_ENTITY_TYPE, AnvilCraftExtrapower.MODID);

    public static final Supplier<BlockEntityType<SolarPanelBlockEntity>> SOLAR_PANEL= BLOCK_ENTITYS.register(SOLAR_PANEL_BLOCK_ID,
            () -> BlockEntityType.Builder.of(SolarPanelBlockEntity::new, ModBlock.SOLAR_PANEL.get()).build(null));
    public static final Supplier<BlockEntityType<CrateBlockEntity>> CRATE= BLOCK_ENTITYS.register("crate",
            () -> BlockEntityType.Builder.of(CrateBlockEntity::new, CRATE_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<BurningCoalBlockEntity>> BURNING_COAL= BLOCK_ENTITYS.register(BURNING_COAL_BLOCK_ID,
            () -> BlockEntityType.Builder.of(BurningCoalBlockEntity::new, ModBlock.BURNING_COAL_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<BurningMagnesiumBlockEntity>> BURNING_MAGNESIUM= BLOCK_ENTITYS.register(BURNING_MAGNESIUM_BLOCK_ID,
            () -> BlockEntityType.Builder.of(BurningMagnesiumBlockEntity::new, ModBlock.BURNING_MAGNESIUM_BLOCK.get()).build(null));
    public static final BlockEntityEntry<NuclearCollectorBlockEntity> NUCLEAR_COLLECTOR =
            REGISTRATE.blockEntity("nuclear_collector", NuclearCollectorBlockEntity::createBlockEntity)
                    .validBlock(ModBlock.NUCLEAR_COLLECTOR)
                    .renderer(() -> NuclearCollectorRenderer::new)
                    .register();
    public static void register() {
    }
}
