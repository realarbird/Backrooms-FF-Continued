package com.sp.item;

import com.sp.SPBRevamped;
import com.sp.init.ModBlocks;
import com.sp.init.ModItems;
import net.fabricmc.fabric.api.creativetab.v1.FabricCreativeModeTab;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import net.minecraft.world.item.CreativeModeTab;
import net.minecraft.world.item.ItemStack;

public class ModItemGroups {
    public static final CreativeModeTab BACKROOMS_GROUP = Registry.register(BuiltInRegistries.CREATIVE_MODE_TAB,
            Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "spbrevamped"),
            FabricCreativeModeTab.builder().title(Component.translatable("itemgroup.spbrevamped"))
                    .icon(() -> new ItemStack(ModBlocks.WALL_BLOCK)).displayItems((displayContext, entries) -> {
                        entries.accept(ModBlocks.VOID_BLOCK);
                        entries.accept(ModBlocks.CEILINGLIGHT);
                        entries.accept(ModBlocks.EMERGENCY_LIGHT);
                        entries.accept(ModItems.BACKSHROOM);
                        entries.accept(ModItems.CANNED_FOOD);
                        entries.accept(ModBlocks.WALL_BLOCK);
                        entries.accept(ModBlocks.WALL_BLOCK_2);
                        entries.accept(ModBlocks.CEILING_TILE);
                        entries.accept(ModBlocks.GHOST_CEILING_TILE);
                        entries.accept(ModBlocks.CARPET_BLOCK);

                        entries.accept(ModBlocks.FLUORESCENT_LIGHT);
                        entries.accept(ModBlocks.THIN_FLUORESCENT_LIGHT);

                        entries.accept(ModBlocks.WOODEN_CRATE);
                        entries.accept(ModBlocks.CHAINFENCE);
                        entries.accept(ModBlocks.NEWSTAIRS);
                        entries.accept(ModBlocks.BOTTOM_TRIM);
                        entries.accept(ModBlocks.CONCRETE_BLOCK_1);
                        entries.accept(ModBlocks.CONCRETE_BLOCK_2);
                        entries.accept(ModBlocks.CONCRETE_BLOCK_5);
                        entries.accept(ModBlocks.CONCRETE_BLOCK_6);
                        entries.accept(ModBlocks.CONCRETE_BLOCK_7);
                        entries.accept(ModBlocks.CONCRETE_BLOCK_9);
                        entries.accept(ModBlocks.CONCRETE_BLOCK_9_SLAB);
                        entries.accept(ModBlocks.CONCRETE_BLOCK_10);
                        entries.accept(ModBlocks.CONCRETE_BLOCK_11);
                        entries.accept(ModBlocks.CONCRETE_BLOCK_12);
                        entries.accept(ModBlocks.BRICKS);

                        entries.accept(ModBlocks.THIN_PIPE);
                        entries.accept(ModBlocks.THIN_PIPE_CORNER);
                        entries.accept(ModBlocks.PIPE);
                        entries.accept(ModBlocks.PIPE_MIDDLE);
                        entries.accept(ModBlocks.BIG_PIPE);
                        entries.accept(ModBlocks.BIG_PIPE_MIDDLE);
                        entries.accept(ModBlocks.SMALL_PIPE_SET);
                        entries.accept(ModBlocks.PIPE_CORNER);

                        entries.accept(ModBlocks.WALL_TEXT_1);
                        entries.accept(ModBlocks.WALL_TEXT_2);
                        entries.accept(ModBlocks.WALL_TEXT_3);
                        entries.accept(ModBlocks.WALL_TEXT_4);
                        entries.accept(ModBlocks.WALL_TEXT_5);
                        entries.accept(ModBlocks.WALL_TEXT_6);
                        entries.accept(ModBlocks.WALL_TEXT_7);
                        entries.accept(ModBlocks.WALL_TEXT_8);
                        entries.accept(ModBlocks.WALL_TEXT_99);

                        entries.accept(ModBlocks.WALL_ARROW_1);
                        entries.accept(ModBlocks.WALL_ARROW_2);
                        entries.accept(ModBlocks.WALL_ARROW_3);
                        entries.accept(ModBlocks.WALL_ARROW_4);
                        entries.accept(ModBlocks.WALL_SMALL_1);
                        entries.accept(ModBlocks.WALL_SMALL_2);
                        entries.accept(ModBlocks.WALL_DRAWING_DOOR);
                        entries.accept(ModBlocks.WALL_DRAWING_WINDOW);

                        entries.accept(ModBlocks.RUG_1);
                        entries.accept(ModBlocks.RUG_2);

                        entries.accept(ModBlocks.POOLROOMS_SKY_BLOCK);
                        entries.accept(ModBlocks.POOL_TILES);
                        entries.accept(ModBlocks.POOL_TILE_WALL);
                        entries.accept(ModBlocks.POOL_TILE_SLOPE);

                        entries.accept(ModBlocks.POWER_POLE_TOP);
                        entries.accept(ModBlocks.POWER_POLE);
                        entries.accept(ModBlocks.DIRT);

                        entries.accept(ModBlocks.ROAD);
                        entries.accept(ModBlocks.RED_DIRT);
                        entries.accept(ModBlocks.PLASTIC);
                        entries.accept(ModBlocks.NONE_REFLECTIVE_PLASTIC);
                        entries.accept(ModBlocks.RED_METAL_CASING);
                        entries.accept(ModBlocks.PILLAR);
                        entries.accept(ModBlocks.POLE);
                        entries.accept(ModBlocks.LAMP);
                        entries.accept(ModBlocks.WINDOW);
                        entries.accept(ModBlocks.TINY_FLUORESCENT_LIGHT);
                        entries.accept(ModBlocks.FLOOR_TILING);
                        entries.accept(ModBlocks.DOUBLE_SIDED_SHELF);
                        entries.accept(ModBlocks.ONE_SIDED_SHELF);
                        entries.accept(ModBlocks.PAVEMENT);


                    }).build());




    public static void registerItemGroups() {
        SPBRevamped.LOGGER.info("Registering Item Groups");
    }
}
