package com.extra.power.integration.create.client;

import com.extra.power.init.AnvilCraftExtrapower;
import com.extra.power.integration.create.StressGeneratorBlock;
import com.extra.power.integration.create.StressGeneratorBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.simibubi.create.content.kinetics.base.KineticBlockEntityRenderer;
import dev.dubhe.anvilcraft.client.selection.ModelSelectionRenderer;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.core.Direction;
import net.minecraft.core.Direction.Axis;
import net.minecraft.core.Direction.AxisDirection;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.joml.Quaternionf;

/**
 * 渲染应力发电机的传动轴。
 *
 * <p>{@code stress_generator_axis} 默认朝向为南北方向（模型沿 Z 轴），
 * 渲染时先按方块的 {@code axis} 属性把模型对齐到对应轴向，再绕轴旋转。</p>
 */
public class StressGeneratorRenderer implements BlockEntityRenderer<StressGeneratorBlockEntity>,
    ModelSelectionRenderer<StressGeneratorBlockEntity> {
    public static final ModelResourceLocation AXIS_MODEL = ModelResourceLocation.standalone(
        AnvilCraftExtrapower.of("block/stress_generator_axis")
    );

    public StressGeneratorRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
        StressGeneratorBlockEntity blockEntity,
        float partialTick,
        PoseStack poseStack,
        MultiBufferSource buffer,
        int packedLight,
        int packedOverlay
    ) {
        VertexConsumer consumer = buffer.getBuffer(RenderType.cutoutMipped());
        collectSelectionModels(blockEntity, partialTick, poseStack, (model, pose) -> Minecraft.getInstance()
            .getBlockRenderer()
            .getModelRenderer()
            .renderModel(
                pose.last(),
                consumer,
                null,
                Minecraft.getInstance().getModelManager().getModel(model),
                1.0F,
                1.0F,
                1.0F,
                packedLight,
                packedOverlay,
                ModelData.EMPTY,
                null
            ));
    }

    @Override
    public void collectSelectionModels(
        StressGeneratorBlockEntity blockEntity,
        float partialTick,
        PoseStack poseStack,
        ModelConsumer consumer
    ) {
        Axis axis = blockEntity.getBlockState().getValue(StressGeneratorBlock.AXIS);
        Direction orientation = Direction.get(AxisDirection.POSITIVE, axis);
        // 放置预览等场景下没有实际世界，此时给出静止姿态
        float angle = blockEntity.getLevel() == null
            ? 0
            : KineticBlockEntityRenderer.getAngleForBe(blockEntity, blockEntity.getBlockPos(), axis);

        poseStack.pushPose();
        poseStack.translate(0.5F, 0.5F, 0.5F);
        poseStack.mulPose(new Quaternionf().rotateTo(
            0, 0, 1,
            orientation.getStepX(), orientation.getStepY(), orientation.getStepZ()
        ));
        poseStack.mulPose(new Quaternionf().rotationAxis(angle, 0, 0, 1));
        poseStack.translate(-0.5F, -0.5F, -0.5F);
        consumer.accept(AXIS_MODEL, poseStack);
        poseStack.popPose();
    }
}
