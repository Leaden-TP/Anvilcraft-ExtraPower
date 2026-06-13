package com.extra.power.integration.jade;

import com.extra.power.block.blockentity.NuclearCollectorBlockEntity;
import com.extra.power.block.just_block.NuclearCollectorBlock;
import com.extra.power.integration.jade.provider.NuclearCollectorProvider;
import snownee.jade.api.IWailaClientRegistration;
import snownee.jade.api.IWailaCommonRegistration;
import snownee.jade.api.IWailaPlugin;
import snownee.jade.api.WailaPlugin;

@SuppressWarnings("unused")
@WailaPlugin
public class AddonJadePlugin implements IWailaPlugin {
    @Override
    public void register(IWailaCommonRegistration registration) {
//        registration.registerItemStorage(CrabTrapStorageProvider.INSTANCE, CrabTrapBlockEntity.class);
        registration.registerBlockDataProvider(NuclearCollectorProvider.INSTANCE, NuclearCollectorBlockEntity.class);
    }

    @Override
    public void registerClient(IWailaClientRegistration registration) {
//        registration.registerItemStorageClient(CrabTrapStorageProvider.INSTANCE);
        registration.registerBlockComponent(NuclearCollectorProvider.INSTANCE, NuclearCollectorBlock.class);
    }
}
