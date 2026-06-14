package com.extra.power.client.renderer.blockentity;

import com.extra.power.block.blockentity.NuclearCollectorBlockEntity;
import com.extra.power.client.renderer.blockentity.state.NuclearCollectorRendererState;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.dubhe.anvilcraft.client.renderer.blockentity.PowerProducerRenderer;
import dev.dubhe.anvilcraft.client.support.FeatureRendererSupport;
import net.minecraft.client.renderer.*;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;

import javax.annotation.Nullable;
import java.util.Optional;

import static com.extra.power.block.just_block.NuclearCollectorBlock.OVERHEATED;

public class NuclearCollectorRenderer implements BlockEntityRenderer< NuclearCollectorBlockEntity, NuclearCollectorRendererState> {

    public static final StandaloneModelKey<BlockStateModel> MODEL = new StandaloneModelKey<>(
            () -> "AnvilCraftExtrapower: Nuclear Collector Head Model"
    );
    public static final StandaloneModelKey<BlockStateModel> OVERHEATED_MODEL = new StandaloneModelKey<>(
            () -> "AnvilCraftExtrapower: Nuclear Collector Head Overheated Model"
    );
    public NuclearCollectorRenderer(BlockEntityRendererProvider.Context ignored) {
    }

    @Override
    public NuclearCollectorRendererState createRenderState() {
        return new NuclearCollectorRendererState();
    }

    @Override
    public void extractRenderState(NuclearCollectorBlockEntity be,
                                   NuclearCollectorRendererState state,
                                   float partialTicks,
                                   Vec3 cameraPosition,
                                   ModelFeatureRenderer.CrumblingOverlay breakProgress)
    {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);
        state.setElevation(this.elevation());
        state.setRotation(this.rotation(be, partialTicks));
        state.setCube(FeatureRendererSupport.initialize(this.getModel(be), be));
    }
    @Override
    public void submit(
            NuclearCollectorRendererState state
            , PoseStack pose
            , SubmitNodeCollector submit
            , CameraRenderState camera) {
        pose.pushPose();
        pose.translate(0.5F, state.getElevation(), 0.5F);
        pose.mulPose(Axis.YP.rotationDegrees(state.getRotation()));
        pose.mulPose(Axis.ZP.rotationDegrees(state.getRotation()));
        state.getCube().submit(pose, submit, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }


    protected float rotation(NuclearCollectorBlockEntity blockEntity, float partialTick) {
        return  blockEntity.getRotation() + blockEntity.getServerPower() * NuclearCollectorBlockEntity.ROTATION_PRE_POWER * partialTick/100;
    }

    protected float elevation() {
        return 0.75f;
    }


    protected StandaloneModelKey<BlockStateModel> getModel(NuclearCollectorBlockEntity blockEntity) {
        return Optional.of(blockEntity)
                .filter(be -> be.getLevel() != null)
                .map(be -> be.getBlockState().getValue(OVERHEATED))
                .map(overheated -> overheated ? OVERHEATED_MODEL : MODEL)
                .orElse(MODEL);
    }
}

