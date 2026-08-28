package com.extra.power.init.block;

import com.extra.power.block.just_block.*;
import com.extra.power.init.ModCreativeModeTab;
import com.extra.power.item.RedstoneLinkItem;
import com.extra.power.item.capacitor.FlashingPotatoBatteryItem;
import com.extra.power.item.capacitor.PotatoBatteryItem;
import dev.anvilcraft.lib.v2.registrum.providers.RegistrumRecipeProvider;
import dev.anvilcraft.lib.v2.registrum.util.entry.BlockEntry;
import dev.dubhe.anvilcraft.block.multipart.SimpleMultiPartBlock;
import dev.dubhe.anvilcraft.data.AnvilCraftDatagen;
import dev.dubhe.anvilcraft.init.block.ModBlockTags;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.init.item.ModItemTags;
import dev.dubhe.anvilcraft.init.item.ModItems;
import dev.dubhe.anvilcraft.util.DataGenUtil;
import net.minecraft.data.recipes.*;
import net.minecraft.tags.BlockTags;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.storage.loot.LootPool;
import net.minecraft.world.level.storage.loot.LootTable;
import net.minecraft.world.level.storage.loot.entries.LootItem;
import net.minecraft.world.level.storage.loot.functions.SetItemCountFunction;
import net.minecraft.world.level.storage.loot.providers.number.ConstantValue;
import net.minecraft.world.level.storage.loot.providers.number.UniformGenerator;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;

import static com.extra.power.init.AnvilCraftExtrapower.MODID;
import static com.extra.power.init.AnvilCraftExtrapower.REGISTRATE;

