package com.sp.block.custom;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.core.Direction;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;

public class WindowBlock extends Block {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;

    private static final double[] SHAPE = new double[] {0.4, 0, 0, 0.5, 1, 1};

    public WindowBlock(Properties settings) {
        super(settings);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return switch (state.getValue(FACING)) {
            case NORTH -> Shapes.box(SHAPE[0], SHAPE[1], SHAPE[2], SHAPE[3], SHAPE[4], SHAPE[5]);
            case SOUTH -> Shapes.box(SHAPE[0], SHAPE[1], 1 - SHAPE[5], SHAPE[3], SHAPE[4], 1 - SHAPE[2]);
            case WEST -> Shapes.box(SHAPE[2], SHAPE[1], SHAPE[0], SHAPE[5], SHAPE[4], SHAPE[3]);
            case EAST -> Shapes.box(1 - SHAPE[5], SHAPE[1], SHAPE[0], 1 - SHAPE[2], SHAPE[4], SHAPE[3]);
            default -> Shapes.block();
        };
    }

    @Override
    public boolean propagatesSkylightDown(BlockState state) {
        return true;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection());
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
