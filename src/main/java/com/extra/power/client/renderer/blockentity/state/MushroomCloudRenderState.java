package com.extra.power.client.renderer.blockentity.state;

import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class MushroomCloudRenderState extends BlockEntityRenderState {
    private float epicenterScale;
    private float cloudSize;
    private float rotation;
    private float partialTicks;

    public float getEpicenterScale() { return epicenterScale; }
    public void setEpicenterScale(float scale) { this.epicenterScale = scale; }
    public float getCloudSize() { return cloudSize; }
    public void setCloudSize(float size) { this.cloudSize = size; }
    public float getRotation() { return rotation; }
    public void setRotation(float rotation) { this.rotation = rotation; }
    public float getPartialTicks() { return partialTicks; }
    public void setPartialTicks(float partialTicks) { this.partialTicks = partialTicks; }
}