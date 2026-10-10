package com.extra.power.block.blockentity;

import com.extra.power.block.just_block.HydroelectricGeneratorBlock;
import com.extra.power.init.block.ModBlockEntity;
import dev.dubhe.anvilcraft.api.power.IPowerProducer;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import dev.dubhe.anvilcraft.api.tooltip.providers.IHasAffectRange;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.entity.ConduitBlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FlowingFluid;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import static com.extra.power.init.AnvilCraftExtrapower.CONFIG;

/** 水轮发电机：通过比较水平 3*3 液面高度变化发电 */
public class HydroelectricGeneratorBlockEntity extends BlockEntity implements IPowerProducer, IHasAffectRange {
    /** 一个发电周期 2 秒 */
    public static final int CYCLE = 40;
    /** 每单位实际电量的转速 */
    public static final float ROTATION_PRE_POWER = 0.02f;
    /** 3x3 环形八格偏移（顺时针，自西北角起） */
    private static final int[][] RING = {{-1, -1}, {0, -1}, {1, -1}, {1, 0}, {1, 1}, {0, 1}, {-1, 1}, {-1, 0}};
    /** 四个面各自的三格，角落格与相邻面共用 */
    private static final int[][] FACE_CELLS = {{0, 1, 2}, {2, 3, 4}, {4, 5, 6}, {6, 7, 0}};
    /** 四个面紧邻发电机的正交邻格 */
    private static final int[] FACE_MID = {1, 3, 5, 7};
    private static final Direction[] FACE_DIRS = {Direction.NORTH, Direction.EAST, Direction.SOUTH, Direction.WEST};
    /** 原版潮涌核心的框架分档阈值 */
    private static final int FRAMES_FULL = 42;
    private static final int FRAMES_MID = 28;
    private static final int FRAMES_MIN = 16;

    private final int[] lastLevel = new int[8];
    private final float[] faceChange = new float[4];
    private final boolean[] faceFlowing = new boolean[4];
    private int power = 0;
    /** 本次结算得到的目标发电量，实际发电量每 tick 向其过渡 */
    private int targetPower = 0;
    /** 本周期内四个面的转向投票：1 顺时针、2 逆时针、3 两者都有 */
    private int cycleVotes = 0;
    /** 周期末本次检测的面数、变化量之和与转向投票 */
    private int evalFaces = 0;
    private float evalChange = 0;
    private int evalVotes = 0;
    @Setter
    @Getter
    private PowerGrid grid = null;
    @Getter
    private int time = 0;
    @Getter
    private float rotation = 0;

