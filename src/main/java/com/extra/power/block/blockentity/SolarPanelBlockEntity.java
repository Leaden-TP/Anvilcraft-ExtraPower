package com.extra.power.block.blockentity;

import com.extra.power.block.ModBlockEntity;
import com.extra.power.block.just_block.SolarPanelBlock;
import dev.dubhe.anvilcraft.api.power.IPowerProducer;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.Nullable;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SolarPanelBlockEntity extends BlockEntity implements IPowerProducer {
    private PowerGrid grid;
    private int powerOutput = 0;
    private boolean isActive = false;
    private static int lastPowerOutput = 0; // 用于检测发电量变化
    private boolean lastRainingState = false; // 缓存上一次的天气状态
    private int lastSkyLight = 0; // 缓存上一次的天空光照值
    private int tickCounter = 0; // 用于控制某些计算的频率

    public static void tick(Level level, BlockPos pos, BlockState state, SolarPanelBlockEntity entity) {
        if (level.isClientSide()) return;
        // 每10个tick才进行一次完整的状态检查（减少计算频率）
        entity.tickCounter++;
        boolean shouldFullCheck = entity.tickCounter % 10 == 0;
        // 检查是否为白天且能看到天空
        boolean canGenerate = entity.canGeneratePower(level, pos, shouldFullCheck);
        if (canGenerate) {
            // 计算基础发电量（基于天空光照等级）
            int skyLight = entity.getSkyLight(level, pos, shouldFullCheck);
            int power = Math.max(0, skyLight) / 3 ;
            entity.powerOutput = power;
            // 设置活跃状态
            if (!entity.isActive) {
                entity.isActive = true;
                level.setBlock(pos, state.setValue(SolarPanelBlock.ACTIVE, true), 3);
            }
        }
        else {
            // 夜间或阴天不发电
            entity.powerOutput = 0;
            lastPowerOutput=0;
            // 设置非活跃状态
            if (entity.isActive) {
                entity.isActive = false;
                level.setBlock(pos, state.setValue(SolarPanelBlock.ACTIVE, false), 3);
            }
        }
        // 只有在发电量发生变化时才更新电网
        boolean powerChanged = entity.powerOutput != entity.lastPowerOutput;
        if (entity.grid != null && powerChanged) {
            entity.grid.update(true);
            entity.lastPowerOutput = entity.powerOutput;
        }
        // 重置计数器以防止溢出
        if (entity.tickCounter >= 1000) {
            entity.tickCounter = 0;
        }
    }
    private boolean canGeneratePower(Level level, BlockPos pos, boolean fullCheck) {
        // 如果不是完整检查，使用缓存的结果
        if (!fullCheck) {
            return isActive; // 如果当前是活跃状态，假设仍然可以发电
        }
        // 检查是否为白天
        if (level.isDay()) {
            // 检查是否能看到天空（没有不透明方块遮挡）
            return level.canSeeSky(pos.above());
        }
        return false;
    }
    private int getSkyLight(Level level, BlockPos pos, boolean fullCheck) {
        // 如果不是完整检查，使用缓存的值
        if (!fullCheck) {
            return lastSkyLight;
        }
        // 计算天空光照
        int skyLight = level.getBrightness(LightLayer.SKY, pos.above()) - level.getSkyDarken();
        lastSkyLight = skyLight;
        return skyLight;
    }
    public SolarPanelBlockEntity(BlockPos pos, BlockState blockState) {
        this(ModBlockEntity.SOLAR_PANEL.get(), pos, blockState);
    }

    private SolarPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.powerOutput = tag.getInt("PowerOutput");
        this.isActive = tag.getBoolean("Active");
        this.lastPowerOutput = tag.getInt("LastPowerOutput");
        this.lastRainingState = tag.getBoolean("LastRainingState");
        this.lastSkyLight = tag.getInt("LastSkyLight");
    }

    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("PowerOutput", this.powerOutput);
        tag.putBoolean("Active", this.isActive);
        tag.putInt("LastPowerOutput", this.lastPowerOutput);
        tag.putBoolean("LastRainingState", this.lastRainingState);
        tag.putInt("LastSkyLight", this.lastSkyLight);
    }

    @Override
    public int getOutputPower() {
        return powerOutput;
    }

    @Nullable
    public Level getCurrentLevel() {
        return level;
    }

    public BlockPos getPos() {
        return this.getBlockPos();
    }

    @Override
    public void setGrid(@Nullable PowerGrid grid) {
        this.grid = grid;
    }

    public PowerGrid getGrid() {
        return this.grid;
    }
}
