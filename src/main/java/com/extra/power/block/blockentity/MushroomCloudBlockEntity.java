package com.extra.power.block.blockentity;

import com.extra.power.init.block.ModBlock;
import com.extra.power.init.block.ModBlockEntity;
import com.extra.power.config.ModServerConfig;
import com.extra.power.init.data.ModDamageTypes;
import com.extra.power.init.ModSounds;
import com.extra.power.network.toClient.FlashPayload;
import com.extra.power.network.toClient.ShakePayload;
import dev.dubhe.anvilcraft.api.world.load.LevelLoadManager;
import dev.dubhe.anvilcraft.api.world.load.LoadChuckData;
import net.minecraft.core.BlockPos;
import net.minecraft.core.HolderLookup;
import net.minecraft.core.registries.Registries;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.protocol.Packet;
import net.minecraft.network.protocol.game.ClientGamePacketListener;
import net.minecraft.network.protocol.game.ClientboundBlockEntityDataPacket;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.BlockTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.crafting.RecipeType;
import net.minecraft.world.item.crafting.SingleRecipeInput;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.AABB;
import net.neoforged.neoforge.items.IItemHandler;
import net.neoforged.neoforge.network.PacketDistributor;

import java.util.HashMap;
import java.util.Map;

public class MushroomCloudBlockEntity extends BlockEntity {
    private int ticks = 0;
    private int D_tick = 0;
    private float C_size = 0;
    private float rotation = 0;
    private float epicenterScale = 0.5f;
    private int isExpanding = 0;
    private static final float SCALE_ACCELERATION = 0.09f;
    private float SCALE_SPEED = 0.05f;

    /** 每次爆炸判定（每 2 tick 一次）允许读取的方块数上限，防止一次判定扫爆服务器 */
    private static final int MAX_SCANS_PER_STEP = 20000;
    /** 每次爆炸判定允许破坏的方块数上限：setBlock(…, 11) 会触发邻居与客户端更新，必须限流 */
    private static final int MAX_BREAKS_PER_STEP = 2000;
    /** 椭球边缘多少格以内算"熔融壳层"：只有这一层里的方块才会走熔炉配方（高炉配方是全局的） */
    private static final double MELT_SHELL_THICKNESS = 2.0;
    /** 火焰只在椭球边缘、厚度为熔壳 2 倍的环形壳层里生成 */
    private static final double FIRE_SHELL_THICKNESS = MELT_SHELL_THICKNESS * 2.0;
    /** 起爆后第几步触发闪光 */
    private static final int FLASH_TICK = 5;
    /** 爆心处的火焰伤害，随距离线性衰减 */
    private static final float MAX_FIRE_DAMAGE = 15.0f;
    /** 爆心处的核爆伤害，随距离线性衰减 */
    private static final float MAX_NUKE_DAMAGE = 300.0f;
    /** 火焰伤害附带的燃烧时长（tick） */
    private static final int FIRE_TICKS = 200;

    private int tickScanBudget = MAX_SCANS_PER_STEP;
    private int tickBreakBudget = MAX_BREAKS_PER_STEP;
    /** 椭球开挖的续挖游标（包围盒内的线性索引），预算耗尽时保存进度 */
    private int blastCursor = 0;
    /** 椭球是否已经挖完 */
    private boolean blastDone = false;
    /** 配方查询缓存：同一次爆炸里反复遇到同一种方块时不必重复查表 */
    private final Map<Block, RecipeProducts> recipeCache = new HashMap<>();

    /** 一个方块对应的配方产物：高炉优先，熔炉只在产物是方块时保留 */
    private record RecipeProducts(ItemStack blasting, ItemStack smelting) {}

    public MushroomCloudBlockEntity(BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        super(type, pos, blockState);
    }

