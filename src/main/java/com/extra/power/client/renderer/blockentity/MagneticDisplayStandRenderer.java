package com.extra.power.client.renderer.blockentity;

import com.extra.power.block.blockentity.MagneticDisplayStandBlockEntity;
import com.extra.power.block.blockentity.NuclearCollectorBlockEntity;
import com.extra.power.block.blockentity.SolarPanelBlockEntity;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.blaze3d.vertex.VertexConsumer;
import com.mojang.math.Axis;
import dev.dubhe.anvilcraft.client.renderer.blockentity.BaseShowItemRenderer;
import dev.dubhe.anvilcraft.client.support.RenderModelSupport;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.LightTexture;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.client.resources.model.BakedModel;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public class MagneticDisplayStandRenderer implements BlockEntityRenderer<MagneticDisplayStandBlockEntity> {


    protected ItemStack getDisplayItemStack(MagneticDisplayStandBlockEntity blockEntity) {
        // 使用从服务端同步过来的显示物品
        return blockEntity.getDisplayItemStack();
    }

    protected int getSeed(MagneticDisplayStandBlockEntity blockEntity) {
        return 0;
    }

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
        List<Double> actionState = be.getAction_state();
        if (actionState != null && actionState.size() >= 6) {
            ItemStack stack = getDisplayItemStack(be);
            float x_add = actionState.get(0).floatValue();
            float y_add = actionState.get(1).floatValue();
            float z_add = actionState.get(2).floatValue();
            float rotX = actionState.get(3).floatValue();
            float rotY = actionState.get(4).floatValue();
            float rotZ = actionState.get(5).floatValue();

            if (stack.isEmpty()) return;
            BakedModel model = Minecraft.getInstance().getItemRenderer().getModel(stack, be.getLevel(), null, getSeed(be));
            AABB aabb = RenderModelSupport.getSize(model);
            if (stack.getItem() instanceof BlockItem) {
                double x = 0.5;
                double y = 0.85;
                double z = 0.5;
                poseStack.pushPose();
                poseStack.translate(x, y + y_add, z);
                poseStack.scale(2f, 2f, 2f);
                poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
            } else {
                double modelDepth = aabb.getZsize();
                double x = 0.5;
                double y = 1.0 + modelDepth / 4;
                double z = 0.375;

                poseStack.pushPose();

                // 先平移到计算好的位置，再进行旋转
                poseStack.translate(x+x_add, y + y_add , z+z_add);
                poseStack.mulPose(Axis.XP.rotationDegrees(90.0f+rotX));
                poseStack.mulPose(Axis.YP.rotationDegrees(rotY));
            }
            Minecraft.getInstance()
                    .getItemRenderer()
                    .render(stack, ItemDisplayContext.GROUND, false, poseStack, buffer, packedLight, OverlayTexture.NO_OVERLAY, model);
            poseStack.popPose();
        }
    }
}
