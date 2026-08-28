package com.extra.power.item;

import com.extra.power.entity.TechnicalArrowEntity;
import com.extra.power.init.ModEntities;
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
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.entity.projectile.Arrow;
import net.minecraft.world.item.*;
import net.minecraft.world.item.component.ChargedProjectiles;
import net.minecraft.world.item.component.CustomModelData;
import net.minecraft.world.item.enchantment.Enchantment;
import net.minecraft.world.item.enchantment.Enchantments;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

/**
 * 自动弩 - 能量武器
 * <p>
 * 功能特性：
 * <ul>
 *   <li>可装填最多 64 支箭矢（普通箭、光灵箭、药水箭）</li>
 *   <li>按住右键连续射击，每 2 tick 发射一发</li>
 *   <li>发射的箭矢为 TechnicalArrow，可无视无敌帧</li>
 *   <li>消耗能量：每发 10,000,000，可通过超级电容器补充</li>
 *   <li>支持附魔：无限（不消耗普通箭）、力量（增加伤害）、快速装填（缩短装填时间）</li>
 *   <li>模型状态通过 CustomModelData 控制：
 *       <ul>
 *         <li>0 = 待机（有电）</li>
 *         <li>1 = 电量耗尽</li>
 *         <li>2 = 拉弦（射击/装填中）</li>
 *         <li>3 = 无弹药</li>
 *       </ul>
 *   </li>
 * </ul>
 * </p>
 */
public class AutomaticCrossbowItem extends EnergyWeaponItem {

    // ========== 常量配置 ==========
    public static final int MAX_AMMO = 64;                      // 最大弹药容量
    private static final int SHOT_INTERVAL = 2;                 // 射击间隔（tick）
    private static final int ENERGY_PER_SHOT = 10_000_00;      // 每发能量消耗
    private static final int ENERGY_REFILL_AMOUNT = 20_000_000; // 每个电容器补充量
    private static final int MIN_ENERGY_TO_USE = 10_000_00;    // 开始使用所需最低能量

    // 模型状态（CustomModelData）
    private static final int MODEL_IDLE = 0;        // 待机（有电）
    private static final int MODEL_EXHAUSTED = 1;   // 电量耗尽
    private static final int MODEL_CHARGING = 2;    // 拉弦（射击/装填）
    private static final int MODEL_NO_ARROW = 3;    // 无弹药

    // ========== 构造器 ==========
    public AutomaticCrossbowItem(Properties properties) {
        super(properties.component(ModComponents.RAILGUN_AMMO, ChargedProjectiles.EMPTY));
    }

    // ========== 弹药存取（统一使用 RAILGUN_AMMO 组件） ==========
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

    // ========== 模型状态控制 ==========
    private static void setModelState(ItemStack stack, int state) {
        stack.set(DataComponents.CUSTOM_MODEL_DATA, new CustomModelData(state));
    }

    // ========== 附魔辅助 ==========
    private static int getEnchantmentLevel(Level level, ItemStack stack,
                                           net.minecraft.resources.ResourceKey<Enchantment> key) {
        Holder<Enchantment> ench = level.holderLookup(Registries.ENCHANTMENT).getOrThrow(key);
        return stack.getEnchantmentLevel(ench);
    }

    // ========== 箭矢检索与装填 ==========
    private static boolean isArrow(ItemStack stack) {
        return stack.getItem() instanceof ArrowItem
                || stack.getItem() instanceof SpectralArrowItem
                || stack.getItem() instanceof TippedArrowItem;
    }

