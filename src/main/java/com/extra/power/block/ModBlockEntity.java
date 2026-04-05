package com.extra.power.block;

import com.extra.power.block.blockentity.*;
import com.extra.power.client.renderer.blockentity.FrostControllerRenderer;
import com.extra.power.client.renderer.blockentity.MushroomCloudRenderer;
import com.extra.power.client.renderer.blockentity.NuclearCollectorRenderer;
import com.extra.power.client.renderer.blockentity.SolarPanelRenderer;
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

    public static final Supplier<BlockEntityType<CrateBlockEntity>> CRATE= BLOCK_ENTITYS.register("crate",
            () -> BlockEntityType.Builder.of(CrateBlockEntity::new, CRATE_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<BurningCoalBlockEntity>> BURNING_COAL= BLOCK_ENTITYS.register("burning_coal_block",
            () -> BlockEntityType.Builder.of(BurningCoalBlockEntity::new, ModBlock.BURNING_COAL_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<BurningMagnesiumBlockEntity>> BURNING_MAGNESIUM= BLOCK_ENTITYS.register("burning_magnesium_block",
            () -> BlockEntityType.Builder.of(BurningMagnesiumBlockEntity::new, ModBlock.BURNING_MAGNESIUM_BLOCK.get()).build(null));
    public static final Supplier<BlockEntityType<UraniumRodBlockEntity>> URANIUM_ROD= BLOCK_ENTITYS.register("uranium_rod",
            () -> BlockEntityType.Builder.of(UraniumRodBlockEntity::new, ModBlock.URANIUM_ROD.get()).build(null));
    public static final BlockEntityEntry<NuclearCollectorBlockEntity> NUCLEAR_COLLECTOR =
            REGISTRATE.blockEntity("nuclear_collector", NuclearCollectorBlockEntity::createBlockEntity)
                    .validBlock(ModBlock.NUCLEAR_COLLECTOR)
                    .renderer(() -> NuclearCollectorRenderer::new)
                    .register();
    public static final BlockEntityEntry<MushroomCloudBlockEntity>  MUSHROOM_CLOUD =
            REGISTRATE.blockEntity("mushroom_cloud", MushroomCloudBlockEntity::createBlockEntity)
                    .validBlock(ModBlock.MUSHROOM_CLOUD)
                    .renderer(() -> MushroomCloudRenderer::new)
                    .register();
    public static final BlockEntityEntry<FrostControllerBlockEntity> FROST_CONTROLLER=
            REGISTRATE.blockEntity("frost_controller", FrostControllerBlockEntity::createBlockEntity)
                    .validBlock(ModBlock.FROST_CONTROLLER)
                    .renderer(() -> FrostControllerRenderer::new)
                    .register();
    public static final BlockEntityEntry<SolarPanelBlockEntity> SOLAR_PANEL=
            REGISTRATE.blockEntity("solar_panel", SolarPanelBlockEntity::createBlockEntity)
                    .validBlock(ModBlock.SOLAR_PANEL)
                    .renderer(() -> SolarPanelRenderer::new)
                    .register();
    public static void register() {
    }
}
