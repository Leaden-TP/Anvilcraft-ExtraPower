package com.extra.power.client.renderer.blockentity;

import com.extra.power.block.blockentity.NuclearCollectorBlockEntity;
import com.extra.power.init.AnvilCraftExtrapower;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.dubhe.anvilcraft.client.renderer.blockentity.PowerProducerRenderer;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LevelRenderer;
import org.jetbrains.annotations.NotNull;

import java.util.Optional;

import static com.extra.power.block.just_block.NuclearCollectorBlock.OVERHEATED;

public class NuclearCollectorRenderer implements BlockEntityRenderer<NuclearCollectorBlockEntity> {
    public static final ModelResourceLocation MODEL = ModelResourceLocation.standalone(
            AnvilCraftExtrapower.of("block/nuclear_collector_head"));
    public static final ModelResourceLocation OVERHEATED_MODEL = ModelResourceLocation.standalone(
            AnvilCraftExtrapower.of("block/nuclear_collector_head_overheated"));

    public NuclearCollectorRenderer(BlockEntityRendererProvider.Context context){
    }

    @Override
    public void render(
            @NotNull NuclearCollectorBlockEntity blockEntity,
            float partialTick,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        float rotation = rotation(blockEntity, partialTick);
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
                        Minecraft.getInstance().getModelManager().getModel(getHeadModel(blockEntity)),
                        0,
                        0,
                        0,
                        LightTexture.FULL_BLOCK,
                        packedOverlay
                );
        poseStack.pushPose();
        poseStack.popPose();
    }

    protected float rotation(NuclearCollectorBlockEntity blockEntity, float partialTick) {
        return  blockEntity.getRotation() + blockEntity.getServerPower() * NuclearCollectorBlockEntity.ROTATION_PRE_POWER * partialTick/1000;
    }

    protected float elevation() {
        return 0.75f;
    }


    private ModelResourceLocation getHeadModel(NuclearCollectorBlockEntity blockEntity) {
        return Optional.of(blockEntity)
                .filter(be -> be.getLevel() != null)
                .map(be -> be.getBlockState().getValue(OVERHEATED))
                .map(overheated -> overheated ? OVERHEATED_MODEL : MODEL)
                .orElse(MODEL);
    }
}

