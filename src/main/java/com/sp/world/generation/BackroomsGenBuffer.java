package com.sp.world.generation;

import com.sp.world.generation.chunk_generator.BackroomsChunkGenerator;
import it.unimi.dsi.fastutil.longs.Long2ObjectMap;
import it.unimi.dsi.fastutil.longs.Long2ObjectOpenHashMap;
import net.minecraft.core.BlockPos;
import net.minecraft.core.SectionPos;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.util.ProblemReporter;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.EntityBlock;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.storage.TagValueInput;
import org.jetbrains.annotations.Nullable;

import java.util.LinkedHashMap;
import java.util.Map;

/**
 * Makes the Backrooms chunk generators <b>chunk-local</b> so they work under 26.2's strict worldgen.
 *
 * <p>The generators place whole megarooms/mazes spanning several chunks from a single "anchor" chunk's
 * pass. 26.2 only commits block writes within {@code blockStateWriteRadius} (=1 at FEATURES), so the
 * far-edge writes were dropped, leaving systematic void chunk-columns ("holes/gaps" at region edges).</p>
 *
 * <p>Instead of letting a generator write across chunks, we run its (now deterministic) generation into a
 * per-anchor in-memory buffer — block reads/writes are redirected here by
 * {@code mixin.worldgen.BackroomsWorldGenRegionMixin} while a buffer is {@link #active()} — and then copy
 * only the <i>current</i> chunk's slice out to the real world. Every chunk that overlaps an anchor
 * reconstructs the identical buffer (seeded by the world seed + anchor coords) and copies its own slice, so
 * the world is fully filled with no cross-chunk writes. Maze connectivity survives because the maze opens
 * region-edge walls on a fixed every-other-cell pattern (symmetric → adjacent regions' openings line up)
 * and reads its megaroom doorway markers from the same buffer.</p>
 *
 * <p>Block entities placed by templates (loot crates/shelves with {@code LootTable} NBT) are materialised
 * as detached {@link BlockEntity} instances during the buffered build so
 * {@code StructureTemplate.placeInWorld} can load their NBT, then their data is transferred onto the real
 * block entity when the slice is copied out.</p>
 *
 * <p>Buffers are cached per worldgen worker thread (LRU bounded by total block count), so the chunks of one
 * anchor's region share a single build.</p>
 */
public final class BackroomsGenBuffer {

    /** Eviction budget: total buffered block states held per worker thread (~15-30 MB worst case). */
    private static final int CACHE_ENTRY_BUDGET = 500_000;

    /** The buffer whose block-IO the WorldGenRegion mixin is currently redirecting (null = pass-through). */
    private static final ThreadLocal<BackroomsGenBuffer> ACTIVE = new ThreadLocal<>();

    /** Per-thread LRU of (generator, anchor) -> built buffer. EMPTY sentinel for anchors that placed nothing. */
    private static final ThreadLocal<LruCache> CACHE = ThreadLocal.withInitial(LruCache::new);

    private static final BackroomsGenBuffer EMPTY = new BackroomsGenBuffer();

    /** Packed ChunkPos -> (packed BlockPos -> state); chunk-sliced so copy-out doesn't scan the whole region. */
    private final Long2ObjectOpenHashMap<Long2ObjectOpenHashMap<BlockState>> slices = new Long2ObjectOpenHashMap<>();
    /** Detached block entities created for template NBT loading, keyed by packed BlockPos. */
    private final Long2ObjectOpenHashMap<BlockEntity> blockEntities = new Long2ObjectOpenHashMap<>();
    private int blockCount;

    private BackroomsGenBuffer() {}

    // ---- redirect hooks (called from the WorldGenRegion mixin) ----------------------------------------

    @Nullable
    public static BackroomsGenBuffer active() {
        return ACTIVE.get();
    }

    private static long sliceKey(BlockPos pos) {
        return ChunkPos.pack(SectionPos.blockToSectionCoord(pos.getX()), SectionPos.blockToSectionCoord(pos.getZ()));
    }

    public BlockState get(BlockPos pos) {
        Long2ObjectOpenHashMap<BlockState> slice = slices.get(sliceKey(pos));
        BlockState s = slice != null ? slice.get(pos.asLong()) : null;
        return s != null ? s : Blocks.AIR.defaultBlockState();
    }

    public void put(BlockPos pos, BlockState state) {
        Long2ObjectOpenHashMap<BlockState> slice = slices.computeIfAbsent(sliceKey(pos), k -> new Long2ObjectOpenHashMap<>());
        if (slice.put(pos.asLong(), state) == null) {
            this.blockCount++;
        }
    }

    /**
     * A detached block entity for the buffered state at {@code pos} (created on first request), so
     * {@code placeInWorld} can load template NBT (loot tables etc.) into it. Null if the buffered state has
     * no block entity.
     */
    @Nullable
    public BlockEntity getOrCreateBlockEntity(BlockPos pos) {
        BlockState state = this.get(pos);
        if (!state.hasBlockEntity() || !(state.getBlock() instanceof EntityBlock entityBlock)) {
            return null;
        }
        BlockEntity existing = this.blockEntities.get(pos.asLong());
        if (existing != null && existing.getBlockState() == state) {
            return existing;
        }
        BlockEntity created = entityBlock.newBlockEntity(pos.immutable(), state);
        if (created != null) {
            this.blockEntities.put(pos.asLong(), created);
        }
        return created;
    }

