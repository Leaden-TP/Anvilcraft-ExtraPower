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

        @Comment("The power output of a uranium rod")
        @BoundedDiscrete(min = 1, max = 32768)
        public int powerOutput_of_a_uraniumRod = 1500;

        @Comment("The heat of a uranium rod")
        @BoundedDiscrete(min = 1, max = 32768)
        public int heat_of_a_uraniumRod = 5;

        @Comment("The time interval between water searches")
        @BoundedDiscrete(min = 20, max = 1024)
        public int theTimeOfCheckingWater = 100;

        @Comment("Heat dissipation provided per block of water")
        @BoundedDiscrete(min = 1, max = 1024)
        public int theColdOfWater = 5;

        @Comment("Minimum time interval between water searches")
        @BoundedDiscrete(min = 20, max = 1024)
        public int theMinimumTimeOfCheckingWater = 40;

        @Comment("Maximum water surface area")
        @BoundedDiscrete(min = 20, max = 64)
        public int theMaximumWaterSurfaceArea = 20;
    }
    public static class NuclearExplosion {
        @Comment("Explosion level")
        @BoundedDiscrete(min = 3, max = 128)
        public int Explosionlevel = 18;
    }
    // 附魔发电机
    @CollapsibleObject
    public EnchantedGenerator enchantedGenerator = new EnchantedGenerator();

    public static class EnchantedGenerator {
        @Comment("Maximum power for Overclocking Enchanted Generator")
        @BoundedDiscrete(min = 0, max = 128000000)
        public int maxOverclockingPower = 32768;

        @Comment("Maximum power for Common Enchanted Generator")
        @BoundedDiscrete(min = 0, max = 128000000)
        public int maxCommonPower = 1024;

        @Comment("Power per enchantment's level")
        @BoundedDiscrete(min = 1, max = 1000000)
        public int powerPerLevel = 2;

        @Comment("Overclocking amplification")
        @BoundedDiscrete(min = 1, max = 100)
        public int overclockingAmplification = 8;

        @Comment("Minimum consume enchanted book cooldown")
        @BoundedDiscrete(min = 1, max = 1000)
        public int minConsumeCooldown = 10;
    }

    // 水轮发电机
    @CollapsibleObject
    public HydroelectricGenerator hydroelectricGenerator = new HydroelectricGenerator();

    public static class HydroelectricGenerator {
        @Comment("Base power per face with flowing water")
        @BoundedDiscrete(min = 0, max = 128000000)
        public int basePower = 4;

        @Comment("Coefficient of the accumulated fluid level change")
        @BoundedDiscrete(min = 0, max = 128000000)
        public int levelChangeCoefficient = 1;

        @Comment("Divisor applied to fluid density (vanilla water = 1000)")
        @BoundedDiscrete(min = 1, max = 1000)
        public int densityDivisor = 1000;

        @Comment("Power ramp speed per tick when the output changes")
        @BoundedDiscrete(min = 1, max = 100000)
        public int powerRampPerTick = 1;

        @Comment("Power multiplier for a nearby active conduit with 16-27 frames")
        @BoundedDiscrete(min = 1, max = 100)
        public int conduitMultiplierLow = 2;

        @Comment("Power multiplier for a nearby active conduit with 28-41 frames")
        @BoundedDiscrete(min = 1, max = 100)
        public int conduitMultiplierMid = 3;

        @Comment("Power multiplier for a nearby active conduit with 42 frames")
        @BoundedDiscrete(min = 1, max = 100)
        public int conduitMultiplierHigh = 4;
    }

    // 应力发电机（Create 联动）
    @CollapsibleObject
    public StressGenerator stressGenerator = new StressGenerator();

    public static class StressGenerator {
        @Comment("Generator mode: stress consumed per RPM = coefficient * this (1 RPM = coefficient * 16 SU)")
        @BoundedDiscrete(min = 1, max = 1024)
        public int stressPerRpmMultiplier = 4;

        @Comment("Generator mode: SU consumed per kW generated (power = consumed stress / this)")
        @BoundedDiscrete(min = 1, max = 1000000)
        public int stressPerKilowatt = 64;

        @Comment("Motor mode: fixed rotation speed")
        @BoundedDiscrete(min = 0, max = 256)
        public int motorRpm = 32;

        @Comment("Motor mode: stress provided = coefficient * this")
        @BoundedDiscrete(min = 0, max = 100000)
        public int motorSuPerValue = 64;

        @Comment("Motor mode: power consumed = coefficient * this")
        @BoundedDiscrete(min = 0, max = 1000000)
        public int motorPowerPerValue = 1;

        @Comment("Coefficient of a newly placed stress generator")
        @BoundedDiscrete(min = 1, max = 256)
        public int defaultValue = 1;

        @Comment("Maximum coefficient")
        @BoundedDiscrete(min = 1, max = 256)
        public int maxValue = 256;
    }
}
