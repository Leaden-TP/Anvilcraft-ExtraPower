package com.extra.power.client.renderer.blockentity;

import com.extra.power.block.blockentity.MushroomCloudBlockEntity;
import com.extra.power.client.renderer.blockentity.state.MushroomCloudRenderState;
import com.extra.power.init.AnvilCraftExtrapower;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jspecify.annotations.Nullable;

public class MushroomCloudRenderer implements BlockEntityRenderer<MushroomCloudBlockEntity, MushroomCloudRenderState> {

    public static final StandaloneModelKey<BlockStateModel> HEAD_MODEL = new StandaloneModelKey<>(
            () -> "AnvilCraftExtrapower: Mushroom Cloud Head"
    );
    public static final StandaloneModelKey<BlockStateModel> BOTTOM_MODEL = new StandaloneModelKey<>(
            () -> "AnvilCraftExtrapower: Mushroom Cloud Bottom"
    );
    public static final StandaloneModelKey<BlockStateModel> TOP_SIDE_MODEL = new StandaloneModelKey<>(
            () -> "AnvilCraftExtrapower: Mushroom Cloud Top Side"
    );
    public static final StandaloneModelKey<BlockStateModel> EPICENTER_MODEL = new StandaloneModelKey<>(
            () -> "AnvilCraftExtrapower: Mushroom Cloud Epicenter"
    );
    public static final StandaloneModelKey<BlockStateModel> CIRCLE_MODEL = new StandaloneModelKey<>(
            () -> "AnvilCraftExtrapower: Nuclear Bomb Circle"
    );

    public MushroomCloudRenderer(BlockEntityRendererProvider.Context context) {}

    @Override
    public MushroomCloudRenderState createRenderState() {
        return new MushroomCloudRenderState();
    }

    @Override
    public void extractRenderState(
            MushroomCloudBlockEntity be,
            MushroomCloudRenderState state,
            float partialTicks,
            Vec3 cameraPosition,
            ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.setEpicenterScale(be.getEpicenterScale());
        state.setCloudSize(be.getC_size());
        state.setRotation(be.getRotation());
        state.setPartialTicks(partialTicks);
    }

    @Override
    public void submit(
            MushroomCloudRenderState state,
            PoseStack pose,
            SubmitNodeCollector collector,
            CameraRenderState camera
    ) {
        float scale = state.getCloudSize();
        float partialTick = state.getPartialTicks();

        // 渲染中心光球
        pose.pushPose();
        pose.translate(0.5, 0, 0.5);
        pose.scale(state.getEpicenterScale(), state.getEpicenterScale(), state.getEpicenterScale());
        pose.popPose();

        // 渲染蘑菇云主体
        renderCloudPart(pose, collector, HEAD_MODEL, 1.0f, scale);
        renderCloudPart(pose, collector, BOTTOM_MODEL, 1.0f, scale);
        renderCloudPart(pose, collector, TOP_SIDE_MODEL, 1.2f, scale);

        // 渲染旋转圆环
        renderCirclePart(pose, collector, CIRCLE_MODEL, 0, scale, state.getRotation(), partialTick);
        renderCirclePart(pose, collector, CIRCLE_MODEL, 1, scale, state.getRotation(), partialTick);
    }

    private void renderCloudPart(
            PoseStack pose,
            SubmitNodeCollector collector,
            StandaloneModelKey<BlockStateModel> model,
            float plus,
            float scale
    ) {
        pose.pushPose();
        pose.translate(-0.5 * scale, scale - (plus - 1) * scale, -0.5 * scale);
        pose.scale(scale * plus, scale * plus, scale * plus);
        pose.popPose();
    }

    private void renderCirclePart(
            PoseStack pose,
            SubmitNodeCollector collector,
            StandaloneModelKey<BlockStateModel> model,
            float a,
            float scale,
            float rotation,
            float partialTick
    ) {
        pose.pushPose();
        pose.translate(0.25, -a * 5 + a * scale, 0.25);
        pose.scale(scale * (1 + a), scale * (1 + a), scale * (1 + a));
        pose.mulPose(Axis.YP.rotationDegrees(rotation + partialTick));
        pose.popPose();
    }
}