@SuppressWarnings("unused")
public class ModBlock {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);

    //技术性方块
    public static final DeferredBlock<LightBlock> LIGHT;

    static {
        REGISTRATE.defaultCreativeTab(ModCreativeModeTab.MOD_TAB.getKey());
        //技术性方块
        LIGHT = BLOCKS.register("light",
                () -> new LightBlock(BlockBehaviour.Properties.of()
                        .strength(-1f, -1f)
                        .sound(SoundType.GLASS)
                        .air()
                        .lightLevel(state -> 15)
                        .noOcclusion()));
    }
    public static final BlockEntry<? extends Block> LLAMA_ANVIL = REGISTRATE.block("llama_anvil",
                    LlamaAnvilBlock::new)
            .lang("Llama Anvil")
            .initialProperties(() -> Blocks.CAKE)
            .properties(p -> p.noOcclusion().isValidSpawn(Blocks::never))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .recipe((ctx, provider) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ctx.get())
                        .pattern("CCC")
                        .pattern(" C ")
                        .pattern("CCC")
                        .define('C', ModBlocks.CREAM_BLOCK)
                        .unlockedBy("hasitem", AnvilCraftDatagen.has(ModBlocks.CREAM_BLOCK))
                        .save(provider);
            })
            .item()
            .tag(ItemTags.ANVIL)
            .build()
            .tag(BlockTags.ANVIL, BlockTags.MINEABLE_WITH_PICKAXE, ModBlockTags.NON_MAGNETIC, ModBlockTags.CANT_BROKEN_ANVIL)
            .register();
    public static final BlockEntry<? extends Block> ELECTROMAGNET = REGISTRATE.block("electromagnet",
                    ElectromagnetBlock::new)
            .lang("Electromagnet")
            .initialProperties(() -> Blocks.NETHERITE_BLOCK)
            .properties(p -> p.strength(1f, 3f).noOcclusion())
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .recipe((ctx, provider) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ctx.get())
                        .pattern("I")
                        .pattern("M")
                        .pattern("I")
                        .define('M', ModBlocks.MAGNETO_ELECTRIC_CORE_BLOCK)
                        .define('I', ModItemTags.IRON_PLATES)
                        .unlockedBy("hasitem", AnvilCraftDatagen.has(ModBlocks.MAGNETO_ELECTRIC_CORE_BLOCK))
                        .unlockedBy("hasitem1", AnvilCraftDatagen.has(ModItemTags.IRON_PLATES))
                        .save(provider);
            })
            .tag(ModBlockTags.MAGNET,BlockTags.MINEABLE_WITH_PICKAXE)
            .register();


    public static final BlockEntry<? extends Block> ANVIL_PROJECTOR = REGISTRATE.block("anvil_projector",
                    AnvilProjectorBlock::new)
            .lang("Anvil Projector")
            .initialProperties(() -> Blocks.NETHERITE_BLOCK)
            .properties(p -> p.strength(1f, 3f).noOcclusion())
            .blockstate(DataGenUtil::noExtraModelOrState)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .item()
            .build()
            .recipe((ctx, provider) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ctx.get())
                        .pattern("IPI")
                        .pattern("OAO")
                        .pattern("ICI")
                        .define('C', ModItems.CIRCUIT_BOARD)
                        .define('A', ModBlocks.SPECTRAL_ANVIL)
                        .define('P', ModBlocks.PIEZOELECTRIC_CRYSTAL)
                        .define('I', ModItemTags.IRON_PLATES)
                        .define('O', ModItemTags.COPPER_PLATES)
                        .unlockedBy("hasitem", AnvilCraftDatagen.has(ModItems.CIRCUIT_BOARD))
                        .unlockedBy("hasitem1", AnvilCraftDatagen.has(ModItems.MAGNET_INGOT))
                        .unlockedBy("hasitem2", AnvilCraftDatagen.has(ModItemTags.IRON_PLATES))
                        .save(provider);
            })
            .register();

    public static final BlockEntry<? extends Block> MAGNETIC_DISPLAY_STAND = REGISTRATE.block("magnetic_display_stand",
                    MagneticDisplayStandBlock::new)
            .lang("Magnetic Display Stand")
            .initialProperties(() -> Blocks.NETHERITE_BLOCK)
            .properties(p -> p.strength(1f, 3f).noOcclusion())
            .blockstate(DataGenUtil::noExtraModelOrState)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .item()
            .build()
            .recipe((ctx, provider) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ctx.get(),3)
                        .pattern("III")
                        .pattern(" M ")
                        .pattern("ICI")
                        .define('C', ModItems.CIRCUIT_BOARD)
                        .define('M', ModItems.MAGNET_INGOT)
                        .define('I', ModItemTags.IRON_PLATES)
                        .unlockedBy("hasitem", AnvilCraftDatagen.has(ModItems.CIRCUIT_BOARD))
                        .unlockedBy("hasitem1", AnvilCraftDatagen.has(ModItems.MAGNET_INGOT))
                        .unlockedBy("hasitem2", AnvilCraftDatagen.has(ModItemTags.IRON_PLATES))
                        .save(provider);
            })
            .register();
    public static final BlockEntry<? extends Block> SOLAR_PANEL = REGISTRATE.block("solar_panel", SolarPanelBlock::new)
            .lang("Solar Panel")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.strength(3.0f, 5f))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL)
            .recipe((ctx, provider) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ctx.get())
                        .pattern("ACA")
                        .pattern(" S ")
                        .pattern(" C ")
                        .define('A', Items.DAYLIGHT_DETECTOR)
                        .define('S', Items.SUNFLOWER)
                        .define('C', ModItemTags.IRON_PLATES)
                        .unlockedBy("hasbar", AnvilCraftDatagen.has(Items.SUNFLOWER))
                        .unlockedBy("hasday", AnvilCraftDatagen.has(Items.DAYLIGHT_DETECTOR))
                        .unlockedBy("hasplate", AnvilCraftDatagen.has(ModItemTags.IRON_PLATES))
                        .save(provider, ctx.getId().withSuffix("_from_sunflower"));
            })
            .recipe((ctx, provider) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ctx.get())
                        .pattern("ACA")
                        .pattern(" V ")
                        .pattern(" C ")
                        .define('A', Items.DAYLIGHT_DETECTOR)
                        .define('V', ModItems.CIRCUIT_BOARD)
                        .define('C', ModItemTags.IRON_PLATES)
                        .unlockedBy("hasbar", AnvilCraftDatagen.has(ModItems.CIRCUIT_BOARD))
                        .unlockedBy("hasday", AnvilCraftDatagen.has(Items.DAYLIGHT_DETECTOR))
                        .unlockedBy("hasplate", AnvilCraftDatagen.has(ModItemTags.IRON_PLATES))
                        .save(provider, ctx.getId().withSuffix("_from_circuit_board"));
            })
            .register();
    public static final BlockEntry<? extends Block> REDSTONE_LINK = REGISTRATE.block("redstone_link", RedstoneLinkBlock::new)
            .lang("Redstone Link")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.strength(3.0f, 5f).noOcclusion())
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item(RedstoneLinkItem::new)
            .build()
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .register();

    public static final BlockEntry<? extends Block> BURNING_COAL_BLOCK = REGISTRATE.block("burning_coal_block", BurningCoalBlock::new)
            .lang("Burning Block of Coal")
            .initialProperties(() -> Blocks.COAL_BLOCK)
            .properties(p -> p.strength(2.0f, 5f).lightLevel(state -> 10))
            .item()
            .build()
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL, ModBlockTags.REDHOT_BLOCKS)
            .recipe((ctx, provider) -> {
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.COAL_BLOCK), RecipeCategory.MISC, ctx.get(), 1.0f, 200)
                        .unlockedBy("hascoalblock", AnvilCraftDatagen.has(Items.COAL_BLOCK))
                        .unlockedBy("hascoal", AnvilCraftDatagen.has(Items.COAL))
                        .save(provider);
            })
            .loot((lt, block) -> lt.add(block,
                    LootTable.lootTable()
                            .withPool(LootPool.lootPool()
                                    .setRolls(ConstantValue.exactly(1))
                                    .add(LootItem.lootTableItem(Items.COAL)
                                            .apply(SetItemCountFunction.setCount(UniformGenerator.between(3, 5)))
                                    )
                            )
            ))
            .register();



    public static final BlockEntry<? extends Block> MAGNESIUM_BLOCK = REGISTRATE.block("magnesium_block", MagnesiumBlock::new)
            .lang("Block of Magnesium")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.strength(3.0f, 5f))
            .item()
            .tag(Tags.Items.STORAGE_BLOCKS, com.extra.power.init.data.ModItemTags.STORAGE_BLOCKS_MAGNESIUM)
            .build()
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL, Tags.Blocks.STORAGE_BLOCKS)
            .recipe((ctx, provider) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ctx.get())
                        .pattern("AAA")
                        .pattern("AAA")
                        .pattern("AAA")
                        .define('A', com.extra.power.init.data.ModItemTags.MAGNESIUM_INGOTS)
                        .unlockedBy("hasitem", AnvilCraftDatagen.has(com.extra.power.init.data.ModItemTags.MAGNESIUM_INGOTS))
                        .save(provider);
            })
            .register();

    public static final BlockEntry<? extends Block> BURNING_MAGNESIUM_BLOCK = REGISTRATE.block("burning_magnesium_block", BurningMagnesiumBlock::new)
            .lang("Burning Block of Magnesium")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.strength(2.0f, 5f).lightLevel(state -> 15))
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
    public static final BlockEntry<? extends Block> SULFUR_BLOCK = REGISTRATE.block("sulfur_block", Block::new)
            .lang("Block of Sulfur")
            .initialProperties(() -> Blocks.COAL_BLOCK)
            .properties(p -> p.strength(3.0f, 5f))
            .item()
            .tag(Tags.Items.STORAGE_BLOCKS, com.extra.power.init.data.ModItemTags.STORAGE_BLOCKS_SULFUR)
            .build()
            .tag(BlockTags.MINEABLE_WITH_PICKAXE, BlockTags.NEEDS_STONE_TOOL, Tags.Blocks.STORAGE_BLOCKS)
            .recipe((ctx, provider) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ctx.get())
                        .pattern("AAA")
                        .pattern("AAA")
                        .pattern("AAA")
                        .define('A', com.extra.power.init.data.ModItemTags.SULFUR)
                        .unlockedBy("hasitem", AnvilCraftDatagen.has(com.extra.power.init.data.ModItemTags.SULFUR))
                        .save(provider);
            })
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
            )).register();

    public static final BlockEntry<? extends Block> MAGNESIUM_OXIDE_BLOCK = REGISTRATE.block("magnesium_oxide_block", Block::new)
            .lang("Block of Magnesium Oxide")
            .initialProperties(() -> Blocks.STONE)
            .properties(p -> p.strength(10.0f, 1f))
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

    public static final BlockEntry<? extends Block> POTATO_BATTERY = REGISTRATE.block("potato_battery", PotatoBattery::new)
            .lang("Potato Battery")
            .initialProperties(() -> Blocks.SLIME_BLOCK)
            .properties(p -> p.strength(0.5f, 2f))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item(PotatoBatteryItem::new)
            .tag(com.extra.power.init.data.ModItemTags.CAPACITOR)
            .build()
            .recipe((ctx, provider) -> {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ctx.get(), 1)
                        .requires(ModItemTags.COPPER_PLATES)
                        .requires(Items.POTATO)
                        .requires(ModItemTags.ZINC_PLATES)
                        .group(ctx.getId().toString())
                        .unlockedBy("hascopperitem", AnvilCraftDatagen.has(ModItemTags.COPPER_PLATES))
                        .unlockedBy("haszincitem", AnvilCraftDatagen.has(ModItemTags.ZINC_PLATES))
                        .unlockedBy("haspotato", AnvilCraftDatagen.has(Items.POTATO))
                        .save(provider);
            })
            .register();

    public static final BlockEntry<? extends Block> FLASHING_POTATO_BATTERY = REGISTRATE.block("flashing_potato_battery", PotatoBattery::new)
            .lang("Flashing Potato Battery")
            .initialProperties(() -> Blocks.SLIME_BLOCK)
            .properties(p -> p.strength(0.5f, 2f))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item(FlashingPotatoBatteryItem::new)
            .tag(com.extra.power.init.data.ModItemTags.CAPACITOR)
            .build()
            .register();


    public static final BlockEntry<EnchantedGeneratorBlock> ENCHANTMENT_GENERATOR_BLOCK = REGISTRATE
            .block("enchanted_generator", EnchantedGeneratorBlock::new)
            .lang("Enchantment Generator")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.lightLevel(state -> 9).strength(5.0f, 1200f).noOcclusion().emissiveRendering(ModBlocks::always))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .tag(BlockTags.MINEABLE_WITH_PICKAXE)
            .simpleItem()
            .recipe((ctx, provider) -> ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                    .pattern(" E ")
                    .pattern(" B ")
                    .pattern("CDC")
                    .define('E', Items.ENCHANTED_BOOK)
                    .define('B', ModBlocks.CHARGE_COLLECTOR)
                    .define('C', ModItems.FROST_METAL_INGOT)
                    .define('D', ModItemTags.SILVER_PLATES)
                    .unlockedBy(
                            AnvilCraftDatagen.hasItem(Items.ENCHANTED_BOOK),
                            AnvilCraftDatagen.has(ModBlocks.CHARGE_COLLECTOR)
                    )
                    .save(provider))
            .register();

    public static final BlockEntry<? extends Block> CRATE_BLOCK = REGISTRATE.block("crate", CrateBlock::new)
            .lang("Crate")
            .initialProperties(() -> Blocks.OAK_WOOD)
            .properties(p -> p.strength(2f, 5f))
            .tag(BlockTags.MINEABLE_WITH_AXE)
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
            .properties(p -> p.lightLevel(state -> 10).noOcclusion())
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
            .properties(p -> p.lightLevel(state -> 10).noOcclusion())
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
            .recipe((ctx, provider) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ctx.get())
                        .pattern("E E")
                        .pattern(" U ")
                        .pattern("BCB")
                        .define('U', ModBlocks.URANIUM_BLOCK)
                        .define('B', ModBlocks.EMBER_METAL_BLOCK)
                        .define('E', ModItems.EMBER_METAL_NUGGET)
                        .define('C', ModBlocks.HEAT_COLLECTOR)
                        .unlockedBy("hasu", AnvilCraftDatagen.has(ModBlocks.URANIUM_BLOCK))
                        .unlockedBy("hasb", AnvilCraftDatagen.has(ModBlocks.EMBER_METAL_BLOCK))
                        .unlockedBy("hase", AnvilCraftDatagen.has(ModItems.EMBER_METAL_NUGGET))
                        .unlockedBy("hasc", AnvilCraftDatagen.has(ModBlocks.HEAT_COLLECTOR))
                        .save(provider);
            })
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
            .recipe((ctx, provider) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ctx.get())
                        .pattern(" B ")
                        .pattern("PUP")
                        .pattern(" B ")
                        .define('U', ModBlocks.URANIUM_BLOCK)
                        .define('B', ModBlocks.EMBER_METAL_BLOCK)
                        .define('P', ModItemTags.LEAD_PLATES)
                        .unlockedBy("hasu", AnvilCraftDatagen.has(ModBlocks.URANIUM_BLOCK))
                        .unlockedBy("hasb", AnvilCraftDatagen.has(ModBlocks.EMBER_METAL_BLOCK))
                        .unlockedBy("hasp", AnvilCraftDatagen.has(ModItemTags.LEAD_PLATES))
                        .save(provider);
            })
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


    public static final BlockEntry<? extends Block> SIGN = REGISTRATE.block("sign_base",
                    SignBlock::new)
            .lang("Sign Base")
            .initialProperties(() -> Blocks.LIGHT_BLUE_WOOL)
            .properties(p -> p
                    .noOcclusion()
                    .isValidSpawn(Blocks::never))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .tag(com.extra.power.init.data.ModItemTags.SIGN)
            .build()
            .recipe((ctx, provider) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.REDSTONE, ctx.get(),4)
                        .pattern(" I ")
                        .pattern("IWI")
                        .pattern(" I ")
                        .define('I', ModItemTags.IRON_PLATES)
                        .define('W', ItemTags.WOOL)
                        .unlockedBy("hasitem", AnvilCraftDatagen.has(ModBlocks.MAGNETO_ELECTRIC_CORE_BLOCK))
                        .unlockedBy("hasitem1", AnvilCraftDatagen.has(ModItemTags.IRON_PLATES))
                        .save(provider);
            })
            .register();
    public static final BlockEntry<? extends Block> SIGN_ANVIL_FALL = REGISTRATE.block("sign_anvil_fall",
                    SignBlock::new)
            .lang("Sign Anvil Fall")
            .initialProperties(() -> Blocks.LIGHT_BLUE_WOOL)
            .properties(p -> p
                    .noOcclusion()
                    .isValidSpawn(Blocks::never))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .tag(com.extra.power.init.data.ModItemTags.SIGN)
            .build()
            .recipe((ctx, provider) -> {
                SingleItemRecipeBuilder.stonecutting(
                                Ingredient.of(com.extra.power.init.data.ModItemTags.SIGN),
                                RecipeCategory.BUILDING_BLOCKS,
                                ctx.get(),
                                1
                        ).unlockedBy("has_sign", RegistrumRecipeProvider.has(SIGN.get()))
                        .save(provider);
            })
            .register();
    public static final BlockEntry<? extends Block> SIGN_CONSTRUCTION = REGISTRATE.block("sign_construction",
                    SignBlock::new)
            .lang("Sign Construction")
            .initialProperties(() -> Blocks.LIGHT_BLUE_WOOL)
            .properties(p -> p
                    .noOcclusion()
                    .isValidSpawn(Blocks::never))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .tag(com.extra.power.init.data.ModItemTags.SIGN)
            .build()
            .recipe((ctx, provider) -> {
                SingleItemRecipeBuilder.stonecutting(
                                Ingredient.of(com.extra.power.init.data.ModItemTags.SIGN),
                                RecipeCategory.BUILDING_BLOCKS,
                                ctx.get(),
                                1
                        ).unlockedBy("has_sign", RegistrumRecipeProvider.has(SIGN.get()))
                        .save(provider);
            })
            .register();
    public static final BlockEntry<? extends Block> SIGN_DO_NOT_OPERATE = REGISTRATE.block("sign_do_not_operate",
                    SignBlock::new)
            .lang("Sign Don't Operate")
            .initialProperties(() -> Blocks.LIGHT_BLUE_WOOL)
            .properties(p -> p
                    .noOcclusion()
                    .isValidSpawn(Blocks::never))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .tag(com.extra.power.init.data.ModItemTags.SIGN)
            .build()
            .recipe((ctx, provider) -> {
                SingleItemRecipeBuilder.stonecutting(
                                Ingredient.of(com.extra.power.init.data.ModItemTags.SIGN),
                                RecipeCategory.BUILDING_BLOCKS,
                                ctx.get(),
                                1
                        ).unlockedBy("has_sign", RegistrumRecipeProvider.has(SIGN.get()))
                        .save(provider);
            })
            .register();
    public static final BlockEntry<? extends Block> SIGN_HIGHSPEED_ANVIL = REGISTRATE.block("sign_highspeed_anvil",
                    SignBlock::new)
            .lang("Sign Highspeed Anvil")
            .initialProperties(() -> Blocks.LIGHT_BLUE_WOOL)
            .properties(p -> p
                    .noOcclusion()
                    .isValidSpawn(Blocks::never))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .tag(com.extra.power.init.data.ModItemTags.SIGN)
            .build()
            .recipe((ctx, provider) -> {
                SingleItemRecipeBuilder.stonecutting(
                                Ingredient.of(com.extra.power.init.data.ModItemTags.SIGN),
                                RecipeCategory.BUILDING_BLOCKS,
                                ctx.get(),
                                1
                        ).unlockedBy("has_sign", RegistrumRecipeProvider.has(SIGN.get()))
                        .save(provider);
            })
            .register();
    public static final BlockEntry<? extends Block> SIGN_LASER_HAZARD = REGISTRATE.block("sign_laser_hazard",
                    SignBlock::new)
            .lang("Sign Laser Hazard")
            .initialProperties(() -> Blocks.LIGHT_BLUE_WOOL)
            .properties(p -> p
                    .noOcclusion()
                    .isValidSpawn(Blocks::never))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .tag(com.extra.power.init.data.ModItemTags.SIGN)
            .build()
            .recipe((ctx, provider) -> {
                SingleItemRecipeBuilder.stonecutting(
                                Ingredient.of(com.extra.power.init.data.ModItemTags.SIGN),
                                RecipeCategory.BUILDING_BLOCKS,
                                ctx.get(),
                                1
                        ).unlockedBy("has_sign", RegistrumRecipeProvider.has(SIGN.get()))
                        .save(provider);
            })
            .register();
    public static final BlockEntry<? extends Block> SIGN_RADIATION = REGISTRATE.block("sign_radiation",
                    SignBlock::new)
            .lang("Sign Radiation")
            .initialProperties(() -> Blocks.LIGHT_BLUE_WOOL)
            .properties(p -> p
                    .noOcclusion()
                    .isValidSpawn(Blocks::never))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .tag(com.extra.power.init.data.ModItemTags.SIGN)
            .build()
            .recipe((ctx, provider) -> {
                SingleItemRecipeBuilder.stonecutting(
                                Ingredient.of(com.extra.power.init.data.ModItemTags.SIGN),
                                RecipeCategory.BUILDING_BLOCKS,
                                ctx.get(),
                                1
                        ).unlockedBy("has_sign", RegistrumRecipeProvider.has(SIGN.get()))
                        .save(provider);
            })
            .register();
    public static final BlockEntry<? extends Block> SIGN_TIME_HAZARD = REGISTRATE.block("sign_time_hazard",
                    SignBlock::new)
            .lang("Sign Time Hazard")
            .initialProperties(() -> Blocks.LIGHT_BLUE_WOOL)
            .properties(p -> p
                    .noOcclusion()
                    .isValidSpawn(Blocks::never))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .tag(com.extra.power.init.data.ModItemTags.SIGN)
            .build()
            .recipe((ctx, provider) -> {
                SingleItemRecipeBuilder .stonecutting(
                                Ingredient.of(com.extra.power.init.data.ModItemTags.SIGN),
                                RecipeCategory.BUILDING_BLOCKS,
                                ctx.get(),
                                1
                        ).unlockedBy("has_sign", RegistrumRecipeProvider.has(SIGN.get()))
                        .save(provider);
            })
            .register();
    public static final BlockEntry<? extends Block> SIGN_STRONG_GRAVITY = REGISTRATE.block("sign_strong_gravity",
                    SignBlock::new)
            .lang("Sign Strong Gravity")
            .initialProperties(() -> Blocks.LIGHT_BLUE_WOOL)
            .properties(p -> p
                    .noOcclusion()
                    .isValidSpawn(Blocks::never))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .tag(com.extra.power.init.data.ModItemTags.SIGN)
            .build()
            .recipe((ctx, provider) -> {
                SingleItemRecipeBuilder.stonecutting(
                                Ingredient.of(com.extra.power.init.data.ModItemTags.SIGN),
                                RecipeCategory.BUILDING_BLOCKS,
                                ctx.get(),
                                1
                        ).unlockedBy("has_sign", RegistrumRecipeProvider.has(SIGN.get()))
                        .save(provider);
            })
            .register();
    public static final BlockEntry<? extends Block> SIGN_MAGNETIC_FIELD = REGISTRATE.block("sign_magnetic_field",
                    SignBlock::new)
            .lang("Sign Magnetic Field")
            .initialProperties(() -> Blocks.LIGHT_BLUE_WOOL)
            .properties(p -> p
                    .noOcclusion()
                    .isValidSpawn(Blocks::never))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .tag(com.extra.power.init.data.ModItemTags.SIGN)
            .build()
            .recipe((ctx, provider) -> {
                SingleItemRecipeBuilder.stonecutting(
                                Ingredient.of(com.extra.power.init.data.ModItemTags.SIGN),
                                RecipeCategory.BUILDING_BLOCKS,
                                ctx.get(),
                                1
                        ).unlockedBy("has_sign", RegistrumRecipeProvider.has(SIGN.get()))
                        .save(provider);
            })
            .register();


}