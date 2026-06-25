package com.sp.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;

public class NewStairs extends HorizontalDirectionalBlock {
    public static final MapCodec<NewStairs> CODEC = simpleCodec(NewStairs::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;


    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
            Block.box(0.0, 6.0, 5.0, 16.0, 11.0, 11.0),
            Block.box(0.0, 6.0, 11.0, 16.0, 16.0, 16.0)
    );
    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
            Block.box(0.0, 6.0, 5.0, 16.0, 11.0, 11.0),
            Block.box(0.0, 6.0, 0.0, 16.0, 16.0, 5.0)
    );
    private static final VoxelShape SHAPE_WEST = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
            Block.box(5.0, 6.0, 0.0, 11.0, 11.0, 16.0),
            Block.box(0.0, 6.0, 0.0, 5.0, 16.0, 16.0)
    );
    private static final VoxelShape SHAPE_EAST = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 6.0, 16.0),
            Block.box(5.0, 6.0, 0.0, 11.0, 11.0, 16.0),
            Block.box(11.0, 6.0, 0.0, 16.0, 16.0, 16.0)
    );

    public NewStairs(Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }


    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch ((Direction) state.getValue(FACING)) {
            case UP :
            case DOWN :
            case SOUTH :
            default :
                return SHAPE_NORTH;
            case NORTH :
                return SHAPE_SOUTH;
            case WEST :
                return SHAPE_EAST;
            case EAST :
                return SHAPE_WEST;
        }
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
    }
    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
    }
}
