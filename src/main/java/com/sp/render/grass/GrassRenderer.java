package com.sp.render.grass;

import com.sp.SPBRevampedClient;
import com.sp.compat.modmenu.ConfigStuff;
import com.sp.init.BackroomsLevels;

import java.util.ArrayList;
import java.util.List;

/**
 * Instanced grass renderer — A18 KEEP-REBUILD-or-DROP, Mac-safe.
 *
 * <p>PORT: phase6 delta (A18) — the 1.20.1 version drew all grass blades with a single GPU indirect
 * draw: a Veil compute shader ({@code grass/compute/positions}) frustum-culled + placed blades and
 * wrote them into an SSBO, a Veil program ({@code grass/grass}) tessellated/animated them with a wind
 * noise sampler, and the draw went through {@code glDrawElementsIndirect} into Veil's OPAQUE
 * framebuffer (raw {@code GL43}). None of that has a Mac-safe path in 26.2: Veil is gone, raw compute
 * / SSBO / indirect-draw is Mac-hostile, and 26.2 removed the immediate-mode
 * {@code Tesselator}/{@code BufferBuilder.begin}/{@code MultiBufferSource} world-draw API used by the
 * vanilla cross-quad fallback.
 *
 * <p>The blade-mesh generation (resolution-segmented quads) and the level-aware blade height are kept
 * on the CPU here. Wiring CPU-placed cross-quads into the deferred {@code submit()} pipeline (or the
 * final rebuild-vs-drop call) is deferred to phase 6f per the Mac-safe plan, where the decision is
 * made on perf; {@link GrassQuality} already gates blade count/density/resolution. Until then
 * {@link #render()} is a no-op, so no grass is drawn (delta: no instanced grass, no GPU wind
 * animation).
 */
public class GrassRenderer {

    private float lastHeight;

    public GrassRenderer() {
    }

    /**
     * Level-aware blade height (taller in Level 324). Kept verbatim from the original.
     * PORT: phase6e-b — SPBRevampedClient.isInLevel is rewired in the 6e-b client dispatch.
     */
    private float getGrassHeight() {
        if (SPBRevampedClient.isInLevel(BackroomsLevels.LEVEL324_BACKROOMS_LEVEL)) {
            return 1.5f;
        }
        return 1f;
    }

    /**
     * Build the CPU cross-quad blade mesh as interleaved position(xyz)+normal(xyz) floats, segmented
     * by the configured resolution. Retained from the original {@code createGrassModel} so the 6f
     * vanilla draw can reuse the exact geometry. Front + back faces per segment.
     */
    public float[] buildGrassModel() {
        int segments = ConfigStuff.grassQuality.getResolution();
        float height = getGrassHeight();
        float xStep = 0.1f / segments;
        List<Float> verts = new ArrayList<>();

        for (int i = 0; i < segments; i++) {
            // Front face (normal +z)
            pushVert(verts, 0.6f - xStep * (i + 1), height / segments * (i + 1), 0, 0, 0, 1);
            pushVert(verts, 0.4f + xStep * (i + 1), height / segments * (i + 1), 0, 0, 0, 1);
            pushVert(verts, 0.4f + xStep * i,       height / segments * i,       0, 0, 0, 1);
            pushVert(verts, 0.6f - xStep * i,       height / segments * i,       0, 0, 0, 1);

            // Back face (normal -z)
            pushVert(verts, 0.6f - xStep * i,       height / segments * i,       0, 0, 0, -1);
            pushVert(verts, 0.4f + xStep * i,       height / segments * i,       0, 0, 0, -1);
            pushVert(verts, 0.4f + xStep * (i + 1), height / segments * (i + 1), 0, 0, 0, -1);
            pushVert(verts, 0.6f - xStep * (i + 1), height / segments * (i + 1), 0, 0, 0, -1);
        }

        this.lastHeight = height;
        float[] out = new float[verts.size()];
        for (int i = 0; i < out.length; i++) {
            out[i] = verts.get(i);
        }
        return out;
    }

    private static void pushVert(List<Float> out, float x, float y, float z, float nx, float ny, float nz) {
        out.add(x);
        out.add(y);
        out.add(z);
        out.add(nx);
        out.add(ny);
        out.add(nz);
    }

    /**
     * PORT: phase6 (A18) no-op — GPU indirect draw dropped; vanilla cross-quad draw (or final drop)
     * lands in 6f.
     */
    public void render() {
        // PORT: delta — no grass drawn yet (see class doc). Geometry is available via buildGrassModel().
    }

    public void close() {
        // PORT: delta — no GL buffers to release (the Veil VBO/SSBO/indirect buffers are gone).
    }
}
