package com.extra.power.client.renderer.blockentity;

import com.extra.power.block.blockentity.FrostControllerBlockEntity;
import com.extra.power.block.just_block.FrostControllerBlock;
import com.extra.power.client.renderer.blockentity.state.FrostControllerRenderState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.dubhe.anvilcraft.client.support.FeatureRendererSupport;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.jspecify.annotations.Nullable;

public class FrostControllerRenderer implements BlockEntityRenderer<FrostControllerBlockEntity, FrostControllerRenderState> {
    private static final float ROTATION_SPEED = 0.5f;
    public static final StandaloneModelKey<BlockStateModel> CUBE = new StandaloneModelKey<>(
        () -> "AnvilCraftExtrapower: Frost Controller Core Model"
    );

    public FrostControllerRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public FrostControllerRenderState createRenderState() {
        return new FrostControllerRenderState();
    }

    @Override
    public void extractRenderState(
        FrostControllerBlockEntity be,
        FrostControllerRenderState state,
        float partialTicks,
        Vec3 cameraPosition,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.setCube(FeatureRendererSupport.initialize(this.getModel(), be));
        state.setRotation(this.rotation(be, partialTicks));
        state.setElevation(this.elevation());
    }

    @Override
    public void submit(
        FrostControllerRenderState state,
        PoseStack pose,
        SubmitNodeCollector submit,
        CameraRenderState camera
    ) {

        pose.pushPose();
        pose.translate(0.5F, 0.5f, 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(state.getRotation()));
        pose.mulPose(Axis.ZP.rotationDegrees(state.getRotation()));

        state.getCube().submit(pose, submit, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }

    protected StandaloneModelKey<BlockStateModel> getModel() {
        return CUBE;
    }
    private float rotation(FrostControllerBlockEntity entity, float partialTick) {
        BlockState state = entity.getBlockState();
        if (state.getValue(FrostControllerBlock.ACTIVE)) {
            Level level = entity.getLevel();
            if (level != null) {
                // 使用游戏时间 + partialTick 动态计算旋转角度
                float angle = (level.getGameTime() + partialTick) * ROTATION_SPEED;
                return angle;
            }
        }
        return 0;
    }
    protected float elevation() {
        return 0f;
    }
}