    /**
     * 从玩家背包或副手查找可用箭矢（优先副手）。
     */
    private static ItemStack findAmmoInInventory(Player player) {
        ItemStack offhand = player.getOffhandItem();
        if (isArrow(offhand)) return offhand;
        for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
            ItemStack stack = player.getInventory().getItem(i);
            if (isArrow(stack)) return stack;
        }
        return ItemStack.EMPTY;
    }

    /**
     * 从玩家背包/副手装填箭矢，最多填满 MAX_AMMO 支。
     * 优先从副手取，然后从主背包取。
     * 创造模式或无限附魔不消耗物品（无限附魔仅对普通箭有效，但仍需有至少一支普通箭作为“种子”）。
     */
    private static void loadAmmo(Player player, ItemStack weapon) {
        List<ItemStack> ammoList = new ArrayList<>();
        int remaining = MAX_AMMO;

        // 优先从副手取
        ItemStack offhand = player.getOffhandItem();
        if (isArrow(offhand)) {
            int count = Math.min(offhand.getCount(), remaining);
            for (int i = 0; i < count; i++) {
                ammoList.add(offhand.copyWithCount(1));
            }
            remaining -= count;
            if (!player.hasInfiniteMaterials()) offhand.shrink(count);
        }

        // 从主背包取
        if (remaining > 0) {
            for (int i = 0; i < player.getInventory().getContainerSize(); i++) {
                ItemStack stack = player.getInventory().getItem(i);
                if (isArrow(stack)) {
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

    // ========== 射击逻辑 ==========
    /**
     * 发射一支 TechnicalArrow（无视无敌帧），并消耗弹药/能量。
     * 能量消耗由调用方负责（已在外部通过 consumeEnergy 扣除）。
     * 本方法仅处理弹药消耗和实体生成。
     */
    private void fireArrow(ServerLevel level, ServerPlayer player, ItemStack weapon) {
        List<ItemStack> ammoList = new ArrayList<>(ammo(weapon));
        if (ammoList.isEmpty()) return;

        ItemStack arrowStack = ammoList.get(0);
        boolean infinity = getEnchantmentLevel(level, weapon, Enchantments.INFINITY) > 0
                && arrowStack.is(Items.ARROW);

        // 消耗弹药（无限附魔不消耗）
        if (!infinity) {
            ammoList.remove(0);
            setAmmo(weapon, ammoList);
        }

        // 读取附魔等级
        int multishot = getEnchantmentLevel(level, weapon, Enchantments.MULTISHOT);
        int flame = getEnchantmentLevel(level, weapon, Enchantments.FLAME);
        int power = getEnchantmentLevel(level, weapon, Enchantments.POWER);

        // 多重射击：生成 1 或 3 支箭矢
        int projectileCount = multishot > 0 ? 3 : 1;

        for (int i = 0; i < projectileCount; i++) {
            TechnicalArrowEntity arrow = new TechnicalArrowEntity(ModEntities.TECHNICAL_ARROW.get(), level);
            arrow.setOwner(player);
            arrow.setPos(player.getX(), player.getEyeY() - 0.1, player.getZ());

            // ---------- 只有中间箭（i==0）获得力量加成 ----------
            float damage;
            if (i == 0) {
                damage = 1.5F + power * 0.25F;   // 享受力量附魔
            } else {
                damage = 1.5F;                    // 基础伤害，无力量加成
            }
            arrow.setBaseDamage(damage);

            // 火焰附魔对所有箭矢生效
            if (flame > 0) {
                arrow.setRemainingFireTicks(100); // 5秒
            }

            // 无限附魔：不可拾取（创造模式除外）
            if (infinity) {
                arrow.pickup = Arrow.Pickup.CREATIVE_ONLY;
            }

            // 射击方向（多重射击时左右偏移 ±10°）
            float yRotOffset = 0.0F;
            if (projectileCount == 3) {
                if (i == 1) yRotOffset = -10.0F;
                else if (i == 2) yRotOffset = 10.0F;
            }
            arrow.shootFromRotation(player, player.getXRot(), player.getYRot() + yRotOffset, 0.0F, 4.0F, 1.0F);

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

    // ========== 物品事件重写 ==========
    @Override
    public InteractionResultHolder<ItemStack> use(Level level, Player player, InteractionHand hand) {
        ItemStack weapon = player.getItemInHand(hand);

        // 能量不足则失败（父类会显示提示）
        if (!canStartUsing(player, weapon, MIN_ENERGY_TO_USE)) {
            return InteractionResultHolder.fail(weapon);
        }

        // 无弹药 → 尝试装填
        if (!hasAmmo(weapon)) {
            if (findAmmoInInventory(player).isEmpty()) {
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

        // ---------- 装填阶段 ----------
        if (!hasAmmo(weapon)) {
            int loadTime = 5; // 固定装填时间（可改为受快速装填影响，现保持简单）
            if (elapsed >= loadTime) {
                loadAmmo(player, weapon);
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

        // ---------- 射击阶段 (2tick 周期) ----------
        // 直接尝试消耗能量，若不足则父类自动处理（停止并显示提示）
        int energy = 0;
        if (getEnchantmentLevel(level, weapon, Enchantments.INFINITY) > 0) energy=ENERGY_PER_SHOT*2;
        else  energy=ENERGY_PER_SHOT;
        if (!consumeEnergy(player, weapon, energy)) {
            // 能量不足时父类已调用 stopForInsufficientPower 并切换模型？
            // 但这里需要手动将模型置为耗尽状态，因为父类只停止使用，未切换模型
            setModelState(weapon, MODEL_EXHAUSTED);
            return;
        }

        // 每 2 tick 发射一发：偶数 tick 拉弦，奇数 tick 射出
        if (elapsed % SHOT_INTERVAL == 0) {
            setModelState(weapon, MODEL_CHARGING);
        } else {
            setModelState(weapon, MODEL_IDLE);
            fireArrow(serverLevel, player, weapon);
        }
    }

    @Override
    public void releaseUsing(ItemStack weapon, Level level, LivingEntity user, int remaining) {
        if (!(user instanceof Player player)) return;

        // 检查能量是否足够至少一次射击
        int energy = 0;
        if (getEnchantmentLevel(level, weapon, Enchantments.INFINITY) > 0) energy=ENERGY_PER_SHOT*2;
        else  energy=ENERGY_PER_SHOT;
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