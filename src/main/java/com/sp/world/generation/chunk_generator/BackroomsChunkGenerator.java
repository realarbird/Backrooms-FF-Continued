package com.sp.world.generation.chunk_generator;

import com.sp.compat.modmenu.ConfigStuff;
import com.sp.mixininterfaces.NewServerProperties;
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

import java.util.List;
import java.util.concurrent.CompletableFuture;

public abstract class BackroomsChunkGenerator extends ChunkGenerator {
    private final int placementRadius;

    public BackroomsChunkGenerator(BiomeSource biomeSource) {
        this(biomeSource, 1);
    }

    public BackroomsChunkGenerator(BiomeSource biomeSource, int placementRadius) {
        super(biomeSource);
        this.placementRadius = placementRadius;
    }

    public abstract void generate(WorldGenLevel world, ChunkAccess chunk);

    protected int getExitSpawnRadius(WorldGenLevel world){
        if(world.getServer().isDedicatedServer()) {
            return ((NewServerProperties) ((DedicatedServer)world.getServer()).getProperties()).getExitSpawnRadius();
        } else {
            return ConfigStuff.exitSpawnRadius;
        }
    }

    /**
     * When doing chunk generation, you normally can only change the blockstates within the current chunk (placement radius of 1).
     * But you can increase that radius which is necessary since the maze generators need to place rooms a few chunks away.
     */
    public int getPlacementRadius() {
        return placementRadius;
    }


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
