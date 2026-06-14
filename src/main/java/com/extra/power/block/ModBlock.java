package com.extra.power.block;

import com.extra.power.block.just_block.*;
import com.extra.power.data.recipe.RegistrumBlockRecipeLoader;
import com.extra.power.init.ModCreativeModeTab;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntry;
import dev.dubhe.anvilcraft.block.multipart.SimpleMultiPartBlock;
import dev.dubhe.anvilcraft.init.block.ModBlockTags;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.util.registrater.DataGenUtil;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.common.Tags;
import static com.extra.power.init.AnvilCraftExtrapower.REGISTRATE;

@SuppressWarnings("unused")
public class ModBlock {
    static {
        REGISTRATE.defaultCreativeTab(ModCreativeModeTab.MOD_TAB.getKey());
    }

    public static final BlockEntry<LightBlock> LIGHT = REGISTRATE.block("light", LightBlock::new)
            .lang("Light")
            .initialProperties(() -> Blocks.AIR)
            .properties(p -> p
                    .strength(-1f, -1f)
                    .sound(SoundType.GLASS)
                    .air()
                    .lightLevel(state -> 15)
                    .noOcclusion()
                    .noLootTable()
                    .noTerrainParticles())
            .blockstate(DataGenUtil::noExtraModelOrState)
            .register();

