package com.sp.world.generation.chunk_generator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sp.SPBRevamped;
import com.sp.world.generation.BackroomsGenBuffer;
import com.sp.world.generation.maze_generator.PoolroomsMazeGenerator;
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
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.ArrayList;
import java.util.List;
import java.util.Optional;

public final class PoolroomsChunkGenerator extends BackroomsChunkGenerator {
    public static final MapCodec<PoolroomsChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
                            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.settings)
                    )
                    .apply(instance, instance.stable(PoolroomsChunkGenerator::new))
    );
    private final Holder<NoiseGeneratorSettings> settings;

    private final List<String> mainMegaRoomList = List.of("16x16", "16x24", "16x32", "24x16", "24x24", "24x32", "32x16", "32x24", "32x32");

    public PoolroomsChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        // maze/megaroom regions span anchor-2..anchor+2 chunks (80x80 blocks from origin anchor-32)
        super(biomeSource, 2);
        this.settings = settings;
    }

    @Override
    public boolean isAnchor(ChunkPos pos) {
        return pos.x % SPBRevamped.FINAL_MAZE_SIZE == 0 && pos.z % SPBRevamped.FINAL_MAZE_SIZE == 0;
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



        if(anchorPos.x == 0 && anchorPos.z == 0){
            roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "poolrooms/entrance");
            optional = structureTemplateManager.get(roomIdentifier);

            optional.ifPresent(structureTemplate -> structureTemplate.placeInWorld(
                    world,
                    mutable.set(0, 18, 0),
                    mutable.set(0, 18, 0),
                    structurePlacementData,
                    random,
                    2
            ));

            if(server != null){
                PoolroomsMazeGenerator poolroomsMazeGenerator = new PoolroomsMazeGenerator(8, 10, 10, x, z, "poolrooms/sky");
                poolroomsMazeGenerator.setup(world, true, false, false);
            }

        } else if (anchorPos.x % SPBRevamped.FINAL_MAZE_SIZE == 0 && anchorPos.z % SPBRevamped.FINAL_MAZE_SIZE == 0) {

            if(!anchorPos.getBlockAt(0,20,0).closerThan(new Vec3i(0,20,0), this.getExitSpawnRadius(world))){
                int exit = random.nextIntBetweenInclusive(0,4);

                if(exit == 0){
                    roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "poolrooms/poolrooms_exit");
                    optional = structureTemplateManager.get(roomIdentifier);

                    optional.ifPresent(structureTemplate -> structureTemplate.placeInWorld(
                            world,
                            mutable.set(x - 24, 18, z - 24),
                            mutable.set(x - 24, 18, z - 24),
                            structurePlacementData,
                            random,
                            2
                    ));

                    roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "poolrooms/poolrooms_exit2");
                    optional = structureTemplateManager.get(roomIdentifier);

                    optional.ifPresent(structureTemplate -> structureTemplate.placeInWorld(
                            world,
                            mutable.set(x - 32, 39, z + 17),
                            mutable.set(x - 32, 39, z + 17),
                            structurePlacementData,
                            random,
                            2
                    ));
                }
            }


            if (server != null) {
                double noise = this.noiseSampler(world).noise((x) * 0.002, 0, (z) * 0.002);
                // Biased toward the glass-ceilinged "sky" variant so the player sees a lot more open
                // sky as they explore (the enclosed "dark"/Darkrooms rooms are kept, just rarer).
                // Threshold -0.35 (was 0) lifts sky rooms from ~50% to ~65%. Worldgen only affects
                // freshly generated chunks — an existing poolrooms save won't change.
                boolean shouldSky = noise > -0.35;

                this.generateMegaRooms(world, mutable, shouldSky, x - 32, z - 32);
                PoolroomsMazeGenerator poolroomsMazeGenerator = new PoolroomsMazeGenerator(8, 10, 10, x, z, "poolrooms/" + (shouldSky ? "sky" : "dark"));
                poolroomsMazeGenerator.setup(world, shouldSky, true, false);
            }
        }

        //Removes the ceiling for debugging
