package com.extra.power.block.blockentity;

import com.extra.power.init.block.ModBlock;
import com.extra.power.init.block.ModBlockEntity;
import com.extra.power.block.just_block.NuclearCollectorBlock;
import com.extra.power.config.ModServerConfig;
import dev.dubhe.anvilcraft.api.power.IPowerProducer;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import dev.dubhe.anvilcraft.api.tooltip.providers.IHasAffectRange;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.util.Mth;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import static com.extra.power.block.just_block.NuclearCollectorBlock.OVERHEATED;
import static com.extra.power.util.NuclearCollectorFunction.*;
import static java.lang.Math.min;

public class NuclearCollectorBlockEntity extends BlockEntity implements IPowerProducer, IHasAffectRange {
    public static final float ROTATION_PRE_POWER = 0.002f;
    private int power = 0;
    private int newpower = 0;
    private int heat = 0;
    private int time = 0;
    private int result = 0;        // 0=Succeed, 1=TooClose, 2=TooHot, 3=NoRod, 4=InvalidRange
    private int all_water = 0;
    private int check_all_water = 0;
    private int check_time = ModServerConfig.nuclearCollector.theTimeOfCheckingWater;

    // 客户端动画字段
    private float clientRotation;
    private float previousClientRotation;
    private float clientRotationSpeed;
    @Setter
    @Getter
    private PowerGrid grid = null;
    @Getter
    private int clientHeat;        // 客户端显示的热量

