package com.extra.power.client.renderer.blockentity.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;
import net.minecraft.client.renderer.entity.state.ItemClusterRenderState;
import net.minecraft.world.item.ItemStack;

import javax.annotation.Nullable;
import java.util.List;

/**
 * 磁力展示架渲染状态
 * 存储从服务端同步到客户端的渲染数据
 */
public class MagneticDisplayStandRendererState extends BlockEntityRenderState {
    private ItemStack display = ItemStack.EMPTY;          // 展示的物品，永不为 null
    @Nullable private ItemClusterRenderState displayState; // 物品渲染状态
    private List<Double> actionState;                     // 动画状态
    private float partialTicks;                           // 渲染插值

    public ItemStack getDisplay() {
        return display;
    }

    public void setDisplay(@Nullable ItemStack stack) {
        this.display = (stack == null) ? ItemStack.EMPTY : stack;
    }

    @Nullable
    public ItemClusterRenderState getDisplayState() {
        return displayState;
    }

    public void setDisplayState(@Nullable ItemClusterRenderState state) {
        this.displayState = state;
    }

    public List<Double> getActionState() {
        return actionState;
    }

    public void setActionState(List<Double> actionState) {
        this.actionState = actionState;
    }

    public float getPartialTicks() {
        return partialTicks;
    }

    public void setPartialTicks(float partialTicks) {
        this.partialTicks = partialTicks;
    }
}