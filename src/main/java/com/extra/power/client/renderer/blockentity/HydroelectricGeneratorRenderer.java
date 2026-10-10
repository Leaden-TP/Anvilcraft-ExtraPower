package com.extra.power.client.renderer.blockentity;

import com.extra.power.block.blockentity.HydroelectricGeneratorBlockEntity;
import com.extra.power.init.AnvilCraftExtrapower;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.dubhe.anvilcraft.client.renderer.blockentity.PowerProducerRenderer;
import dev.dubhe.anvilcraft.client.selection.ModelSelectionRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;

public class HydroelectricGeneratorRenderer extends PowerProducerRenderer<HydroelectricGeneratorBlockEntity> {
    public static final ModelResourceLocation MODEL = ModelResourceLocation.standalone(
        AnvilCraftExtrapower.of("block/hydroelectric_generator_axis")
    );

    public HydroelectricGeneratorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    protected float elevation() {
        return 0.5f;
    }

    /** 叶轮转速跟随发电量，转向跟随水流方向；竖直整体共用最下方发电机的旋转 */
    @Override
    protected float rotation(HydroelectricGeneratorBlockEntity blockEntity, float partialTick) {
        HydroelectricGeneratorBlockEntity master = blockEntity.groupMaster();
        return master.getRotation()
            + master.spinDirection() * master.getServerPower()
            * HydroelectricGeneratorBlockEntity.ROTATION_PRE_POWER * partialTick;
    }

    @Override
    protected ModelResourceLocation getModel() {
        return MODEL;
    }

    /** 叶轮仅绕 Y 轴旋转 */
    @Override
    public void collectSelectionModels(
        HydroelectricGeneratorBlockEntity blockEntity,
        float partialTick,
        PoseStack poseStack,
        ModelSelectionRenderer.ModelConsumer consumer
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5F, this.elevation(), 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(this.rotation(blockEntity, partialTick)));
        consumer.accept(this.getModel(), poseStack);
        poseStack.popPose();
    }
}
