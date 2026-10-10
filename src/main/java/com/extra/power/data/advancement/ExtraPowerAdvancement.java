package com.extra.power.data.advancement;

import com.extra.power.advancements.criterion.AnvilProjectionTrigger;
import com.extra.power.advancements.criterion.FrostControllerTrigger;
import com.extra.power.advancements.criterion.IgniteMagnesiumTrigger;
import com.extra.power.advancements.criterion.MagnetAttractAnvilTrigger;
import com.extra.power.advancements.criterion.MagneticDisplayStandTrigger;
import com.extra.power.advancements.criterion.NuclearCollectorTrigger;
import com.extra.power.advancements.criterion.NuclearExplosionHurtTrigger;
import com.extra.power.advancements.criterion.UraniumRodRadiationTrigger;
import com.extra.power.init.AnvilCraftExtrapower;
import com.extra.power.init.ModItems;
import com.extra.power.init.block.ModBlock;
import dev.anvilcraft.lib.v2.registrum.providers.RegistrumAdvancementProvider;
import dev.dubhe.anvilcraft.api.advancement.AdvancementLineHelper;
import net.minecraft.advancements.critereon.MinMaxBounds;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.ItemLike;

public class ExtraPowerAdvancement {
    /** 铁砧工艺的成就背景，直接引用其贴图，不二次分发资源 */
    private static final ResourceLocation BACKGROUND =
        ResourceLocation.fromNamespaceAndPath("anvilcraft", "textures/gui/misc/background/advancement.png");

    /** 铁砧工艺的电容器，为避免与本模组 ModItems 重名而使用全限定名 */
    private static final ItemLike ROOT_ICON = dev.dubhe.anvilcraft.init.item.ModItems.CAPACITOR;

    /** 与铁砧工艺的《强热》一致：集核器周围铀棒（供能）数量的判定下限 */
    private static final int NUCLEAR_POWER_PLANT_RODS = 24;

    /** 《电力强磁》：电磁铁吸引铁砧的判定距离下限（格） */
    private static final int MAGNET_ATTRACT_DISTANCE = 10;

    /** 《隐分身之术》：铁砧投影机产生的虚影判定长度下限（格） */
    private static final int ANVIL_PROJECTION_LENGTH = 16;

