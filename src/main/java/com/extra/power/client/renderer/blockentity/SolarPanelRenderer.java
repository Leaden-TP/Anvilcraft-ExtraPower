package com.extra.power.client.renderer.blockentity;

import com.extra.power.block.blockentity.SolarPanelBlockEntity;
import com.extra.power.block.just_block.SolarPanelBlock;
import com.extra.power.client.renderer.blockentity.state.SolarPanelRenderState;
import com.extra.power.client.support.FeatureRendererSupport;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.client.AnvilCraftClient;
import net.minecraft.client.renderer.SubmitNodeCollector;
import net.minecraft.client.renderer.block.dispatch.BlockStateModel;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.renderer.feature.ModelFeatureRenderer;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.client.renderer.texture.OverlayTexture;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.neoforged.neoforge.client.model.standalone.StandaloneModelKey;
import org.joml.Quaternionf;
import org.joml.Vector3f;
import org.jspecify.annotations.Nullable;

import java.util.Optional;

public class SolarPanelRenderer implements BlockEntityRenderer<SolarPanelBlockEntity, SolarPanelRenderState> {
    public static final StandaloneModelKey<BlockStateModel> HEAD = new StandaloneModelKey<>(
        () -> "AnvilCraftExtrapower: Solar Panel Head Model"
    );
    public static final StandaloneModelKey<BlockStateModel> HEAD_SUNFLOWER = new StandaloneModelKey<>(
        () -> "AnvilCraftExtrapower: Solar Panel Sunflower Head Model"
    );
    public static final StandaloneModelKey<BlockStateModel> HEAD_CLOSING = new StandaloneModelKey<>(
        () -> "AnvilCraftExtrapower: Solar Panel Head Closing Model"
    );
    public static final StandaloneModelKey<BlockStateModel> HEAD_SUNFLOWER_CLOSING = new StandaloneModelKey<>(
        () -> "AnvilCraftExtrapower: Solar Panel Sunflower Head Closing Model"
    );

    @SuppressWarnings("unused")
    public SolarPanelRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public SolarPanelRenderState createRenderState() {
        return new SolarPanelRenderState();
    }

    @Override
    public void extractRenderState(
        SolarPanelBlockEntity be,
        SolarPanelRenderState state,
        float partialTicks,
        Vec3 cameraPosition,
        ModelFeatureRenderer.@Nullable CrumblingOverlay breakProgress
    ) {
        BlockEntityRenderer.super.extractRenderState(be, state, partialTicks, cameraPosition, breakProgress);

        // 设置头部模型
        state.setHead(FeatureRendererSupport.initialize(this.getHeadModel(be), be));

        // 计算旋转
        Vector3f normal = be.getNormalVector();
        if (normal != null && !normal.equals(new Vector3f()) && !normal.equals(new Vector3f(Float.NaN))) {
            // 计算水平旋转角度
            float yaw = (float) Math.atan2(normal.x(), normal.z());
            // 计算俯仰角度
            float pitch = (float) Math.asin(normal.y());

            state.addRotation(new Quaternionf().rotateY(yaw));
            state.addRotation(Axis.XP.rotation(-pitch));
        }
    }

    private StandaloneModelKey<BlockStateModel> getHeadModel(SolarPanelBlockEntity blockEntity) {
        boolean isSunflower = Optional.of(blockEntity)
            .filter(ignore -> AnvilCraftClient.CONFIG.heliostatsSunflowerModel)
            .filter(be -> be.getLevel() != null)
            .map(be -> be.getLevel().getBiome(be.getBlockPos()))
            .map(biome -> biome.is(Biomes.SUNFLOWER_PLAINS))
            .orElse(false);

        boolean isActive = blockEntity.getBlockState().getValue(SolarPanelBlock.ACTIVE);

        if (isSunflower && isActive) {
            return HEAD_SUNFLOWER;
        } else if (isSunflower && !isActive) {
            return HEAD_SUNFLOWER_CLOSING;
        } else if (!isSunflower && isActive) {
            return HEAD;
        } else {
            return HEAD_CLOSING;
        }
    }

    @Override
    public void submit(
        SolarPanelRenderState state,
        PoseStack pose,
        SubmitNodeCollector collector,
        CameraRenderState camera
    ) {
        pose.pushPose();
        pose.translate(0.5, 1.35, 0.5);

        // 应用所有旋转
        for (Quaternionf rotation : state.getRotation()) {
            pose.mulPose(rotation);
        }

        // 渲染头部模型
        state.getHead().submit(pose, collector, state.lightCoords, OverlayTexture.NO_OVERLAY, 0);
        pose.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(SolarPanelBlockEntity blockEntity) {
        return AABB.ofSize(blockEntity.getBlockPos().getCenter().add(0, 0.5f, 0), 3, 2, 3);
    }

    @Override
    public int getViewDistance() {
        return AnvilCraft.CLIENT_CONFIG.heliostatsRenderDistance;
    }
}

