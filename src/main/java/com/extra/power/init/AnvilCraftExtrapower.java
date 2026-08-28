package com.extra.power.init;


import com.extra.power.block.blockentity.RedstoneLinkNetwork;
import com.extra.power.init.block.ModBlockEntity;
import com.extra.power.config.ModServerConfig;
import com.extra.power.data.ModDatagen;
import com.extra.power.init.block.ModFluids;
import com.extra.power.init.data.ModDamageTypes;
import dev.anvilcraft.lib.v2.config.ConfigManager;
import dev.anvilcraft.lib.v2.registrum.Registrum;
import dev.dubhe.anvilcraft.api.heat.collector.HeatSourceEntry;
import dev.dubhe.anvilcraft.util.ModInteractionMap;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.fml.event.lifecycle.FMLCommonSetupEvent;
import net.neoforged.fml.event.lifecycle.FMLLoadCompleteEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import net.neoforged.neoforge.network.registration.PayloadRegistrar;
import org.jetbrains.annotations.NotNull;
import org.slf4j.Logger;
import com.mojang.logging.LogUtils;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.ModContainer;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStartingEvent;
import net.neoforged.neoforge.event.server.ServerStoppingEvent;

import static com.extra.power.init.block.ModBlock.BLOCKS;
import static com.extra.power.init.ModCreativeModeTab.CREATIVE_MODE_TABS;
import static com.extra.power.init.ModItems.*;
import static dev.dubhe.anvilcraft.api.heat.collector.HeatCollectorManager.registerEntry;
import static net.minecraft.world.level.block.AbstractFurnaceBlock.LIT;


@Mod(AnvilCraftExtrapower.MODID)
public class AnvilCraftExtrapower {
    public static final String MODID = "anvilcraftextrapower";
    public static final Logger LOGGER = LogUtils.getLogger();
    public static final Registrum REGISTRATE = Registrum.create(MODID);
    public static final ModServerConfig CONFIG = ConfigManager.register(AnvilCraftExtrapower.MODID, ModServerConfig::new);

    public AnvilCraftExtrapower(IEventBus modEventBus, ModContainer modContainer) {
        CREATIVE_MODE_TABS.register(modEventBus);
        ModEntities.ENTITY_TYPES.register(modEventBus);
        BLOCKS.register(modEventBus);
        ITEMS.register(modEventBus);
        NeoForge.EVENT_BUS.register(this);
        modEventBus.addListener(this::commonSetup);
        modEventBus.addListener(this::registerPayload);
        ModSounds.SOUNDS.register(modEventBus);
        ModDamageTypes.DAMAGE_TYPES.register(modEventBus);


        ModBlockEntity.register();
        ModFluids.register(modEventBus);
        ModDatagen.init();
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

    @SubscribeEvent
    public void onServerStopping(ServerStoppingEvent event) {
        RedstoneLinkNetwork.clear();
    }
    public void registerPayload(@NotNull RegisterPayloadHandlersEvent event) {
        PayloadRegistrar registrar = event.registrar("1");
        ModNetworks.init(registrar);
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
