package com.extra.power.client.renderer.blockentity;

import com.extra.power.block.blockentity.RedstoneLinkBlockEntity;
import com.extra.power.block.just_block.RedstoneLinkColor;
import com.mojang.blaze3d.vertex.PoseStack;
import com.mojang.math.Axis;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.MultiBufferSource;
import net.minecraft.client.renderer.blockentity.BlockEntityRenderer;
import net.minecraft.client.renderer.blockentity.BlockEntityRendererProvider;
import net.minecraft.world.item.ItemDisplayContext;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.phys.AABB;
import org.jetbrains.annotations.NotNull;

/**
 * 在方块正中间、离底部 5 像素的位置渲染信号标记物品；蓝色模式渲染屏障（不支持物品标记）。
 */
public class RedstoneLinkRenderer implements BlockEntityRenderer<RedstoneLinkBlockEntity> {
    private static final float ITEM_Y = 5.0f / 16.0f;

    public RedstoneLinkRenderer(BlockEntityRendererProvider.Context context) {
    }

    @Override
    public void render(
            @NotNull RedstoneLinkBlockEntity be,
            float partialTick,
            @NotNull PoseStack poseStack,
            @NotNull MultiBufferSource buffer,
            int packedLight,
            int packedOverlay
    ) {
        ItemStack display = be.getMarker();
        if (be.getColor() == RedstoneLinkColor.BLUE) {
            display = new ItemStack(Items.BARRIER);
        }
        if (display.isEmpty()) return;

        poseStack.pushPose();
        poseStack.translate(0.5, ITEM_Y, 0.5);
        poseStack.mulPose(Axis.XP.rotationDegrees(90.0f));
        poseStack.scale(0.5f, 0.5f, 0.5f);
        Minecraft.getInstance().getItemRenderer().renderStatic(
                display,
                ItemDisplayContext.FIXED,
                packedLight,
                packedOverlay,
                poseStack,
                buffer,
                be.getLevel(),
                0
        );
        poseStack.popPose();
    }

    @Override
    public AABB getRenderBoundingBox(RedstoneLinkBlockEntity blockEntity) {
        return new AABB(blockEntity.getBlockPos());
    }
}
