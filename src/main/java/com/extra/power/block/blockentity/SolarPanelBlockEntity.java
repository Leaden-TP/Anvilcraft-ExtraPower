package com.extra.power.block.blockentity;

import com.extra.power.block.ModBlockEntity;
import com.extra.power.block.just_block.SolarPanelBlock;
import dev.dubhe.anvilcraft.api.power.IPowerProducer;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import dev.dubhe.anvilcraft.util.Util;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.LightLayer;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;
import org.joml.Vector3f;

import javax.annotation.ParametersAreNonnullByDefault;

@ParametersAreNonnullByDefault
public class SolarPanelBlockEntity extends BlockEntity implements IPowerProducer {
    private PowerGrid grid;
    private int powerOutput = 0;
    private int lastPowerOutput = 0;
    private int power = 0;
    private int tickcounter = 0;
    private boolean wasNight = false;  // 改名，明确表示黑夜状态
    private Vector3f normalVector = new Vector3f(0, 1, 0);
    private static final int MAX_DISTANCE = 32;

    // 平滑动画相关变量
    private Vector3f targetNormalVector = new Vector3f(0, 1, 0);
    private Vector3f prevNormalVector = new Vector3f(0, 1, 0);
    private float lerpProgress = 0.0f;
    private static final float LERP_SPEED = 0.1f;
    private static final float DIRECTION_CHANGE_THRESHOLD = 0.1f;
    private static final int POWER_BASE = 8;  // 基础发电量，可调整

    public static SolarPanelBlockEntity createBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState blockState
    ) {
        return new SolarPanelBlockEntity(type, pos, blockState);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, SolarPanelBlockEntity entity) {
        entity.tickcounter++;
        boolean isNight = level.isDarkOutside();  // 正确命名

        if (level.isClientSide()) {
            // 客户端：每2 tick或昼夜变化时更新动画
            if ((entity.tickcounter % 2 == 0) || (isNight != entity.wasNight)) {
                entity.updateAnimation(level, isNight);
            }
        }

        // 服务端：每15 tick或昼夜变化时更新发电
        if ((entity.tickcounter % 15 == 0) || (isNight != entity.wasNight)) {
            entity.wasNight = isNight;
            entity.updatePowerState(level, pos, state, isNight);

            // 更新方块状态（ACTIVE 属性）
            boolean active = entity.getOutputPower() > 0;
            if (state.getValue(SolarPanelBlock.ACTIVE) != active) {
                level.setBlock(pos, state.setValue(SolarPanelBlock.ACTIVE, active), 3);
            }

            // 若功率变化，通知电网
            if (entity.lastPowerOutput != entity.power) {
                entity.lastPowerOutput = entity.power;
                entity.powerOutput = entity.power;
                if (entity.grid != null) {
                    entity.grid.markChanged();
                }
            }

            entity.tickcounter = 0;
        }
    }

    private void updateAnimation(Level level, boolean isNight) {
        // 如果是夜晚，复位到水平位置
        if (isNight) {
            resetToDefaultPosition();
            return;
        }

        // 获取太阳角度（弧度），与定日镜保持一致
        double sunAngle = Util.getSunAngle(level, this.worldPosition.getBottomCenter());
        // 角度转换：原函数返回 0~2PI，调整使正午对应 PI/2
        sunAngle = (sunAngle <= Math.PI / 2 * 3 ? sunAngle + Math.PI / 2 : sunAngle - Math.PI / 2 * 3) +Math.PI;

        // 计算太阳方向向量（三维，忽略Z轴，因为太阳在XZ平面投影为X轴方向，实际太阳路径是东西向，但这里简化为XZ平面）
        Vector3f sunVector = new Vector3f(
                (float) Math.cos(sunAngle),
                (float) Math.sin(sunAngle),
                0
        ).normalize();
        targetNormalVector.set(sunVector);
        // 如果方向变化超过阈值，开始插值
        if (targetNormalVector.distance(normalVector) > DIRECTION_CHANGE_THRESHOLD) {
            prevNormalVector.set(normalVector);
            lerpProgress = 0.0f;
        }
        // 执行插值
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

    private void updatePowerState(Level level, BlockPos pos, BlockState state, boolean isNight) {
        // 夜晚不发电
        if (isNight) {
            power = 0;
            return;
        }

        // 获取太阳角度，与动画计算一致
        double sunAngle = Util.getSunAngle(level, this.worldPosition.getBottomCenter());
        sunAngle = sunAngle <= Math.PI / 2 * 3 ? sunAngle + Math.PI / 2 : sunAngle - Math.PI / 2 * 3;

        // 计算太阳方向向量
        Vector3f sunVector = new Vector3f(
                (float) Math.cos(sunAngle),
                (float) Math.sin(sunAngle),
                0
        ).normalize();

        // 光线检测：从太阳能板上方中心沿太阳方向发射射线
        Vec3 startPos = Vec3.atCenterOf(pos.above());
        Vec3 endPos = startPos.add(sunVector.x * MAX_DISTANCE, sunVector.y * MAX_DISTANCE, sunVector.z * MAX_DISTANCE);

        BlockHitResult hitResult = level.clip(new ClipContext(
                startPos,
                endPos,
                ClipContext.Block.COLLIDER,
                ClipContext.Fluid.NONE,
                (CollisionContext) null
        ));

        boolean hasClearView = hitResult.getType() == BlockHitResult.Type.MISS ||
                startPos.distanceTo(hitResult.getLocation()) > MAX_DISTANCE - 2;

        if (!hasClearView) {
            power = 0;
            return;
        }

        // 检查天空光照是否达到最大值（晴天无云），参考定日镜
        int skyLight = level.getBrightness(LightLayer.SKY, pos.above());
            // 非晴天，发电量减半或更少，可根据云量调整
            power = skyLight;
        // 雨天或雷雨天进一步降低
        if (level.isRaining()) {
            power = power / 2;
        }
        // 限制最大功率 256
        power = Math.min(power, 256);
    }

    public Vector3f getNormalVector() {
        return new Vector3f(normalVector);
    }

    public SolarPanelBlockEntity(BlockPos pos, BlockState blockState) {
        this(ModBlockEntity.SOLAR_PANEL.get(), pos, blockState);
    }

    private SolarPanelBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.lastPowerOutput = input.getIntOr("LastPowerOutput", 0);
        this.wasNight = input.getBooleanOr("wasNight", false);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("LastPowerOutput", this.lastPowerOutput);
        output.putBoolean("wasNight", this.wasNight);
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

    // 客户端接收同步数据（如果需要）
    public void setClientPower(int power, boolean night) {
        this.powerOutput = power;
        this.wasNight = night;
        if (level != null && level.isClientSide()) {
            level.sendBlockUpdated(getBlockPos(), getBlockState(), getBlockState(), 3);
        }
    }
}