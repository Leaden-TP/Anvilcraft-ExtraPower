package com.extra.power.block.blockentity;

import com.extra.power.block.ModBlock;
import com.extra.power.block.ModBlockEntity;
import com.extra.power.block.just_block.BurningCoalBlock;
import com.extra.power.init.ModHeaterInfos;
import dev.dubhe.anvilcraft.api.heat.HeaterManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;




public class BurningCoalBlockEntity extends BlockEntity {
    public int tickCounter = 0;
    public int Counter = 0;
    public BurningCoalBlockEntity(BlockPos pos, BlockState state) {
        this(ModBlockEntity.BURNING_COAL.get(), pos, state);
    }
    public BurningCoalBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState state) {
        super(type, pos, state);
    }

    public static void tick(Level level, BlockPos pos ,BlockState state,BurningCoalBlockEntity entity) {
        if (level.isClientSide()) return;
        entity.tickCounter+=1;
        HeaterManager.addProducer(pos, level, ModHeaterInfos.COAL_BURNING);
        if (entity.tickCounter>=10){
            if (state.getValue(BurningCoalBlock.ToBoom)){
                BurningCoalBlock.explosion(level,pos,3.5f);
            }
            entity.Counter+=1;
            entity.tickCounter=0;
        }
        if (entity.Counter>=960){
            entity.Counter=0;
            disappear(level,pos); ;
        }
    }
    public static void disappear(Level level, BlockPos pos) {
        level.setBlockAndUpdate(pos, ModBlock.ASHES_BLOCK.get().defaultBlockState());
        level.playSound(null,pos, SoundEvents.FIRE_EXTINGUISH, SoundSource.PLAYERS,
                0.7F, 1.0F);
    }
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
       this.Counter = tag.getInt("counter");
    }
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
       tag.putInt("counter", this.Counter);
    }
}
