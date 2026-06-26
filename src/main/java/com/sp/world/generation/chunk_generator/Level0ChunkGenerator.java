package com.sp.world.generation.chunk_generator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sp.SPBRevamped;
import com.sp.init.ModBlocks;
import com.sp.world.generation.maze_generator.Level0MazeGenerator;
import net.minecraft.world.level.block.state.BlockState;
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
        super(biomeSource, 5);
    }

    @Override
    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

    public void generate(WorldGenLevel world, ChunkAccess chunk) {
        int x = chunk.getPos().getMinBlockX();
        int z = chunk.getPos().getMinBlockZ();
        RandomSource random = RandomSource.create();

        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        MinecraftServer server = world.getServer();

        if (server != null) {
            StructureTemplateManager structureTemplateManager = world.getServer().getStructureManager();
            Optional<StructureTemplate> optional;

            int megaRooms = random.nextIntBetweenInclusive(1, 2);

            Identifier roomIdentifier;
            StructurePlaceSettings structurePlacementData = new StructurePlaceSettings();

            //Spawn Point
            if((float) chunk.getPos().x == 0 && (float) chunk.getPos().z  == 0) {
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
            } else if (((float) chunk.getPos().x) % SPBRevamped.FINAL_MAZE_SIZE == 0 && ((float) chunk.getPos().z) % SPBRevamped.FINAL_MAZE_SIZE == 0) {


                if(!chunk.getPos().getBlockAt(0,20,0).closerThan(new Vec3i(0,20,0), this.getExitSpawnRadius(world))) {
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
                    if (!isNearMegaRooms(x, z, world)) {

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


            ////Code for 8 x 8 Roof////
            for(int i = 0; i < 2; i++) {
                for(int j = 0; j < 2; j++) {
                    roomIdentifier = this.getRoof();
                    structurePlacementData = this.randRotation();
                    optional = structureTemplateManager.get(roomIdentifier);

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

    }

    public boolean isNearMegaRooms(int x, int z,WorldGenLevel world){
        BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
        boolean near = false;

        for(int i = -80; i <= 80; i += 80){
            for(int j = -80; j <= 80; j += 80){
                BlockState blockState = world.getBlockState(mutable.set(x + i, 19, z + j));
                if (blockState == Blocks.WOOL.red().defaultBlockState()){
                    near = true;
                    break;
                }
            }
        }

        return near;
    }

    public Identifier getRoof(){
        RandomSource random = RandomSource.create();
        int roofNumber = random.nextIntBetweenInclusive(1,5);

        if (roofNumber == 1){
            return Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level0/roof2");
        }
        else {
            return Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level0/roof1");
        }


    }

    public StructurePlaceSettings randRotation(){
        StructurePlaceSettings structurePlacementData = new StructurePlaceSettings();
        RandomSource random = RandomSource.create();
        int rot = random.nextIntBetweenInclusive(1,2);

        if(rot == 1){
            structurePlacementData.setMirror(Mirror.NONE).setRotation(Rotation.NONE).setIgnoreEntities(true);
        }else{
            structurePlacementData.setMirror(Mirror.NONE).setRotation(Rotation.CLOCKWISE_90).setIgnoreEntities(true);
        }
        return structurePlacementData;
    }
}