    public static final BlockEntry<? extends Block> SOLAR_PANEL = REGISTRATE.block("solar_panel", SolarPanelBlock::new)
            .lang("Solar Panel")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.strength(3.0f, 5f))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL)
            .recipe(RegistrumBlockRecipeLoader::solarPanelFromSunflower)
            .recipe(RegistrumBlockRecipeLoader::solarPanelFromCircuitBoard)
            .register();

    public static final BlockEntry<? extends Block> BURNING_COAL_BLOCK = REGISTRATE.block("burning_coal_block", BurningCoalBlock::new)
            .lang("Burning Block of Coal")
            .initialProperties(() -> Blocks.COAL_BLOCK)
            .properties(p -> p.strength(2.0f, 5f).lightLevel(state -> 10))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL, ModBlockTags.REDHOT_BLOCKS)
            .recipe(RegistrumBlockRecipeLoader::burningCoalBlock)
            .loot((lt, block) -> lt.add(block,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(net.minecraft.world.item.Items.COAL)
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 5)))
                                    )
                            )
            ))
            .register();

    public static final BlockEntry<? extends Block> SULFUR_BLOCK = REGISTRATE.block("sulfur_block", Block::new)
            .lang("Block of Sulfur")
            .initialProperties(() -> Blocks.COAL_BLOCK)
            .properties(p -> p.strength(3.0f, 5f))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .tag(Tags.Items.STORAGE_BLOCKS, com.extra.power.init.ModItemTags.STORAGE_BLOCKS_SULFUR)
            .build()
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL, Tags.Blocks.STORAGE_BLOCKS)
            .recipe(RegistrumBlockRecipeLoader::sulfurBlock)
            .register();

    public static final BlockEntry<? extends Block> ASHES_BLOCK = REGISTRATE.block("ashes_block", AshesBlock::new)
            .lang("Ashes")
            .initialProperties(() -> Blocks.SAND)
            .properties(p -> p.strength(1.0f, 3f))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .tag(BlockTags.MINEABLE_WITH_SHOVEL)
            .loot((lt, block) -> lt.add(block,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(com.extra.power.init.ModItems.ASHES.get())
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 5)))
                                    )
                            )
            ))
            .register();

    public static final BlockEntry<? extends Block> MAGNESIUM_OXIDE_BLOCK = REGISTRATE.block("magnesium_oxide_block", Block::new)
            .lang("Block of Magnesium Oxide")
            .initialProperties(() -> Blocks.STONE)
            .properties(p -> p.strength(10.0f, 1f))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .loot((lt, block) -> lt.add(block,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(com.extra.power.init.ModItems.MAGNESIUM_OXIDE.get())
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 5)))
                                    )
                            )
            ))
            .register();

    public static final BlockEntry<? extends Block> MAGNESIUM_BLOCK = REGISTRATE.block("magnesium_block", MagnesiumBlock::new)
            .lang("Block of Magnesium")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.strength(3.0f, 5f))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .tag(Tags.Items.STORAGE_BLOCKS, com.extra.power.init.ModItemTags.STORAGE_BLOCKS_MAGNESIUM)
            .build()
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL, Tags.Blocks.STORAGE_BLOCKS)
            .recipe(RegistrumBlockRecipeLoader::magnesiumBlock)
            .register();

    public static final BlockEntry<? extends Block> BURNING_MAGNESIUM_BLOCK = REGISTRATE.block("burning_magnesium_block", BurningMagnesiumBlock::new)
            .lang("Burning Block of Magnesium")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.strength(2.0f, 5f).lightLevel(state -> 15))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .loot((lt, block) -> lt.add(block,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(com.extra.power.init.ModItems.MAGNESIUM_OXIDE.get())
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 5)))
                                    )
                            )
            ))
            .item()
            .build()
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL, ModBlockTags.INCANDESCENT_BLOCKS)
            .register();

    public static final BlockEntry<? extends Block> POTATO_BATTERY = REGISTRATE.block("potato_battery", PotatoBattery::new)
            .lang("Potato Battery")
            .initialProperties(() -> Blocks.SLIME_BLOCK)
            .properties(p -> p.strength(0.5f, 2f))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .tag(com.extra.power.init.ModItemTags.CAPACITOR)
            .build()
            .recipe(RegistrumBlockRecipeLoader::potatoBattery)
            .register();

    public static final BlockEntry<? extends Block> FLASHING_POTATO_BATTERY = REGISTRATE.block("flashing_potato_battery", PotatoBattery::new)
            .lang("Flashing Potato Battery")
            .initialProperties(() -> Blocks.SLIME_BLOCK)
            .properties(p -> p.strength(0.5f, 2f))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .tag(com.extra.power.init.ModItemTags.CAPACITOR)
            .build()
            .register();

    public static final BlockEntry<? extends Block> CRATE_BLOCK = REGISTRATE.block("crate", CrateBlock::new)
            .lang("Crate")
            .initialProperties(() -> Blocks.OAK_WOOD)
            .properties(p -> p.strength(2f, 5f))
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .register();

    public static final BlockEntry<? extends Block> SEMI_FINISHED_CRATE = REGISTRATE.block("semi_finished_crate", Block::new)
            .lang("Semi-Finished Crate")
            .initialProperties(() -> Blocks.OAK_WOOD)
            .properties(p -> p.strength(2f, 5f))
            .tag(BlockTags.MINEABLE_WITH_AXE)
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .register();

    public static final BlockEntry<? extends Block> URANIUM_ROD = REGISTRATE.block("uranium_rod",
                    properties -> new UraniumRodBlock(properties, 0.5d))
            .lang("Uranium Rod")
            .initialProperties(() -> Blocks.NETHERITE_BLOCK)
            .properties(p -> p.lightLevel(state -> 10).noOcclusion().emissiveRendering(ModBlocks::always))
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, ModBlockTags.MEKANISM_CARDBOARD_BOX_BLACKLIST, BlockTags.WITHER_IMMUNE)
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .loot(SimpleMultiPartBlock::loot)
            .register();

    public static final BlockEntry<? extends Block> FROST_CONTROLLER = REGISTRATE.block("frost_controller",
                    FrostControllerBlock::new)
            .lang("Frost Controller")
            .initialProperties(() -> Blocks.NETHERITE_BLOCK)
            .properties(p -> p.lightLevel(state -> 10).noOcclusion().emissiveRendering(ModBlocks::always))
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, ModBlockTags.MEKANISM_CARDBOARD_BOX_BLACKLIST, BlockTags.WITHER_IMMUNE)
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .loot(SimpleMultiPartBlock::loot)
            .register();

    public static final BlockEntry<? extends Block> NUCLEAR_COLLECTOR = REGISTRATE.block("nuclear_collector",
                    NuclearCollectorBlock::new)
            .lang("Nuclear Collector")
            .initialProperties(() -> Blocks.NETHERITE_BLOCK)
            .properties(p -> p.strength(5f, 1200f).lightLevel(state -> 10).noOcclusion().emissiveRendering(ModBlocks::always))
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.WITHER_IMMUNE)
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .recipe(RegistrumBlockRecipeLoader::nuclearCollector)
            .register();

    public static final BlockEntry<? extends Block> NUCLEAR_BOMB = REGISTRATE.block("nuclear_bomb",
                    NuclearBombBlock::new)
            .lang("Nuclear Bomb")
            .initialProperties(() -> Blocks.ANVIL)
            .properties(p -> p.strength(5f, 1200f).lightLevel(state -> 15).noOcclusion())
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.WITHER_IMMUNE)
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .recipe(RegistrumBlockRecipeLoader::nuclearBomb)
            .register();

    public static final BlockEntry<? extends Block> MUSHROOM_CLOUD = REGISTRATE.block("mushroom_cloud",
                    MushroomCloudBlock::new)
            .lang("Mushroom Cloud")
            .initialProperties(() -> Blocks.NETHERITE_BLOCK)
            .properties(p -> p.strength(-1f, -1f).lightLevel(state -> 15).air())
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .register();

    public static final BlockEntry<? extends Block> PEDOMETER = REGISTRATE.block("pedometer",
                    PedometerBlock::new)
            .lang("Pedometer")
            .initialProperties(() -> Blocks.NETHERITE_BLOCK)
            .properties(p -> p.strength(2f, 3f).lightLevel(state -> state.getValue(PedometerBlock.PROGRESS) != 0 ? 9 : 0))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .register();

    public static final BlockEntry<? extends Block> ANVIL_PROJECTOR = REGISTRATE.block("anvil_projector",
                    AnvilProjectorBlock::new)
            .lang("Anvil Projector")
            .initialProperties(() -> Blocks.NETHERITE_BLOCK)
            .properties(p -> p.strength(2f, 3f).noOcclusion())
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .register();

    public static final BlockEntry<? extends Block> MAGNETIC_DISPLAY_STAND = REGISTRATE.block("magnetic_display_stand",
                    MagneticDisplayStandBlock::new)
            .lang("Magnetic Display Stand")
            .initialProperties(() -> Blocks.NETHERITE_BLOCK)
            .properties(p -> p.strength(2f, 3f).noOcclusion())
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .register();
    public static void register() {
    }
}
