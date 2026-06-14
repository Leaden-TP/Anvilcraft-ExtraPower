package com.extra.power.data.recipe;

import com.extra.power.init.ModItemTags;
import com.jcraft.jorbis.Block;
import dev.anvilcraft.lib.v2.registrum.providers.DataGenContext;
import dev.anvilcraft.lib.v2.registrum.providers.generators.RegistrumRecipeProvider;
import dev.dubhe.anvilcraft.data.AnvilCraftDatagen;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import dev.dubhe.anvilcraft.init.item.ModItems;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.data.recipes.SimpleCookingRecipeBuilder;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.CookingBookCategory;
import net.minecraft.world.item.crafting.Ingredient;
import net.minecraft.world.level.ItemLike;

public class RegistrumBlockRecipeLoader {
    public static <T extends net.minecraft.world.level.block.Block> void solarPanelFromSunflower(
            DataGenContext<net.minecraft.world.level.block.Block, T> ctx, RegistrumRecipeProvider provider) {
        HolderGetter<Item> lookup = provider.getItems();
        ShapedRecipeBuilder.shaped(lookup, RecipeCategory.BUILDING_BLOCKS,ctx.get())
                .pattern("ACA")
                .pattern(" S ")
                .pattern(" C")
                .define('A', Items.DAYLIGHT_DETECTOR)
                .define('S', Items.SUNFLOWER)
                .define('C', dev.dubhe.anvilcraft.init.item.ModItemTags.IRON_PLATES)
                .unlockedBy("has_sunflower", AnvilCraftDatagen.has(lookup, Items.SUNFLOWER))
                .unlockedBy("has_daylight_detector", AnvilCraftDatagen.has(lookup, Items.DAYLIGHT_DETECTOR))
                .unlockedBy("has_iron_plate", AnvilCraftDatagen.has(lookup, dev.dubhe.anvilcraft.init.item.ModItemTags.IRON_PLATES))
                .save(provider, String.valueOf(ctx.getId().withSuffix("_from_sunflower")));
    }

    public static <T extends net.minecraft.world.level.block.Block> void solarPanelFromCircuitBoard(
            DataGenContext<net.minecraft.world.level.block.Block, T> ctx, RegistrumRecipeProvider provider) {
        HolderGetter<Item> lookup = provider.getItems();
        ShapedRecipeBuilder.shaped(lookup, RecipeCategory.BUILDING_BLOCKS,  ctx.get())
                .pattern("ACA")
                .pattern(" V ")
                .pattern(" C ")
                .define('A', Items.DAYLIGHT_DETECTOR)
                .define('V', ModItems.CIRCUIT_BOARD)
                .define('C', dev.dubhe.anvilcraft.init.item.ModItemTags.IRON_PLATES)
                .unlockedBy("has_circuit_board", AnvilCraftDatagen.has(lookup, ModItems.CIRCUIT_BOARD))
                .unlockedBy("has_daylight_detector", AnvilCraftDatagen.has(lookup, Items.DAYLIGHT_DETECTOR))
                .unlockedBy("has_iron_plate", AnvilCraftDatagen.has(lookup, dev.dubhe.anvilcraft.init.item.ModItemTags.IRON_PLATES))
                .save(provider, String.valueOf(ctx.getId().withSuffix("_from_circuit_board")));
    }

    public static <T extends net.minecraft.world.level.block.Block> void burningCoalBlock(
            DataGenContext<net.minecraft.world.level.block.Block, T> ctx, RegistrumRecipeProvider provider) {
        HolderGetter<Item> lookup = provider.getItems();
        SimpleCookingRecipeBuilder.smelting(
                        Ingredient.of(Items.COAL_BLOCK),
                        RecipeCategory.MISC,
                        CookingBookCategory.BLOCKS,
                         ctx.get(),
                        1.0f,
                        200)
                .unlockedBy("has_coal_block",AnvilCraftDatagen.has(lookup,Items.COAL_BLOCK))
                .unlockedBy("has_coal", AnvilCraftDatagen.has(lookup,Items.COAL))
                .save(provider);
    }

    public static <T extends net.minecraft.world.level.block.Block> void sulfurBlock(
            DataGenContext<net.minecraft.world.level.block.Block, T> ctx, RegistrumRecipeProvider provider) {
        HolderGetter<Item> lookup = provider.getItems();
        ShapedRecipeBuilder.shaped(lookup, RecipeCategory.BUILDING_BLOCKS,  ctx.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItemTags.SULFUR)
                .unlockedBy("has_sulfur", AnvilCraftDatagen.has(lookup, ModItemTags.SULFUR))
                .save(provider);
    }

