package com.extra.power.init;

import com.extra.power.init.block.ModBlock;
import com.extra.power.integration.create.StressGeneratorRegistration;
import com.extra.power.mixin.ModItemGroupsAccessor;
import dev.anvilcraft.lib.v2.registrum.util.CreativeTabSection;
import dev.anvilcraft.lib.v2.registrum.util.CreativeTabSections;
import dev.dubhe.anvilcraft.init.block.ModBlocks;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.CreativeModeTab;

public class ModItemTabContents {
    private ModItemTabContents() {
    }

    private static CreativeTabSection createSection(String texturePath, String titleKey) {
        return CreativeTabSection.builder(
                        ResourceLocation.fromNamespaceAndPath(
                                "anvilcraft",
                                "textures/gui/creative_inventory/section/" + texturePath + ".png"
                        )
                )
                .textAlignment(CreativeTabSection.TextAlignment.RIGHT)
                .textRange(16, 49)
                .text(Component.translatable(titleKey))
                .tooltip(Component.translatable(titleKey))
                .build();
    }

    public static void addItems(
            CreativeModeTab.ItemDisplayParameters parameters,
            CreativeModeTab.Output output
    ) {
        if (ModItemGroupsAccessor.anvilcraft$isLegacyCreativeTabEnabled()) {

            addelectricity(output);
            addAutomation(output);
            addMaterials(output);
            addsign(output);
            addWeapons(output);
            return;
        }
        CreativeTabSections.build(
                AnvilCraftExtrapower.of("addon_items"),
                parameters,
                output,
                sections -> {

                    sections.section(
                            createSection(
                                    "items/power",
                                    "itemGroup.anvilcraftextrapower.section.electricity"
                            ),
                            ModItemTabContents::addelectricity
                    );
                    sections.section(
                            createSection(
                                    "items/produced",
                                    "itemGroup.anvilcraftextrapower.section.automation"
                            ),
                            ModItemTabContents::addAutomation
                    );
                    sections.section(
                            createSection(
                                    "items/materials",
                                    "itemGroup.anvilcraftextrapower.section.materials"
                            ),
                            ModItemTabContents::addMaterials
                    );
                    sections.section(
                            createSection(
                                    "building_blocks/special",
                                    "itemGroup.anvilcraftextrapower.section.sign"
                            ),
                            ModItemTabContents::addsign
                    );
                    sections.section(
                            createSection(
                                    "items/guns",
                                    "itemGroup.anvilcraftextrapower.section.weapons"
                            ),
                            ModItemTabContents::addWeapons
                    );
                }
        );
    }

    private static void addAutomation(CreativeModeTab.Output output) {
        output.accept(ModBlock.LLAMA_ANVIL);
        output.accept(ModBlock.REDSTONE_LINK);
        output.accept(ModBlock.ANVIL_PROJECTOR);
        output.accept(ModBlock.ELECTROMAGNET);
        output.accept(ModBlock.MAGNETIC_DISPLAY_STAND);
    }

    private static void addMaterials(CreativeModeTab.Output output) {
        output.accept(ModItems.ASHES);
        output.accept(ModBlock.ASHES_BLOCK);
        output.accept(ModItems.MAGNESIUM_OXIDE);
        output.accept(ModBlock.MAGNESIUM_OXIDE_BLOCK);
        output.accept(ModItems.MAGNESIUM_NUGGET);
        output.accept(ModItems.MAGNESIUM_INGOT);
        output.accept(ModBlock.MAGNESIUM_BLOCK);
        output.accept(ModItems.SULFUR);
        output.accept(ModBlock.SULFUR_BLOCK);
        output.accept(ModItems.SULFURIC_ACID);
    }

    private static void addWeapons(CreativeModeTab.Output output) {
        output.accept(ModItems.AUTOMATIC_CROSSBOW);
        output.accept(ModBlock.NUCLEAR_BOMB);
        output.accept(ModBlock.MUSHROOM_CLOUD);
    }

    private static void addelectricity(CreativeModeTab.Output output) {
        output.accept(ModBlock.SOLAR_PANEL);
        output.accept(ModBlock.HYDROELECTRIC_GENERATOR);
        output.accept(ModBlock.ENCHANTMENT_GENERATOR_BLOCK);
        output.accept(ModBlock.NUCLEAR_COLLECTOR);
        output.accept(ModBlock.FROST_CONTROLLER);
        output.accept(ModBlock.URANIUM_ROD);
        output.accept(ModBlocks.PLUTONIUM_BLOCK);
        output.accept(StressGeneratorRegistration.STRESS_GENERATOR);
        output.accept(ModBlock.BURNING_MAGNESIUM_BLOCK);
        output.accept(ModBlock.BURNING_COAL_BLOCK);
        output.accept(ModBlock.POTATO_BATTERY);
        output.accept(ModBlock.FLASHING_POTATO_BATTERY);
        output.accept(ModItems.LEAD_ACID_BATTERY);
        output.accept(ModItems.LEAD_ACID_BATTERY_EMPTY);
        output.accept(ModItems.MULTIPHASE_CAPACITOR);
        output.accept(ModItems.MULTIPHASE_CAPACITOR_EMPTY);
    }

    private static void addsign(CreativeModeTab.Output output) {
        output.accept(ModBlock.SIGN);
        output.accept(ModBlock.SIGN_ANVIL_FALL);
        output.accept(ModBlock.SIGN_CONSTRUCTION);
        output.accept(ModBlock.SIGN_DO_NOT_OPERATE);
        output.accept(ModBlock.SIGN_HIGHSPEED_ANVIL);
        output.accept(ModBlock.SIGN_LASER_HAZARD);
        output.accept(ModBlock.SIGN_RADIATION);
        output.accept(ModBlock.SIGN_TIME_HAZARD);
        output.accept(ModBlock.SIGN_STRONG_GRAVITY);
        output.accept(ModBlock.SIGN_MAGNETIC_FIELD);
    }
}
