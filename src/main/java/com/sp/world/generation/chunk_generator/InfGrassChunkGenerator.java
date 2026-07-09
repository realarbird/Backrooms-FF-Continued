package com.sp.world.generation.chunk_generator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sp.SPBRevamped;
import com.sp.init.ModBlocks;
import com.sp.world.generation.BackroomsGenBuffer;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.DoubleBlockHalf;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.LevelHeightAccessor;
import net.minecraft.world.level.levelgen.Heightmap;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeManager;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.NoiseColumn;
import net.minecraft.world.level.levelgen.RandomState;
import org.joml.SimplexNoise;

import java.util.List;
import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public final class InfGrassChunkGenerator extends BackroomsChunkGenerator {
    public static final MapCodec<InfGrassChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
                            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.settings)
                    )
                    .apply(instance, instance.stable(InfGrassChunkGenerator::new))
    );
    private final Holder<NoiseGeneratorSettings> settings;

    public InfGrassChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        // features up to 20 wide at the chunk origin; poles offset up to +25 in z — spill ≤2 chunks
        super(biomeSource, 2);
        this.settings = settings;
    }

    @Override
    public boolean isAnchor(ChunkPos pos) {
        return true; // every chunk places its own features
    }

    @Override
    public void generateAnchored(WorldGenLevel world, ChunkPos anchorPos) {
        int x = anchorPos.getMinBlockX();
        int z = anchorPos.getMinBlockZ();
        RandomSource random = RandomSource.create(BackroomsGenBuffer.seed(world, x, z));


        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        MinecraftServer server = world.getServer();

        StructureTemplateManager structureTemplateManager = world.getServer().getStructureManager();
        Optional<StructureTemplate> optional;

        Identifier roomIdentifier;
        StructurePlaceSettings structurePlacementData = new StructurePlaceSettings();
        structurePlacementData.setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true);


        float sampler = SimplexNoise.noise(x, 0);
        if (sampler >= 0.6) {
            if (server != null) {
                roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "inf_grass/utility_pole");

                optional = structureTemplateManager.get(roomIdentifier);

                for (int j = 0; j < 16; j++) {
                    if ((z + j) % 21 == 0) {
                        int finalJ = j;
                        optional.ifPresent(structureTemplate -> structureTemplate.placeInWorld(
                                world,
                                mutable.set(x, 31, z + finalJ + sampler * 10),
                                mutable.set(x, 31, z + finalJ + sampler * 10),
                                structurePlacementData, random, 2));
                    }
                }


            }
        } else {
            float rand = random.nextFloat();
            if (rand < 0.01f) {
                roomIdentifier = this.randFeature(!anchorPos.getBlockAt(0,20,0).closerThan(new Vec3i(0,20,0), this.getExitSpawnRadius(world)), random);

                optional = structureTemplateManager.get(roomIdentifier);

                optional.ifPresent(structureTemplate -> structureTemplate.placeInWorld(
                        world,
                        mutable.set(x, 31, z),
                        mutable.set(x, 31, z),
                        structurePlacementData, random, 2));

            }
        }
    }

    private Identifier randFeature(boolean exit, RandomSource random) {
        int rand = random.nextIntBetweenInclusive(1,3);
        if(exit){
            if(rand == 1){
                return Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "inf_grass/exit");
            }
        }
        return Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "inf_grass/feature" + rand);
    }


    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }


    /* this method builds the shape of the terrain. it places stone everywhere, which will later be overwritten with grass, terracotta, snow, sand, etc
         by the buildSurface method. it also is responsible for putting the water in oceans. it returns a CompletableFuture-- you'll likely want this to be delegated to worker threads. */
    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        BlockState shortGrass = Blocks.SHORT_GRASS.defaultBlockState();
        BlockState tallGrassLower = Blocks.TALL_GRASS.defaultBlockState()
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.LOWER);
        BlockState tallGrassUpper = Blocks.TALL_GRASS.defaultBlockState()
                .setValue(BlockStateProperties.DOUBLE_BLOCK_HALF, DoubleBlockHalf.UPPER);

        for(int k = 0; k < 16; ++k) {
            for(int l = 0; l < 16; ++l) {
                chunk.setBlockState(mutable.set(k, 30, l), ModBlocks.DIRT.defaultBlockState());

                // Dense field grass (the original drew GPU-instanced blades — dropped with Veil; a
                // swaying meadow of short/tall grass stands in, animated by the wind hook in the
                // block core shader, and drawn with the mod's high-res grass textures so it reads
                // as real grass rather than blocky Minecraft sprites). Position-hashed so
                // regeneration is deterministic. Only short/tall GRASS is used (no ferns) so the
                // whole field stays visually consistent with those HD blade textures.
                int wx = chunk.getPos().getMinBlockX() + k;
                int wz = chunk.getPos().getMinBlockZ() + l;
                float rand = hash01(wx, wz);
                if (rand < 0.65f) {
                    chunk.setBlockState(mutable.set(k, 31, l), shortGrass);
                } else if (rand < 0.97f) {
                    chunk.setBlockState(mutable.set(k, 31, l), tallGrassLower);
                    chunk.setBlockState(mutable.set(k, 32, l), tallGrassUpper);
                }
                // remaining ~3% left bare — natural thin patches
            }
        }

        return CompletableFuture.completedFuture(chunk);
    }

    /** Deterministic per-column hash in [0,1) for grass placement. */
    private static float hash01(int x, int z) {
        long h = x * 341873128712L + z * 132897987541L;
        h = (h ^ (h >>> 33)) * 0xFF51AFD7ED558CCDL;
        h = (h ^ (h >>> 33)) * 0xC4CEB9FE1A85EC53L;
        h ^= (h >>> 33);
        return (float) ((h >>> 11) * 0x1.0p-53);
    }


    @Override
    public int getSeaLevel() {
        return 0;
    }

    /* the lowest value that blocks can be placed in the world. in a vanilla world, this is -64. */
    @Override
    public int getMinY() {
        return 0;
    }

    /* this method returns the height of the terrain at a given coordinate. it's used for structure generation */
    @Override
    public int getBaseHeight(int x, int z, Heightmap.Types heightmap, LevelHeightAccessor world, RandomState randomState) {
        return this.getGenDepth();
    }

    /* this method returns a "core sample" of the world at a given coordinate. it's used for structure generation */
    @Override
    public NoiseColumn getBaseColumn(int x, int z, LevelHeightAccessor world, RandomState randomState) {
        BlockState[] states = new BlockState[world.getHeight()];

        for (int i = 0; i < states.length; i++) {
            states[i] = Blocks.AIR.defaultBlockState();
        }

        return new NoiseColumn(0, states);
    }

    /* this method adds text to the f3 menu. for NoiseChunkGenerator, it's the NoiseRouter line */
    @Override
    public void addDebugScreenInfo(List<String> text, RandomState randomState, BlockPos pos) {
    }

    /* the distance between the highest and lowest points in the world. in vanilla, this is 384 (64+325) */
    @Override
    public int getGenDepth() {
        return 384;
    }




    /* the method that creates non-noise caves (i.e., all the caves we had before the caves and cliffs update) */
    @Override
    public void applyCarvers(WorldGenRegion chunkRegion, long seed, RandomState randomState, BiomeManager biomeManager, StructureManager structureManager, ChunkAccess chunk) {
    }

    /* the method that places grass, dirt, and other things on top of the world, as well as handling the bedrock and deepslate layers,
    as well as a few other miscellaneous things. without this method, your world is just a blank stone (or whatever your default block is) canvas (plus any ores, etc) */
    @Override
    public void buildSurface(WorldGenRegion region, StructureManager structures, RandomState randomState, ChunkAccess chunk) {

    }

    /* this method spawns entities in the world */
    @Override
    public void spawnOriginalMobs(WorldGenRegion region) {

    }



}

