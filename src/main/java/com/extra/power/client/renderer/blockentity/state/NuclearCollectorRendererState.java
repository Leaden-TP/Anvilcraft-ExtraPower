package com.extra.power.client.renderer.blockentity.state;

import dev.dubhe.anvilcraft.client.renderer.blockentity.state.PowerGeneratorRenderState;
import net.minecraft.client.renderer.block.BlockModelRenderState;
import net.minecraft.client.renderer.blockentity.state.BlockEntityRenderState;

public class NuclearCollectorRendererState extends BlockEntityRenderState {
    private BlockModelRenderState cube;
    private float elevation;
    private float rotation;

    public BlockModelRenderState getCube() {
        return this.cube;
    }

    public float getElevation() {
        return this.elevation;
    }

    public float getRotation() {
        return this.rotation;
    }

    public void setCube(final BlockModelRenderState cube) {
        this.cube = cube;
    }

    public void setElevation(final float elevation) {
        this.elevation = elevation;
    }

    public void setRotation(final float rotation) {
        this.rotation = rotation;
    }
}
