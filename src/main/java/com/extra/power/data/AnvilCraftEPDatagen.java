package com.extra.power.data;


import com.extra.power.data.advancements.AdvancementHandler;
import com.extra.power.init.AnvilCraftExtrapower;
import com.tterrag.registrate.providers.ProviderType;
import com.tterrag.registrate.providers.RegistrateRecipeProvider;
import net.minecraft.advancements.Criterion;
import net.minecraft.advancements.critereon.InventoryChangeTrigger;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.tags.TagKey;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.ItemLike;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.neoforge.data.event.GatherDataEvent;

import static dev.dubhe.anvilcraft.AnvilCraft.REGISTRATE;

@EventBusSubscriber(modid = AnvilCraftExtrapower.MODID)
public class AnvilCraftEPDatagen {

    /**
     * 初始化生成器
     */
    @SubscribeEvent
    public static void gatherData(GatherDataEvent event) {
    }
    public static void init() {
        REGISTRATE.addDataGenerator(ProviderType.ADVANCEMENT, AdvancementHandler::init);
    }

    public static Criterion<InventoryChangeTrigger.TriggerInstance> has(ItemLike itemLike) {
        return RegistrateRecipeProvider.has(itemLike);
    }

    public static Criterion<InventoryChangeTrigger.TriggerInstance> has(TagKey<Item> tag) {
        return RegistrateRecipeProvider.has(tag);
    }

    public static String hasItem(TagKey<Item> item) {
        return "has_" + item.location().getPath();
    }

    public static String hasItem(ItemLike item) {
        return "has_" + BuiltInRegistries.ITEM.getKey(item.asItem()).getPath();
    }
}
