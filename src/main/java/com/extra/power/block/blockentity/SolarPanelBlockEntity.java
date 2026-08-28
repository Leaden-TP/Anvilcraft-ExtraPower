package com.extra.power.block.blockentity;

import com.extra.power.init.block.ModBlockEntity;
import com.extra.power.block.just_block.SolarPanelBlock;
import dev.dubhe.anvilcraft.api.power.IPowerProducer;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import net.minecraft.MethodsReturnNonnullByDefault;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
@MethodsReturnNonnullByDefault
public class SolarPanelBlockEntity extends BlockEntity implements IPowerProducer {
    private PowerGrid grid;
    private int powerOutput = 0;
    private int lastPowerOutput = 0;
    private int power = 0;
    private int tickcounter = 0;
    private boolean wasDay = false;
    private Vector3f normalVector = new Vector3f(0, 1, 0);
    private static final int MAX_DISTANCE = 32;

    // 平滑动画相关变量--使用了AI进行动画润色
    private Vector3f targetNormalVector = new Vector3f(0, 1, 0);
    private Vector3f prevNormalVector = new Vector3f(0, 1, 0);
    private float lerpProgress = 0.0f;
    private static final float LERP_SPEED = 0.1f;
    private static final float DIRECTION_CHANGE_THRESHOLD = 0.1f;

    public static SolarPanelBlockEntity createBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState blockState
    ) {
        return new SolarPanelBlockEntity(type, pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SolarPanelBlockEntity entity) {
        entity.tickcounter++;
        boolean isDay = level.isDay();


        if (level.isClientSide()) {
            if ((entity.tickcounter % 2 == 0) || (isDay != entity.wasDay)) { // 更频繁地更新动画
                entity.updateAnimation(level, isDay);
            }
        }

        // 服务端和客户端都执行的逻辑
        if ((entity.tickcounter % 15 == 0) || (isDay != entity.wasDay)) {
            entity.wasDay = isDay;
            entity.updatePowerState(level, pos, state, isDay);
            if (entity.getOutputPower()==0){level.setBlock(pos, state.setValue(SolarPanelBlock.ACTIVE, false), 3);}
            else {level.setBlock(pos, state.setValue(SolarPanelBlock.ACTIVE, true), 3);}
            if (entity.lastPowerOutput != entity.power || entity.powerOutput!=entity.power) {
                entity.lastPowerOutput = entity.power;
                entity.powerOutput = entity.power;
                if (entity.grid != null) {
                    entity.grid.markChanged();
                }
            }

            entity.tickcounter = 0;
        }
    }

    private void updateAnimation(Level level, boolean isDay) {
        if (isDay) {
            // 计算太阳角度和方向向量
            double sunAngle = level.getSunAngle(1);
            sunAngle = sunAngle <= Math.PI / 2 * 3 ? sunAngle + Math.PI / 2 : sunAngle - Math.PI / 2 * 3;

            if (sunAngle <= Math.PI) {
                Vector3f sunVector = new Vector3f(
                        (float) Math.cos(sunAngle),
                        (float) Math.sin(sunAngle),
                        0
                ).normalize();

                // 更新目标方向
                targetNormalVector.set(sunVector);

                // 如果方向变化超过阈值，重置插值进度
                if (targetNormalVector.distance(normalVector) > DIRECTION_CHANGE_THRESHOLD) {
                    prevNormalVector.set(normalVector);
                    lerpProgress = 0.0f;
                }
            } else {
                resetToDefaultPosition();
            }
        } else {
            resetToDefaultPosition();
        }

        // 更新插值进度
        if (lerpProgress < 1.0f) {
            lerpProgress += LERP_SPEED;
            normalVector = new Vector3f(prevNormalVector).lerp(targetNormalVector, lerpProgress);
        } else {
            normalVector.set(targetNormalVector);
        }
    }

    private void resetToDefaultPosition() {
        targetNormalVector.set(0, 1, 0);
        if (normalVector.y() < 0.99f) {
            prevNormalVector.set(normalVector);
            lerpProgress = 0.0f;
        } else {
            normalVector.set(0, 1, 0);
        }
    }

    private void updatePowerState(Level level, BlockPos pos, BlockState state, boolean isDay) {
        if (isDay) {
            double sunAngle = level.getSunAngle(1);
            sunAngle = sunAngle <= Math.PI / 2 * 3 ? sunAngle + Math.PI / 2 : sunAngle - Math.PI / 2 * 3;

            if (sunAngle <= Math.PI) {
                Vector3f sunVector = new Vector3f(
                        (float) Math.cos(sunAngle),
                        (float) Math.sin(sunAngle),
                        0
                ).normalize();

                // 视线检测
                Vec3 startPos = Vec3.atCenterOf(pos.above());
                Vec3 endPos = startPos.add(sunVector.x * MAX_DISTANCE, sunVector.y * MAX_DISTANCE, sunVector.z * MAX_DISTANCE);

                BlockHitResult hitResult = level.clip(new ClipContext(
                        startPos,
                        endPos,
                        ClipContext.Block.COLLIDER,
                        ClipContext.Fluid.NONE,
                        CollisionContext.empty()
                ));

                // 如果没有障碍物或障碍物距离足够远
                boolean hasClearView = hitResult.getType() == BlockHitResult.Type.MISS ||
                        startPos.distanceTo(hitResult.getLocation()) > MAX_DISTANCE - 2;

                if (hasClearView) {
                    int skyLight = level.getBrightness(LightLayer.SKY, pos.above()) - level.getSkyDarken();
                    int newPower = Math.max(0, skyLight);

                    if (level.isRaining()) {
                        newPower /= 2;
                    }

                    if (newPower != power) {
                        power = newPower;
                        level.setBlock(pos, state.setValue(SolarPanelBlock.ACTIVE, true), 3);
                    }
                } else {
                    power = 0;
                    level.setBlock(pos, state.setValue(SolarPanelBlock.ACTIVE, false), 3);
                }
            } else {
                power = 0;
                level.setBlock(pos, state.setValue(SolarPanelBlock.ACTIVE, false), 3);
            }
        } else {
            power = 0;
            level.setBlock(pos, state.setValue(SolarPanelBlock.ACTIVE, false), 3);
        }
    }

    public Vector3f getNormalVector() {
        return new Vector3f(normalVector); // 返回副本
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
