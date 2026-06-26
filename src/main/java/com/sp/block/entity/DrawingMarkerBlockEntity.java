package com.sp.block.entity;

import com.sp.block.custom.DrawingMarker;
import com.sp.block.custom.WallText;
import com.sp.init.BackroomsLevels;
import com.sp.init.ModBlockEntities;
import com.sp.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class DrawingMarkerBlockEntity extends BlockEntity {
    RandomSource random = RandomSource.create();

    public DrawingMarkerBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.DRAWING_MARKER_BLOCK_ENTITY, pos, state);
    }

    public void tick(Level world, BlockPos pos, BlockState state) {
        BlockState blockState = world.getBlockState(pos);
        int rand = random.nextIntBetweenInclusive(1, 4);
        int rand2 = random.nextIntBetweenInclusive(1, 10);

        if (world.isClientSide()) {
            return;
        }

        if (world.dimension() != BackroomsLevels.LEVEL0_WORLD_KEY) {
            return;
        }

        world.removeBlockEntity(pos);
        // PORT: phase6 - LevelChunk.blockEntityNbts (pending block-entity NBT map) is no longer public in 26.2; removeBlockEntity above suffices
        if (rand2 == 1 && !blockState.getValue(DrawingMarker.TYPE)) {
            switch (rand) {
                case 2:
                    world.setBlockAndUpdate(pos, ModBlocks.WALL_ARROW_2.defaultBlockState().setValue(WallText.FACING, blockState.getValue(DrawingMarker.FACING)));
                    break;
                case 3:
                    world.setBlockAndUpdate(pos, ModBlocks.WALL_ARROW_3.defaultBlockState().setValue(WallText.FACING, blockState.getValue(DrawingMarker.FACING)));
                    break;
                case 4:
                    world.setBlockAndUpdate(pos, ModBlocks.WALL_ARROW_4.defaultBlockState().setValue(WallText.FACING, blockState.getValue(DrawingMarker.FACING)));
                    break;
                default:
                    world.setBlockAndUpdate(pos, ModBlocks.WALL_ARROW_1.defaultBlockState().setValue(WallText.FACING, blockState.getValue(DrawingMarker.FACING)));
                    break;
            }
        } else if (rand2 == 1 && blockState.getValue(DrawingMarker.TYPE)) {
            switch (rand) {
                case 2, 4:
                    world.setBlockAndUpdate(pos, ModBlocks.WALL_SMALL_1.defaultBlockState().setValue(WallText.FACING, blockState.getValue(DrawingMarker.FACING)));
                    break;
                default:
                    world.setBlockAndUpdate(pos, ModBlocks.WALL_SMALL_2.defaultBlockState().setValue(WallText.FACING, blockState.getValue(DrawingMarker.FACING)));
                    break;
            }
        } else {
            world.setBlockAndUpdate(pos, Blocks.AIR.defaultBlockState());
        }
    }
}