    public boolean isEmpty() {
        return this.blockCount == 0;
    }

    // ---- build + copy ----------------------------------------------------------------------------------

    /**
     * Deterministic seed for a world position: mixes the world seed AND the dimension with the coordinate
     * pair (vanilla decoration-seed multipliers), so generators can self-seed every random draw from
     * positions and every chunk reconstructs the identical region — but different worlds still differ, and
     * two Backrooms levels don't draw identical random streams at the same coordinates.
     */
    public static long seed(WorldGenLevel world, int x, int z) {
        long worldSeed = world.getSeed() * 6364136223846793005L + 1442695040888963407L;
        long dimSalt = world.getLevel().dimension().identifier().toString().hashCode() * 0x9E3779B97F4A7C15L;
        return worldSeed ^ dimSalt ^ ((long) x * 341873128712L + (long) z * 132897987541L);
    }

    /**
     * Builds (or returns the cached) buffer for {@code anchor}'s region by running the generator's
     * {@code generateAnchored} with block-IO redirected into the buffer. Returns null if the anchor placed
     * nothing (cached as the EMPTY sentinel).
     */
    @Nullable
    public static BackroomsGenBuffer buildFor(BackroomsChunkGenerator generator, WorldGenLevel world, ChunkPos anchor) {
        // Key by generator identity + anchor: every dimension's worker threads share the per-thread cache,
        // and different levels use overlapping anchor positions — a position-only key let one level's
        // buffer (rooms and template air) get copied into another level's chunks.
        CacheKey key = new CacheKey(generator, anchor.pack());
        LruCache cache = CACHE.get();
        BackroomsGenBuffer cached = cache.get(key);
        if (cached != null) {
            return cached == EMPTY ? null : cached;
        }

        BackroomsGenBuffer buffer = new BackroomsGenBuffer();
        ACTIVE.set(buffer);
        try {
            generator.generateAnchored(world, anchor);
        } finally {
            ACTIVE.remove();
        }

        if (buffer.isEmpty()) {
            cache.putBuffer(key, EMPTY);
            return null;
        }
        cache.putBuffer(key, buffer);
        return buffer;
    }

    /**
     * Copies the buffered slice belonging to {@code target} out to the real world. Uses
     * {@link Block#UPDATE_CLIENTS} so block entities are created and post-processing positions are marked,
     * matching what a direct {@code placeInWorld(..., flags=2)} would have done. Template NBT captured on a
     * detached block entity during the build is transferred onto the real block entity.
     */
    public void copyChunkInto(ChunkPos target, WorldGenLevel world) {
        Long2ObjectOpenHashMap<BlockState> slice = slices.get(target.pack());
        if (slice == null) {
            return;
        }
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        for (Long2ObjectMap.Entry<BlockState> e : slice.long2ObjectEntrySet()) {
            long packed = e.getLongKey();
            mutable.set(BlockPos.getX(packed), BlockPos.getY(packed), BlockPos.getZ(packed));
            world.setBlock(mutable, e.getValue(), Block.UPDATE_CLIENTS);

            BlockEntity detached = this.blockEntities.get(packed);
            if (detached != null && detached.getBlockState() == e.getValue()) {
                BlockEntity real = world.getBlockEntity(mutable);
                if (real != null && real.getType() == detached.getType()) {
                    CompoundTag tag = detached.saveWithoutMetadata(world.registryAccess());
                    real.loadWithComponents(TagValueInput.create(ProblemReporter.DISCARDING, world.registryAccess(), tag));
                }
            }
        }
    }

    /** Generator identity (one instance per dimension) + packed anchor ChunkPos. */
    private record CacheKey(BackroomsChunkGenerator generator, long anchor) {}

    /** Access-ordered LRU bounded by the total number of buffered block states, not buffer count. */
    private static final class LruCache extends LinkedHashMap<CacheKey, BackroomsGenBuffer> {
        private int totalBlocks;

        LruCache() {
            super(64, 0.75f, true);
        }

        // not named put(): same erasure as the inherited Map.put(K,V) but a void return
        void putBuffer(CacheKey key, BackroomsGenBuffer buffer) {
            BackroomsGenBuffer prev = super.put(key, buffer);
            if (prev != null) {
                this.totalBlocks -= prev.blockCount;
            }
            this.totalBlocks += buffer.blockCount;

            // evict eldest until within budget, but always keep the entry just inserted
            var it = this.entrySet().iterator();
            while (this.totalBlocks > CACHE_ENTRY_BUDGET && this.size() > 1 && it.hasNext()) {
                Map.Entry<CacheKey, BackroomsGenBuffer> eldest = it.next();
                if (eldest.getValue() == buffer) {
                    continue;
                }
                this.totalBlocks -= eldest.getValue().blockCount;
                it.remove();
            }
        }
    }
}
