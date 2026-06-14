package com.extra.power.config;

import com.extra.power.init.AnvilCraftExtrapower;

import dev.anvilcraft.lib.v2.config.BoundedDiscrete;
import dev.anvilcraft.lib.v2.config.CollapsibleObject;
import dev.anvilcraft.lib.v2.config.Comment;
import dev.anvilcraft.lib.v2.config.Config;
import net.neoforged.fml.config.ModConfig;

@Config(name = AnvilCraftExtrapower.MODID, type = ModConfig.Type.SERVER)
public class ModServerConfig {
    @CollapsibleObject
    public static NuclearCollector  nuclearCollector  = new  NuclearCollector();
    @CollapsibleObject
    public static NuclearExplosion  nuclearExplosion  = new  NuclearExplosion();
    public static class NuclearCollector {
        @Comment("Maximum heat that the Nuclear Collector can withstand")
        @BoundedDiscrete(min = 100, max = 32768)
        public int baseHeatLimit = 1024;

        @Comment("The power output of a uranium rod(this*5)")
        @BoundedDiscrete(min = 1, max = 32768)
        public int powerOutput_of_a_uraniumRod = 150;

        @Comment("The time interval between water searches")
        @BoundedDiscrete(min = 20, max = 1024)
        public int theTimeOfCheckingWater = 100;

        @Comment("Minimum time interval between water searches")
        @BoundedDiscrete(min = 20, max = 1024)
        public int theMinimumTimeOfCheckingWater = 40;

        @Comment("Maximum water surface area")
        @BoundedDiscrete(min = 20, max = 64)
        public int theMaximumWaterSurfaceArea = 20;
    }
    public static class NuclearExplosion {
        @Comment("Explosion level")
        @BoundedDiscrete(min = 3, max = 15)
        public int Explosionlevel = 9;
    }
}
