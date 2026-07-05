package com.sp.world.generation.chunk_generator;

import com.sp.compat.modmenu.ConfigStuff;
import com.sp.mixininterfaces.NewServerProperties;
import com.sp.world.generation.BackroomsGenBuffer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.dedicated.DedicatedServer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.RandomState;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.util.RandomSource;

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class BackroomsChunkGenerator extends ChunkGenerator {
    /** How many chunks out (each axis) this generator's anchored structures can reach from their anchor. */
    private final int anchorReach;

    public BackroomsChunkGenerator(BiomeSource biomeSource, int anchorReach) {
        super(biomeSource);
        this.anchorReach = anchorReach;
    }

    /**
     * Whether {@code pos} is an anchor this generator places cross-chunk content from (e.g. the every-5th
     * chunk maze grid). {@link #generateAnchored} is only ever invoked for anchor positions.
     */
    public abstract boolean isAnchor(ChunkPos pos);

    /**
     * Generates the room/maze content of the region anchored at {@code anchorPos} — deterministically:
     * every random draw must be seeded from the world seed + a position (see
     * {@link BackroomsGenBuffer#seed}). Block I/O is redirected into a {@link BackroomsGenBuffer} by the
     * WorldGenRegion mixin, so this may freely "write" across the whole region — only the slice belonging to
     * the chunk being decorated is copied out to the real world (see {@link #applyBiomeDecoration}).
     */
    public abstract void generateAnchored(WorldGenLevel world, ChunkPos anchorPos);

    /**
     * Strictly chunk-local content, written directly to the real region AFTER the anchored slices were
     * copied out (so it can read what the anchored pass produced — e.g. Level 0's roof pass checks for
     * megaroom ceiling markers). All reads/writes must stay inside {@code chunk}(±1).
     */
    public void generateLocal(WorldGenLevel world, ChunkAccess chunk) {
    }

    /**
     * Block placement entry point — the 26.2 (mojmap) per-chunk decoration stage (the rename of Yarn
     * 1.20.1's {@code generateFeatures}, where the original mod ran its room/maze generation).
     *
     * <p>PORT FIX: the generators place whole megarooms/mazes spanning several chunks from one "anchor"
     * chunk, but 26.2 only commits writes within {@code blockStateWriteRadius} (=1) and drops the rest,
     * leaving void chunk-columns (death loop + black screen + holes/gaps). So instead of writing across
     * chunks, we reconstruct each overlapping anchor's region into a deterministic in-memory
     * {@link BackroomsGenBuffer} and copy only THIS chunk's slice out. Every chunk that overlaps an anchor
     * rebuilds the identical buffer (cached per worker thread) and copies its own slice → the world is fully
     * filled, chunk-locally.</p>
     */
    @Override
    public void applyBiomeDecoration(WorldGenLevel world, ChunkAccess chunk, StructureManager structureManager) {
        ChunkPos cpos = chunk.getPos();
        for (int ax = cpos.x - this.anchorReach; ax <= cpos.x + this.anchorReach; ax++) {
            for (int az = cpos.z - this.anchorReach; az <= cpos.z + this.anchorReach; az++) {
                ChunkPos anchor = new ChunkPos(ax, az);
                if (!this.isAnchor(anchor)) {
                    continue;
                }
                BackroomsGenBuffer buffer = BackroomsGenBuffer.buildFor(this, world, anchor);
                if (buffer != null) {
                    buffer.copyChunkInto(cpos, world);
                }
            }
        }
        this.generateLocal(world, chunk);
    }

    protected int getExitSpawnRadius(WorldGenLevel world){
        if(world.getServer().isDedicatedServer()) {
            return ((NewServerProperties) ((DedicatedServer)world.getServer()).getProperties()).getExitSpawnRadius();
        } else {
            return ConfigStuff.exitSpawnRadius;
        }
    }

    /**
     * A large-scale noise sampler seeded from the world seed, created lazily on first use (the generator is
     * constructed by codec before any world exists). Deterministic across server restarts, unlike the
     * original's {@code new ImprovedNoise(RandomSource.create())} field — that reseeded every launch, which
     * would let one anchor's region be rebuilt differently after a restart and reintroduce seams.
     */
    protected ImprovedNoise noiseSampler(WorldGenLevel world) {
        ImprovedNoise sampler = this.noiseSampler;
        if (sampler == null) {
            synchronized (this) {
                if (this.noiseSampler == null) {
                    this.noiseSampler = new ImprovedNoise(RandomSource.create(world.getSeed()));
                }
                sampler = this.noiseSampler;
            }
        }
        return sampler;
    }

    private volatile ImprovedNoise noiseSampler;


    /**
     * We Don't need to use any of these methods
     */
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    public int getSeaLevel() {
        return 0;
    }

    @Override
    public int getMinY() {
        return 0;
    }

    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmap, LevelHeightAccessor world, RandomState randomState) {
        return this.getGenDepth();
    }

    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor world, RandomState randomState) {
        BlockState[] states = new BlockState[world.getHeight()];

        for (int i = 0; i < states.length; i++) {
            states[i] = Blocks.AIR.defaultBlockState();
        }

        return new NoiseColumn(0, states);
    }

    @Override
    public void addDebugScreenInfo(List<String> text, RandomState randomState, BlockPos pos) {
    }

    @Override
    public int getGenDepth() {
        return 384;
    }

    @Override
    public void applyCarvers(WorldGenRegion chunkRegion, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk) {
    }

    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState randomState, ChunkAccess chunk) {

    }
    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {

    }
}