    public HydroelectricGeneratorBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public HydroelectricGeneratorBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.HYDROELECTRIC_GENERATOR.get(), pos, state);
    }

    public static HydroelectricGeneratorBlockEntity createBlockEntity(
        BlockEntityType<?> type,
        BlockPos pos,
        BlockState blockState
    ) {
        return new HydroelectricGeneratorBlockEntity(type, pos, blockState);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.power = tag.getInt("power");
        this.targetPower = tag.getInt("targetPower");
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("power", this.power);
        tag.putInt("targetPower", this.targetPower);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, HydroelectricGeneratorBlockEntity entity) {
        if (level.isClientSide()) entity.clientTick();
        else entity.serverTick();
    }

    public void clientTick() {
        // 只有整体最下方的发电机累加旋转，其余成员渲染时取它的值
        if (this.groupMaster() != this) return;
        this.rotation += this.spinDirection() * this.getServerPower() * ROTATION_PRE_POWER;
    }

    private void serverTick() {
        Level level = this.level;
        if (level == null) return;
        this.time++;
        this.sample(level);
        // 竖直整体由世界时间对齐同一周期：先各自结算，再统一应用
        long phase = level.getGameTime() % CYCLE;
        if (phase == 0) this.evaluateCycle();
        else if (phase == 1) this.applyCycle(level);
        this.rampPower();
    }

    /** 周期末快照本次检测结果，供整体统一判定 */
    private void evaluateCycle() {
        int faces = 0;
        float change = 0;
        for (int f = 0; f < 4; f++) {
            if (this.faceFlowing[f]) faces++;
            change += this.faceChange[f];
            this.faceFlowing[f] = false;
            this.faceChange[f] = 0;
        }
        this.evalFaces = faces;
        this.evalChange = change;
        this.evalVotes = this.cycleVotes;
        this.cycleVotes = 0;
    }

    /** 应用本次结算：按自身检测结果得出目标发电量 */
    private void applyCycle(Level level) {
        int newPower = 0;
        if (this.canGenerate()) {
            long raw = Math.round(this.evalFaces * (double) CONFIG.hydroelectricGenerator.powerPerFlowingFace
                + this.evalChange * CONFIG.hydroelectricGenerator.levelChangeCoefficient);
            long total = raw * this.conduitMultiplier(level);
            newPower = (int) Math.min(Math.max(total, 0L), Integer.MAX_VALUE);
        }
        this.syncClockwise(level);
        this.targetPower = newPower;
    }

    /** 发电量变化时由原发电量按每 tick 固定幅度线性过渡到目标值 */
    private void rampPower() {
        if (this.power == this.targetPower) return;
        int rate = Math.max(1, CONFIG.hydroelectricGenerator.powerRampPerTick);
        if (this.power < this.targetPower) {
            this.power = (int) Math.min((long) this.power + rate, this.targetPower);
        } else {
            this.power = (int) Math.max((long) this.power - rate, this.targetPower);
        }
        if (this.grid != null) this.grid.markChanged();
    }

    /** 本次检测能否正常发电 */
    private boolean canGenerate() {
        return this.evalFaces > 0 && this.evalVotes != 3;
    }

    /** 整体最下方的发电机，找不到时返回自身 */
    public HydroelectricGeneratorBlockEntity groupMaster() {
        Level level = this.level;
        if (level == null) return this;
        BlockPos.MutableBlockPos mpos = this.worldPosition.mutable();
        HydroelectricGeneratorBlockEntity master = this;
        while (level.getBlockState(mpos.move(0, -1, 0)).getBlock() instanceof HydroelectricGeneratorBlock) {
            if (level.getBlockEntity(mpos) instanceof HydroelectricGeneratorBlockEntity be) master = be;
        }
        return master;
    }

    /** 每 tick 采样 3*3 液面高度并与上一 tick 比较 */
    private void sample(Level level) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        int[] levels = new int[8];
        float[] densities = new float[8];
        boolean[] flowing = new boolean[8];
        FluidState[] fluids = new FluidState[8];
        float divisor = Math.max(1, CONFIG.hydroelectricGenerator.densityDivisor);
        for (int i = 0; i < 8; i++) {
            mpos.setWithOffset(this.worldPosition, RING[i][0], 0, RING[i][1]);
            FluidState fluid = level.getFluidState(mpos);
            fluids[i] = fluid;
            if (!fluid.hasProperty(FlowingFluid.LEVEL)) continue;
            levels[i] = fluid.getValue(FlowingFluid.LEVEL);
            flowing[i] = !fluid.isSource();
            densities[i] = fluid.getFluidType().getDensity() / divisor;
        }
        // 顺时针记 1、逆时针记 2，两种同时出现即方向不统一
        int votes = 0;
        for (int f = 0; f < 4; f++) {
            for (int c = 0; c < 3; c++) {
                int i = FACE_CELLS[f][c];
                int diff = Math.abs(levels[i] - this.lastLevel[i]);
                if (diff > 0) this.faceChange[f] += diff * densities[i];
                if (flowing[i]) this.faceFlowing[f] = true;
            }
            int mid = FACE_MID[f];
            if (!flowing[mid]) continue;
            mpos.setWithOffset(this.worldPosition, RING[mid][0], 0, RING[mid][1]);
            Vec3 flow = fluids[mid].getFlow(level, mpos);
            Direction dir = FACE_DIRS[f];
            // 面的顺时针切向：北->东->南->西
            double dot = flow.x * -dir.getStepZ() + flow.z * dir.getStepX();
            if (dot > 1.0E-4) votes |= 1;
            else if (dot < -1.0E-4) votes |= 2;
        }
        this.cycleVotes |= votes;
        System.arraycopy(levels, 0, this.lastLevel, 0, 8);
    }

    /** 把水流方向写入方块状态，供渲染器决定叶轮转向 */
    private void syncClockwise(Level level) {
        if (this.evalVotes != 1 && this.evalVotes != 2) return;
        BlockState state = this.getBlockState();
        boolean clockwise = this.evalVotes == 1;
        if (state.getValue(HydroelectricGeneratorBlock.CLOCKWISE) == clockwise) return;
        level.setBlockAndUpdate(this.worldPosition, state.setValue(HydroelectricGeneratorBlock.CLOCKWISE, clockwise));
    }

    /** 水流是否顺时针 */
    public boolean isClockwise() {
        BlockState state = this.getBlockState();
        return !state.hasProperty(HydroelectricGeneratorBlock.CLOCKWISE)
            || state.getValue(HydroelectricGeneratorBlock.CLOCKWISE);
    }

    /** 叶轮转向：俯视顺时针为 -1，逆时针为 1 */
    public float spinDirection() {
        return this.isClockwise() ? -1.0F : 1.0F;
    }

    /** 下方激活的潮涌核心按框架数放大发电量 */
    private int conduitMultiplier(Level level) {
        BlockPos conduitPos = this.worldPosition.below();
        if (!(level.getBlockEntity(conduitPos) instanceof ConduitBlockEntity conduit)) return 1;
        if (!conduit.isActive()) return 1;
        int frames = countConduitFrames(level, conduitPos);
        if (frames >= FRAMES_FULL) return CONFIG.hydroelectricGenerator.conduitMultiplierHigh;
        if (frames >= FRAMES_MID) return CONFIG.hydroelectricGenerator.conduitMultiplierMid;
        if (frames >= FRAMES_MIN) return CONFIG.hydroelectricGenerator.conduitMultiplierLow;
        return 1;
    }

    /** 复刻原版潮涌核心的框架统计 */
    private static int countConduitFrames(Level level, BlockPos pos) {
        for (int x = -1; x <= 1; x++) {
            for (int y = -1; y <= 1; y++) {
                for (int z = -1; z <= 1; z++) {
                    if (!level.isWaterAt(pos.offset(x, y, z))) return 0;
                }
            }
        }
        int count = 0;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int x = -2; x <= 2; x++) {
            for (int y = -2; y <= 2; y++) {
                for (int z = -2; z <= 2; z++) {
                    int ax = Math.abs(x);
                    int ay = Math.abs(y);
                    int az = Math.abs(z);
                    if (ax <= 1 && ay <= 1 && az <= 1) continue;
                    if (!(x == 0 && (ay == 2 || az == 2) || y == 0 && (ax == 2 || az == 2) || z == 0 && (ax == 2 || ay == 2))) {
                        continue;
                    }
                    mpos.setWithOffset(pos, x, y, z);
                    if (level.getBlockState(mpos).isConduitFrame(level, mpos, pos)) count++;
                }
            }
        }
        return count;
    }

    @Override
    public @Nullable Level getCurrentLevel() {
        return this.level;
    }

    @Override
    public BlockPos getPos() {
        return this.getBlockPos();
    }

    @Override
    public int getOutputPower() {
        return this.power;
    }

    @Override
    public int getRange() {
        return 1;
    }

    @Override
    public AABB shape() {
        return AABB.ofSize(this.getBlockPos().getCenter(), 3, 3, 3);
    }
}
