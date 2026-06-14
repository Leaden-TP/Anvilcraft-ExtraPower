package com.extra.power.block.blockentity;

import com.extra.power.block.ModBlock;
import com.extra.power.block.ModBlockEntity;
import com.extra.power.block.just_block.NuclearCollectorBlock;
import com.extra.power.config.ModServerConfig;
import com.extra.power.network.NuclearCollectorPacket;
import dev.dubhe.anvilcraft.api.power.IPowerProducer;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import dev.dubhe.anvilcraft.api.tooltip.providers.IHasAffectRange;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;
import org.jetbrains.annotations.Nullable;

import static com.extra.power.block.just_block.NuclearCollectorBlock.OVERHEATED;
import static com.extra.power.function.NuclearCollectorFunction.*;
import static java.lang.Math.max;
import static java.lang.Math.min;

public class NuclearCollectorBlockEntity extends BlockEntity implements IPowerProducer, IHasAffectRange {
    public static final float ROTATION_PRE_POWER = 0.002f;
    private int power = 0;
    private int newpower = 0;
    private int heat = 0;
    private int time = 0;
    private int result = 0;//0->Succeed,1->TooClose,2->TooHot,3->NoRod ,4-> InvalidRange(WaterOrIce)
    private int all_water = 0;
    private int check_all_water = 0;
    private int check_time = ModServerConfig.nuclearCollector.theTimeOfCheckingWater;
    @Getter
    private float rotation = 0;
    @Setter
    @Getter
    private PowerGrid grid = null;
    @Getter
    private int clientHeat;

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

    @Override
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        this.time = input.getIntOr("time", 0);
        this.power = input.getIntOr("power", 0);
        this.heat = input.getIntOr("heat", 0);
        this.all_water = input.getIntOr("all_water", 0);
    }

    @Override
    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("time", this.time);
        output.putInt("power", this.power);
        output.putInt("heat", this.heat);
        output.putInt("all_water", this.all_water);
    }

    @Override
    public void gridTick() {
        // 电力网格专用 tick（如需可扩展）
    }

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

    // ---------- 网络同步 ----------
    private void syncToClient() {
        if (level == null || level.isClientSide()) return;
        PacketDistributor.sendToPlayersTrackingChunk(
                (ServerLevel) level,
                level.getChunk(getBlockPos()).getPos(),
                NuclearCollectorPacket.fromCollector(this)
        );
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NuclearCollectorBlockEntity entity) {
        entity.rotation += entity.getOutputPower() * ROTATION_PRE_POWER / 10;
        entity.time++;
        if (level == null || level.isClientSide()) return;

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

            if (entity.all_water != 0 && entity.all_water >= 20) {
                entity.check_time += min(ModServerConfig.nuclearCollector.theTimeOfCheckingWater * entity.all_water / 100,
                        ModServerConfig.nuclearCollector.theTimeOfCheckingWater * 5);
                entity.heat += rod / (entity.all_water / 20);
                if (rod / (entity.all_water / 20) <= 1 && rod != 0) {
                    entity.heat += 1;
                }
            } else {
                entity.heat += rod;
            }

            entity.newpower = rod * ModServerConfig.nuclearCollector.powerOutput_of_a_uraniumRod;
            if (entity.newpower == 0 && entity.result != 4) {
                entity.result = 3;
                return;
            }

            if (entity.heat >= ModServerConfig.nuclearCollector.baseHeatLimit * 2 / 3) {
                level.setBlock(pos, state.setValue(OVERHEATED, true), 11);
                entity.check_time = ModServerConfig.nuclearCollector.theMinimumTimeOfCheckingWater;
                checkRod(level, entity.getBlockPos(), entity, false);
                entity.result = 2;
            } else {
                level.setBlock(pos, state.setValue(OVERHEATED, false), 11);
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
            } else entity.result = 0;

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

        // 更新电网功率
        if (entity.power != entity.newpower && entity.grid != null && entity.result == 0) {
            entity.power = entity.newpower;
            entity.grid.markChanged();
        }

        // 定期向客户端同步状态（每 20 tick）
        if (entity.time % 2 == 0) {
            entity.syncToClient();
        }
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
        return power;
    }

    @Override
    public int getRange() {
        return ModServerConfig.nuclearCollector.theMaximumWaterSurfaceArea / 2;
    }

    @Override
    public AABB shape() {
        return AABB.ofSize(getBlockPos().getCenter(), 5, 3, 5);
    }

    // 客户端获取数据的方法
    public int getWorkResult() {
        return result;
    }

    public int getHeat() {
        return heat;
    }

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    public void setClientHeat(int heat) {
        this.clientHeat = heat;
    }

    public void setPower(int power) {
        this.power = power;
    }

    public void setResult(int result) {
        this.result = result;
    }
}