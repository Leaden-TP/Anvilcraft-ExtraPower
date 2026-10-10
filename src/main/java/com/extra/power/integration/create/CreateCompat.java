package com.extra.power.integration.create;

import com.extra.power.init.AnvilCraftExtrapower;
import dev.dubhe.anvilcraft.api.event.AnvilBehaviorRegisterEvent;
import net.neoforged.fml.ModList;

/**
 * Create 联动入口。
 *
 * <p>本类自身不引用任何 Create 类型，只有确认 Create 已加载后才会触碰
 * {@link StressGeneratorRegistration} 中与 Create 相关的部分。</p>
 *
 * <p>方块与物品始终注册：未安装 Create 时会注册为无法使用的版本，
 * 因此标签、掉落物与模型资源不会因为方块缺失而损坏。</p>
 */
public final class CreateCompat {
    public static final String CREATE_MOD_ID = "create";

    private CreateCompat() {
    }

    /** Create 是否已加载 */
    public static boolean isLoaded() {
        ModList modList = ModList.get();
        return modList != null && modList.isLoaded(CREATE_MOD_ID);
    }

    /** 注册 Create 联动内容，需要在主类构造阶段末尾（默认创造模式标签页设置之后）调用 */
    public static void init() {
        StressGeneratorRegistration.register();
        if (!isLoaded()) {
            AnvilCraftExtrapower.LOGGER.info(
                "Create is not installed, the stress generator is registered but unusable"
            );
        }
    }

    /** 注册应力发电机的铁砧行为（铁砧砸击后转化为电动机） */
    public static void registerAnvilBehaviors(AnvilBehaviorRegisterEvent event) {
        if (!isLoaded()) return;
        event.registerBehavior(
            StressGeneratorRegistration.STRESS_GENERATOR.get(),
            new StressGeneratorAnvilBehavior()
        );
    }
}
