package com.extra.power.init;

import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.api.advancement.AdvancementLineHelper;
import net.minecraft.advancements.*;
import net.minecraft.network.chat.Component;

public class ModAdvancements {
    public static final AdvancementHolder ROOT;

    static {
        AdvancementLineHelper mainLine = new AdvancementLineHelper();
        ROOT = mainLine.next()
                .display(
                        dev.dubhe.anvilcraft.init.item.ModItems.CAPACITOR,
                        Component.translatable("advancements.anvilcraftextrapower.root.title"),
                        Component.translatable("advancements.anvilcraftextrapower.root.description"),
                        AnvilCraft.of("textures/gui/advancements/background.png"),
                        AdvancementType.TASK,
                        false,
                        true,
                        false
                )
                .playerFirstDetected("join")
                .rewardLoot(ModLootTables.ADVANCEMENT_ROOT)
                .build("root");
    }
}
