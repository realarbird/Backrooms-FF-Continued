package com.sp.block.custom.pipes;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.DirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class Pipe extends DirectionalBlock {
    public static final MapCodec<Pipe> CODEC = simpleCodec(Pipe::new);

    private static final VoxelShape SHAPE_EAST = Block.box(0.0, 2.0, 2.0, 16.0, 16.0, 14.0);
    private static final VoxelShape SHAPE_WEST = Block.box(0.0, 2.0, 2.0, 16.0, 16.0, 14.0);
    private static final VoxelShape SHAPE_NORTH = Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 16.0);
    private static final VoxelShape SHAPE_SOUTH = Block.box(2.0, 2.0, 0.0, 14.0, 14.0, 16.0);
    private static final VoxelShape VERTICAL = Block.box(2.0, 0.0, 2.0, 14.0, 16.0, 14.0);

    public Pipe(Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends DirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
            case UP, DOWN -> {
                return VERTICAL;
            }
            case NORTH -> {
                return SHAPE_NORTH;
            }
            case WEST -> {
                return SHAPE_WEST;
            }
            case EAST -> {
                return SHAPE_EAST;
            }
            default -> {
                return SHAPE_SOUTH;
            }
        }
    }


    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getNearestLookingDirection().getOpposite());
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
