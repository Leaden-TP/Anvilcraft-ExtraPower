package com.extra.power.init;

import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import com.tterrag.registrate.util.entry.ItemEntry;
import net.minecraft.data.recipes.RecipeCategory;
import net.minecraft.data.recipes.ShapedRecipeBuilder;
import net.minecraft.data.recipes.ShapelessRecipeBuilder;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.common.Tags;
import net.neoforged.neoforge.registries.DeferredHolder;
import net.neoforged.neoforge.registries.DeferredRegister;
import static com.extra.power.init.AnvilCraftExtrapower.MODID;
import static com.extra.power.block.ModBlock.*;
import static com.extra.power.init.AnvilCraftExtrapower.REGISTRATE;

public class ModItems {
    static {
        REGISTRATE.defaultCreativeTab(ModCreativeModeTab.MOD_TAB.getKey());
    }
    public static final DeferredRegister.Items ITEMS = DeferredRegister.Items.createItems(MODID);

 public static final ItemEntry<Item> MAGNESIUM_INGOT = REGISTRATE.item("magnesium_ingot", Item::new)
        .tag(com.extra.power.init.ModItemTags.MAGNESIUM_INGOTS, Tags.Items.INGOTS, ItemTags.BEACON_PAYMENT_ITEMS)
        .recipe((ctx, provider) -> {
            ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ctx.get(), 9)
                    .requires(MAGNESIUM_BLOCK)
                    .group(ctx.getId().toString())
                    .unlockedBy("hasitem", RegistrateRecipeProvider.has(ModItems.MAGNESIUM_NUGGET.get()))
                    .unlockedBy("hasitemblock", RegistrateRecipeProvider.has(MAGNESIUM_BLOCK))
                    .save(provider, ctx.getId().withSuffix("_from_block"));
            ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                    .pattern("AAA")
                    .pattern("AAA")
                    .pattern("AAA")
                    .define('A', com.extra.power.init.ModItemTags.MAGNESIUM_NUGGETS)
                    .group(ctx.getId().toString())
                    .unlockedBy("hasitem", RegistrateRecipeProvider.has(ModItems.MAGNESIUM_INGOT))
                    .save(provider, ctx.getId().withSuffix("_from_nuggets"));
        })
        .register();
    public static final ItemEntry<Item> MAGNESIUM_NUGGET = REGISTRATE.item("magnesium_nugget", Item::new)
            .tag(ModItemTags.MAGNESIUM_NUGGETS, Tags.Items.NUGGETS)
            .recipe((ctx, provider) -> {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ctx.get(), 9)
                        .requires(MAGNESIUM_INGOT)
                        .group(ctx.getId().toString())
                        .unlockedBy("hasitem", RegistrateRecipeProvider.has(ModItems.MAGNESIUM_INGOT))
                        .unlockedBy("hasitemblock", RegistrateRecipeProvider.has(MAGNESIUM_BLOCK))
                        .save(provider);
            })
            .register();
    public static final ItemEntry<Item>SULFUR = REGISTRATE.item("sulfur", Item::new)
            .tag(ModItemTags.SULFUR)
            .recipe((ctx, provider) -> {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ctx.get(), 9)
                        .requires(SULFUR_BLOCK)
                        .group(ctx.getId().toString())
                        .unlockedBy("hasitemblock", RegistrateRecipeProvider.has(SULFUR_BLOCK))
                        .save(provider);
            })
            .register();
    public static final ItemEntry<Item>ASHES = REGISTRATE.item("ashes", Item::new)
            .register();
    public static final ItemEntry<Item>COAL_POWDER= REGISTRATE.item("coal_powder", Item::new)
            .register();
    public static final ItemEntry<Item>MAGNESIUM_OXIDE = REGISTRATE.item("magnesium_oxide", Item::new)
            .register();
    public static final ItemEntry<Item>LEAD_ACID_ELECTROLYTE = REGISTRATE.item("lead_acid_electrolyte", Item::new)
            .register();
    //电池
    public static final ItemEntry<Item> LEAD_ACID_BATTERY = REGISTRATE.item("lead_acid_battery", Item::new)
            .tag(ModItemTags.CAPACITOR)
            .register();
    public static final ItemEntry<Item>LEAD_ACID_BATTERY_EMPTY = REGISTRATE.item("lead_acid_battery_empty", Item::new)
            .tag(ModItemTags.CAPACITOR)
            .recipe((ctx, provider) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                        .pattern("   ")
                        .pattern("C Z")
                        .pattern("RLR")
                        .define('A',ModItems.LEAD_ACID_ELECTROLYTE)
                        .define('C', dev.dubhe.anvilcraft.init.item.ModItemTags.COPPER_PLATES)
                        .define('Z', dev.dubhe.anvilcraft.init.item.ModItemTags.ZINC_PLATES)
                        .define('R', dev.dubhe.anvilcraft.init.item.ModItems.ROYAL_STEEL_INGOT)
                        .group(ctx.getId().toString())
                        .unlockedBy("hasiteml", RegistrateRecipeProvider.has(ModItems.LEAD_ACID_ELECTROLYTE))
                        .unlockedBy("hasitemc", RegistrateRecipeProvider.has(dev.dubhe.anvilcraft.init.item.ModItemTags.COPPER_PLATES))
                        .unlockedBy("hasitemz", RegistrateRecipeProvider.has(dev.dubhe.anvilcraft.init.item.ModItemTags.ZINC_PLATES))
                        .unlockedBy("hasitemr", RegistrateRecipeProvider.has(dev.dubhe.anvilcraft.init.item.ModItems.ROYAL_STEEL_INGOT))
                        .save(provider);
            })
            .register();
    public static final ItemEntry<Item>MULTIPHASE_CAPACITOR_EMPTY = REGISTRATE.item("multiphase_capacitor_empty", Item::new)
            .tag(ModItemTags.CAPACITOR)
            .recipe((ctx, provider) -> {
                ShapedRecipeBuilder.shaped(RecipeCategory.MISC, ctx.get())
                        .pattern("P")
                        .pattern("M")
                        .pattern("P")
                        .define('M', dev.dubhe.anvilcraft.init.item.ModItems.MULTIPHASE_MATTER)
                        .define('P', dev.dubhe.anvilcraft.init.item.ModItemTags.LEAD_PLATES)
                        .group(ctx.getId().toString())
                        .unlockedBy("hasitemm", RegistrateRecipeProvider.has(dev.dubhe.anvilcraft.init.item.ModItems.MULTIPHASE_MATTER))
                        .save(provider);
            })
            .register();
    public static final ItemEntry<Item>MULTIPHASE_CAPACITOR = REGISTRATE.item("multiphase_capacitor", Item::new)
            .tag(ModItemTags.CAPACITOR)
            .recipe((ctx, provider) -> {
                ShapelessRecipeBuilder.shapeless(RecipeCategory.MISC, ctx.get(), 1)
                        .requires(ModItems.MULTIPHASE_CAPACITOR_EMPTY)
                        .requires(dev.dubhe.anvilcraft.init.item.ModItems.MULTIPHASE_MATTER)
                        .group(ctx.getId().toString())
                        .unlockedBy("hasitemm", RegistrateRecipeProvider.has(ModItems.MULTIPHASE_CAPACITOR_EMPTY))
                        .save(provider);
            })
            .register();
}
