package com.extra.power.client.renderer.blockentity;

import com.extra.power.block.blockentity.EnchantedGeneratorBlockEntity;
import com.extra.power.init.AnvilCraftExtrapower;
import dev.dubhe.anvilcraft.client.renderer.blockentity.PowerProducerRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class EnchantedGeneratorRenderer extends PowerProducerRenderer<EnchantedGeneratorBlockEntity> {
    public static final ModelResourceLocation MODEL = ModelResourceLocation.standalone(
            AnvilCraftExtrapower.of("block/enchanted_generator_head")
    );
    public EnchantedGeneratorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected float elevation() {
        return 0.75f;
    }

    @Override
    protected float rotation(EnchantedGeneratorBlockEntity blockEntity, float partialTick) {
        return blockEntity.getRotation() + blockEntity.getServerPower() * EnchantedGeneratorBlockEntity.ROTATION_PRE_POWER * partialTick;
    }

    @Override
    protected ModelResourceLocation getModel() {
        return MODEL;
    }
}