    public NuclearCollectorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public NuclearCollectorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.NUCLEAR_COLLECTOR.get(), pos, state);
    }

    public static NuclearCollectorBlockEntity createBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState blockState
    ) {
        return new NuclearCollectorBlockEntity(type, pos, blockState);
    }

    // ---------- NBT ----------
    @Override
    protected void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.time = tag.getInt("time");
        this.power = tag.getInt("power");
        this.heat = tag.getInt("heat");
        this.all_water = tag.getInt("all_water");
        this.result = tag.getInt("result");
        this.check_all_water = tag.getInt("check_all_water");
        this.check_time = tag.getInt("check_time");
    }

    @Override
    protected void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("time", this.time);
        tag.putInt("power", this.power);
        tag.putInt("heat", this.heat);
        tag.putInt("all_water", this.all_water);
        tag.putInt("result", this.result);
        tag.putInt("check_all_water", this.check_all_water);
        tag.putInt("check_time", this.check_time);
    }

    // ---------- 网络同步 ----------
    @Override
    public CompoundTag getUpdateTag(HolderLookup.Provider registries) {
        CompoundTag tag = super.getUpdateTag(registries);
        tag.putInt("heat", heat);
        tag.putInt("result", result);
        tag.putInt("power", power);
        return tag;
    }

    @Override
    public void handleUpdateTag(CompoundTag tag, HolderLookup.Provider registries) {
        super.handleUpdateTag(tag, registries);
        this.clientHeat = tag.getInt("heat");
        this.result = tag.getInt("result");
        // 可选：更新power用于客户端旋转，但旋转速度由服务端power决定，通过getServerPower()获得
        // 但客户端需要知道功率，这里同步power以便clientTick中使用
        this.power = tag.getInt("power");
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }


    // ---------- 接口方法 ----------
    @Override
    public void gridTick() {
        // 电力网格专用 tick（如需扩展）
    }

    @Override
    public @Nullable Level getCurrentLevel() {
        return level;
    }

    @Override
    public BlockPos getPos() {
        return getBlockPos();
    }

    @Override
    public int getOutputPower() {
        return this.power;
    }

    public int getServerPower() {
        return this.power;
    }

    @Override
    public int getRange() {
        return ModServerConfig.nuclearCollector.theMaximumWaterSurfaceArea / 2;
    }

    @Override
    public AABB shape() {
        return AABB.ofSize(getBlockPos().getCenter(), 5, 3, 5);
    }

    // ---------- 客户端数据 ----------
    public int getWorkResult() {
        return result;
    }

    public int getHeat() {
        return heat;
    }

    public float getClientRotation(float partialTick) {
        return Mth.lerp(partialTick, previousClientRotation, clientRotation);
    }

    // 服务端调用此方法向客户端推送数据（非必须，因为已有同步包，但保留兼容）
    public void applyClientTelemetry(byte result, int heat) {
        this.result = result;
        this.clientHeat = heat;
    }

    // ---------- 客户端 tick ----------
    private void clientTick() {
        previousClientRotation = clientRotation;
        float targetSpeed = getServerPower() * ROTATION_PRE_POWER / 10.0f;
        clientRotationSpeed = Mth.lerp(0.2f, clientRotationSpeed, targetSpeed);
        clientRotation += clientRotationSpeed;
        wrapClientRotation();
    }

    private void wrapClientRotation() {
        float turns = (float) Math.floor(clientRotation / 360.0f);
        if (turns == 0.0f) return;
        float offset = turns * 360.0f;
        clientRotation -= offset;
        previousClientRotation -= offset;
    }

    // ---------- 静态辅助方法 ----------
    public static boolean isAnotherCollectorNearby(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int i = -4; i <= 4; i++) {
            for (int j = -2; j <= 2; j++) {
                for (int k = -4; k <= 4; k++) {
                    mpos.set(pos).move(i, j, k);
                    if (level.isOutsideBuildHeight(mpos)) continue;
                    BlockState blockState = level.getBlockState(mpos);
                    if (blockState.getBlock() instanceof NuclearCollectorBlock && (i != 0 || j != 0 || k != 0)) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static boolean Validrange(Level level, BlockPos pos, int y) {
        return checkWater(level, pos, y, false) >= 0;
    }

    public static void ChangeGrid(NuclearCollectorBlockEntity entity) {
        if (entity.power != 0 && entity.grid != null) {
            entity.power = 0;
            entity.grid.markChanged();
        }
    }

    // ---------- 主 tick ----------
    public static void tick(Level level, BlockPos pos, BlockState state, NuclearCollectorBlockEntity entity) {
        if (level.isClientSide()) {
            entity.clientTick();
            return;
        }
        entity.time++;

        // 每 20 tick 故障检查（太近）
        if (entity.time % 20 == 0) {
            if (isAnotherCollectorNearby(level, entity.getBlockPos())) {
                ChangeGrid(entity);
                entity.result = 1;
                return;
            }
        }

        // 每 5 秒检查一次总水量（check_all_water == 0 时触发）
        if (entity.check_all_water == 0) {
            int y = findPoint(level, entity.getBlockPos());
            entity.all_water = 0;
            for (int i = y; i > 0; i--) {
                if (!Validrange(level, pos, y)) {
                    ChangeGrid(entity);
                    entity.result = 4;
                    entity.check_all_water = 5;
                    return;
                } else entity.result = 0;
                entity.all_water += checkWater(level, entity.getBlockPos(), i, false);
            }
            entity.check_all_water = 5;
        }

        // 每 20 tick 铀棒检查和产热
        if (entity.time % 20 == 0) {
            entity.check_time = ModServerConfig.nuclearCollector.theTimeOfCheckingWater;
            int rod = checkRod(level, entity.getBlockPos(), entity, true);
            int controller = check_for_cold(level, entity.getBlockPos());
            if (entity.all_water >= 20) {
                entity.check_time += min(ModServerConfig.nuclearCollector.theTimeOfCheckingWater * entity.all_water / 100,
                        ModServerConfig.nuclearCollector.theTimeOfCheckingWater * 5);
                entity.heat += rod / (entity.all_water / 20);
                if (rod / (entity.all_water / 20) <= 1 && rod != 0) {
                    entity.heat += 1;
                }
            } else {
                entity.heat += rod;
            }

            entity.newpower = checkRod_for_power(level, entity.getBlockPos()) * ModServerConfig.nuclearCollector.powerOutput_of_a_uraniumRod;
            if (controller >0 && entity.all_water <=0) entity.newpower /= 3;
            if (entity.newpower == 0 && entity.result != 4 &&entity.result != 2) {
                entity.result = 3;
                return;
            }

            boolean overheated = entity.heat >= ModServerConfig.nuclearCollector.baseHeatLimit * 2 / 3;
            if (state.getValue(OVERHEATED) != overheated) {
                state = state.setValue(OVERHEATED, overheated);
                level.setBlock(pos, state, 11);
            }
            if (overheated) {
                entity.check_time = ModServerConfig.nuclearCollector.theMinimumTimeOfCheckingWater;
                checkRod(level, entity.getBlockPos(), entity, false);
                entity.result = 2;
            } else {
                if (entity.result != 4) entity.result = 0;
            }

            if (entity.heat >= ModServerConfig.nuclearCollector.baseHeatLimit) {
                entity.heat = ModServerConfig.nuclearCollector.baseHeatLimit;
                level.setBlockAndUpdate(pos, ModBlock.MUSHROOM_CLOUD.get().defaultBlockState());
            }
        }

        if (entity.result != 0) {
            ChangeGrid(entity);
        }

        // 耗水逻辑
        if (entity.time % entity.check_time == 0 && entity.heat != 0) {
            int y = findPoint(level, entity.getBlockPos());
            if (!Validrange(level, pos, y)) {
                ChangeGrid(entity);
                entity.result = 4;
                return;
            } else if (state.getValue(OVERHEATED))entity.result = 2;

            BlockState block = level.getBlockState(pos.above(y));
            int checkWater = checkWater(level, entity.getBlockPos(), y, true);
            if (y != 0) checkWater += 1;
            if (checkWater * 10 <= entity.heat) {
                entity.heat -= checkWater * 5;
            } else {
                entity.heat = 0;
            }

            // 冰块溶解链
            if (block == Blocks.WATER.defaultBlockState()) {
                level.setBlock(pos.above(y), Blocks.AIR.defaultBlockState(), 11);
            }
            if (block == Blocks.BLUE_ICE.defaultBlockState()) {
                level.setBlock(pos.above(y), Blocks.PACKED_ICE.defaultBlockState(), 3);
            }
            if (block == Blocks.PACKED_ICE.defaultBlockState()) {
                level.setBlock(pos.above(y), Blocks.ICE.defaultBlockState(), 3);
            }
            if (block == Blocks.ICE.defaultBlockState() || block == Blocks.FROSTED_ICE.defaultBlockState()) {
                level.setBlock(pos.above(y), Blocks.WATER.defaultBlockState(), 3);
            }

            entity.time = 0;
            entity.check_all_water -= 1;
            entity.all_water -= checkWater;
            if (checkWater == 0) entity.all_water = 0;
        }
        if (entity.time >= entity.check_time+5) {entity.time = 0;}
        // 更新电网功率
        if (entity.power != entity.newpower && entity.grid != null && entity.result == 0) {
            entity.power = entity.newpower;
            entity.grid.markChanged();
            level.sendBlockUpdated(pos, state, state, 3);
        }
    }
}