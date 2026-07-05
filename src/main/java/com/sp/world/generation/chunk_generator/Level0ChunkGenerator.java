package com.sp.world.generation.chunk_generator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sp.SPBRevamped;
import com.sp.init.ModBlocks;
import com.sp.world.generation.BackroomsGenBuffer;
import com.sp.world.generation.maze_generator.Level0MazeGenerator;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Vec3i;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;

import java.util.Optional;

public class Level0ChunkGenerator extends BackroomsChunkGenerator {
    public static final MapCodec<Level0ChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource)
                    ).apply(instance, instance.stable(Level0ChunkGenerator::new)));


    public Level0ChunkGenerator(BiomeSource biomeSource) {
        // maze/megaroom regions span anchor-2..anchor+2 chunks (80x80 blocks from origin anchor-32)
        super(biomeSource, 2);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    @Override
    public boolean isAnchor(ChunkPos pos) {
        return pos.x % SPBRevamped.FINAL_MAZE_SIZE == 0 && pos.z % SPBRevamped.FINAL_MAZE_SIZE == 0;
    }

    @Override
    public void generateAnchored(WorldGenLevel world, ChunkPos anchorPos) {
        int x = anchorPos.getMinBlockX();
        int z = anchorPos.getMinBlockZ();
        // The megaroom roll below must stay this random's FIRST draw — placesMegaRoom replays it for
        // neighboring anchors to resolve megaroom adjacency deterministically.
        RandomSource random = RandomSource.create(BackroomsGenBuffer.seed(world, x, z));

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        MinecraftServer server = world.getServer();

        if (server != null) {
            StructureTemplateManager structureTemplateManager = world.getServer().getStructureManager();
            Optional<StructureTemplate> optional;

            int megaRooms = random.nextIntBetweenInclusive(1, 2);

            Identifier roomIdentifier;
            StructurePlaceSettings structurePlacementData = new StructurePlaceSettings();

            //Spawn Point
            if(anchorPos.x == 0 && anchorPos.z == 0) {
                for(int i = 0; i < 16; i++) {
                    for(int j = 0; j < 16; j++){
                        if(i == 0 && j == 0){
                            world.setBlock(mutable.set(i, 25, j), ModBlocks.GHOST_CEILING_TILE.defaultBlockState(), 16);
                        } else {
                            world.setBlock(mutable.set(i, 25, j), ModBlocks.CEILING_TILE.defaultBlockState(), 16);
                        }
                    }
                }
                world.setBlock(mutable.set(0, 25, 0), ModBlocks.GHOST_CEILING_TILE.defaultBlockState(), 16);

                roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level0/megaroom1");
                structurePlacementData.setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true);
                optional = structureTemplateManager.get(roomIdentifier);

                if (optional.isPresent()) {
                    optional.get().placeInWorld(
                            world,
                            mutable.set(x - 32, 18, z - 32),
                            mutable.set(x - 32, 18, z - 32),
                            structurePlacementData, random, 2);
                    optional.get().placeInWorld(
                            world,
                            mutable.set(x, 18, z - 32),
                            mutable.set(x, 18, z - 32),
                            structurePlacementData, random, 2);
                    optional.get().placeInWorld(
                            world,
                            mutable.set(x - 32, 18, z),
                            mutable.set(x - 32, 18, z),
                            structurePlacementData, random, 2);
                    optional.get().placeInWorld(
                            world,
                            mutable.set(x, 18, z),
                            mutable.set(x, 18, z),
                            structurePlacementData, random, 2);
                }
            } else {


                if(!anchorPos.getBlockAt(0,20,0).closerThan(new Vec3i(0,20,0), this.getExitSpawnRadius(world))) {
                    if(megaRooms != 1){
                        roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level0/stairwell_0");
                        structurePlacementData.setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true);
                        optional = structureTemplateManager.get(roomIdentifier);

                        if (optional.isPresent()) {
                            optional.get().placeInWorld(
                                    world,
                                    mutable.set(x + 15,4,z + 15),
                                    mutable.set(x + 15,4,z + 15),
                                    structurePlacementData, random, 2
                            );
                        }

                    }
                }

                if (megaRooms == 1) {
                    if (!hasEarlierMegaRoomNeighbor(world, x, z, 0)) {

                        megaRooms = random.nextIntBetweenInclusive(1, 6);
                        roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level0/megaroom" + megaRooms);
                        structurePlacementData.setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true);
                        optional = structureTemplateManager.get(roomIdentifier);

                        if (optional.isPresent()) {
                            if (megaRooms == 1 || megaRooms == 2) {
                                optional.get().placeInWorld(
                                        world,
                                        mutable.set(x - 32, 18, z - 32),
                                        mutable.set(x - 32, 18, z - 32),
                                        structurePlacementData, random, 2);
                                optional.get().placeInWorld(
                                        world,
                                        mutable.set(x, 18, z - 32),
                                        mutable.set(x, 18, z - 32),
                                        structurePlacementData, random, 2);
                                optional.get().placeInWorld(
                                        world,
                                        mutable.set(x - 32, 18, z),
                                        mutable.set(x - 32, 18, z),
                                        structurePlacementData, random, 2);
                                optional.get().placeInWorld(
                                        world,
                                        mutable.set(x, 18, z),
                                        mutable.set(x, 18, z),
                                        structurePlacementData, random, 2);
                            } else {
                                optional.get().placeInWorld(
                                        world,
                                        mutable.set(x - 16, 18, z - 16),
                                        mutable.set(x - 16, 18, z - 16),
                                        structurePlacementData, random, 2);
                                Level0MazeGenerator level0MazeGenerator = new Level0MazeGenerator(16, 5, 5, x, z, "level0");
                                level0MazeGenerator.setup(world, false, false, false);
                            }
                        }
                    } else {

                        Level0MazeGenerator level0MazeGenerator = new Level0MazeGenerator(16, 5, 5, x, z, "level0");
                        level0MazeGenerator.setup(world, false, false, false);

                    }
                } else {

                    Level0MazeGenerator level0MazeGenerator = new Level0MazeGenerator(16, 5, 5, x, z, "level0");
                    level0MazeGenerator.setup(world, false, false, false);
                }
            }
        }

    }

    /**
     * Per-chunk 8x8 roof quadrants, written directly to the real chunk AFTER the anchored slices were
     * copied out — the cyan-wool check (megaroom below) and the y=25 air check (room already has a ceiling)
     * read what the anchored pass produced.
     */
    @Override
    public void generateLocal(WorldGenLevel world, ChunkAccess chunk) {
        if (world.getServer() == null) {
            return;
        }
        int x = chunk.getPos().getMinBlockX();
        int z = chunk.getPos().getMinBlockZ();
        StructureTemplateManager structureTemplateManager = world.getServer().getStructureManager();
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();

        for(int i = 0; i < 2; i++) {
            for(int j = 0; j < 2; j++) {
                RandomSource random = RandomSource.create(BackroomsGenBuffer.seed(world, x + 8 * i, z + 8 * j));
                Identifier roomIdentifier = this.getRoof(world, x + 8 * i, z + 8 * j);
                StructurePlaceSettings structurePlacementData = this.randRotation(world, x + 8 * i, z + 8 * j);
                Optional<StructureTemplate> optional = structureTemplateManager.get(roomIdentifier);

                if (optional.isPresent()) {
                    if (world.getBlockState(mutable.set(x + 8 * i, 18, z + 8 * j)) != Blocks.WOOL.cyan().defaultBlockState() && world.getBlockState(mutable.set(x + 8 * i, 25, z + 8 * j)) == Blocks.AIR.defaultBlockState() ){
                        if (structurePlacementData.getRotation() == Rotation.CLOCKWISE_90) {
                            optional.get().placeInWorld(world, new BlockPos((x + 7) + 8 * i, 25, (z) + 8 * j), mutable.set((x + 7) + 8 * i, 25, (z) + 8 * j), structurePlacementData, random, 16);
                        } else {
                            optional.get().placeInWorld(world, new BlockPos((x) + 8 * i, 25, (z) + 8 * j), mutable.set((x) + 8 * i, 25, (z) + 8 * j), structurePlacementData, random, 16);
                        }
                    }
                }
            }
        }
    }

    /**
     * Deterministic replacement for the original's red-wool scan of the 8 neighboring anchor regions
     * (buffered builds can't see other regions, and the original read was generation-order dependent
     * anyway). An anchor's megaroom is suppressed iff a lexicographically-earlier (z, then x) neighbor
     * anchor itself places one — replaying each neighbor's first seeded roll — which reproduces the
     * original's "first placed wins" density without cross-region reads.
     */
    private boolean hasEarlierMegaRoomNeighbor(WorldGenLevel world, int x, int z, int depth) {
        for (int j = -80; j <= 80; j += 80) {
            for (int i = -80; i <= 80; i += 80) {
                if (i == 0 && j == 0) continue;
                int nx = x + i;
                int nz = z + j;
                if (nz > z || (nz == z && nx >= x)) continue;
                if (this.placesMegaRoom(world, nx, nz, depth)) {
                    return true;
                }
            }
        }
        return false;
    }

    private boolean placesMegaRoom(WorldGenLevel world, int x, int z, int depth) {
        if (x == 0 && z == 0) {
            return true; // spawn region always has megaroom1
        }
        boolean wants = RandomSource.create(BackroomsGenBuffer.seed(world, x, z)).nextIntBetweenInclusive(1, 2) == 1;
        if (!wants) {
            return false;
        }
        // deep in a chain of wanting anchors the influence of even-earlier ones has decayed — approximate
        if (depth >= 8) {
            return true;
        }
        return !this.hasEarlierMegaRoomNeighbor(world, x, z, depth + 1);
    }

    public Identifier getRoof(WorldGenLevel world, int wx, int wz){
        RandomSource random = RandomSource.create(BackroomsGenBuffer.seed(world, wx, wz));
        int roofNumber = random.nextIntBetweenInclusive(1,5);

        if (roofNumber == 1){
            return Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level0/roof2");
        }
        else {
            return Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level0/roof1");
        }


    }

    public StructurePlaceSettings randRotation(WorldGenLevel world, int wx, int wz){
        StructurePlaceSettings structurePlacementData = new StructurePlaceSettings();
        RandomSource random = RandomSource.create(BackroomsGenBuffer.seed(world, wx, wz) ^ 0x5DEECE66DL);
        int rot = random.nextIntBetweenInclusive(1,2);

        if(rot == 1){
            structurePlacementData.setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true);
        }else{
            structurePlacementData.setMirror(Mirror.NONE).setRotation(Rotation.CLOCKWISE_90).setIgnoreEntities(true);
        }
        return structurePlacementData;
    }
}
