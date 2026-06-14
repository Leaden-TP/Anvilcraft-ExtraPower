package com.extra.power.init;

import com.extra.power.data.recipe.RegistrumItemRecipeLoader;
import com.extra.power.item.capacitor.EmptyLeadacidBatteryItem;
import com.extra.power.item.capacitor.LeadacidBatteryItem;
import com.extra.power.item.capacitor.MultiphaseCapacitorItem;
import dev.anvilcraft.lib.v2.registrum.util.entry.ItemEntry;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.item.Item;

import net.neoforged.neoforge.common.Tags;
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
            .lang("Magnesium Ingot")
            .tag(ModItemTags.MAGNESIUM_INGOTS, Tags.Items.INGOTS, ItemTags.BEACON_PAYMENT_ITEMS)
            .recipe(RegistrumItemRecipeLoader::magnesiumIngot)
            .register();

    public static final ItemEntry<Item> MAGNESIUM_NUGGET = REGISTRATE.item("magnesium_nugget", Item::new)
            .lang("Magnesium Nugget")
            .tag(ModItemTags.MAGNESIUM_NUGGETS, Tags.Items.NUGGETS)
            .recipe(RegistrumItemRecipeLoader::magnesiumNugget)
            .register();

    public static final ItemEntry<Item> SULFUR = REGISTRATE.item("sulfur", Item::new)
            .lang("Sulfur")
            .tag(ModItemTags.SULFUR)
            .recipe(RegistrumItemRecipeLoader::sulfur)
            .register();

    public static final ItemEntry<Item> ASHES = REGISTRATE.item("ashes", Item::new)
            .lang("Ashes")
            .register();

    public static final ItemEntry<Item> COAL_POWDER = REGISTRATE.item("coal_powder", Item::new)
            .lang("Coal Powder")
            .tag(ModItemTags.COAL_POWDER)
            .register();

    public static final ItemEntry<Item> MAGNESIUM_OXIDE = REGISTRATE.item("magnesium_oxide", Item::new)
            .lang("Magnesium Oxide")
            .register();

    public static final ItemEntry<Item> SULFURIC_ACID = REGISTRATE.item("sulfuric_acid", Item::new)
            .lang("Sulfuric Acid")
            .register();

    public static final ItemEntry<LeadacidBatteryItem> LEAD_ACID_BATTERY = REGISTRATE.item("lead_acid_battery", LeadacidBatteryItem::new)
            .lang("Lead-acid Battery")
            .tag(ModItemTags.CAPACITOR)
            .register();

    public static final ItemEntry<EmptyLeadacidBatteryItem> LEAD_ACID_BATTERY_EMPTY = REGISTRATE.item("lead_acid_battery_empty", EmptyLeadacidBatteryItem::new)
            .lang("Lead-acid Battery Empty")
            .tag(ModItemTags.CAPACITOR)
            .recipe(RegistrumItemRecipeLoader::leadAcidBatteryEmpty)
            .register();

    public static final ItemEntry<MultiphaseCapacitorItem> MULTIPHASE_CAPACITOR_EMPTY = REGISTRATE.item("multiphase_capacitor_empty", MultiphaseCapacitorItem::new)
            .lang("Multiphase Capacitor Empty")
            .tag(ModItemTags.CAPACITOR)
            .recipe(RegistrumItemRecipeLoader::multiphaseCapacitorEmpty)
            .register();

    public static final ItemEntry<MultiphaseCapacitorItem> MULTIPHASE_CAPACITOR = REGISTRATE.item("multiphase_capacitor", MultiphaseCapacitorItem::new)
            .lang("Multiphase Capacitor")
            .tag(ModItemTags.CAPACITOR)
            .recipe(RegistrumItemRecipeLoader::multiphaseCapacitor)
            .register();
}