    public static void init(RegistrumAdvancementProvider provider) {
        AdvancementLineHelper mainLine = new AdvancementLineHelper(provider, AnvilCraftExtrapower.MODID);

        // region root
        mainLine.next()
            .root(ROOT_ICON, "root", BACKGROUND)
            .enterPowerGrid("enter_power_grid")
            .save("root");
        // endregion

        // region 火力发电线
        AdvancementLineHelper burningLine = mainLine.createBranch();
        burningLine.next()
            .task(ModBlock.BURNING_COAL_BLOCK, "burning_coal")
            .hasItems("has_burning_coal_block", ModBlock.BURNING_COAL_BLOCK)
            .save("burning_coal");
        burningLine.next()
            .task(ModBlock.MAGNESIUM_BLOCK, "ignite_magnesium")
            .addCriterion("ignite_magnesium", IgniteMagnesiumTrigger.TriggerInstance.igniteMagnesium())
            .save("ignite_magnesium");
        // endregion

        // region 太阳能线
        AdvancementLineHelper solarLine = mainLine.createBranch();
        solarLine.next()
            .task(ModBlock.SOLAR_PANEL, "solar_panel")
            .playerPlace("place_solar_panel", ModBlock.SOLAR_PANEL)
            .save("solar_panel");
        solarLine.next()
            .task(ModBlock.ENCHANTMENT_GENERATOR_BLOCK, "knowledge_is_power")
            .recipeMod("craft_enchanted_generator", AnvilCraftExtrapower.MODID, "enchanted_generator")
            .save("knowledge_is_power");
        // endregion

        // region 核能线
        // 《为什么我嘴里有股金属味》《掌控它》《核电，轻而易举啊》都是《核能利用》的直接后继成就
        AdvancementLineHelper nuclearLine = mainLine.createBranch();
        nuclearLine.next()
            .task(ModBlock.NUCLEAR_COLLECTOR, "nuclear_collector")
            .recipeMod("craft_nuclear_collector", AnvilCraftExtrapower.MODID, "nuclear_collector")
            .save("nuclear_collector");
        nuclearLine.createBranch().next()
            .task(ModBlock.URANIUM_ROD, "uranium_rod_radiation")
            .addCriterion("uranium_rod_radiation", UraniumRodRadiationTrigger.TriggerInstance.radiation())
            .save("uranium_rod_radiation");
        nuclearLine.createBranch().next()
            .task(ModBlock.FROST_CONTROLLER, "frost_controller")
            .addCriterion("control_uranium_rod", FrostControllerTrigger.TriggerInstance.controlRod())
            .save("frost_controller");
        nuclearLine.createBranch().next()
            .challenge(ModBlock.NUCLEAR_COLLECTOR, "nuclear_power_plant")
            .addCriterion(
                "rods",
                NuclearCollectorTrigger.TriggerInstance.rods(MinMaxBounds.Ints.atLeast(NUCLEAR_POWER_PLANT_RODS))
            )
            .save("nuclear_power_plant");
        // endregion

        // region 核弹线
        AdvancementLineHelper bombLine = mainLine.createBranch();
        bombLine.next()
            .task(ModBlock.NUCLEAR_BOMB, "nuclear_bomb")
            .recipeMod("craft_nuclear_bomb", AnvilCraftExtrapower.MODID, "nuclear_bomb")
            .save("nuclear_bomb");
        bombLine.next()
            .task(ModBlock.MUSHROOM_CLOUD, "nuclear_explosion")
            .addCriterion(
                "nuclear_explosion_hurt",
                NuclearExplosionHurtTrigger.TriggerInstance.hurtByNuclearExplosion()
            )
            .save("nuclear_explosion");
        // endregion

        // region 自动弩线
        // 自动弩由铁砧工艺的能量武器平台制作，没有原版合成配方，所以直接判定获得物品
        mainLine.createBranch().next()
            .task(ModItems.AUTOMATIC_CROSSBOW, "automatic_crossbow")
            .hasItems("has_automatic_crossbow", ModItems.AUTOMATIC_CROSSBOW)
            .save("automatic_crossbow");
        // endregion

        // region 土豆电池线
        AdvancementLineHelper potatoLine = mainLine.createBranch();
        potatoLine.next()
            .task(ModBlock.POTATO_BATTERY, "potato_battery")
            .recipeMod("craft_potato_battery", AnvilCraftExtrapower.MODID, "potato_battery")
            .save("potato_battery");
        potatoLine.next()
            .task(ModBlock.FLASHING_POTATO_BATTERY, "flashing_potato_battery")
            .hasItems("has_flashing_potato_battery", ModBlock.FLASHING_POTATO_BATTERY)
            .save("flashing_potato_battery");
        // endregion

        // region 红石收发器线
        // 《电力强磁》《隐分身之术》《磁力展示台展示磁力展示台》都是《无线红石收发器》的直接后继成就
        AdvancementLineHelper wirelessLine = mainLine.createBranch();
        wirelessLine.next()
            .task(ModBlock.REDSTONE_LINK, "wireless_redstone")
            .recipeMod("craft_redstone_link", AnvilCraftExtrapower.MODID, "redstone_link")
            .save("wireless_redstone");
        wirelessLine.createBranch().next()
            .task(ModBlock.ELECTROMAGNET, "strong_magnetism")
            .addCriterion(
                "attract_anvil",
                MagnetAttractAnvilTrigger.TriggerInstance.distance(MinMaxBounds.Ints.atLeast(MAGNET_ATTRACT_DISTANCE))
            )
            .save("strong_magnetism");
        wirelessLine.createBranch().next()
            .task(ModBlock.ANVIL_PROJECTOR, "anvil_projection")
            .addCriterion(
                "projection",
                AnvilProjectionTrigger.TriggerInstance.blocks(MinMaxBounds.Ints.atLeast(ANVIL_PROJECTION_LENGTH))
            )
            .save("anvil_projection");
        wirelessLine.createBranch().next()
            .task(ModBlock.MAGNETIC_DISPLAY_STAND, "display_stand_on_display_stand")
            .addCriterion("display_stand", MagneticDisplayStandTrigger.TriggerInstance.displayItself())
            .save("display_stand_on_display_stand");
        // endregion
    }
}
