package com.extra.power.block.blockentity;

import com.extra.power.block.ModBlockEntity;
import com.extra.power.block.just_block.NuclearCollectorBlock;
import com.extra.power.block.just_block.UraniumRodBlock;
import com.extra.power.config.ModServerConfig;
import dev.dubhe.anvilcraft.api.power.IPowerProducer;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import dev.dubhe.anvilcraft.api.tooltip.providers.IHasAffectRange;
import dev.dubhe.anvilcraft.block.state.Vertical3PartHalf;
import lombok.Getter;
import lombok.Setter;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.*;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.concurrent.atomic.AtomicInteger;

import static com.extra.power.block.just_block.NuclearCollectorBlock.OVERHEATED;
import static com.extra.power.block.just_block.UraniumRodBlock.ACTIVE;
import static com.extra.power.block.just_block.UraniumRodBlock.HALF;
import static net.minecraft.world.level.block.Block.dropResources;

public class NuclearCollectorBlockEntity extends BlockEntity implements IPowerProducer, IHasAffectRange {
    public static final float ROTATION_PRE_POWER = 0.002f;
    private int power = 0;
    private int newpower = 0;
    private int heat = 0;
    private int time = 0;
    private int check_time = ModServerConfig.nuclearCollector.theTimeOfCheckingWater;
    @Getter
    private float rotation = 0;
    @Setter
    @Getter
    private PowerGrid grid = null;
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
    public void loadAdditional(CompoundTag tag, HolderLookup.  Provider registries) {
        super.loadAdditional(tag, registries);
        this.time = tag.getInt("time");
        this.power = tag.getInt("power");
        this.heat = tag.getInt("heat");
    }
    @Override
    public void saveAdditional(  CompoundTag tag, HolderLookup.  Provider registries) {
        super.loadAdditional(tag, registries);
        tag.putInt("time", this.time);
        tag.putInt("power", this.power);
        tag.putInt("heat", this.heat);
    }
    @Override
    public void gridTick() {
    }
    public static int checkRod(Level level, BlockPos pos) {
        int effective_rod = 0;
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        for (int i = -2; i <= 2; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -2; k <= 2; k++) {
                    mpos.set(pos).move(i, j, k);
                    if (level.isOutsideBuildHeight(mpos)) continue;
                    BlockState blockState = level.getBlockState(mpos);
                    if (blockState.getBlock() instanceof UraniumRodBlock && blockState.getValue(HALF)== Vertical3PartHalf.MID) {

                        effective_rod+= blockState.getValue(ACTIVE);
                    }
                    if (blockState.getBlock() instanceof UraniumRodBlock &&blockState.getValue(ACTIVE) ==0) {
                        level.setBlock(mpos, blockState.setValue(ACTIVE, 5),11);
                    }
                }
            }
        }
        return effective_rod;
    }
    public static int findPoint(Level level, BlockPos pos) {
            for (int j = 1; j <= 20; j++) {
                    BlockPos mpos = pos.above(j);
                    if (level.isOutsideBuildHeight(mpos)) return j-1;
                    BlockState blockState = level.getBlockState(mpos);
                    if (!blockState.is(Blocks.WATER)) {
                        if (blockState.is(Blocks.ICE)||blockState.is(Blocks.PACKED_ICE)
                                ||blockState.is(Blocks.BLUE_ICE)||blockState.is(Blocks.FROSTED_ICE)) {
                            return j;
                        }
                        return j-1;
                    }
        }
        return 20;
    }
    public static int checkWater(Level level, BlockPos pos, int y) {
    BlockPos startPos = pos.above(y); // 使用正确的起始Y坐标
    AtomicInteger waterAbsorbed = new AtomicInteger();

    BlockPos.breadthFirstTraversal(startPos, 8, 81, (currentPos, queue) -> {
        // 添加同一平面的相邻方块
        for(Direction direction : Direction.Plane.HORIZONTAL) {
            BlockPos neighborPos = currentPos.relative(direction);
            if (neighborPos.getY() == startPos.getY()) {
                queue.accept(neighborPos);
            }
        }
    }, (currentPos) -> {
        if (currentPos.equals(startPos)) {
            return true;
        }

        if (currentPos.getY() != startPos.getY()) {
            return false;
        }

        BlockState blockstate = level.getBlockState(currentPos);
        Block block = blockstate.getBlock();

        // 先处理冰的转化
        if (block == Blocks.BLUE_ICE) {
            level.setBlock(currentPos, Blocks.PACKED_ICE.defaultBlockState(), 3);
            waterAbsorbed.getAndIncrement();
            return true;
        }
        if (block == Blocks.PACKED_ICE) {
            level.setBlock(currentPos, Blocks.ICE.defaultBlockState(), 3);
            waterAbsorbed.getAndIncrement();
            return true;
        }
        if (block == Blocks.ICE || block == Blocks.FROSTED_ICE) {
            level.setBlock(currentPos, Blocks.WATER.defaultBlockState(), 3);
            waterAbsorbed.getAndIncrement();
            return true;
        }

        // 然后处理水源
        if (block == Blocks.WATER) {
            FluidState fluidState = blockstate.getFluidState();
            if (fluidState.isSource()) {
                level.setBlock(currentPos, Blocks.AIR.defaultBlockState(), 3);
                waterAbsorbed.getAndIncrement();
                return true;
            }
        }

        // 最后处理可拾取的水方块（如水花盆等）
        if (block instanceof BucketPickup) {
            BucketPickup bucketpickup = (BucketPickup)block;
            if (!bucketpickup.pickupBlock(null, level, currentPos, blockstate).isEmpty()) {
                waterAbsorbed.getAndIncrement();
                return true;
            }
        }

        // 处理水生植物
        if (blockstate.is(Blocks.KELP) ||
            blockstate.is(Blocks.KELP_PLANT) ||
            blockstate.is(Blocks.SEAGRASS) ||
            blockstate.is(Blocks.TALL_SEAGRASS)) {
            BlockEntity blockentity = blockstate.hasBlockEntity() ? level.getBlockEntity(currentPos) : null;
            dropResources(blockstate, level, currentPos, blockentity);
            level.setBlock(currentPos, Blocks.AIR.defaultBlockState(), 3);
            return true;
        }

        return false;
    });

    return waterAbsorbed.get();
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
        return 16;
    }

    @Override
    public AABB shape() {
        return AABB.ofSize(getBlockPos().getCenter(), 5, 3, 5);
    }

    public static void tick(Level level, BlockPos pos, BlockState state, NuclearCollectorBlockEntity entity) {
        entity.rotation += entity.getServerPower() * ROTATION_PRE_POWER / 100;
        if (level == null || level.isClientSide()) return;
        if (isAnotherCollectorNearby(level, entity.getBlockPos()))return;
        entity.time++;
        if (entity.time % 20 == 0) {
            checkRod(level,entity.getBlockPos());
            entity.check_time=ModServerConfig.nuclearCollector.theTimeOfCheckingWater;
            entity.heat+= checkRod(level,entity.getBlockPos());
            if ((ModServerConfig.nuclearCollector.theTimeOfCheckingWater-entity.heat)
                    /5>=ModServerConfig.nuclearCollector.theMinimumTimeOfCheckingWater){
                entity.check_time-=entity.heat;
            }else {
                entity.check_time=ModServerConfig.nuclearCollector.theMinimumTimeOfCheckingWater;
            }
            if(entity.heat>=ModServerConfig.nuclearCollector.baseHeatLimit/3*2){
                level.setBlock(pos, state.setValue(OVERHEATED, true),11);
            }else {
                if (state.getValue(OVERHEATED)){
                level.setBlock(pos, state.setValue(OVERHEATED, false),11);}
            }
            entity.newpower=checkRod(level,entity.worldPosition)*ModServerConfig.nuclearCollector.powerOutput_of_a_uraniumRod;
        }
        if (entity.time % entity.check_time == 0 && entity.heat!=0) {
            int y=findPoint(level, entity.getPos());
            BlockState block = level.getBlockState(pos.above(y));
            int checkWater=checkWater(level, entity.getPos(),y)+1;
            if(checkWater <= entity.heat){
                entity.heat-=checkWater*10;
            }else {
                entity.heat=0;
            }
            checkWater(level, entity.getPos(),y);
            if (block==Blocks.WATER.defaultBlockState()) {
            level.setBlock(pos.above(y), Blocks.AIR.defaultBlockState(), 11);}
            if (block == Blocks.BLUE_ICE.defaultBlockState()) {
                level.setBlock(pos.above(y), Blocks.PACKED_ICE.defaultBlockState(), 3);}
            if (block == Blocks.PACKED_ICE.defaultBlockState()) {
                level.setBlock(pos.above(y), Blocks.ICE.defaultBlockState(), 3);}
            if (block == Blocks.ICE.defaultBlockState() || block == Blocks.FROSTED_ICE.defaultBlockState()) {
                level.setBlock(pos.above(y), Blocks.WATER.defaultBlockState(), 3);}
            entity.time=0;
        }
        if (entity.power != entity.newpower&& entity.grid != null) {
            entity.power = entity.newpower;
            entity.grid.markChanged();}
    }
}


