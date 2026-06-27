package com.sp.render.pbr;

import it.unimi.dsi.fastutil.objects.Object2IntMap;
import it.unimi.dsi.fastutil.objects.Object2IntOpenHashMap;
import net.minecraft.world.level.block.Block;

import java.util.ArrayList;
import java.util.List;

/**
 * PORT: phase6 (B6) — block→id map for the (dropped) custom GLSL pipeline.
 *
 * <p>The original assigned every block an integer id (mirroring iris) so the PBR/light/distortion
 * GLSL shaders could branch per block, and seeded the ids from each block's vanilla render layer via
 * {@code RenderLayers.getBlockLayer(...)}. On macOS the entire custom-shader pipeline is dropped, so
 * these ids have no consumer.
 *
 * <p>This class is reduced to a minimal no-op stub. It is kept (rather than deleted) only because
 * {@code SPBRevampedClient} (phase 6e-b) and the world-render mixin (phase 6f) still reference its API
 * ({@link #init}, {@link #init()}, {@link #registerBlockID}, {@link #getBlockID}). Registered
 * callbacks are still invoked (against an empty map) so those call sites run harmlessly; the map is
 * never read by any shader.
 *
 * <p>PORT: delta — id seeding from the vanilla block render layer is dropped ({@code
 * RenderLayers.getBlockLayer} no longer exists in 26.2; terrain layers moved to the
 * {@code ChunkSectionLayer} enum + model JSON). {@link #getBlockID} always returns {@code -1}.
 */
public class BlockIdMap {
    private static final Object2IntMap<Block> BLOCK_IDS = new Object2IntOpenHashMap<>();
    private static final List<BlockIDCallback> BLOCK_ID_CALLBACKS = new ArrayList<>();
    public static boolean init = false;

    public static void init() {
        BLOCK_IDS.clear();
        init = true;

        // PORT: delta (B6) — per-block id seeding from the vanilla render layer dropped; no shader reads these.
        for (BlockIDCallback blockIDCallback : BLOCK_ID_CALLBACKS) {
            blockIDCallback.apply(BLOCK_IDS);
        }
    }

    public static int getBlockID(Block block) {
        // PORT: delta (B6) — ids no longer drive any shader; always -1.
        return -1;
    }

    /**
     * Register a callback invoked when the block ids are (re)initialized. Retained for API
     * compatibility with the phase-6e-b client entrypoint; the supplied map is no longer consumed.
     */
    public static void registerBlockID(BlockIDCallback function) {
        BLOCK_ID_CALLBACKS.add(function);
    }

    @FunctionalInterface
    public interface BlockIDCallback {
        void apply(Object2IntMap<Block> blockIDs);
    }
}
