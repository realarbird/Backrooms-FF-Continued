package com.sp.render.pbr;

import net.minecraft.world.level.block.Block;

import java.util.HashMap;

/**
 * PORT: phase6 (B6) — the PBR vertex pipeline is DROPPED on macOS (custom GLSL incl. geometry shader,
 * immediate-mode chunk-build tagging, and a custom block render layer — none have a Mac-safe path).
 *
 * <p>This class is reduced to a minimal no-op stub. It is kept (rather than deleted) only because
 * {@code SPBRevampedClient} (ported in phase 6e-b) still calls {@link #registerPBR} to declare which
 * blocks <i>were</i> PBR. Those calls now do nothing: the per-block {@link PbrMaterial} is still
 * recorded so the data survives for reference, but no custom render layer is assigned — blocks render
 * with their vanilla cutout/translucent terrain layer (set in each block's model JSON), with no PBR
 * normal/specular/parallax.
 *
 * <p>PORT: delta — the original called {@code BlockRenderLayerMap.INSTANCE.putBlock(block,
 * RenderLayers.getPbrLayer())}. Fabric 26.2 removed {@code BlockRenderLayerMap} and Minecraft removed
 * the public custom-block-layer factory, so the layer assignment is dropped entirely.
 */
public abstract class PbrRegistry {
    private static final HashMap<Block, PbrMaterial> PBR_MATERIALS = new HashMap<>();

    /**
     * PORT: phase6 (B6) no-op — records the material for reference but assigns no custom render layer.
     */
    public static void registerPBR(Block block, PbrMaterial material) {
        PBR_MATERIALS.remove(block);
        PBR_MATERIALS.put(block, material);
        // PORT: delta — Veil PBR render-layer assignment dropped (no BlockRenderLayerMap in 26.2).
    }

    public static PbrMaterial getMaterial(Block block) {
        return PBR_MATERIALS.get(block);
    }

    /**
     * New PBR Material record (data only — no longer drives any shader).
     *
     * @param enableHeight whether parallax occlusion mapping was enabled
     * @param depthMultiplier the height-texture multiplier
     * @param zoom texture scale
     * @param textureResolution the (square) PBR texture resolution
     */
    public record PbrMaterial(boolean enableHeight, float depthMultiplier, float zoom, int textureResolution) {
    }
}
