package com.extra.power.item;

import com.extra.power.entity.TechnicalArrowEntity;
import com.extra.power.init.ModEntities;
import com.extra.power.mixin.AbstractArrowAccessor;
import dev.dubhe.anvilcraft.init.item.ModComponents;
import dev.dubhe.anvilcraft.item.weapon.EnergyWeaponItem;
import net.minecraft.ChatFormatting;
import net.minecraft.core.Holder;
import net.minecraft.core.component.DataComponents;
import net.minecraft.core.registries.Registries;
import net.minecraft.network.chat.CommonComponents;
import net.minecraft.network.chat.Component;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundEvents;
import net.minecraft.sounds.SoundSource;
import net.minecraft.tags.ItemTags;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.entity.projectile.FireworkRocketEntity;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动弩：能量武器，可装填最多 64 发弹药，按住右键每 2 tick 发射一发无视无敌帧的抛射物。
 * 弹药与原版弩一致：{@code #minecraft:arrows} 标签内的箭矢（普通箭、光灵箭、药水箭及模组箭）与烟花火箭；
 * 其中烟花火箭只能拿在手上装填，箭矢可从背包装填。
 * 箭矢以 TechnicalArrow 发射（携带弹药的药水效果），烟花则以 FireworkRocketEntity 发射。
 * 每发消耗 ENERGY_PER_SHOT 能量（可用超级电容器补充），支持无限/力量/火焰/多重射击/穿透附魔，
 * 其中穿透与多重射击互斥（同原版），模型状态：0 待机、1 耗尽、2 拉弦、3 无弹药。
 */
public class AutomaticCrossbowItem extends EnergyWeaponItem {

    // 常量配置
    public static final int MAX_AMMO = 64;                      // 最大弹药容量
    private static final int SHOT_INTERVAL = 2;                 // 射击间隔（tick）
    private static final int ENERGY_PER_SHOT = 10_000_00;      // 每发能量消耗
    private static final int MIN_ENERGY_TO_USE = 10_000_00;    // 开始使用所需最低能量

    // 模型状态（CustomModelData）
    private static final int MODEL_IDLE = 0;        // 待机（有电）
    private static final int MODEL_EXHAUSTED = 1;   // 电量耗尽
    private static final int MODEL_CHARGING = 2;    // 拉弦（射击/装填）
    private static final int MODEL_NO_ARROW = 3;    // 无弹药

    // 构造器
    public AutomaticCrossbowItem(Properties properties) {
        super(properties.component(ModComponents.RAILGUN_AMMO, ChargedProjectiles.EMPTY));
    }

    @Override
    protected int minimumEnergy() {
        return MIN_ENERGY_TO_USE;
    }

    // 弹药存取（统一使用 RAILGUN_AMMO 组件）
    private static List<ItemStack> ammo(ItemStack weapon) {
        ChargedProjectiles stored = weapon.get(ModComponents.RAILGUN_AMMO);
        return stored != null ? stored.getItems() : List.of();
    }

    private static void setAmmo(ItemStack weapon, List<ItemStack> ammoList) {
        weapon.set(ModComponents.RAILGUN_AMMO, ChargedProjectiles.of(ammoList));
        // 移除旧的标准组件（避免冲突）
        weapon.remove(DataComponents.CHARGED_PROJECTILES);
    }

    private static boolean hasAmmo(ItemStack weapon) {
        return !ammo(weapon).isEmpty();
    }

    // 模型状态控制
    private static void setModelState(ItemStack stack, int state) {
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(state));
    }

    // 附魔辅助
    private static int getEnchantmentLevel(Level level, ItemStack stack,
                                           net.minecraft.resources.ResourceKey<Enchantment> key) {
        Holder<Enchantment> ench = level.holderLookup(Registries.ENCHANTMENT).getOrThrow(key);
        return stack.getEnchantmentLevel(ench);
    }

    // 弹药检索与装填
    /**
     * 手持可用弹药，对应原版弩的 {@code getSupportedHeldProjectiles()}：
     * {@code #minecraft:arrows} 标签内的箭矢 + 烟花火箭。
     */
    private static boolean isHeldAmmo(ItemStack stack) {
        return ProjectileWeaponItem.ARROW_OR_FIREWORK.test(stack);
    }

    /**
     * 背包内可用弹药，对应原版弩的 {@code getAllSupportedProjectiles()}：只接受 {@code #minecraft:arrows} 标签内的箭矢。
     * 烟花火箭不在此列——与原版一样必须拿在手上才能装填。
     */
    private static boolean isInventoryAmmo(ItemStack stack) {
        return ProjectileWeaponItem.ARROW_ONLY.test(stack);
    }

    /** 无限附魔只对箭矢生效（与原版一致，烟花不会被保留），且需要弹仓内首发是箭矢。 */
    private static boolean isInfinityActive(Level level, ItemStack weapon) {
        if (getEnchantmentLevel(level, weapon, Enchantments.INFINITY) <= 0) return false;
        List<ItemStack> ammoList = ammo(weapon);
        return !ammoList.isEmpty() && ammoList.getFirst().is(ItemTags.ARROWS);
    }

    /** 查找可用弹药：先看另一只手（可含烟花火箭），再扫背包（仅箭矢）。 */
    private static ItemStack findAmmo(Player player, InteractionHand weaponHand) {
        ItemStack held = otherHand(player, weaponHand);
        if (isHeldAmmo(held)) return held;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isInventoryAmmo(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    /**
     * 先装另一只手里的弹药（箭矢或烟花火箭），再从主背包装箭矢，共至 MAX_AMMO。
     * 烟花火箭只能从手上装填，与原版弩一致。
     * 创造模式或无限附魔不消耗物品，无限附魔仅对箭矢有效但仍需至少一支箭矢作为“种子”。
     */
    private static void loadAmmo(Player player, ItemStack weapon, InteractionHand weaponHand) {
        List<ItemStack> ammoList = new ArrayList<>();
        int remaining = MAX_AMMO;

        // 优先从手持（另一只手）取，此处允许烟花火箭
        ItemStack held = otherHand(player, weaponHand);
        if (isHeldAmmo(held)) {
            int count = Math.min(held.getCount(), remaining);
            for (int i = 0; i < count; i++) {
                ammoList.add(held.copyWithCount(1));
            }
            remaining -= count;
            if (!player.hasInfiniteMaterials()) held.shrink(count);
        }

        // 再从主背包取，仅箭矢
        if (remaining > 0) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (isInventoryAmmo(stack)) {
                    int count = Math.min(stack.getCount(), remaining);
                    for (int j = 0; j < count; j++) {
                        ammoList.add(stack.copyWithCount(1));
                    }
                    remaining -= count;
                    if (!player.hasInfiniteMaterials()) stack.shrink(count);
                    if (remaining == 0) break;
                }
            }
        }
        setAmmo(weapon, ammoList);
    }

    public static boolean isLoading(Player player, ItemStack stack, InteractionHand hand) {
        return true;
    }

    // 射击逻辑
    /**
     * 发射一发弹药，仅处理弹药消耗与实体生成，能量消耗由调用方通过 consumeEnergy 扣除。
     * 箭矢生成无视无敌帧的 {@link TechnicalArrowEntity}；烟花火箭按原版弩的方式生成 {@link FireworkRocketEntity}。
     */
    private void fireProjectile(ServerLevel level, ServerPlayer player, ItemStack weapon) {
        List<ItemStack> ammoList = new ArrayList<>(ammo(weapon));
        if (ammoList.isEmpty()) return;

        ItemStack ammoStack = ammoList.get(0);
        boolean firework = ammoStack.is(Items.FIREWORK_ROCKET);
        boolean infinity = isInfinityActive(level, weapon);

        // 消耗弹药（无限附魔且为箭矢时不消耗）
        if (!infinity) {
            ammoList.remove(0);
            setAmmo(weapon, ammoList);
        }

        // 读取附魔等级
        int multishot = getEnchantmentLevel(level, weapon, Enchantments.MULTISHOT);
        int piercing = getEnchantmentLevel(level, weapon, Enchantments.PIERCING);
        int flame = getEnchantmentLevel(level, weapon, Enchantments.FLAME);
        int power = getEnchantmentLevel(level, weapon, Enchantments.POWER);

        // 穿透与多重射击互斥（原版由 exclusive_set/crossbow 保证）；万一同时存在（作弊、其他模组等），穿透优先。
        if (piercing > 0) multishot = 0;

        // 多重射击：生成 1 或 3 发
        int projectileCount = multishot > 0 ? 3 : 1;
        // 烟花初速与原版弩一致（1.6），箭矢沿用本弩的 4.0
        float velocity = firework ? 1.6F : 4.0F;

        for (int i = 0; i < projectileCount; i++) {
            // 多重射击时左右偏移 ±10°
            float yRotOffset = 0.0F;
            if (projectileCount == 3) {
                if (i == 1) yRotOffset = -10.0F;
                else if (i == 2) yRotOffset = 10.0F;
            }

            if (firework) {
                FireworkRocketEntity rocket = new FireworkRocketEntity(
                        level, ammoStack, player,
                        player.getX(), player.getEyeY() - 0.15, player.getZ(), true);
                rocket.shootFromRotation(player, player.getXRot(), player.getYRot() + yRotOffset, 0.0F, velocity, 1.0F);
                level.addFreshEntity(rocket);
                continue;
            }

            TechnicalArrowEntity arrow = new TechnicalArrowEntity(
                    ModEntities.TECHNICAL_ARROW.get(), player, level, ammoStack);

            // 只有中间箭（i==0）获得力量加成
            float damage;
            if (i == 0) {
                damage = 2.0F + power * 0.5F;   // 享受力量附魔
            } else {
                damage = 2.0F;                    // 基础伤害，无力量加成
            }
            arrow.setBaseDamage(damage);

            // 穿透：交给原版 AbstractArrow 的穿透逻辑处理（可依次命中 piercing + 1 个目标）
            if (piercing > 0) {
                ((AbstractArrowAccessor) arrow).anvilcraftextrapower$setPierceLevel((byte) piercing);
            }

            // 火焰附魔对所有箭矢生效
            if (flame > 0) {
                arrow.setRemainingFireTicks(100); // 5秒
            }

            // 无限附魔：不可拾取（创造模式除外）
            if (infinity) {
                arrow.pickup = Arrow.Pickup.CREATIVE_ONLY;
            }

            arrow.shootFromRotation(player, player.getXRot(), player.getYRot() + yRotOffset, 0.0F, velocity, 1.0F);

            level.addFreshEntity(arrow);
        }

        // 音效
        level.playSound(null, player.blockPosition(),
                SoundEvents.CROSSBOW_SHOOT, SoundSource.PLAYERS, 1.0F, 1.0F);

        // 弹药耗尽则停止使用并切换模型
        if (!hasAmmo(weapon)) {
            setModelState(weapon, MODEL_NO_ARROW);
            player.stopUsingItem();
        }
    }

    // 物品事件重写
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack weapon = player.getItemInHand(hand);

        // 能量不足则失败（父类会显示提示）
        if (!canStartUsing(player, weapon, MIN_ENERGY_TO_USE)) {
            return InteractionResultHolder.fail(weapon);
        }

        // 无弹药 → 尝试装填
        if (!hasAmmo(weapon)) {
            if (findAmmo(player, hand).isEmpty()) {
                return InteractionResultHolder.fail(weapon);
            }
            player.startUsingItem(hand);
            setModelState(weapon, MODEL_CHARGING);
            return InteractionResultHolder.consume(weapon);
        }

        // 有弹药 → 直接开始射击
        player.startUsingItem(hand);
        setModelState(weapon, MODEL_IDLE);
        return InteractionResultHolder.consume(weapon);
    }

    @Override
    public void onUseTick(Level level, LivingEntity user, ItemStack weapon, int remaining) {
        if (!(user instanceof ServerPlayer player) || !(level instanceof ServerLevel serverLevel)) return;
        int elapsed = getUseDuration(weapon, user) - remaining;

        // 装填阶段
        if (!hasAmmo(weapon)) {
            int loadTime = 5; // 固定装填时间（暂不受快速装填影响）
            if (elapsed >= loadTime) {
                loadAmmo(player, weapon, player.getUsedItemHand());
                if (hasAmmo(weapon)) {
                    setModelState(weapon, MODEL_IDLE);
                    player.getCooldowns().addCooldown(this, 40);
                    player.stopUsingItem();
                    level.playSound(null, player.blockPosition(),
                            SoundEvents.CROSSBOW_LOADING_END.value(), SoundSource.PLAYERS, 1.0F, 1.0F);
                    level.playSound(null, player.blockPosition(),
                            SoundEvents.IRON_TRAPDOOR_CLOSE, SoundSource.PLAYERS, 1.5F, 1.0F);
                    // 装填完成，下一 tick 自动进入射击（玩家需再次右键）
                } else {
                    // 仍无箭矢（可能被用完），停止使用
                    setModelState(weapon, MODEL_NO_ARROW);
                    player.stopUsingItem();
                }
            } else {
                setModelState(weapon, MODEL_CHARGING);
            }
            return;
        }

        // 射击阶段（2 tick 周期）
        // 直接尝试消耗能量，若不足则父类自动处理（停止并显示提示）
        int energy = isInfinityActive(level, weapon) ? ENERGY_PER_SHOT * 2 : ENERGY_PER_SHOT;
        if (!consumeEnergy(player, weapon, energy)) {
            // 父类仅停止使用未切换模型，需手动将模型置为耗尽状态
            setModelState(weapon, MODEL_EXHAUSTED);
            return;
        }

        // 每 2 tick 发射一发：偶数 tick 拉弦，奇数 tick 射出
        if (elapsed % SHOT_INTERVAL == 0) {
            setModelState(weapon, MODEL_CHARGING);
        } else {
            setModelState(weapon, MODEL_IDLE);
            fireProjectile(serverLevel, player, weapon);
        }
    }

    @Override
    public void releaseUsing(ItemStack weapon, Level level, LivingEntity user, int remaining) {
        if (!(user instanceof Player player)) return;

        // 检查能量是否足够至少一次射击
        int energy = isInfinityActive(level, weapon) ? ENERGY_PER_SHOT * 2 : ENERGY_PER_SHOT;
        boolean hasEnoughEnergy = hasEnergyAvailable(weapon, energy);
        boolean hasAmmo = hasAmmo(weapon);

        if (!hasEnoughEnergy) {
            setModelState(weapon, MODEL_EXHAUSTED);
        } else if (!hasAmmo) {
            setModelState(weapon, MODEL_NO_ARROW);
        } else {
            setModelState(weapon, MODEL_IDLE);
        }
    }

    @Override
    public int getUseDuration(ItemStack stack, LivingEntity entity) {
        return 72000; // 足够长，允许持续使用
    }

    @Override
    public void appendHoverText(ItemStack stack, TooltipContext context, List<Component> tooltip, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltip, flag);
        List<ItemStack> loaded = ammo(stack);
        if (!loaded.isEmpty()) {
            tooltip.add(Component.translatable("item.minecraft.crossbow.projectile")
                    .append(CommonComponents.SPACE)
                    .append(loaded.getFirst().getDisplayName())
                    .append(Component.literal(" x" + loaded.size()).withStyle(ChatFormatting.GRAY)));
        }
    }
    private static ItemStack otherHand(Player player, InteractionHand hand) {
        return player.getItemInHand(hand == InteractionHand.MAIN_HAND ? InteractionHand.OFF_HAND : InteractionHand.MAIN_HAND);
    }
}