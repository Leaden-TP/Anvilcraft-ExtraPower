package com.extra.power.config;

import com.extra.power.init.AnvilCraftExtrapower;
import dev.anvilcraft.lib.config.BoundedDiscrete;
import dev.anvilcraft.lib.config.CollapsibleObject;
import dev.anvilcraft.lib.config.Comment;
import net.neoforged.fml.config.ModConfig;
import dev.anvilcraft.lib.config.Config;

@Config(name = AnvilCraftExtrapower.MODID, type = ModConfig.Type.SERVER)
public class ModServerConfig {
    @CollapsibleObject
    public static NuclearCollector  nuclearCollector  = new  NuclearCollector();

    public static class NuclearCollector {
        @Comment("Maximum heat that the Nuclear Collector can withstand")
        @BoundedDiscrete(min = 100, max = 32768)
        public int baseHeatLimit = 1024;

        @Comment("The power output of a uranium rod(this*5)")
        @BoundedDiscrete(min = 1, max = 32768)
        public int powerOutput_of_a_uraniumRod = 1000;

        @Comment("The time interval between water searches")
        @BoundedDiscrete(min = 20, max = 1024)
        public int theTimeOfCheckingWater = 100;

        @Comment("Minimum time interval between water searches")
        @BoundedDiscrete(min = 20, max = 1024)
        public int theMinimumTimeOfCheckingWater = 40;
    }
}
