package com.sp.render.bird;

/**
 * Birds (flock) renderer — A17 KEEP-REBUILD, Mac-safe.
 *
 * <p>PORT: phase6 delta (A17) — the 1.20.1 version drew the whole flock with a single GPU
 * indirect draw: a Veil compute shader ({@code bird/compute/positions}) frustum-culled and wrote
 * instance positions into an SSBO, a Veil fragment program ({@code bird/bird}) shaded them, and the
 * draw went through {@code glDrawElementsIndirect} into Veil's OPAQUE framebuffer. None of that has
 * a Mac-safe path in 26.2: Veil is gone, raw {@code GL43} compute / SSBO / indirect-draw is
 * Mac-hostile, and 26.2 removed the immediate-mode {@code Tesselator}/{@code BufferBuilder.begin} /
 * {@code MultiBufferSource} world-draw API in favour of the deferred {@code submit()} pipeline.
 *
 * <p>The boids simulation ({@link FlockManager}, CPU, no Veil) and the quality gating
 * ({@link BirdQuality}) are kept verbatim. The actual draw is reduced to the CPU side here: this
 * class retains the per-bird model geometry as plain vertex data for a future vanilla draw. Wiring
 * those quads into the deferred render pipeline (birds as a batched submit, or as lightweight
 * entities with a registered renderer) is deferred to phase 6f, where the bird/grass rebuild-vs-drop
 * call is made on perf. Until then {@link #render()} is a no-op, so no birds are drawn (delta: the
 * flock is simulated but not yet visible).
 */
public class BirdRenderer {

    /**
     * The single-bird model: 4 triangles (front/bottom/right/left faces), as interleaved
     * position(xyz)+normal(xyz) floats. Kept from the original {@code createGrassModel} body so the
     * 6f vanilla draw can reuse the exact geometry.
     */
    public static final float[] BIRD_MODEL = {
            // Front face
            0.000000f, 0.000000f, -1.000000f, 0.8402f, 0.2425f, -0.4851f,
            0.000000f, 2.000000f, 0.000000f, 0.8402f, 0.2425f, -0.4851f,
            0.866025f, 0.000000f, 0.500000f, 0.8402f, 0.2425f, -0.4851f,
            // Bottom face
            0.000000f, 0.000000f, -1.000000f, 0.0000f, -1.0000f, 0.0000f,
            0.866025f, 0.000000f, 0.500000f, 0.0000f, -1.0000f, 0.0000f,
            -0.866025f, 0.000000f, 0.500000f, 0.0000f, -1.0000f, 0.0000f,
            // Right face
            0.866025f, 0.000000f, 0.500000f, 0.0000f, 0.2425f, 0.9701f,
            0.000000f, 2.000000f, 0.000000f, 0.0000f, 0.2425f, 0.9701f,
            -0.866025f, 0.000000f, 0.500000f, 0.0000f, 0.2425f, 0.9701f,
            // Left face
            -0.866025f, 0.000000f, 0.500000f, -0.8402f, 0.2425f, -0.4851f,
            0.000000f, 2.000000f, 0.000000f, -0.8402f, 0.2425f, -0.4851f,
            0.000000f, 0.000000f, -1.000000f, -0.8402f, 0.2425f, -0.4851f,
    };

    public BirdRenderer() {
    }

    /**
     * PORT: phase6 (A17) no-op — GPU indirect draw dropped; vanilla submit-based draw lands in 6f.
     */
    public void render() {
        // PORT: delta — flock is simulated by FlockManager but not yet drawn (see class doc).
    }

    public void close() {
        // PORT: delta — no GL buffers to release (the Veil VBO/SSBO/indirect buffers are gone).
    }
}
