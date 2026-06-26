package com.sp.world.generation.chunk_generator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sp.SPBRevamped;
import com.sp.world.generation.maze_generator.Level1MazeGenerator;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.Optional;

@SuppressWarnings("OptionalIsPresent")
public final class Level1ChunkGenerator extends BackroomsChunkGenerator {
    public static final MapCodec<Level1ChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
                            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.settings)
                    )
                    .apply(instance, instance.stable(Level1ChunkGenerator::new))
    );
    private final Holder<NoiseGeneratorSettings> settings;
    RandomSource random = RandomSource.create();
    ImprovedNoise noiseSampler = new ImprovedNoise(random);

    public Level1ChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        super(biomeSource, 10);
        this.settings = settings;
    }

    public void generate(WorldGenLevel world, ChunkAccess chunk) {
        int x = chunk.getPos().getMinBlockX();
        int z = chunk.getPos().getMinBlockZ();

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        MinecraftServer server = world.getServer();

        StructureTemplateManager structureTemplateManager = world.getServer().getStructureManager();
        StructurePlaceSettings structurePlacementData = new StructurePlaceSettings();

        if (isStartChunk(chunk)) {
            Identifier roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level1/stairwell_1");
            structurePlacementData.setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true);
            Optional<StructureTemplate> stairwellStructureIn = structureTemplateManager.get(roomIdentifier);

            if (stairwellStructureIn.isPresent()) {
                stairwellStructureIn.get().placeInWorld(
                        world,
                        mutable.set(-1,19,-1),
                        mutable.set(-1,19,-1),
                        structurePlacementData, random, 2
                );
            }

            Level1MazeGenerator level1MazeGenerator = new Level1MazeGenerator(8, 10, 10, x, z, "level1");
            level1MazeGenerator.setup(world, false, false, false);
            return;
        }

        if (!isOnMazeGrid(chunk)) {
            return;
        }

        double noise1 = noiseSampler.noise((x) * 0.002, 0, (z) * 0.002);
        if (server == null) {
            return;
        }

        if (!chunk.getPos().getBlockAt(0, 20, 0).closerThan(new Vec3i(0, 20, 0), this.getExitSpawnRadius(world))) {
            if (noise1 <= 0) {
                boolean exitToLevel1 = random.nextBoolean();

                if (exitToLevel1) {
                    Identifier roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level1/stairwell2_1");
                    structurePlacementData.setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true);
                    Optional<StructureTemplate> stairwellStructureOutTo2 = structureTemplateManager.get(roomIdentifier);

                    if (stairwellStructureOutTo2.isPresent()) {
                        stairwellStructureOutTo2.get().placeInWorld(
                                world,
                                mutable.set(x + 16, 11, z + 16),
                                mutable.set(x + 16, 11, z + 16),
                                structurePlacementData, random, 2
                        );
                    }
                }/* else {
                    Identifier roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level1/stairwell324_1");
                    Optional<StructureTemplate> stairwellStructureOutTo324 = structureTemplateManager.get(roomIdentifier);

                    if (stairwellStructureOutTo324.isPresent()) {
                        stairwellStructureOutTo324.get().placeInWorld(
                                world,
                                mutable.set(x + 16, 20, z + 16),
                                mutable.set(x + 16, 20, z + 16),
                                structurePlacementData, random, 2
                        );
                    }
                }*/
            }
        }

        if (noise1 > 0) {
            Identifier roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level1/megaroom1");
            structurePlacementData.setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true);
            Optional<StructureTemplate> megaRoom = structureTemplateManager.get(roomIdentifier);

            if (megaRoom.isPresent()) {
                megaRoom.get().placeInWorld(
                        world,
                        mutable.set(x - 32, 19, z - 32),
                        mutable.set(x - 32, 19, z - 32),
                        structurePlacementData, random, 2);
                megaRoom.get().placeInWorld(
                        world,
                        mutable.set(x, 19, z - 32),
                        mutable.set(x, 19, z - 32),
                        structurePlacementData, random, 2);

                Identifier lightRoomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level1/light" + random.nextIntBetweenInclusive(1,6));
                structurePlacementData.setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true);

                Optional<StructureTemplate> lightStructure = structureTemplateManager.get(lightRoomIdentifier);

                if (lightStructure.isPresent()) {
                    lightStructure.get().placeInWorld(
                            world,
                            mutable.set(x - 32, 19, z - 32),
                            mutable.set(x - 32, 19, z - 32),
                            structurePlacementData, random, 16);
                    lightStructure.get().placeInWorld(
                            world,
                            mutable.set(x, 19, z - 32),
                            mutable.set(x, 19, z - 32),
                            structurePlacementData, random, 16);
                }
            } else {
                if (world.getBlockState(mutable.set(x, 19, z)) != Blocks.WOOL.red().defaultBlockState()) {
                    Level1MazeGenerator level1MazeGenerator = new Level1MazeGenerator(8, 10, 10, x, z, "level1");
                    level1MazeGenerator.setup(world, false, false, true);
                }
            }

            return;
        }

        if (world.getBlockState(mutable.set(x, 19, z)) != Blocks.WOOL.red().defaultBlockState()) {
            Level1MazeGenerator level1MazeGenerator = new Level1MazeGenerator(8, 10, 10, x, z, "level1");
            level1MazeGenerator.setup(world, false, false, true);
        }
    }

    private static boolean isStartChunk(ChunkAccess chunk) {
        return (float) chunk.getPos().x == 0 && (float) chunk.getPos().z == 0;
    }

    private static boolean isOnMazeGrid(ChunkAccess chunk) {
        return ((float) chunk.getPos().x) % SPBRevamped.FINAL_MAZE_SIZE == 0 && ((float) chunk.getPos().z) % SPBRevamped.FINAL_MAZE_SIZE == 0;
    }

    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }
}

