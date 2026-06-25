package com.sp.init;

import com.sp.SPBRevamped;
import com.sp.block.SprintBlockSoundGroup;
import com.sp.block.custom.*;
import com.sp.block.custom.pipes.*;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.Item;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.SlabBlock;
import net.minecraft.world.level.block.TransparentBlock;
import net.minecraft.world.level.block.state.BlockBehaviour;

import java.util.function.Function;

public class ModBlocks {

    public static final Block VOID_BLOCK = registerBlock("void_block", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).forceSolidOn().noTerrainParticles());

    //////Level 1 Stuff//////
    public static final Block WALL_BLOCK = registerBlock("wall_block", WallBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).destroyTime(-1f).forceSolidOn().noTerrainParticles().sound(SprintBlockSoundGroup.WALL));

    public static final Block WALL_BLOCK_2 = registerBlock("wall_block_2", WallBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).destroyTime(-1f).forceSolidOn().noTerrainParticles().sound(SprintBlockSoundGroup.WALL));


    public static final Block CARPET_BLOCK = registerBlock("carpet_block", CarpetBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.white()).destroyTime(-1f).forceSolidOn().noTerrainParticles().sound(SprintBlockSoundGroup.CARPET));

    public static final Block CEILING_TILE = registerBlock("ceiling_tile", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).destroyTime(-1f).forceSolidOn().noTerrainParticles().sound(SprintBlockSoundGroup.CEILING_TILE));

    public static final Block GHOST_CEILING_TILE = registerBlock("ghost_ceiling_tile", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).destroyTime(-1f).forceSolidOn().noTerrainParticles().noCollision());


    public static final Block FLUORESCENT_LIGHT = registerBlock("fluorescent_light", FluorescentLightBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).destroyTime(-1f).noTerrainParticles());


    public static final Block WALL_ARROW_1 = registerBlock("arrow1", WallText::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block WALL_ARROW_2 = registerBlock("arrow2", WallText::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block WALL_ARROW_3 = registerBlock("arrow3", WallText::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block WALL_ARROW_4 = registerBlock("arrow4", WallText::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));


    public static final Block WALL_SMALL_1 = registerBlock("wall_small_1", WallText::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block WALL_SMALL_2 = registerBlock("wall_small_2", WallText::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block WALL_DRAWING_DOOR = registerBlock("wall_drawing_door", WallText::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block WALL_DRAWING_WINDOW = registerBlock("wall_drawing_window", WallText::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block drawingMarker = registerBlock("drawing_marker", DrawingMarker::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles());


    public static final Block RUG_1 = registerBlock("rug1", Rug::new, BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.white()).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block RUG_2 = registerBlock("rug2", Rug::new, BlockBehaviour.Properties.ofFullCopy(Blocks.WOOL.white()).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block CHAINFENCE = registerBlock("chain_fence", ChainFence::new, BlockBehaviour.Properties.ofFullCopy(Blocks.IRON_BLOCK).destroyTime(-1f).noTerrainParticles());

    public static final Block NEWSTAIRS = registerBlock("new_stairs", NewStairs::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().sound(SprintBlockSoundGroup.CONCRETE));

    public static final Block BRICKS = registerBlock("bricks", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles());



    //////Level 2 and 3 Stuff//////
    public static final Block THIN_FLUORESCENT_LIGHT = registerBlock("thin_fluorescent_light", ThinFluorescentLightBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).destroyTime(-1f).noTerrainParticles());

    public static final Block EMERGENCY_LIGHT = registerBlock("emergency_light", EmergencyLightBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).destroyTime(-1f).noTerrainParticles());

    public static final Block BOTTOM_TRIM = registerBlock("bottom_trim", BottomTrim::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noCollision().noOcclusion().noTerrainParticles().sound(SprintBlockSoundGroup.SILENT));

    public static final Block CONCRETE_BLOCK_1 = registerBlock("concrete1", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).forceSolidOn().noTerrainParticles());

    public static final Block CONCRETE_BLOCK_2 = registerBlock("concrete2", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).forceSolidOn().noTerrainParticles());

    public static final Block CONCRETE_BLOCK_5 = registerBlock("concrete5", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).forceSolidOn().noTerrainParticles());

    public static final Block CONCRETE_BLOCK_6 = registerBlock("concrete6", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).forceSolidOn().noTerrainParticles().sound(SprintBlockSoundGroup.CONCRETE));

    public static final Block CONCRETE_BLOCK_7 = registerBlock("concrete7", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).forceSolidOn().noTerrainParticles());

    public static final Block CONCRETE_BLOCK_9 = registerBlock("concrete9", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).forceSolidOn().noTerrainParticles());

    public static final Block CONCRETE_BLOCK_9_SLAB = registerBlock("concrete9slab", SlabBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).forceSolidOn().noTerrainParticles());

    public static final Block CONCRETE_BLOCK_10 = registerBlock("concrete10", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).forceSolidOn().noTerrainParticles());

    public static final Block CONCRETE_BLOCK_11 = registerBlock("concrete11", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).forceSolidOn().noTerrainParticles());

    public static final Block CONCRETE_BLOCK_12 = registerBlock("concrete12", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).forceSolidOn().noTerrainParticles());


    public static final Block THIN_PIPE = registerBlock("thin_pipe", ThinPipe::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block THIN_PIPE_CORNER = registerBlock("thin_pipe_corner", ThinPipeCorner::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block PIPE = registerBlock("pipe", Pipe::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().sound(SprintBlockSoundGroup.SILENT));

    public static final Block PIPE_MIDDLE = registerBlock("pipe_middle", Pipe::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().sound(SprintBlockSoundGroup.SILENT));

    public static final Block PIPE_CORNER = registerBlock("pipe_corner", PipeCorner::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().sound(SprintBlockSoundGroup.SILENT));

    public static final Block BIG_PIPE = registerBlock("big_pipe", Pipe::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().sound(SprintBlockSoundGroup.SILENT));

    public static final Block BIG_PIPE_MIDDLE = registerBlock("big_pipe_middle", Pipe::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().sound(SprintBlockSoundGroup.SILENT));

    public static final Block SMALL_PIPE_SET = registerBlock("small_pipe_set", SmallPipeSet::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));


    public static final Block WALL_TEXT_1 = registerBlock("wall_text_1", WallText::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block WALL_TEXT_2 = registerBlock("wall_text_2", WallText::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block WALL_TEXT_3 = registerBlock("wall_text_3", WallText::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block WALL_TEXT_4 = registerBlock("wall_text_4", WallText::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block WALL_TEXT_5 = registerBlock("wall_text_5", WallText::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block WALL_TEXT_6 = registerBlock("wall_text_6", WallText::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block WALL_TEXT_7 = registerBlock("wall_text_7", WallText::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block WALL_TEXT_8 = registerBlock("wall_text_8", WallText::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));

    public static final Block WALL_TEXT_99 = registerBlock("wall_text_99", WallText::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().noCollision().sound(SprintBlockSoundGroup.SILENT));


    public static final Block WOODEN_CRATE = registerBlock("wooden_crate", WoodenCrate::new, BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).destroyTime(-1f).forceSolidOn().noTerrainParticles());



    //////Poolrooms Stuff//////
    public static final Block POOLROOMS_SKY_BLOCK = registerBlock("pool_sky", TransparentBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).destroyTime(-1f).noTerrainParticles().sound(SprintBlockSoundGroup.SILENT));

    public static final Block POOL_TILES = registerBlock("pool_tiles", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).forceSolidOn().noTerrainParticles().sound(SprintBlockSoundGroup.CONCRETE));

    public static final Block POOL_TILE_WALL = registerBlock("pool_tile_wall", PoolTileWall::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).forceSolidOn().noTerrainParticles());

    public static final Block POOL_TILE_SLOPE = registerBlock("slope", PoolTileSlopeBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.STONE).destroyTime(-1f).noTerrainParticles().noOcclusion().sound(SprintBlockSoundGroup.CONCRETE));

    public static final Block CEILINGLIGHT = registerBlock("ceiling_light", CeilingLight::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).destroyTime(-1f).noTerrainParticles());


    ///////Infinite Fields Stuff//////
    public static final Block POWER_POLE_TOP = registerBlock("power_pole_top", props -> new UtilityPole(props, 5), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).destroyTime(-1f).noTerrainParticles());

    public static final Block POWER_POLE = registerBlock("power_pole", props -> new UtilityPole(props, 5), BlockBehaviour.Properties.ofFullCopy(Blocks.OAK_PLANKS).destroyTime(-1f).noTerrainParticles());

    public static final Block DIRT = registerBlock("dirt", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK).destroyTime(-1f).noTerrainParticles().sound(SprintBlockSoundGroup.GRASS2));


    ///////Level 324 Stuff//////
    public static final Block ROAD = registerBlock("road", Block::new, BlockBehaviour.Properties.ofFullCopy(ModBlocks.CONCRETE_BLOCK_11).destroyTime(-1f).noTerrainParticles().sound(SprintBlockSoundGroup.CONCRETE));

    public static final Block RED_DIRT = registerBlock("red_dirt", Block::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GRASS_BLOCK).destroyTime(-1f).noTerrainParticles().sound(SprintBlockSoundGroup.GRASS2));

    public static final Block PLASTIC = registerBlock("plastic", Block::new, BlockBehaviour.Properties.ofFullCopy(ModBlocks.CONCRETE_BLOCK_9).destroyTime(-1f).noTerrainParticles().sound(SprintBlockSoundGroup.SHROOMLIGHT));

    public static final Block NONE_REFLECTIVE_PLASTIC = registerBlock("none_reflective_plastic", Block::new, BlockBehaviour.Properties.ofFullCopy(ModBlocks.CONCRETE_BLOCK_9).destroyTime(-1f).noTerrainParticles().sound(SprintBlockSoundGroup.SHROOMLIGHT));

    public static final Block RED_METAL_CASING = registerBlock("red_metal_casing", RedMetalCasingBlock::new, BlockBehaviour.Properties.ofFullCopy(ModBlocks.CONCRETE_BLOCK_9).destroyTime(-1f).noTerrainParticles().sound(SprintBlockSoundGroup.METAL));

    public static final Block PILLAR = registerBlock("pillar", PillarBlock::new, BlockBehaviour.Properties.ofFullCopy(ModBlocks.CONCRETE_BLOCK_9).destroyTime(-1f).noTerrainParticles().sound(SprintBlockSoundGroup.SHROOMLIGHT));

    public static final Block POLE = registerBlock("pole", props -> new UtilityPole(props, 6), BlockBehaviour.Properties.ofFullCopy(ModBlocks.CONCRETE_BLOCK_9).destroyTime(-1f).noTerrainParticles().sound(SprintBlockSoundGroup.SHROOMLIGHT));

    public static final Block LAMP = registerBlock("lamp", LampBlock::new, BlockBehaviour.Properties.ofFullCopy(ModBlocks.CONCRETE_BLOCK_9).destroyTime(-1f).noTerrainParticles().sound(SprintBlockSoundGroup.METAL));

    public static final Block WINDOW = registerBlock("window", WindowBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).destroyTime(-1f).noTerrainParticles().sound(SprintBlockSoundGroup.GLASS));

    public static final Block TINY_FLUORESCENT_LIGHT = registerBlock("tiny_fluorescent_light", TinyFluorescentLightBlock::new, BlockBehaviour.Properties.ofFullCopy(Blocks.GLASS).destroyTime(-1f).noTerrainParticles());

    public static final Block FLOOR_TILING = registerBlock("floor_tiling", Block::new, BlockBehaviour.Properties.ofFullCopy(ModBlocks.CONCRETE_BLOCK_11).destroyTime(-1f).noTerrainParticles().sound(SprintBlockSoundGroup.CONCRETE));

    public static final Block DOUBLE_SIDED_SHELF = registerBlock("double_sided_shelf", DoubleSidedShelfBlock::new, BlockBehaviour.Properties.ofFullCopy(ModBlocks.CONCRETE_BLOCK_11).destroyTime(-1f).noTerrainParticles().sound(SprintBlockSoundGroup.CONCRETE));

    public static final Block ONE_SIDED_SHELF = registerBlock("one_sided_shelf", DoubleSidedShelfBlock::new, BlockBehaviour.Properties.ofFullCopy(ModBlocks.CONCRETE_BLOCK_11).destroyTime(-1f).noTerrainParticles().sound(SprintBlockSoundGroup.WOOD));

    public static final Block PAVEMENT = registerBlock("pavement", Block::new, BlockBehaviour.Properties.ofFullCopy(ModBlocks.CONCRETE_BLOCK_11).destroyTime(-1f).noTerrainParticles().sound(SprintBlockSoundGroup.CONCRETE));


    // 26.2: blocks/items must have their ResourceKey assigned via .setId(...) before construction.
    // The factory receives the id-stamped Properties so each block is built with its registry key set.
    private static Block registerBlock(String name, Function<BlockBehaviour.Properties, Block> factory, BlockBehaviour.Properties settings) {
        ResourceKey<Block> blockKey = ResourceKey.create(Registries.BLOCK, Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, name));
        Block block = factory.apply(settings.setId(blockKey));
        registerBlockItem(name, block);
        return Registry.register(BuiltInRegistries.BLOCK, blockKey.identifier(), block);
    }
    private static Item registerBlockItem(String name, Block block) {
        ResourceKey<Item> itemKey = ResourceKey.create(Registries.ITEM, Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, name));
        return Registry.register(BuiltInRegistries.ITEM, itemKey.identifier(),
                new BlockItem(block, new Item.Properties().setId(itemKey).useBlockDescriptionPrefix()));
    }

    public static void init() {

    }

}
