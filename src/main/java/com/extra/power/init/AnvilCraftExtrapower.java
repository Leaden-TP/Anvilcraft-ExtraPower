package com.extra.power.init;

import com.extra.power.block.ModBlock;
import com.tterrag.registrate.Registrate;
import dev.dubhe.anvilcraft.api.heat.collector.HeatSourceEntry;
import dev.dubhe.anvilcraft.util.ModInteractionMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;

import static com.extra.power.block.ModBlock.BLOCKS;
import static com.extra.power.block.ModBlockEntity.BLOCK_ENTITYS;
import static com.extra.power.init.ModCreativeModeTab.CREATIVE_MODE_TABS;
import static com.extra.power.init.ModItems.*;
import static dev.dubhe.anvilcraft.api.heat.collector.HeatCollectorManager.registerEntry;
import static net.minecraft.world.level.block.AbstractFurnaceBlock.LIT;


@Mod(AnvilCraftExtrapower.MODID)
public class AnvilCraftExtrapower {
    public static final String MODID = "anvilcraftextrapower";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Registrate REGISTRATE = Registrate.create(MODID);

    public AnvilCraftExtrapower(IEventBus modEventBus, ModContainer modContainer) {
        BLOCK_ENTITYS.register(modEventBus);
        CREATIVE_MODE_TABS.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::commonSetup);
    }

    private static void registerEvents(@NotNull IEventBus eventBus) {
        eventBus.addListener(AnvilCraftExtrapower::loadComplete);
    }

    public static @NotNull ResourceLocation of(String path) {
        return ResourceLocation.fromNamespaceAndPath(MODID, path);
    }

    public static void loadComplete(@NotNull FMLLoadCompleteEvent event) {
        event.enqueueWork(ModInteractionMap::initInteractionMap);
    }
    @SubscribeEvent
    public void onServerStarting(ServerStartingEvent event) {
        LOGGER.info("AnvilCraftExtraPower initialized!");
        LOGGER.info("(*^▽^*)");
    }
    private void commonSetup(final FMLCommonSetupEvent event) {
        event.enqueueWork(this::registerHeatSources);
    }
    private void registerHeatSources() {
        registerEntry(HeatSourceEntry.predicateAlways(4, state -> state.is(Blocks.FURNACE)&&state.getValue(LIT)));
        registerEntry(HeatSourceEntry.predicateAlways(8, state -> state.is(Blocks.SMOKER)&&state.getValue(LIT)));
        registerEntry(HeatSourceEntry.predicateAlways(16, state -> state.is(Blocks.BLAST_FURNACE)&&state.getValue(LIT)));
        registerEntry(HeatSourceEntry.simple(1, Blocks.FIRE, Blocks.AIR));
    }
}
