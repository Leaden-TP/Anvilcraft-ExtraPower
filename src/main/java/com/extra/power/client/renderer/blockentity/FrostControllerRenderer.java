package com.extra.power.client.renderer.blockentity;

import com.extra.power.block.blockentity.FrostControllerBlockEntity;
import com.extra.power.block.just_block.FrostControllerBlock;
import com.extra.power.init.AnvilCraftExtrapower;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import org.jetbrains.annotations.NotNull;

import java.util.List;
import java.util.Optional;


public class FrostControllerRenderer implements BlockEntityRenderer<FrostControllerBlockEntity> {
    private static final float ROTATION_SPEED = 0.5f;
    public static final ModelResourceLocation MODEL = ModelResourceLocation.standalone(
            AnvilCraftExtrapower.of("block/frost_controller_core"));
    public FrostControllerRenderer(BlockEntityRendererProvider.Context context){
    }
    public void render(
            @NotNull FrostControllerBlockEntity be,
            float partialTick,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        float rotation = be.getClientRotation(partialTick);
        final VertexConsumer vertexConsumer = buffer.getBuffer(RenderType.translucent());
        poseStack.translate(0.5F, elevation(), 0.5F);
        poseStack.mulPose(Axis.YP.rotationDegrees(rotation));
        poseStack.mulPose(Axis.ZP.rotationDegrees(rotation));
        Minecraft.getInstance()
                .getBlockRenderer()
                .getModelRenderer()
                .renderModel(
                        poseStack.last(),
                        vertexConsumer,
                        null,
                        Minecraft.getInstance().getModelManager().getModel(getModel(be)),
                        0,
                        0,
                        0,
                        LightTexture.FULL_BLOCK,
                        packedOverlay
                );
        poseStack.pushPose();
        poseStack.popPose();
    }

    protected float elevation() {
        return 0.5f;
    }

    private ModelResourceLocation getModel(FrostControllerBlockEntity blockEntity) {
        return MODEL;
    }
}

