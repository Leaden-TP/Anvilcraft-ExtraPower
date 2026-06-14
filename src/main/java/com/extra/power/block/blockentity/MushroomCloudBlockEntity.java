package com.extra.power.block.blockentity;

import com.extra.power.block.ModBlock;
import com.extra.power.block.ModBlockEntity;
import com.extra.power.config.ModServerConfig;
import com.extra.power.function.Explosion;
import com.extra.power.init.ModDamageTypes;
import com.extra.power.init.ModSounds;
import com.extra.power.network.FlashPayload;
import com.extra.power.network.ShakePayload;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Holder;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.List;

public class MushroomCloudBlockEntity extends BlockEntity {
    private int ticks = 0;
    private int D_tick=0;//destroy tick 爆炸的第一阶段的计时刻
    private int S_r=10;//smollest radius 爆炸最小半径
    private float C_size = 0;//Cloud size 蘑菇云大小
    private float rotation = 0; // 核爆圈旋转角度
    private int last_y= ModServerConfig.nuclearExplosion.Explosionlevel*3;// 3阶段冲击波清理层数
    private boolean level_2=false; //爆炸的第二阶段
    private float epicenterScale = 0.5f; // 当前缩放值
    private int isExpanding = 0;  // 膨胀阶段
    private static final float SCALE_ACCELERATION = 0.09f; // 缩放加速度
    private  float SCALE_SPEED = 0.05f; // 缩放速度
    public MushroomCloudBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }
    public MushroomCloudBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.MUSHROOM_CLOUD.get(), pos, state);
    }
    public static MushroomCloudBlockEntity createBlockEntity(
            BlockEntityType<?> type,
            BlockPos pos,
            BlockState blockState
    ) {
        return new MushroomCloudBlockEntity(type, pos, blockState);
    }
    protected void loadAdditional(ValueInput input) {
        super.loadAdditional(input);
        input.getIntOr("y" , 0);
        input.getIntOr("D_tick" , 0);
        input.getBooleanOr("level_2" , false);

    }

    public void saveAdditional(ValueOutput output) {
        super.saveAdditional(output);
        output.putInt("y", this.last_y);
        output.putInt("D_tick", this.D_tick);
        output.putBoolean("level_2", this.level_2);

    }
    public float getEpicenterScale() {
        return epicenterScale;
    }

    public float getC_size() {
        return C_size;
    }
    public float getRotation() {
        return rotation;
    }

    public static void tick(Level level, BlockPos pos, BlockState state, MushroomCloudBlockEntity entity) {
        entity.SCALE_SPEED += SCALE_ACCELERATION;
        //光球膨胀
        if (entity.isExpanding == 0) {
            entity.epicenterScale += entity.SCALE_SPEED;
            if (entity.epicenterScale >= 5) {
                entity.isExpanding += 1;
            }
        }
        //光球收缩
        if (entity.isExpanding == 1) {
            entity.epicenterScale -= entity.SCALE_SPEED;
            if (entity.epicenterScale <= 0.5f) {
                entity.isExpanding += 1;
                entity.SCALE_SPEED = 0.3f;
            }
        }
        //蘑菇云
        if (entity.isExpanding == 2) {
            entity.rotation += 10;
            if (entity.C_size < 10){
                if (entity.SCALE_SPEED > 0.08) {
                    entity.SCALE_SPEED -= entity.SCALE_ACCELERATION;
                }else entity.SCALE_SPEED=0.08f;
                    entity.C_size += entity.SCALE_SPEED;
            }
        }
        if (!level.isClientSide()) {
            entity.ticks++;
            if (entity.ticks % 2 == 0) {
                if (entity.isExpanding == 2 ) {
                    int shakeRadius = ModServerConfig.nuclearExplosion.Explosionlevel*ModServerConfig.nuclearExplosion.Explosionlevel+16;
                    AABB area = new AABB(pos).inflate(shakeRadius);
                    List<Player> players = level.getEntitiesOfClass(Player.class, area);

                    for (Player player : players) {
                        double distance = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
                        // 简单的线性衰减
                        float intensity = (float) Math.max(0, 1.0 - 1*(distance / (shakeRadius * shakeRadius)));

                        if (intensity > 0.1f) { // 忽略太微弱的震动
                            // 发送数据包到客户端
                            entity.sendShakePacket(player, intensity*10+1, 40);
                        }
                    }
                }
                int damageRadius = ModServerConfig.nuclearExplosion.Explosionlevel*ModServerConfig.nuclearExplosion.Explosionlevel;
                AABB area_0 = new AABB(pos).inflate(damageRadius);
                List<LivingEntity> entities = level.getEntitiesOfClass(LivingEntity.class, area_0);
                // 获取自定义伤害源
                Holder<DamageType> damageTypeHolder = level.registryAccess().holderOrThrow(ModDamageTypes.NUCLEAR_EXPLOSION);
                DamageSource damageSource =new DamageSource(damageTypeHolder);
                for (LivingEntity living : entities) {
                    // 计算距离，距离越近伤害越高
                    double distance = living.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
                    if (distance > damageRadius * damageRadius) continue;
                    float damage = 75.0f * (1.0f - (float)(distance / (damageRadius * damageRadius)));
                    if (damage < 1.0f) damage = 1.0f;
                    living.hurt(damageSource, damage);
                    living.setRemainingFireTicks(160);
                    living.addEffect(new MobEffectInstance(
                            MobEffects.WITHER,
                            600,
                            2,
                            true,
                            true
                    ));
                }
                if (entity.D_tick <= entity.S_r)entity.D_tick ++;
                if ( entity.D_tick <= entity.S_r) {
                    if (entity.D_tick != entity.S_r) entity.removeSomething_ball(level, pos, entity.D_tick);
                    if (entity.D_tick == 1){
                        entity.clearDroppedItems(level,pos) ;
                        level.playSound(null, pos, ModSounds.NUCLEAR_EXPLOSION.get(),
                            SoundSource.BLOCKS, 4.0f, 0.8f + level.getRandom().nextFloat() * 0.4f);}
                    if (entity.D_tick == entity.S_r/2){
                        int flashRadius = ModServerConfig.nuclearExplosion.Explosionlevel*ModServerConfig.nuclearExplosion.Explosionlevel;
                        AABB area_1 = new AABB(pos).inflate(flashRadius);
                        List<Player> players = level.getEntitiesOfClass(Player.class, area_1);
                        for (Player player : players) {
                            double distance = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
                            float intensity = (float) Math.max(0, 1.0 - distance / (flashRadius * flashRadius));
                            if (intensity > 0.1f) {
                                entity.sendFlashPacket(player, 1, 30);
                            }
                        }
                    }
                }
                else if(entity.last_y > ModServerConfig.nuclearExplosion.Explosionlevel*2 ){
                    entity.last_y=entity.removeSomething_circle(level, pos, entity.S_r+(ModServerConfig.nuclearExplosion.Explosionlevel*3-entity.last_y)
                            *(ModServerConfig.nuclearExplosion.Explosionlevel*3-entity.last_y)/2,entity.last_y , false);//x^2/2+s_r
                    if (!entity.level_2) entity.level_2=entity.removeSomething_ball_level_2(level, pos, entity.S_r*2);
                }
                else if(entity.last_y > ModServerConfig.nuclearExplosion.Explosionlevel){
                    entity.last_y=entity.removeSomething_circle(level, pos, ModServerConfig.nuclearExplosion.Explosionlevel*
                            ModServerConfig.nuclearExplosion.Explosionlevel/2+entity.S_r,entity.last_y ,false);
                    if (!entity.level_2) entity.level_2=entity.removeSomething_ball_level_2(level, pos, entity.S_r*5);
                }
                else if(entity.last_y >= 0 ){
                    entity.last_y=entity.removeSomething_circle(level, pos, entity.S_r+entity.last_y*entity.last_y/2,entity.last_y ,true);//x^2/2+s_r
                    if (!entity.level_2) entity.level_2=entity.removeSomething_ball_level_2(level, pos, entity.S_r*2);
                }
                else if (!entity.level_2) entity.level_2=entity.removeSomething_ball_level_2(level, pos, entity.S_r*5);
                if (entity.C_size >= 10 && entity.last_y == 0) {
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);}
            }
        }
    }

    private void removeSomething_ball(Level level, BlockPos center,int r )  {
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                for (int y = -r; y <= r; y++) {
                    // 判断是否在球内
                    if (x*x + z*z + y*y <= r*r) {
                        BlockPos target = center.offset(x, y, z);
                        BlockState state = level.getBlockState(target);
                        if (level.isEmptyBlock(target)) continue;
                        if (level.isOutsideBuildHeight(target))continue;
                        if (!state.is(BlockTags.WITHER_IMMUNE)  && !state.isAir() && !state.is(ModBlock.MUSHROOM_CLOUD)) {
                            level.setBlock(target, Blocks.AIR.defaultBlockState(), 11);
                        }
                    }
                }
            }
        }
    }
    private boolean removeSomething_ball_level_2(Level level, BlockPos center,int r )  {
        int count = 0;
        for (int x = -r; x <= r; x++) {
            for (int z = -r; z <= r; z++) {
                for (int y = r; y >= -3; y--) {
                    // 判断是否在球内
                    if (x*x + z*z + y*y <= r*r) {
                        BlockPos target = center.offset(x, y+this.S_r/2, z);
                        BlockState state = level.getBlockState(target);
                        if (level.isEmptyBlock(target)) continue;
                        if (level.isOutsideBuildHeight(target))continue;
                        if (!state.is(BlockTags.WITHER_IMMUNE)  && !state.isAir() && !state.is(ModBlock.MUSHROOM_CLOUD)) {
                            level.setBlock(target, Blocks.AIR.defaultBlockState(), 11);
                            count++;
                        }
                        if (count >= 2500) {
                            break;
                        }
                    }
                }
                if (count >= 2500) {
                    break;
                }
            }
            if (count >= 2500) {
                break;
            }
        }
        return count < 2500 ? true : false;
    }
