package com.extra.power.block.blockentity;

import com.extra.power.block.ModBlockEntity;
import com.extra.power.block.just_block.SolarPanelBlock;
import dev.dubhe.anvilcraft.api.power.IPowerProducer;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
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
    private int lastPowerOutput = 0;
    private int power = 0;
    private int tickcounter = 0;
    private boolean wasDay = false; // 缓存昼夜状态

    public static void tick(Level level, BlockPos pos, BlockState state, SolarPanelBlockEntity entity) {
        if (level.isClientSide()) return;

        entity.tickcounter++;
        boolean isDay = level.isDay();

        // 只在昼夜变化或每15tick时检查一次
        if ((entity.tickcounter % 15 == 0) || (isDay != entity.wasDay)) {
            entity.wasDay = isDay;

            if (isDay) {
                int skyLight = level.getBrightness(LightLayer.SKY, pos.above()) - level.getSkyDarken();
                boolean canSeeSky = level.canSeeSky(pos.above());

                if (canSeeSky) {
                    int newPower = Math.max(0, skyLight) / 3;
                    if (level.isRaining()) {
                        newPower /= 2;
                    }
                    // 只在功率变化时更新状态
                    if (newPower != entity.power) {
                        entity.power = newPower;
                        level.setBlock(pos, state.setValue(SolarPanelBlock.ACTIVE, true), 3);
                    }
                } else {
                    entity.power = 0;
                    level.setBlock(pos, state.setValue(SolarPanelBlock.ACTIVE, false), 3);
                }
            } else {
                entity.power = 0;
                level.setBlock(pos, state.setValue(SolarPanelBlock.ACTIVE, false), 3);
            }

            // 只在发电量变化时更新电网
            if (entity.lastPowerOutput != entity.power) {
                entity.lastPowerOutput = entity.power;
                entity.powerOutput = entity.power;
                level.playSound(null,pos, SoundEvents.ANVIL_LAND, SoundSource.PLAYERS,
                        0.7F, 1.0F);
                if (entity.grid != null) {
                    entity.grid.update(true);
                }
            }

            entity.tickcounter = 0;
        }
    }

    public SolarPanelBlockEntity(BlockPos pos, BlockState blockState) {
        this(ModBlockEntity.SOLAR_PANEL.get(), pos, blockState);
    }

    private SolarPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.lastPowerOutput = tag.getInt("LastPowerOutput");
    }

    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("LastPowerOutput", this.lastPowerOutput);
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
