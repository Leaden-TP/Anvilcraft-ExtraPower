package com.extra.power.client.renderer.blockentity;

import com.extra.power.block.blockentity.MagneticDisplayStandBlockEntity;
import com.extra.power.client.renderer.blockentity.state.MagneticDisplayStandRendererState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.dubhe.anvilcraft.client.support.FeatureRendererSupport;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.item.ItemModelResolver;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jspecify.annotations.Nullable;

import java.util.List;

public class MagneticDisplayStandRenderer implements BlockEntityRenderer<MagneticDisplayStandBlockEntity, MagneticDisplayStandRendererState> {
    @SuppressWarnings("unused")
    private final ItemModelResolver itemModelResolver;

    public MagneticDisplayStandRenderer(BlockEntityRendererProvider.Context context) {
        this.itemModelResolver = context.itemModelResolver();
    }

    @Override
    public MagneticDisplayStandRendererState createRenderState() {
        return new MagneticDisplayStandRendererState();
    }

    @Override
    public void extractRenderState(
            MagneticDisplayStandBlockEntity be,
            MagneticDisplayStandRendererState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        ItemStack displayStack = be.getDisplayItemStack();
        state.setDisplay(displayStack);
        if (!displayStack.isEmpty()) {
            state.setDisplayState(FeatureRendererSupport.initialize(displayStack, this.itemModelResolver));
        } else {
            state.setDisplayState(null);
        }
        List<Double> actionState = be.getAction_state();
        if (actionState != null && actionState.size() >= 6) {
            state.setActionState(actionState);
        }
        state.setPartialTicks(partialTicks);
    }

    @Override
    public void submit(
            MagneticDisplayStandRendererState state,
            PoseStack pose,
            SubmitNodeCollector collector,
            CameraRenderState camera
    ) {
        ItemStack stack = state.getDisplay();
        if (stack.isEmpty()) return;

        List<Double> actionState = state.getActionState();
        if (actionState == null || actionState.size() < 6) return;

        float xAdd = actionState.get(0).floatValue();
        float yAdd = actionState.get(1).floatValue();
        float zAdd = actionState.get(2).floatValue();
        float rotX = actionState.get(3).floatValue();
        float rotY = actionState.get(4).floatValue();
        float rotZ = actionState.get(5).floatValue();

        ItemClusterRenderState cluster = state.getDisplayState();
        if (cluster == null) return;

        pose.pushPose();
        if (stack.getItem() instanceof BlockItem) {
            double x = 0.5;
            double y = 0.85;
            double z = 0.5;
            pose.translate(x + xAdd, y + yAdd, z + zAdd);
            pose.scale(2.0f, 2.0f, 2.0f);
            pose.mulPose(Axis.YP.rotationDegrees(rotY + state.getPartialTicks()));
        } else {
            AABB aabb = cluster.item.getModelBoundingBox();
            double modelDepth = aabb.getZsize();
            double x = 0.5;
            double y = 1.0 + modelDepth / 4;
            double z = 0.375;
            pose.translate(x + xAdd, y + yAdd, z + zAdd);
            pose.mulPose(Axis.XP.rotationDegrees(90.0f + rotX + state.getPartialTicks()));
            pose.mulPose(Axis.YP.rotationDegrees(rotY + state.getPartialTicks()));
        }
        cluster.item.submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }

    // ---------- 大范围渲染支持 ----------
    @Override
    public AABB getRenderBoundingBox(MagneticDisplayStandBlockEntity blockEntity) {
        return AABB.INFINITE;
    }


    @Override
    public int getViewDistance() {
        return 128;
    }
}