private int removeSomething_circle(Level level, BlockPos center, int r, int y , boolean fire) {
    for (int x = -r; x <= r; x++) {
        for (int z = -r; z <= r; z++) {
            // 判断是否在圆内
            if (x * x + z * z <= r * r) {
                BlockPos target = center.offset(x, y - this.S_r / 2, z);
                BlockState state = level.getBlockState(target);
                // 跳过无效位置
                if (level.isOutsideBuildHeight(target)) continue;
                // 检查是否应该摧毁
                if (!state.is(BlockTags.WITHER_IMMUNE) &&
                    !state.isAir() &&
                    !state.is(ModBlock.MUSHROOM_CLOUD) &&
                    !state.is(Blocks.WATER)) {
                    level.setBlock(target, Blocks.AIR.defaultBlockState(), 11);
                    if (level.getRandom().nextFloat() < 0.3f && fire) {
                        level.setBlock(target, Blocks.FIRE.defaultBlockState(), 11 | 2);
                    }
                }
            }
        }
    }
    return  y -= 1;
}
    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }
    private void clearDroppedItems(Level level, BlockPos center) {
        AABB area = new AABB(center).inflate(3); // 3x3x3范围
        List<ItemEntity> items = level.getEntitiesOfClass(ItemEntity.class, area);
        for (ItemEntity item : items) {
            item.discard();
        }
    }
    //发包
    private void sendShakePacket(Player player, float intensity, int duration) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new ShakePayload(intensity, duration));
        }
    }
    private void sendFlashPacket(Player player, float intensity, int duration) {
        if (player instanceof ServerPlayer serverPlayer) {
            PacketDistributor.sendToPlayer(serverPlayer, new FlashPayload(intensity, duration));
        }
    }
}
