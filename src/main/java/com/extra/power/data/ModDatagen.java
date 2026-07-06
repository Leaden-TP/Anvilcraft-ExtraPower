package com.extra.power.data;

import com.extra.power.data.lang.LangHandler;
import com.extra.power.init.AnvilCraftExtrapower;
import dev.anvilcraft.lib.v2.registrum.providers.ProviderType;

import net.neoforged.fml.common.EventBusSubscriber;

import static com.extra.power.init.AnvilCraftExtrapower.REGISTRATE;


public class ModDatagen {
    /**
     * 初始化生成器
     */
    public static void init() {
        REGISTRATE.addDataGenerator(ProviderType.LANG, LangHandler::init);
    }
}