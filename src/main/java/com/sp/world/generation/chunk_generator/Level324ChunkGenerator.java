package com.sp.world.generation.chunk_generator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sp.SPBRevamped;
import com.sp.init.ModBlocks;
import com.sp.world.generation.BackroomsGenBuffer;
import net.minecraft.core.Holder;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.StructureManager;
import net.minecraft.world.level.levelgen.blending.Blender;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;
import net.minecraft.world.level.levelgen.RandomState;

import java.util.Optional;
import java.util.concurrent.CompletableFuture;

public class Level324ChunkGenerator extends BackroomsChunkGenerator {
    public static final MapCodec<Level324ChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
                            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.settings)
                    )
                    .apply(instance, instance.stable(Level324ChunkGenerator::new))
    );

    private final Holder<NoiseGeneratorSettings> settings;

    public Level324ChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        // gas_station is 48 wide from block (8,0) → spills 3 chunks from its anchor
        super(biomeSource, 3);
        this.settings = settings;
    }

    @Override
    public boolean isAnchor(ChunkPos pos) {
        return true; // every chunk places its own lamps/poles
    }

    @Override
    public void generateAnchored(WorldGenLevel world, ChunkPos anchorPos) {
        int x = anchorPos.getMinBlockX();
        int z = anchorPos.getMinBlockZ();
        RandomSource random = RandomSource.create(BackroomsGenBuffer.seed(world, x, z));
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        StructureTemplateManager structureTemplateManager = world.getServer().getStructureManager();

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                if (!((z + j > -1 && z + j < 27) && (x + i > 7 && x + i < 56))) {
                    if (i == 8 && j == 7) {
                        BlockPos placementPos = mutable.set(x + i, 4, z + j);

                        StructurePlaceSettings structurePlacementData = new StructurePlaceSettings();
                        structurePlacementData.setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true);

                        Optional<StructureTemplate> optional = structureTemplateManager.get(Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level324/hanging_lamp" + (random.nextIntBetweenInclusive(0, 5) == 0 ? "_on" : "_off")));

                        optional.ifPresent(structureTemplate -> structureTemplate.placeInWorld(
                                world,
                                placementPos,
                                placementPos,
                                structurePlacementData, random, 2));
                    }

                    if ((x + i) % 1000 == 9) {
                        if ((z + j) % 21 == 0) {
                            BlockPos placementPos = mutable.set(x + i, 65, z + j);
                            StructurePlaceSettings structurePlacementData = new StructurePlaceSettings();
                            structurePlacementData.setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true);

                            Optional<StructureTemplate> optional = structureTemplateManager.get(Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "inf_grass/utility_pole"));

                            optional.ifPresent(structureTemplate -> structureTemplate.placeInWorld(
                                    world,
                                    placementPos,
                                    placementPos,
                                    structurePlacementData, random, 2));

                        }
                    }
                }

                if ((x + i) == 8) {
                    if ((z + j) == 0) {

                        BlockPos placementPos = mutable.set(x + i, -2, z + j);
                        StructurePlaceSettings structurePlacementData = new StructurePlaceSettings();
                        structurePlacementData.setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true);

                        Optional<StructureTemplate> optional = structureTemplateManager.get(Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level324/gas_station"));

                        optional.ifPresent(structureTemplate -> structureTemplate.placeInWorld(
                                world,
                                placementPos,
                                placementPos,
                                structurePlacementData, random, 2));
                    }
                }
            }
        }
    }

    @Override
    public CompletableFuture<ChunkAccess> fillFromNoise(Blender blender, RandomState randomState, StructureManager structureManager, ChunkAccess chunk) {
        int x = chunk.getPos().getMinBlockX();
        int z = chunk.getPos().getMinBlockZ();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for (int i = 0; i < 16; i++) {
            for (int j = 0; j < 16; j++) {
                if (!((z + j > -1 && z + j < 27) && (x + i > 7 && x + i < 55))) {
                    chunk.setBlockState(mutable.set(i, 0, j), ModBlocks.CONCRETE_BLOCK_11.defaultBlockState());

                    chunk.setBlockState(mutable.set(i, 63, j), ModBlocks.CONCRETE_BLOCK_11.defaultBlockState());

                    if ((i + Math.abs(x * 16)) % 1000 < 8/* || (j + Math.abs(chunk.getPos().z * 16)) % 1000 < 8*/) {
                        chunk.setBlockState(mutable.set(i, 64, j), ModBlocks.ROAD.defaultBlockState());
                    } else {
                        chunk.setBlockState(mutable.set(i, 64, j), ModBlocks.RED_DIRT.defaultBlockState());
                    }
                }
            }
        }

        for (int i = 0; i < 2; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 1; k < 63; k++) {
                    chunk.setBlockState(mutable.set(i, k, j), ModBlocks.CONCRETE_BLOCK_11.defaultBlockState());
                }
            }

            for (int j = 14; j < 16; j++) {
                for (int k = 1; k < 63; k++) {
                    chunk.setBlockState(mutable.set(i, k, j), ModBlocks.CONCRETE_BLOCK_11.defaultBlockState());
                }
            }
        }

        for (int i = 14; i < 16; i++) {
            for (int j = 0; j < 2; j++) {
                for (int k = 1; k < 63; k++) {
                    chunk.setBlockState(mutable.set(i, k, j), ModBlocks.CONCRETE_BLOCK_11.defaultBlockState());
                }
            }

            for (int j = 14; j < 16; j++) {
                for (int k = 1; k < 63; k++) {
                    chunk.setBlockState(mutable.set(i, k, j), ModBlocks.CONCRETE_BLOCK_11.defaultBlockState());
                }
            }
        }


        return CompletableFuture.completedFuture(chunk);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }
}