    public MushroomCloudBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntity.MUSHROOM_CLOUD.get(), pos, state);
    }

    public static MushroomCloudBlockEntity createBlockEntity(
            BlockEntityType<?> type, BlockPos pos, BlockState blockState) {
        return new MushroomCloudBlockEntity(type, pos, blockState);
    }

    @Override
    public void loadAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.loadAdditional(tag, registries);
        this.D_tick = tag.getInt("D_tick");
        this.blastCursor = tag.getInt("blast_cursor");
        this.blastDone = tag.getBoolean("blast_done");
    }

    @Override
    public void saveAdditional(CompoundTag tag, HolderLookup.Provider registries) {
        super.saveAdditional(tag, registries);
        tag.putInt("D_tick", this.D_tick);
        tag.putInt("blast_cursor", this.blastCursor);
        tag.putBoolean("blast_done", this.blastDone);
    }

    public float getEpicenterScale() { return epicenterScale; }
    public float getC_size() { return C_size; }
    public float getRotation() { return rotation; }

    public static void tick(Level level, BlockPos pos, BlockState state, MushroomCloudBlockEntity entity) {
        entity.SCALE_SPEED += SCALE_ACCELERATION;

        // 光球膨胀
        if (entity.isExpanding == 0) {
            entity.epicenterScale += entity.SCALE_SPEED;
            if (entity.epicenterScale >= 5) entity.isExpanding += 1;
        }
        // 光球收缩
        if (entity.isExpanding == 1) {
            entity.epicenterScale -= entity.SCALE_SPEED;
            if (entity.epicenterScale <= 0.5f) {
                entity.isExpanding += 1;
                entity.SCALE_SPEED = 0.3f;
            }
        }
        // 蘑菇云
        if (entity.isExpanding == 2) {
            entity.rotation += 10;
            if (entity.C_size < 10) {
                if (entity.SCALE_SPEED > 0.08) {
                    entity.SCALE_SPEED -= SCALE_ACCELERATION;
                } else entity.SCALE_SPEED = 0.08f;
                entity.C_size += entity.SCALE_SPEED;
            }
        }
        if (entity.isExpanding == 2) {
            if (level instanceof ServerLevel serverLevel) {
                LevelLoadManager.reload(serverLevel, pos,
                        LoadChuckData.createLoadChuckData(3, pos, false, serverLevel));
            }
        }

        if (!level.isClientSide()) {
            entity.ticks++;
            if (entity.ticks % 2 == 0) {

                // ========== 震动效果（仅视觉，不受防爆影响） ==========
                if (entity.isExpanding == 2) {
                    int shakeRadius = ModServerConfig.nuclearExplosion.Explosionlevel
                            * ModServerConfig.nuclearExplosion.Explosionlevel + 16;
                    AABB area = new AABB(pos).inflate(shakeRadius);
                    for (Player player : level.getEntitiesOfClass(Player.class, area)) {
                        double distance = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
                        float intensity = (float) Math.max(0,
                                1.0 - (distance / ((double) shakeRadius * shakeRadius)));
                        if (intensity > 0.1f) {
                            entity.sendShakePacket(player, intensity * 10 + 1, 40);
                        }
                    }
                }

                // ========== 防爆判定：mobGriefing=false 时不进行爆炸判定 ==========
                boolean griefingEnabled = level.getGameRules()
                        .getBoolean(GameRules.RULE_MOBGRIEFING);

                // ========== 伤害 + 椭球开挖（音效/闪光等表现不受防爆影响） ==========
                entity.destroyTerrain(level, pos, griefingEnabled);

                // ========== 视觉结束后清理蘑菇云方块 ==========
                // 椭球挖完才收尾；防爆时不会开挖，蘑菇云成型后即可移除，避免方块永久残留
                if (entity.C_size >= 10 && (entity.blastDone || !griefingEnabled)) {
                    LevelLoadManager.unregister(pos, level);
                    level.setBlock(pos, Blocks.AIR.defaultBlockState(), 11);
                }
            }
        }
    }

    // ---------- 爆炸范围（椭球） ----------

    /** 椭球水平半长轴：Explosionlevel * 3 */
    private double semiMajor() {
        return ModServerConfig.nuclearExplosion.Explosionlevel * 3.0;
    }

    /** 椭球垂直半短轴：Explosionlevel * 3 / 2 */
    private double semiMinor() {
        return ModServerConfig.nuclearExplosion.Explosionlevel * 3.0 / 2.0;
    }

    /**
     * 爆炸判定中的方块部分。
     * <p>破坏范围是以方块位置为中心的椭球：水平半长轴 {@link #semiMajor()}、
     * 垂直半短轴 {@link #semiMinor()}；只有椭球边缘 {@link #MELT_SHELL_THICKNESS} 格以内的
     * 壳层才会走熔炉/高炉配方，壳层以内的方块直接炸空。
     * <p><b>水：椭球范围内的水会全部被清除</b>，不再限于爆心附近的小球。
     * <p>按"从顶部往下"的切片顺序逐格扫描，游标 blastCursor 记录进度：
     * 预算耗尽时保存游标并返回 false，下一 tick 从原处继续，不会漏挖也不会重复扫描。
     * <p>火焰只在椭球边缘、厚度为 {@link #FIRE_SHELL_THICKNESS} 的环形壳层里生成，
     * 避免坑底与坑中心被点燃。
     *
     * @return 是否已经把这个椭球挖完
     */
    private boolean carveEllipsoid(Level level, BlockPos center) {
        final double a = this.semiMajor();
        final double b = this.semiMinor();
        final int maxXZ = (int) Math.ceil(a);
        final int maxY = (int) Math.ceil(b);
        final int sizeXZ = 2 * maxXZ + 1;
        final int slice = sizeXZ * sizeXZ;
        final int total = slice * (2 * maxY + 1);

        final double a2 = a * a;
        final double b2 = b * b;
        final double a4 = a2 * a2;
        final double b4 = b2 * b2;

        for (int index = this.blastCursor; index < total; index++) {
            // 预算按扫描量计数：预算耗尽时保存游标，下一 tick 接着挖
            if (--this.tickScanBudget <= 0) {
                this.blastCursor = index;
                return false;
            }

            int layer = index / slice;
            int rest = index % slice;
            int y = maxY - layer;                    // 从顶部切片开始往下挖
            int x = rest / sizeXZ - maxXZ;
            int z = rest % sizeXZ - maxXZ;

            double vertical = (double) y * y;
            double horizontal = (double) x * x + (double) z * z;
            double f = horizontal / a2 + vertical / b2;
            if (f >= 1.0) continue;                  // 椭球之外，不归爆炸判定管

            BlockPos target = center.offset(x, y, z);
            if (level.isOutsideBuildHeight(target)) continue;
            BlockState state = level.getBlockState(target);
            if (state.isAir() || state.is(BlockTags.WITHER_IMMUNE)
                    || state.is(ModBlock.MUSHROOM_CLOUD)) {
                continue;
            }
            boolean water = state.is(Blocks.WATER);
            if (this.tickBreakBudget <= 0) {
                this.blastCursor = index;
                return false;
            }

            // 配方处理：高炉配方全局执行且优先，熔炉配方只在这一格位于椭球边缘壳层时生效
            if (!water && this.tryRecipe(level, target, state,
                    isInMeltShell(f, horizontal, y, a4, b4))) {
                this.tickBreakBudget--;
                continue;
            }

            if (state instanceof IItemHandler) {
                level.destroyBlock(target, false);
            } else {
                level.setBlock(target, Blocks.AIR.defaultBlockState(), 11);
            }
            this.tickBreakBudget--;

            // 火焰只在椭球边缘、厚度为 FIRE_SHELL_THICKNESS 的环形壳层里生成，
            // 且只在爆心及以上（y >= 0）生成；水格不点火
            if (!water && y >= 0
                    && isInShell(f, horizontal, y, a4, b4, FIRE_SHELL_THICKNESS)
                    && level.random.nextFloat() < 0.3f) {
                level.setBlock(target, Blocks.FIRE.defaultBlockState(), 11 | 2);
            }
        }

        this.blastCursor = 0;
        return true;
    }

    /**
     * 该格是否位于椭球边缘、厚度为 {@code thickness} 的壳层里（距椭球表面 thickness 格以内）。
     * <p>用椭球函数 f 与它的梯度估算到表面的距离 (1-f)/|∇f|，
     * 因此只需判断 (1-f) <= thickness * |∇f|，不用做除法。
     *
     * @param f          椭球函数值（&lt;1 表示在椭球内部）
     * @param horizontal x² + z²
     */
    private static boolean isInShell(double f, double horizontal, int y,
                                     double a4, double b4, double thickness) {
        double gradient = 2.0 * Math.sqrt(horizontal / a4 + (double) y * y / b4);
        return (1.0 - f) <= thickness * gradient;
    }

    /**
     * 该格是否位于椭球边缘的"熔融壳层"（距椭球表面 {@link #MELT_SHELL_THICKNESS} 格以内）。
     * <p>用椭球函数 f 与它的梯度估算到表面的距离 (1-f)/|∇f|，
     * 因此只需要判断 (1-f) &lt;= 厚度 * |∇f|，不用做除法；越靠外侧误差越小，
     * 爆心附近 (1-f) 很大、|∇f| 很小，自然会判定为"不在壳层内"。
     *
     * @param f          椭球函数值（&lt;1 表示在椭球内部）
     * @param horizontal x² + z²
     */
    private static boolean isInMeltShell(double f, double horizontal, int y, double a4, double b4) {
        return isInShell(f, horizontal, y, a4, b4, MELT_SHELL_THICKNESS);
    }

    /**
     * 爆炸判定中的伤害与总体流程。
     *
     * @param griefingEnabled false（防爆）时只保留音效/闪光等表现，不进行爆炸判定
     */
    private void destroyTerrain(Level level, BlockPos pos, boolean griefingEnabled) {
        // 起爆瞬间的表现（只做一次）
        if (this.D_tick == 0) {
            // 伤害只在起爆瞬间结算一次：先火焰、后核爆，且只针对生物实体
            if (griefingEnabled) {
                this.applyExplosionDamage(level, pos);
                this.clearDroppedItems(level, pos);
            }
            level.playSound(null, pos, ModSounds.NUCLEAR_EXPLOSION.get(),
                    SoundSource.BLOCKS, 4.0f, 0.8f + level.random.nextFloat() * 0.4f);
        }
        this.D_tick++;
        if (this.D_tick == FLASH_TICK) {
            this.sendFlash(level, pos);
        }

        if (!griefingEnabled) return;

        // 重置本次判定的性能预算
        this.tickScanBudget = MAX_SCANS_PER_STEP;
        this.tickBreakBudget = MAX_BREAKS_PER_STEP;

        if (!this.blastDone) {
            this.blastDone = this.carveEllipsoid(level, pos);
        }
    }

    /** 闪光效果（仅表现，不受防爆影响） */
    private void sendFlash(Level level, BlockPos pos) {
        int flashRadius = ModServerConfig.nuclearExplosion.Explosionlevel
                * ModServerConfig.nuclearExplosion.Explosionlevel;
        AABB flashArea = new AABB(pos).inflate(flashRadius);
        for (Player player : level.getEntitiesOfClass(Player.class, flashArea)) {
            double distance = player.distanceToSqr(pos.getX(), pos.getY(), pos.getZ());
            float intensity = (float) Math.max(0,
                    1.0 - distance / ((double) flashRadius * flashRadius));
            if (intensity > 0.1f) {
                this.sendFlashPacket(player, 1, 30);
            }
        }
    }

    /**
     * 伤害判定：只针对生物实体，掉落物/船/矿车等非生物实体不受影响，
     * 伤害源也不带攻击者实体。
     * <p>顺序为：先按距离衰减的火焰伤害（并点燃），再结算随距离衰减的核爆伤害。
     */
    private void applyExplosionDamage(Level level, BlockPos pos) {
        int damageRadius = ModServerConfig.nuclearExplosion.Explosionlevel
                * ModServerConfig.nuclearExplosion.Explosionlevel;
        double damageRadiusSq = (double) damageRadius * damageRadius;
        AABB damageArea = new AABB(pos).inflate(damageRadius);

        DamageSource nuclearSource = getMushroomCloudDamageSource(level);
        DamageSource fireSource = level.damageSources().onFire();

        for (Entity entity : level.getEntitiesOfClass(Entity.class, damageArea)) {
            double distSq = entity.distanceToSqr(
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (distSq > damageRadiusSq) continue;

            float distanceFactor = (float) (1.0 - distSq / damageRadiusSq);
            if (distanceFactor <= 0.0f) continue;

            // 1) 先施加随距离变化的火焰伤害
            float fireDamage = MAX_FIRE_DAMAGE * distanceFactor;
            if (fireDamage >= 0.5f) {
                entity.setRemainingFireTicks(FIRE_TICKS);
                entity.hurt(fireSource, 3);
            }
        }
        for (LivingEntity living : level.getEntitiesOfClass(LivingEntity.class, damageArea)) {
            double distSq = living.distanceToSqr(
                    pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5);
            if (distSq > damageRadiusSq) continue;

            float distanceFactor = (float) (1.0 - distSq / damageRadiusSq);
            if (distanceFactor <= 0.0f) continue;

            // 2) 再施加随距离变化的核爆伤害；清空无敌帧，保证两段伤害都完整结算
            float nukeDamage = MAX_NUKE_DAMAGE * distanceFactor;
            living.invulnerableTime = 0;
            living.hurt(nuclearSource, nukeDamage);

            // 凋零效果
            living.addEffect(new MobEffectInstance(
                    MobEffects.WITHER, 600, 2, true, true));
        }

    }

    // ---------- 高温熔炼 ----------

    /**
     * 爆炸判定遇到方块时的配方处理。
     * <ul>
     *   <li><b>高炉配方：全局执行、优先级最高</b>——椭球内任何方块只要能走高炉配方就先按配方处理；</li>
     *   <li>熔炉配方：只有该格位于椭球边缘的熔融壳层里才生效，且只有产物是方块时才就地替换；</li>
     *   <li>产物是方块：就地替换原方块（配方有多个产物也只放一个，其余直接无视）；
     *       产物是物品：方块消失，产物掉在原地。</li>
     * </ul>
     * 产物按方块种类缓存，同一次爆炸里反复遇到同一种方块不会重复查表。
     *
     * @param inMeltShell 该格是否位于椭球边缘的熔融壳层（见 {@link #isInMeltShell}）
     * @return true 表示这一格已按配方处理，调用方不要再做普通破坏
     */
    private boolean tryRecipe(Level level, BlockPos target, BlockState state, boolean inMeltShell) {
        if (!(level instanceof ServerLevel serverLevel)) return false;
        Block block = state.getBlock();
        if (block.asItem() == Items.AIR) return false;   // 没有物品形式就谈不上配方

        RecipeProducts products = this.recipeCache.computeIfAbsent(block,
                b -> findRecipeProducts(serverLevel, b));

        ItemStack product = products.blasting();          // 高炉配方：全局、优先
        if (product.isEmpty() && inMeltShell) {
            product = products.smelting();               // 熔炉配方：只在熔融壳层
        }
        if (product.isEmpty()) return false;

        if (product.getItem() instanceof BlockItem blockItem) {
            // 产物是方块：原方块就地变成产物
            level.setBlock(target, blockItem.getBlock().defaultBlockState(), 11);
        } else {
            // 产物是物品：方块消失，产物掉在原地
            level.setBlock(target, Blocks.AIR.defaultBlockState(), 11);
            Block.popResource(level, target, product.copy());
        }
        return true;
    }

    /** 查一个方块的配方产物：高炉产物总是保留；熔炉产物只在产物是方块时保留 */
    private static RecipeProducts findRecipeProducts(ServerLevel level, Block block) {
        var manager = level.getRecipeManager();
        ItemStack input = new ItemStack(block);

        ItemStack blasting = manager
                .getRecipeFor(RecipeType.BLASTING, new SingleRecipeInput(input), level)
                .map(holder -> holder.value().getResultItem(level.registryAccess()))
                .orElse(ItemStack.EMPTY);

        ItemStack smelting = manager
                .getRecipeFor(RecipeType.SMELTING, new SingleRecipeInput(input), level)
                .map(holder -> holder.value().getResultItem(level.registryAccess()))
                .filter(stack -> stack.getItem() instanceof BlockItem)
                .orElse(ItemStack.EMPTY);

        return new RecipeProducts(blasting, smelting);
    }

    // ---------- 杂项 ----------

    @Override
    public Packet<ClientGamePacketListener> getUpdatePacket() {
        return ClientboundBlockEntityDataPacket.create(this);
    }

    private static DamageSource getMushroomCloudDamageSource(Level level) {
        var holder = level.registryAccess()
                .registryOrThrow(Registries.DAMAGE_TYPE)
                .getHolderOrThrow(ModDamageTypes.NUCLEAR_EXPLOSION);
        return new DamageSource(holder);
    }

    private void clearDroppedItems(Level level, BlockPos center) {
        AABB area = new AABB(center).inflate(3);
        for (ItemEntity item : level.getEntitiesOfClass(ItemEntity.class, area)) {
            item.discard();
        }
    }

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