package com.extra.power.client.renderer.blockentity;

import com.extra.power.block.blockentity.MushroomCloudBlockEntity;
import com.extra.power.init.AnvilCraftExtrapower;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.Minecraft;
import net.neoforged.neoforge.client.model.data.ModelData;
import org.jetbrains.annotations.NotNull;

public class MushroomCloudRenderer implements BlockEntityRenderer<MushroomCloudBlockEntity> {
    public static final ModelResourceLocation HEAD_MODEL = ModelResourceLocation.standalone(
            AnvilCraftExtrapower.of("block/mushroom_cloud_head"));
    public static final ModelResourceLocation BOTTOM_MODEL = ModelResourceLocation.standalone(
            AnvilCraftExtrapower.of("block/mushroom_cloud_bottom"));
    public static final ModelResourceLocation TOP_SIDE_MODEL = ModelResourceLocation.standalone(
            AnvilCraftExtrapower.of("block/mushroom_cloud_top_side"));
    public static final ModelResourceLocation EPICENTER_MODEL = ModelResourceLocation.standalone(
            AnvilCraftExtrapower.of("block/mushroom_epicenter"));
    public static final ModelResourceLocation CIRCLE_MODEL = ModelResourceLocation.standalone(
            AnvilCraftExtrapower.of("block/nuclear_bomb_circle"));
    public static final ModelResourceLocation CIRCLE_MODEL_2 = ModelResourceLocation.standalone(
            AnvilCraftExtrapower.of("block/nuclear_bomb_circle"));

    public MushroomCloudRenderer(BlockEntityRendererProvider.Context context){
    }

    @Override
    public void render(@NotNull MushroomCloudBlockEntity blockEntity,
                       float partialTick,
                       @NotNull PoseStack poseStack,
                       @NotNull MultiBufferSource buffer,
                       int packedLight,
                       int packedOverlay)
    {
        renderEpicenter(blockEntity, partialTick, poseStack, buffer, packedLight, packedOverlay);
        renderCloud(blockEntity, HEAD_MODEL, poseStack, buffer, packedOverlay,1);
        renderCloud(blockEntity, BOTTOM_MODEL, poseStack, buffer, packedOverlay,1);
        renderCloud(blockEntity, TOP_SIDE_MODEL, poseStack, buffer, packedOverlay,1.2f);
        CircleRotation(blockEntity, CIRCLE_MODEL, poseStack, buffer, packedOverlay,partialTick,0);
        CircleRotation(blockEntity, CIRCLE_MODEL_2, poseStack, buffer, packedOverlay,partialTick,1);
    }

    private void renderEpicenter(MushroomCloudBlockEntity blockEntity, float partialTick,
                                 PoseStack poseStack, MultiBufferSource buffer,
                                 int packedLight, int packedOverlay) {
        float scale = blockEntity.getEpicenterScale(); // 获取当前缩放值
        poseStack.pushPose();
        poseStack.translate(0.5, 0, 0.5);
        poseStack.scale(scale, scale, scale);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.translucent());
        renderModel(poseStack, vertexConsumer, EPICENTER_MODEL, packedOverlay);
        poseStack.popPose();
    }
    private void renderCloud(MushroomCloudBlockEntity blockEntity, ModelResourceLocation model,
                             PoseStack poseStack, MultiBufferSource buffer, int packedOverlay,float plus) {
        float scale = blockEntity.getC_size(); // 获取当前缩放值
        poseStack.pushPose();
        poseStack.translate(-0.5*scale, scale-(plus-1)*scale, -0.5*scale);
        poseStack.scale(scale*plus, scale*plus, scale*plus);
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.translucent());
        renderModel(poseStack, vertexConsumer, model, packedOverlay);
        poseStack.popPose();
    }
    private void CircleRotation(MushroomCloudBlockEntity blockEntity, ModelResourceLocation model,
                                PoseStack poseStack, MultiBufferSource buffer, int packedOverlay,float partialTick,float a) {
        float scale = blockEntity.getC_size(); // 获取当前缩放值
        poseStack.pushPose();
        poseStack.translate(0.25, 0-a*5+a*scale, 0.25);
        poseStack.scale(scale*(1+a), scale*(1+a), scale*(1+a));
        poseStack.mulPose(Axis.YP.rotationDegrees(blockEntity.getRotation()+partialTick));
        VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.translucent());
        renderModel(poseStack, vertexConsumer, model, packedOverlay);
        poseStack.popPose();
    }
    protected float rotation(MushroomCloudBlockEntity blockEntity, float partialTick) {
        return  0;
    }

    protected float elevation(MushroomCloudBlockEntity blockEntity) {
        return 0;
    }
    private void renderModel(PoseStack poseStack, VertexConsumer vertexConsumer,
                             ModelResourceLocation model, int packedOverlay) {
        Minecraft.getInstance()
                .getBlockRenderer()
                .getModelRenderer()
                .renderModel(
                        poseStack.last(),
                        vertexConsumer,
                        null,
                        Minecraft.getInstance().getModelManager().getModel(model),
                        0, 0, 0,
                        LightTexture.FULL_BLOCK,
                        packedOverlay, ModelData.EMPTY, RenderType.translucent());
    }
}
