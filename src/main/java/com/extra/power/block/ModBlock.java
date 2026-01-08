package com.extra.power.block;

import com.extra.power.block.just_block.*;
import com.extra.power.init.ModCreativeModeTab;
import com.extra.power.item.capacitor.FlashingPotatoBatteryItem;
import com.extra.power.item.capacitor.PotatoBatteryItem;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.BlockEntry;
import dev.dubhe.anvilcraft.block.EmberMetalBlock;
import dev.dubhe.anvilcraft.init.block.ModBlockTags;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.init.item.ModItemTags;
import dev.dubhe.anvilcraft.util.DataGenUtil;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredBlock;
import net.neoforged.neoforge.registries.DeferredRegister;
import static com.extra.power.init.AnvilCraftExtrapower.MODID;
import static com.extra.power.init.AnvilCraftExtrapower.REGISTRATE;

public class ModBlock {
    public static final DeferredRegister.Blocks BLOCKS = DeferredRegister.createBlocks(MODID);
    public static final String SOLAR_PANEL_BLOCK_ID = "solar_panel";
    public static final String BURNING_COAL_BLOCK_ID = "burning_coal_block";
    public static final String SULFUR_BLOCK_ID = "sulfur_block";
    public static final String MAGNESIUM_BLOCK_ID = "magnesium_block";
    public static final String BURNING_MAGNESIUM_BLOCK_ID = "burning_magnesium_block";
    public static final String ASHES_BLOCK_ID = "ashes_block";
    //技术性方块
    public static final DeferredBlock<Light>LIGHT;
    static {
        REGISTRATE.defaultCreativeTab(ModCreativeModeTab.MOD_TAB.getKey());
        //技术性方块
        LIGHT= BLOCKS.register("light",
                ()->new Light(BlockBehaviour.Properties.of()
                        .strength(-1f, -1f)
                        .sound(SoundType.GLASS)
                        .air()
                        .lightLevel(state -> 15)
                        .noOcclusion()));
    }
    public static final BlockEntry<? extends Block>SOLAR_PANEL= REGISTRATE.block(SOLAR_PANEL_BLOCK_ID, SolarPanelBlock::new)
            .lang("Solar Panel")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.strength(3.0f, 5f))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .tag(
                    BlockTags.MINEABLE_WITH_PICKAXE,
                    BlockTags.NEEDS_STONE_TOOL
            )
            .recipe((ctx, provider) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ctx.get())
                        .pattern("A")
                        .pattern("B")
                        .pattern("C")
                        .define('A', Items.DAYLIGHT_DETECTOR)
                        .define('B', Items.IRON_BARS)
                        .define('C', ModItemTags.IRON_PLATES)
                        .unlockedBy("hasbar", RegistrateRecipeProvider.has(Items.IRON_BARS))
                        .unlockedBy("hasday", RegistrateRecipeProvider.has(Items.DAYLIGHT_DETECTOR))
                        .unlockedBy("hasplate", RegistrateRecipeProvider.has(ModItemTags.IRON_PLATES))
                        .save(provider);
            })
            .register();
    public static final BlockEntry<? extends Block>BURNING_COAL_BLOCK= REGISTRATE.block(BURNING_COAL_BLOCK_ID, BurningCoalBlock::new)
            .lang("Burning Block of Coal")
            .initialProperties(() -> Blocks.COAL_BLOCK)
            .properties(p -> p.strength(2.0f, 5f).lightLevel(state -> 10))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .tag(
                    BlockTags.MINEABLE_WITH_PICKAXE,
                    BlockTags.NEEDS_STONE_TOOL,
                    ModBlockTags.REDHOT_BLOCKS
            )
            .recipe((ctx, provider) -> {
                SimpleCookingRecipeBuilder.smelting(Ingredient.of(Items.COAL_BLOCK), RecipeCategory.MISC, ctx.get(), 1.0f, 200) // 输入物品，输出物品，经验值，熔炼时间（ tick，200 tick = 10秒）
                        .unlockedBy("hascoalblock", RegistrateRecipeProvider.has(Items.COAL_BLOCK))
                        .unlockedBy("hascoal", RegistrateRecipeProvider.has(Items.COAL))
                        .save(provider);
            })
            .register();
    public static final BlockEntry<? extends Block>SULFUR_BLOCK= REGISTRATE.block(SULFUR_BLOCK_ID, Block::new)
            .lang("Block of Sulfur")
            .initialProperties(() -> Blocks.COAL_BLOCK)
            .properties(p -> p.strength(3.0f, 5f))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .tag(Tags.Items.STORAGE_BLOCKS, com.extra.power.init.ModItemTags.STORAGE_BLOCKS_SULFUR)
            .build()
            .tag(
                    BlockTags.MINEABLE_WITH_PICKAXE,
                    BlockTags.NEEDS_STONE_TOOL,
                    Tags.Blocks.STORAGE_BLOCKS
            )
            .recipe((ctx, provider) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ctx.get())
                        .pattern("AAA")
                        .pattern("AAA")
                        .pattern("AAA")
                        .define('A', com.extra.power.init.ModItemTags.SULFUR)
                        .unlockedBy("hasitem", RegistrateRecipeProvider.has(com.extra.power.init.ModItemTags.SULFUR))
                        .save(provider);
            })
            .register();
    public static final BlockEntry<? extends Block>ASHES_BLOCK= REGISTRATE.block(ASHES_BLOCK_ID, AshesBlock::new)
            .lang("Ashes")
            .initialProperties(() -> Blocks.SAND)
            .properties(p -> p.strength(1.0f, 3f))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .tag(
                    BlockTags.MINEABLE_WITH_SHOVEL
            ).register();
    public static final BlockEntry<? extends Block>MAGNESIUM_OXIDE_BLOCK= REGISTRATE.block("magnesium_oxide_block", Block::new)
            .lang("Block of Magnesium Oxide")
            .initialProperties(() -> Blocks.STONE)
            .properties(p -> p.strength(10.0f, 1f))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .tag(
                    BlockTags.MINEABLE_WITH_PICKAXE
            ).register();
    public static final BlockEntry<? extends Block>MAGNESIUM_BLOCK= REGISTRATE.block(MAGNESIUM_BLOCK_ID, MagnesiumBlock::new)
            .lang("Block of Magnesium")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.strength(3.0f, 5f))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .tag(Tags.Items.STORAGE_BLOCKS, com.extra.power.init.ModItemTags.STORAGE_BLOCKS_MAGNESIUM)
            .build()
            .tag(
                    BlockTags.MINEABLE_WITH_PICKAXE,
                    BlockTags.NEEDS_STONE_TOOL,
                    Tags.Blocks.STORAGE_BLOCKS
            )
            .recipe((ctx, provider) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.BUILDING_BLOCKS, ctx.get())
                        .pattern("AAA")
                        .pattern("AAA")
                        .pattern("AAA")
                        .define('A', com.extra.power.init.ModItemTags.MAGNESIUM_INGOTS)
                        .unlockedBy("hasitem", RegistrateRecipeProvider.has(com.extra.power.init.ModItemTags.MAGNESIUM_INGOTS))
                        .save(provider);
            })
            .register();
    public static final BlockEntry<? extends Block>BURNING_MAGNESIUM_BLOCK= REGISTRATE.block(BURNING_MAGNESIUM_BLOCK_ID, BurningMagnesiumBlock::new)
            .lang("Burning Block of Magnesium")
            .initialProperties(() -> Blocks.IRON_BLOCK)
            .properties(p -> p.strength(2.0f, 5f)
            .lightLevel(state -> 15))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .tag(
                    BlockTags.MINEABLE_WITH_PICKAXE,
                    BlockTags.NEEDS_STONE_TOOL,
                    ModBlockTags.INCANDESCENT_BLOCKS
            ).register();
    public static final BlockEntry<? extends Block>POTATO_BATTERY= REGISTRATE.block("potato_battery", PotatoBattery::new)
            .lang("Potato Battery")
            .initialProperties(() -> Blocks.SLIME_BLOCK)
            .properties(p -> p.strength(0.5f, 2f))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .tag(com.extra.power.init.ModItemTags.CAPACITOR)
            .build()
            .recipe((ctx, provider) -> {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ctx.get(), 1)
                        .requires(ModItemTags.COPPER_PLATES).requires(Items.POTATO).requires(ModItemTags.ZINC_PLATES)
                        .group(ctx.getId().toString())
                        .unlockedBy("hascopperitem", RegistrateRecipeProvider.has(ModItemTags.COPPER_PLATES))
                        .unlockedBy("haszincitem",RegistrateRecipeProvider.has(ModItemTags.ZINC_PLATES))
                        .unlockedBy("haspotato", RegistrateRecipeProvider.has(Items.POTATO))
                        .save(provider);
            })
            .register();
    public static final BlockEntry<? extends Block>FLASHING_POTATO_BATTERY= REGISTRATE.block("flashing_potato_battery", PotatoBattery::new)
            .lang("Flashing Potato Battery")
            .initialProperties(() -> Blocks.SLIME_BLOCK)
            .properties(p -> p.strength(0.5f, 2f))
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item() 
            .tag(com.extra.power.init.ModItemTags.CAPACITOR)
            .build()
            .register();
    public static final BlockEntry<? extends Block>CRATE_BLOCK= REGISTRATE.block("crate", CrateBlock::new)
            .lang("Crate")
            .initialProperties(() -> Blocks.OAK_WOOD)
            .properties(p -> p.strength(2f, 5f))
            .tag(
                    BlockTags.MINEABLE_WITH_AXE
            )
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .register();
    public static final BlockEntry<? extends Block>SEMI_FINISHED_CRATE= REGISTRATE.block("semi_finished_crate", Block::new)
            .lang("Semi-Finished Crate")
            .initialProperties(() -> Blocks.OAK_WOOD)
            .properties(p -> p.strength(2f, 5f))
            .tag(
                    BlockTags.MINEABLE_WITH_AXE
            )
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .register();
    public static final BlockEntry<? extends Block>URANIUM_ROD= REGISTRATE.block("uranium_rod",
                    properties -> new UraniumRodBlock(properties, 0.5d))
            .lang("Uranium Rod")
            .initialProperties(() -> Blocks.NETHERITE_BLOCK)
            .properties(p -> p.lightLevel(state -> 10).noOcclusion().emissiveRendering(ModBlocks::always))
            .tag(
                    BlockTags.MINEABLE_WITH_PICKAXE,
                    ModBlockTags.MEKANISM_CARDBOARD_BOX_BLACKLIST
            )
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .register();
    public static final BlockEntry<? extends Block>NUCLEAR_COLLECTOR= REGISTRATE.block("nuclear_collector",
                      NuclearCollectorBlock::new)
            .lang("Nuclear Collector")
            .initialProperties(() -> Blocks.NETHERITE_BLOCK)
            .properties(p ->
                    p.strength(5f, 1200f).lightLevel(state -> 10).noOcclusion().emissiveRendering(ModBlocks::always))
            .tag(
                    BlockTags.MINEABLE_WITH_PICKAXE
            )
            .blockstate(DataGenUtil::noExtraModelOrState)
            .item()
            .build()
            .register();
}
