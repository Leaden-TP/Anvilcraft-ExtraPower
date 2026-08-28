package com.extra.power.init.block;

import com.extra.power.block.blockentity.*;
import com.extra.power.client.renderer.blockentity.*;

import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntityEntry;
import dev.dubhe.anvilcraft.init.block.ModBlocks;

import static com.extra.power.init.block.ModBlock.*;
import static com.extra.power.init.AnvilCraftExtrapower.REGISTRATE;

public class ModBlockEntity {
    public static final BlockEntityEntry<CrateBlockEntity> CRATE =
            REGISTRATE.blockEntity("crate", CrateBlockEntity::createBlockEntity)
                    .validBlock(CRATE_BLOCK)
                    .register();

    public static final BlockEntityEntry<BurningCoalBlockEntity> BURNING_COAL =
            REGISTRATE.blockEntity("burning_coal_block", BurningCoalBlockEntity::createBlockEntity)
                    .validBlock(ModBlock.BURNING_COAL_BLOCK)
                    .register();

    public static final BlockEntityEntry<BurningMagnesiumBlockEntity> BURNING_MAGNESIUM =
            REGISTRATE.blockEntity("burning_magnesium_block", BurningMagnesiumBlockEntity::createBlockEntity)
                    .validBlock(ModBlock.BURNING_MAGNESIUM_BLOCK)
                    .register();

    public static final BlockEntityEntry<UraniumRodBlockEntity> URANIUM_ROD =
            REGISTRATE.blockEntity("uranium_rod", UraniumRodBlockEntity::createBlockEntity)
                    .validBlock(ModBlock.URANIUM_ROD)
                    .register();

    public static final BlockEntityEntry<NuclearCollectorBlockEntity> NUCLEAR_COLLECTOR =
            REGISTRATE.blockEntity("nuclear_collector", NuclearCollectorBlockEntity::createBlockEntity)
                    .validBlock(ModBlock.NUCLEAR_COLLECTOR)
                    .renderer(() -> NuclearCollectorRenderer::new)
                    .register();

    public static final BlockEntityEntry<MushroomCloudBlockEntity> MUSHROOM_CLOUD =
            REGISTRATE.blockEntity("mushroom_cloud", MushroomCloudBlockEntity::createBlockEntity)
                    .validBlock(ModBlock.MUSHROOM_CLOUD)
                    .renderer(() -> MushroomCloudRenderer::new)
                    .register();

    public static final BlockEntityEntry<FrostControllerBlockEntity> FROST_CONTROLLER =
            REGISTRATE.blockEntity("frost_controller", FrostControllerBlockEntity::createBlockEntity)
                    .validBlock(ModBlock.FROST_CONTROLLER)
                    .renderer(() -> FrostControllerRenderer::new)
                    .register();

    public static final BlockEntityEntry<SolarPanelBlockEntity> SOLAR_PANEL =
            REGISTRATE.blockEntity("solar_panel", SolarPanelBlockEntity::createBlockEntity)
                    .validBlock(ModBlock.SOLAR_PANEL)
                    .renderer(() -> SolarPanelRenderer::new)
                    .register();

    public static final BlockEntityEntry<MagneticDisplayStandBlockEntity> MAGNETIC_DISPLAY_STAND =
            REGISTRATE.blockEntity("magnetic_display_stand", MagneticDisplayStandBlockEntity::createBlockEntity)
                    .validBlock(ModBlock.MAGNETIC_DISPLAY_STAND)
                    .renderer(() -> MagneticDisplayStandRenderer::new)
                    .register();

    public static final BlockEntityEntry<RedstoneLinkBlockEntity> REDSTONE_LINK =
            REGISTRATE.blockEntity("redstone_link", RedstoneLinkBlockEntity::createBlockEntity)
                    .validBlock(ModBlock.REDSTONE_LINK)
                    .renderer(() -> RedstoneLinkRenderer::new)
                    .register();
    public static final BlockEntityEntry<ElectromagnetBlockEntity> ELECTROMAGNET =
            REGISTRATE.blockEntity("electromagnet", ElectromagnetBlockEntity::createBlockEntity)
                    .validBlock(ModBlock.ELECTROMAGNET)
                    .register();
    public static final BlockEntityEntry<EnchantedGeneratorBlockEntity> ENCHANTMENT_GENERATOR =
            REGISTRATE.blockEntity("enchanted_generator", EnchantedGeneratorBlockEntity::createBlockEntity)
                    .validBlock(ModBlock.ENCHANTMENT_GENERATOR_BLOCK)
                    .renderer(() -> EnchantedGeneratorRenderer::new)
                    .register();
    public static void register() {
    }
}