    public static <T extends net.minecraft.world.level.block.Block> void magnesiumBlock(
            DataGenContext<net.minecraft.world.level.block.Block, T> ctx, RegistrumRecipeProvider provider) {
        HolderGetter<Item> lookup = provider.getItems();
        ShapedRecipeBuilder.shaped(lookup, RecipeCategory.BUILDING_BLOCKS, ctx.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItemTags.MAGNESIUM_INGOTS)
                .unlockedBy("has_magnesium_ingot", AnvilCraftDatagen.has(lookup, ModItemTags.MAGNESIUM_INGOTS))
                .save(provider);
    }

    public static <T extends net.minecraft.world.level.block.Block> void potatoBattery(
            DataGenContext<net.minecraft.world.level.block.Block, T> ctx, RegistrumRecipeProvider provider) {
        HolderGetter<Item> lookup = provider.getItems();
        ShapelessRecipeBuilder.shapeless(lookup, RecipeCategory.MISC,  ctx.get(), 1)
                .requires(dev.dubhe.anvilcraft.init.item.ModItemTags.COPPER_PLATES)
                .requires(Items.POTATO)
                .requires(dev.dubhe.anvilcraft.init.item.ModItemTags.ZINC_PLATES)
                .group(ctx.getId().toString())
                .unlockedBy("has_copper_plate", AnvilCraftDatagen.has(lookup, dev.dubhe.anvilcraft.init.item.ModItemTags.COPPER_PLATES))
                .unlockedBy("has_potato", AnvilCraftDatagen.has(lookup, Items.POTATO))
                .unlockedBy("has_zinc_plate", AnvilCraftDatagen.has(lookup, dev.dubhe.anvilcraft.init.item.ModItemTags.ZINC_PLATES))
                .save(provider);
    }

    public static <T extends net.minecraft.world.level.block.Block> void nuclearCollector(
            DataGenContext<net.minecraft.world.level.block.Block, T> ctx, RegistrumRecipeProvider provider) {
        HolderGetter<Item> lookup = provider.getItems();
        ShapedRecipeBuilder.shaped(lookup, RecipeCategory.BUILDING_BLOCKS,  ctx.get())
                .pattern("E E")
                .pattern(" U ")
                .pattern("BCB")
                .define('U', ModBlocks.URANIUM_BLOCK.asItem())
                .define('B', ModBlocks.EMBER_METAL_BLOCK.asItem())
                .define('E', ModItems.EMBER_METAL_NUGGET)
                .define('C', ModBlocks.HEAT_COLLECTOR.asItem())
                .unlockedBy("has_uranium_block", AnvilCraftDatagen.has(lookup, ModBlocks.URANIUM_BLOCK.asItem()))
                .unlockedBy("has_ember_metal_block", AnvilCraftDatagen.has(lookup, ModBlocks.EMBER_METAL_BLOCK.asItem()))
                .unlockedBy("has_ember_nugget", AnvilCraftDatagen.has(lookup, ModItems.EMBER_METAL_NUGGET))
                .unlockedBy("has_heat_collector", AnvilCraftDatagen.has(lookup, ModBlocks.HEAT_COLLECTOR.asItem()))
                .save(provider);
    }

    public static <T extends net.minecraft.world.level.block.Block> void nuclearBomb(
            DataGenContext<net.minecraft.world.level.block.Block, T> ctx, RegistrumRecipeProvider provider) {
        HolderGetter<Item> lookup = provider.getItems();
        ShapedRecipeBuilder.shaped(lookup, RecipeCategory.BUILDING_BLOCKS, ctx.get())
                .pattern(" B ")
                .pattern("PUP")
                .pattern(" B ")
                .define('U', ModBlocks.URANIUM_BLOCK.asItem())
                .define('B', ModBlocks.EMBER_METAL_BLOCK.asItem())
                .define('P', dev.dubhe.anvilcraft.init.item.ModItemTags.LEAD_PLATES)
                .unlockedBy("has_uranium_block", AnvilCraftDatagen.has(lookup, ModBlocks.URANIUM_BLOCK.asItem()))
                .unlockedBy("has_ember_metal_block", AnvilCraftDatagen.has(lookup, ModBlocks.EMBER_METAL_BLOCK.asItem()))
                .unlockedBy("has_lead_plate", AnvilCraftDatagen.has(lookup, dev.dubhe.anvilcraft.init.item.ModItemTags.LEAD_PLATES))
                .save(provider);
    }

}
