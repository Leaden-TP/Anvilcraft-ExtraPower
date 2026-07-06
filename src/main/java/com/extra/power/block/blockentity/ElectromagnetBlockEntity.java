package com.extra.power.block.blockentity;

import com.extra.power.block.ModBlockEntity;
import com.extra.power.block.just_block.ElectromagnetBlock;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.power.IPowerConsumer;
import dev.dubhe.anvilcraft.api.power.PowerGrid;
import dev.dubhe.anvilcraft.init.block.ModBlockTags;
import dev.dubhe.anvilcraft.util.TriggerUtil;
import lombok.Getter;
import net.minecraft.core.BlockPos;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.entity.item.FallingBlockEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LiquidBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.Nullable;

import java.util.List;
import static com.extra.power.block.just_block.ElectromagnetBlock.LIT;


public class ElectromagnetBlockEntity extends BlockEntity implements IPowerConsumer {
    private static final double ACCELERATION = 0.5;
    private static final int RANGE = AnvilCraft.CONFIG.magnetAttractsDistance*4;
    private static int tickCounter = 0;
    @Getter
    private PowerGrid grid;
    public ElectromagnetBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntity.ELECTROMAGNET.get(), pos, state);
    }
    public ElectromagnetBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }
    public static ElectromagnetBlockEntity createBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState blockState
    ) {
        return new ElectromagnetBlockEntity(type, pos, blockState);
    }
    public void tick(Level level,BlockPos pos, BlockState state, ElectromagnetBlockEntity entity) {
        if (level == null || level.isClientSide()) return;
        this.flushState(level, pos);
        entity.tickCounter++;
        AABB area = new AABB(
                pos.getX(), pos.getY() + 1, pos.getZ(),
                pos.getX() + 1, pos.getY() + RANGE + 1, pos.getZ() + 1
        );
        List<FallingBlockEntity> fallingBlocks = level.getEntitiesOfClass(
                FallingBlockEntity.class,
                area,
                e -> e.getBlockState().is(BlockTags.ANVIL)
        );
        for (FallingBlockEntity falling : fallingBlocks) {
            falling.setDeltaMovement(falling.getDeltaMovement().add(0, -ACCELERATION, 0));
        }
        if (entity.tickCounter%2==0){
            entity.attract(state, level, pos);
            entity.tickCounter=0;
        }
    }

    private void attract(BlockState state, Level level, BlockPos magnetPos) {
        if (level.isClientSide()) return;
        if (state.getValue(LIT) || state.getValue(ElectromagnetBlock.OVERLOAD)) return;
        if (level.getBlockState(magnetPos.below()).is(BlockTags.ANVIL)) return;
        int distance = AnvilCraft.CONFIG.magnetAttractsDistance*4;
        BlockPos currentPos = magnetPos;
        checkAnvil:
        for (int i = 0; i < distance; i++) {
            currentPos = currentPos.below();
            BlockState state1 = level.getBlockState(currentPos);

            if (state1.is(BlockTags.ANVIL) && !state1.is(ModBlockTags.NON_MAGNETIC)) {
                level.destroyBlock(magnetPos.below(), true);
                level.setBlockAndUpdate(magnetPos.below(), state1);
                level.setBlockAndUpdate(currentPos, Blocks.AIR.defaultBlockState());
                TriggerUtil.liftingAnvil(level, currentPos);
                break;
            }
            List<FallingBlockEntity> entities =
                    level.getEntitiesOfClass(FallingBlockEntity.class, new AABB(currentPos));
            for (FallingBlockEntity entity : entities) {
                BlockState state2 = entity.getBlockState();
                if (state2.is(BlockTags.ANVIL) && !state2.is(ModBlockTags.NON_MAGNETIC)) {
                    level.destroyBlock(magnetPos.below(), true);
                    level.setBlockAndUpdate(magnetPos.below(), state2);
                    entity.discard();
                    TriggerUtil.liftingAnvil(level, currentPos);
                    break checkAnvil;
                }
            }
            BlockState blockState = level.getBlockState(currentPos);
            if (level.isEmptyBlock(currentPos) || blockState.getBlock() instanceof LiquidBlock) {
                continue;
            }
            return;
        }
    }
    @Nullable
    public Level getCurrentLevel() {
        return level;
    }

    public BlockPos getPos() {
        return this.getBlockPos();
    }

    @Override
    public int getInputPower() {
        return 16;
    }

    @Override
    public void setGrid(@Nullable PowerGrid grid) {
        this.grid = grid;
    }
}
