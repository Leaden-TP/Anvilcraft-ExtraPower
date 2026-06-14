package com.extra.power.block.blockentity;

import com.extra.power.block.just_block.FrostControllerBlock;
import com.extra.power.block.just_block.UraniumRodBlock;
import dev.dubhe.anvilcraft.block.state.Vertical3PartHalf;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;

import static com.extra.power.block.ModBlockEntity.URANIUM_ROD;

public class UraniumRodBlockEntity extends BlockEntity {
    private static final int RADIUS = 15; // 效果半径
    private static final int DURATION = 200; // 效果持续时间(刻)
    private static final int AMPLIFIER = 1; // 效果等级
    private static int tickCounter = 0;
    public UraniumRodBlockEntity(BlockPos pos, BlockState state) {
        super(URANIUM_ROD.get(), pos, state);
    }
    // 每tick执行的方法
    public static void tick(Level level, BlockPos pos, BlockState state, UraniumRodBlockEntity entity) {
        if (level.isClientSide()) return;
        if (state.getValue(UraniumRodBlock.HALF)!=(Vertical3PartHalf.MID)||
                state.getValue(UraniumRodBlock.ACTIVE)==0)return;
        entity.tickCounter++;
        if (tickCounter>=20 ){
            if(!state.getValue(UraniumRodBlock.UNDER_CONTROL) && !(state.getValue(UraniumRodBlock.ACTIVE)<5)
            && state.getValue(UraniumRodBlock.ACTIVE)>0){
            AABB area = new AABB(pos).inflate(RADIUS*state.getValue(UraniumRodBlock.ACTIVE));
            for (LivingEntity living : ((ServerLevel)level).getEntitiesOfClass(
                    LivingEntity.class, area)) {
                // 添加凋零效果
                living.addEffect(new MobEffectInstance(
                        MobEffects.WITHER,
                        DURATION,
                        AMPLIFIER,
                        true,
                        true
                ));
                }
            }
            int controller = checkController(level,pos);
            if (controller<=4){
                level.setBlock(pos, state.setValue(UraniumRodBlock.ACTIVE, 5-controller), 11);
            }else {
                    level.setBlock(pos, state.setValue(UraniumRodBlock.ACTIVE, 1), 11);
            }
            entity.tickCounter=0;
        }
    }
    public static int checkController(Level level, BlockPos pos) {
        BlockPos.MutableBlockPos mpos = new BlockPos.MutableBlockPos();
        int controller = 0;
        for (int i = -1; i <= 1; i++) {
            for (int j = -1; j <= 1; j++) {
                for (int k = -1; k <= 1; k++) {
                    mpos.set(pos).move(i, j, k);
                    if (level.isOutsideBuildHeight(mpos)) continue;
                    BlockState blockState = level.getBlockState(mpos);
                    if (blockState.getBlock() instanceof FrostControllerBlock   && blockState.getValue(FrostControllerBlock.HALF) == Vertical3PartHalf.MID
                            &&  blockState.getValue(FrostControllerBlock.ACTIVE)) {
                        controller+=1;
                    }
                }
            }
        }
        return controller;
    }
    public static UraniumRodBlockEntity createBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState blockState
    ) {
        return new UraniumRodBlockEntity(pos, blockState);
    }
}
