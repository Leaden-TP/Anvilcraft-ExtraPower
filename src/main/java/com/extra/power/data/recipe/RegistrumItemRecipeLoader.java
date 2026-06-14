package com.extra.power.data.recipe;

import com.extra.power.block.ModBlock;
import com.extra.power.init.ModItemTags;
import com.extra.power.init.ModItems;
import dev.anvilcraft.lib.v2.registrum.providers.DataGenContext;
import dev.anvilcraft.lib.v2.registrum.providers.generators.RegistrumRecipeProvider;
import dev.dubhe.anvilcraft.data.AnvilCraftDatagen;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import net.minecraft.core.HolderGetter;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.world.item.Item;

public class RegistrumItemRecipeLoader {
    public static <T extends Item> void magnesiumIngot(
            DataGenContext<Item, T> ctx, RegistrumRecipeProvider provider) {
        HolderGetter<Item> lookup = provider.getItems();
        // 从镁块分解
        ShapelessRecipeBuilder.shapeless(lookup, RecipeCategory.MISC, ctx.get(), 9)
                .requires(ModBlock.MAGNESIUM_BLOCK)
                .group(ctx.getId().toString())
                .unlockedBy("has_magnesium_block", AnvilCraftDatagen.has(lookup, ModBlock.MAGNESIUM_BLOCK.asItem()))
                .save(provider, String.valueOf(ctx.getId().withSuffix("_from_block")));
        // 从镁粒合成
        ShapedRecipeBuilder.shaped(lookup, RecipeCategory.MISC, ctx.get())
                .pattern("AAA")
                .pattern("AAA")
                .pattern("AAA")
                .define('A', ModItemTags.MAGNESIUM_NUGGETS)
                .group(ctx.getId().toString())
                .unlockedBy("has_magnesium_nugget", AnvilCraftDatagen.has(lookup, ModItems.MAGNESIUM_NUGGET.get()))
                .save(provider, String.valueOf(ctx.getId().withSuffix("_from_nuggets")));
    }

    public static <T extends Item> void magnesiumNugget(
            DataGenContext<Item, T> ctx, RegistrumRecipeProvider provider) {
        HolderGetter<Item> lookup = provider.getItems();
        ShapelessRecipeBuilder.shapeless(lookup, RecipeCategory.MISC, ctx.get(), 9)
                .requires(ModItems.MAGNESIUM_INGOT)
                .group(ctx.getId().toString())
                .unlockedBy("has_magnesium_ingot", AnvilCraftDatagen.has(lookup, ModItems.MAGNESIUM_INGOT.get()))
                .save(provider);
    }

    public static <T extends Item> void sulfur(
            DataGenContext<Item, T> ctx, RegistrumRecipeProvider provider) {
        HolderGetter<Item> lookup = provider.getItems();
        ShapelessRecipeBuilder.shapeless(lookup, RecipeCategory.MISC, ctx.get(), 9)
                .requires(ModBlock.SULFUR_BLOCK)
                .group(ctx.getId().toString())
                .unlockedBy("has_sulfur_block", AnvilCraftDatagen.has(lookup, ModBlock.SULFUR_BLOCK.asItem()))
                .save(provider);
    }

    public static <T extends Item> void leadAcidBatteryEmpty(
            DataGenContext<Item, T> ctx, RegistrumRecipeProvider provider) {
        HolderGetter<Item> lookup = provider.getItems();
        ShapedRecipeBuilder.shaped(lookup, RecipeCategory.MISC, ctx.get())
                .pattern("   ")
                .pattern("A A")
                .pattern("RBR")
                .define('A', dev.dubhe.anvilcraft.init.item.ModItemTags.LEAD_INGOTS)
                .define('B', ModItems.SULFURIC_ACID.get())
                .define('R', dev.dubhe.anvilcraft.init.item.ModItems.ROYAL_STEEL_INGOT)
                .group(ctx.getId().toString())
                .unlockedBy("has_sulfuric_acid", AnvilCraftDatagen.has(lookup, ModItems.SULFURIC_ACID.get()))
                .unlockedBy("has_royal_steel", AnvilCraftDatagen.has(lookup, dev.dubhe.anvilcraft.init.item.ModItems.ROYAL_STEEL_INGOT))
                .save(provider);
    }

    public static <T extends Item> void multiphaseCapacitorEmpty(
            DataGenContext<Item, T> ctx, RegistrumRecipeProvider provider) {
        HolderGetter<Item> lookup = provider.getItems();
        ShapedRecipeBuilder.shaped(lookup, RecipeCategory.MISC, ctx.get())
                .pattern("P")
                .pattern("M")
                .pattern("P")
                .define('M', dev.dubhe.anvilcraft.init.item.ModItems.MULTIPHASE_MATTER)
                .define('P', dev.dubhe.anvilcraft.init.item.ModItemTags.LEAD_PLATES)
                .group(ctx.getId().toString())
                .unlockedBy("has_multiphase_matter", AnvilCraftDatagen.has(lookup, dev.dubhe.anvilcraft.init.item.ModItems.MULTIPHASE_MATTER))
                .save(provider);
    }

    public static <T extends Item> void multiphaseCapacitor(
            DataGenContext<Item, T> ctx, RegistrumRecipeProvider provider) {
        HolderGetter<Item> lookup = provider.getItems();
        ShapelessRecipeBuilder.shapeless(lookup, RecipeCategory.MISC, ctx.get(), 1)
                .requires(ModItems.MULTIPHASE_CAPACITOR_EMPTY)
                .requires(ModBlocks.MULTIPHASE_MATTER_BLOCK)
                .group(ctx.getId().toString())
                .unlockedBy("has_empty_capacitor", AnvilCraftDatagen.has(lookup, ModItems.MULTIPHASE_CAPACITOR_EMPTY.get()))
                .save(provider);
    }
}
