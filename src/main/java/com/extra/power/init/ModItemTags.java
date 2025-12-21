package com.extra.power.init;

import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;

import static dev.dubhe.anvilcraft.init.item.ModItemTags.bindC;

public class ModItemTags {
    public static final TagKey<Item> STORAGE_BLOCKS_MAGNESIUM = bindC("storage_blocks/magnesium");
    public static final TagKey<Item> STORAGE_BLOCKS_SULFUR = bindC("storage_blocks/sulfur");

    public static final TagKey<Item> COAL_POWDER = bindC("dust/coal");

    public static final TagKey<Item> MAGNESIUM_NUGGETS = bindC("nuggets/magnesium");

    public static final TagKey<Item> MAGNESIUM_INGOTS = bindC("ingots/magnesium");

    public static final TagKey<Item> SULFUR = bindC("sulfur");

    public static final TagKey<Item>CAPACITOR= bindC("capacitor");
}
