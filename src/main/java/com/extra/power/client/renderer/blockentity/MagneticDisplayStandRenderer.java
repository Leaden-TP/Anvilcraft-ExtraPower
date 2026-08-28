package com.extra.power.client.renderer.blockentity;

import com.extra.power.block.blockentity.MagneticDisplayStandBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.dubhe.anvilcraft.client.support.RenderModelSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

public class MagneticDisplayStandRenderer implements BlockEntityRenderer<MagneticDisplayStandBlockEntity> {

    public MagneticDisplayStandRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            @NotNull MagneticDisplayStandBlockEntity be,
            float partialTick,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        ItemStack stack = be.getDisplayItemStack();
        if (stack.isEmpty()) return;

        float yOffset = be.getClientYOffset(partialTick);
        float zOffset = be.getClientZOffset(partialTick);
        float rotX = be.getClientRotationX(partialTick);
        float rotY = be.getClientRotationY(partialTick);

        BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, be.getLevel(), null, 0);
        AABB aabb = RenderModelSupport.getSize(model);
        poseStack.pushPose();

        if (stack.getItem() instanceof BlockItem) {
            double x = 0.5;
            double y = 0.85;
            double z = 0.5;
            poseStack.translate(x, y + yOffset, z + zOffset);
            poseStack.scale(2.0f, 2.0f, 2.0f);
            poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        } else {
            double modelDepth = aabb.getZsize();
            double x = 0.5;
            double y = 1.0 + modelDepth / 4.0;
            double z = 0.375;
            poseStack.translate(x, y + yOffset, z + zOffset);
            poseStack.mulPose(Axis.XP.rotationDegrees(90.0f + rotX));
            poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
        }

        Minecraft.getInstance().getItemRenderer().render(
                stack, ItemDisplayContext.GROUND, false, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, model
        );
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(MagneticDisplayStandBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos()).inflate(2.0, 7.0, 2.0);
    }

    @Override
    public int getViewDistance() {
        return 128;
    }
}