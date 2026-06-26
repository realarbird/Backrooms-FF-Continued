package com.sp.mixininterfaces;

// PORT: phase6 — GlUniform (net.minecraft.client.gl.GlUniform) was the Veil/GLSL-era
// shader-uniform handle and is removed in the 26.2 Mac-safe render rebuild. These accessors
// expose the VHS warp-shader uniforms; they will be rewritten against the new uniform/pipeline
// API during the render phase. Left as Object so the cast targets compile in the interim.
public interface uniformTest {
    Object getWarpAngle();
    Object getAtlasAspectRatio();

}
