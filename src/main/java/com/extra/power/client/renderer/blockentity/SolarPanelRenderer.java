package com.extra.power.client.renderer.blockentity;

import com.extra.power.block.blockentity.SolarPanelBlockEntity;
import com.extra.power.block.just_block.SolarPanelBlock;
import com.extra.power.init.AnvilCraftExtrapower;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import dev.dubhe.anvilcraft.AnvilCraft;
import dev.dubhe.anvilcraft.client.AnvilCraftClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.RenderType;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.client.resources.model.ModelResourceLocation;
import net.minecraft.world.level.biome.Biomes;
import net.minecraft.world.phys.AABB;
import org.joml.Quaternionf;
import org.joml.Vector3f;

import java.util.Optional;

public class SolarPanelRenderer implements BlockEntityRenderer<SolarPanelBlockEntity> {
    private static final ModelResourceLocation SOLAR_PANEL_HEAD = ModelResourceLocation.standalone(AnvilCraftExtrapower.of("block/solar_panel_head"));
    private static final ModelResourceLocation SOLAR_PANEL_HEAD_SUNFLOWER = ModelResourceLocation.standalone(AnvilCraftExtrapower.of(
            "block/solar_panel_head_sunflower"));
    private static final ModelResourceLocation SOLAR_PANEL_HEAD_CLOSING = ModelResourceLocation.standalone(AnvilCraftExtrapower.of("block/solar_panel_head_closing"));
    private static final ModelResourceLocation SOLAR_PANEL_HEAD_SUNFLOWER_CLOSING = ModelResourceLocation.standalone(AnvilCraftExtrapower.of(
            "block/solar_panel_head_sunflower_closing"));

    public SolarPanelRenderer(BlockEntityRendererProvider.Context context) {
    }

    private ModelResourceLocation getHeadModel(SolarPanelBlockEntity blockEntity) {
        ModelResourceLocation a=Optional.of(blockEntity)
                .filter(ignore -> AnvilCraftClient.CONFIG.heliostatsSunflowerModel)
                .filter(be -> be.getLevel() != null)
                .map(be -> be.getLevel().getBiome(be.getBlockPos()))
                .map(biome -> biome.is(Biomes.SUNFLOWER_PLAINS))
                .orElse(false) ? SOLAR_PANEL_HEAD_SUNFLOWER : SOLAR_PANEL_HEAD;
        if (a==SOLAR_PANEL_HEAD_SUNFLOWER &&  blockEntity.getBlockState().getValue(SolarPanelBlock.ACTIVE)) {
            return SOLAR_PANEL_HEAD_SUNFLOWER;
        }
        else if (a==SOLAR_PANEL_HEAD_SUNFLOWER && !blockEntity.getBlockState().getValue(SolarPanelBlock.ACTIVE)) {
            return SOLAR_PANEL_HEAD_SUNFLOWER_CLOSING;
        }
        else if (a==SOLAR_PANEL_HEAD && blockEntity.getBlockState().getValue(SolarPanelBlock.ACTIVE)) {
            return SOLAR_PANEL_HEAD;
        }
        else {
            return SOLAR_PANEL_HEAD_CLOSING;
        }
    }

    @Override
    public void render(
            SolarPanelBlockEntity blockEntity,
            float partialTick,
            PoseStack poseStack,
            MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        poseStack.pushPose();
        poseStack.translate(0.5, 1.35, 0.5);

        // 获取法向量并调整渲染方向
        Vector3f normal = blockEntity.getNormalVector();
        if (normal != null) {
            // 计算旋转角度
            float yaw = (float) Math.atan2(normal.x(), normal.z());
            float pitch = (float) Math.asin(normal.y());
            poseStack.mulPose(Axis.YP.rotation(yaw));
            poseStack.mulPose(Axis.XP.rotation(-pitch));
        }

        Minecraft minecraft = Minecraft.getInstance();
        minecraft.getBlockRenderer().getModelRenderer().renderModel(
                poseStack.last(),
                buffer.getBuffer(RenderType.cutout()),
                null,
                minecraft.getModelManager().getModel(this.getHeadModel(blockEntity)),
                0,
                0,
                0,
                packedLight,
                packedOverlay
        );
        poseStack.popPose();
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

