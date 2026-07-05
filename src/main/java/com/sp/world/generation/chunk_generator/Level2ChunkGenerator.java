package com.sp.world.generation.chunk_generator;

import com.mojang.serialization.MapCodec;
import com.mojang.serialization.codecs.RecordCodecBuilder;
import com.sp.SPBRevamped;
import com.sp.world.generation.BackroomsGenBuffer;
import net.minecraft.core.Holder;
import net.minecraft.server.MinecraftServer;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructurePlaceSettings;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplate;
import net.minecraft.world.level.levelgen.structure.templatesystem.StructureTemplateManager;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.resources.Identifier;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.WorldGenLevel;
import net.minecraft.world.level.biome.BiomeSource;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.level.chunk.ChunkAccess;
import net.minecraft.world.level.chunk.ChunkGenerator;
import net.minecraft.world.level.levelgen.NoiseGeneratorSettings;

import java.util.Optional;

public final class Level2ChunkGenerator extends BackroomsChunkGenerator {
    public static final MapCodec<Level2ChunkGenerator> CODEC = RecordCodecBuilder.mapCodec(
            instance -> instance.group(
                            BiomeSource.CODEC.fieldOf("biome_source").forGetter(generator -> generator.biomeSource),
                            NoiseGeneratorSettings.CODEC.fieldOf("settings").forGetter(generator -> generator.settings)
                    )
                    .apply(instance, instance.stable(Level2ChunkGenerator::new))
    );
    private final Holder<NoiseGeneratorSettings> settings;

    public Level2ChunkGenerator(BiomeSource biomeSource, Holder<NoiseGeneratorSettings> settings) {
        // rooms are 16-17 wide, placed at (x-1, z) along the x==0 chunk column — spill 1 chunk at most
        super(biomeSource, 1);
        this.settings = settings;
    }

    @Override
    public boolean isAnchor(ChunkPos pos) {
        return pos.x == 0;
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



        if(anchorPos.x == 0 && anchorPos.z == 0 ){
            roomIdentifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level2/stairwell2_2");
            optional = structureTemplateManager.get(roomIdentifier);

            optional.ifPresent(structureTemplate -> structureTemplate.placeInWorld(
                    world,
                    mutable.set(x - 1, 19, z),
                    mutable.set(x - 1, 19, z),
                    structurePlacementData, random, 2));

        } else if (anchorPos.x == 0){
            double noise1 = this.noiseSampler(world).noise((x) * 0.02, 0, (z) * 0.02);
            if (server != null) {

                if (noise1 > 0.0) {
                    roomIdentifier = this.getRoom(false, random);
                }
                else{
                    roomIdentifier = this.getRoom(true, random);
                }

                optional = structureTemplateManager.get(roomIdentifier);

                optional.ifPresent(structureTemplate -> structureTemplate.placeInWorld(
                        world,
                        mutable.set(x - 1, 19, z),
                        mutable.set(x - 1, 19, z),
                        structurePlacementData, random, 2));
            }
        }
    }


    public Identifier getRoom(boolean dark, RandomSource random){
        int roomNumber = random.nextIntBetweenInclusive(1,18);
        Identifier identifier;

        if(dark){
            identifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level2/dark_room" + roomNumber);
        }else{
            identifier = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level2/room" + roomNumber);
        }
        return identifier;
    }


    protected MapCodec<? extends ChunkGenerator> codec() {
        return CODEC;
    }

}