//        for(int i = 0; i < 16; i++){
//            for(int j = 0; j < 16; j++) {
//                world.setBlock(mutable.set(x + i, 28, z + j), Blocks.AIR.defaultBlockState(), 2);
//                world.setBlock(mutable.set(x + i, 27, z + j), Blocks.AIR.defaultBlockState(), 2);
//            }
//        }

    }

    private void generateMegaRooms(WorldGenLevel world, BlockPos.MutableBlockPos mutable, boolean sky, int originX, int originY) {
        int size = 8;
        int rows = 10;
        int cols = 10;
        String levelDirectory = "poolrooms/sky";
        if (!sky) {
            levelDirectory = "poolrooms/dark";
        }

        RandomSource random = RandomSource.create(BackroomsGenBuffer.seed(world, originX, originY));
        StructureTemplateManager structureTemplateManager = world.getServer().getStructureManager();
        Identifier roomIdentifier = null;

        //Initial Random Mega Room
        int w = random.nextIntBetweenInclusive(1, 6);
        int p = random.nextIntBetweenInclusive(1, 3);


        if (w == 1)
            roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, levelDirectory + "/megaroom_16x16_" + p);
        else if (w == 2)
            roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, levelDirectory + "/megaroom_16x24_" + p);
        else if (w == 3)
            roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, levelDirectory + "/megaroom_16x32_" + p);
        else if (w == 4)
            roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, levelDirectory + "/megaroom_24x24_" + p);
        else if (w == 5)
            roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, levelDirectory + "/megaroom_24x32_" + p);
        else if (w == 6)
            roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, levelDirectory + "/megaroom_32x32_" + p);

        StructurePlaceSettings structurePlacementData = new StructurePlaceSettings();
        structurePlacementData.setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true);
        Optional<StructureTemplate> optional = structureTemplateManager.get(roomIdentifier);

        int directoryLength = levelDirectory.length();
        int roomWidth = Integer.parseInt(roomIdentifier.getPath().substring(directoryLength + 10, directoryLength + 12));
        int roomHeight = Integer.parseInt(roomIdentifier.getPath().substring(directoryLength + 13, directoryLength + 15));

        int randX = random.nextIntBetweenInclusive(1, cols - (1 + (roomWidth / size)));
        int randY = random.nextIntBetweenInclusive(1, rows - (1 + (roomHeight / size)));

        BlockPos structurePos = mutable.set(randX + ((size - 1) * randX) + originX, 18, randY + ((size - 1) * randY) + originY);

        if (optional.isPresent() &&
                world.getBlockState(mutable.set(structurePos.getX(), 18, structurePos.getZ())) != Blocks.WOOL.purple().defaultBlockState()
        ) {
            optional.get().placeInWorld(world, structurePos, structurePos, structurePlacementData, random, 2);
        }

        List<String> megaRoomList = new ArrayList<>(mainMegaRoomList);

        //Fill area with more mega rooms randomly
        while (!megaRoomList.isEmpty()) {
            int ind = random.nextIntBetweenInclusive(0, megaRoomList.size() - 1);
            String currentMegaRoom = megaRoomList.get(ind);
            int xx = Integer.parseInt(currentMegaRoom.substring(0, 2));
            int yy = Integer.parseInt(currentMegaRoom.substring(3, 5));
            p = random.nextIntBetweenInclusive(1, 3);
            if (yy < xx) {
                structurePlacementData.setMirror(Mirror.NONE).setRotation(Rotation.COUNTERCLOCKWISE_90).setIgnoreEntities(true);
                roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, levelDirectory + "/megaroom_" + yy + "x" + xx + "_" + p);
            } else {
                structurePlacementData.setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true);
                roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, levelDirectory + "/megaroom_" + xx + "x" + yy + "_" + p);
            }
            roomWidth = xx;
            roomHeight = yy;


            boolean placed = false;
            for (int ay = 1; ay < rows - ((roomHeight / size)); ay++) {
                for (int ax = 1; ax < cols - ((roomWidth / size)); ax++) {
                    if (!placed) {
                        BlockPos StructurePos = mutable.set(ax + ((size - 1) * ax) + originX, 18, ay + ((size - 1) * ay) + originY);

                        boolean clear = true;
                        for (int ry = -1; ry <= roomHeight; ry++) {
                            for (int bx = -1; bx <= roomWidth; bx++) {
                                if (clear) {
                                    if (world.getBlockState(new BlockPos(StructurePos.getX() + bx, 18, StructurePos.getZ() + ry)) == Blocks.WOOL.purple().defaultBlockState()) {
                                        clear = false;
                                        break;
                                    }
                                }
                            }
                        }


                        if (clear) {
                            optional = structureTemplateManager.get(roomIdentifier);
                            if (optional.isPresent()) {
                                if (structurePlacementData.getRotation() == Rotation.COUNTERCLOCKWISE_90) {
                                    optional.get().placeInWorld(world, new BlockPos(StructurePos.getX(), 18, StructurePos.getZ() + (roomHeight - 1)), new BlockPos(StructurePos.getX(), 19, StructurePos.getZ() + (roomWidth - 1)), structurePlacementData, random, 2);
                                } else {
                                    optional.get().placeInWorld(world, StructurePos, StructurePos, structurePlacementData, random, 2);
                                }
                                placed = true;
                                break;
                            }
                        }
                    }
                }
            }
            megaRoomList.remove(ind);
        }

    }

    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }
}

