package com.sp.block.custom.pipes;

import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class ThinPipe extends Block {
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final EnumProperty<AttachFace> FACE = BlockStateProperties.ATTACH_FACE;

    private static final VoxelShape FLOOR_X_AXIS_SHAPE = Block.box(0.0, 0.0, 6.0, 16.0, 2.0, 10.0);
    private static final VoxelShape FLOOR_Z_AXIS_SHAPE = Block.box(6.0, 0.0, 0.0, 10.0, 2.0, 16.0);
    private static final VoxelShape EAST_WALL_SHAPE = Block.box(0.0, 0.0, 6.0, 2.0, 16.0, 10.0);
    private static final VoxelShape WEST_WALL_SHAPE = Block.box(14.0, 0.0, 6.0, 16.0, 16.0, 10.0);
    private static final VoxelShape NORTH_WALL_SHAPE = Block.box(6.0, 0.0, 14.0, 10.0, 16.0, 16.0);
    private static final VoxelShape SOUTH_WALL_SHAPE = Block.box(6.0, 0.0, 0.0, 10.0, 16.0, 2.0);
    private static final VoxelShape CEILING_X_AXIS_SHAPE = Block.box(0.0, 14.0, 6.0, 16.0, 16.0, 10.0);
    private static final VoxelShape CEILING_Z_AXIS_SHAPE = Block.box(6.0, 14.0, 0.0, 10.0, 16.0, 16.0);

    public ThinPipe(Properties settings) {
        super(settings);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACE)) {
            case FLOOR:
                switch ((state.getValue(FACING)).getAxis()) {
                    case X:
                        return FLOOR_X_AXIS_SHAPE;
                    case Z:
                    default:
                        return FLOOR_Z_AXIS_SHAPE;
                }
            case WALL:
                switch (state.getValue(FACING)) {
                    case EAST:
                        return EAST_WALL_SHAPE;
                    case WEST:
                        return WEST_WALL_SHAPE;
                    case SOUTH:
                        return SOUTH_WALL_SHAPE;
                    case NORTH:
                    default:
                        return NORTH_WALL_SHAPE;
                }
            case CEILING:
            default:
                switch ((state.getValue(FACING)).getAxis()) {
                    case X:
                        return CEILING_X_AXIS_SHAPE;
                    case Z:
                    default:
                        return CEILING_Z_AXIS_SHAPE;
                }
        }
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        for (Direction direction : ctx.getNearestLookingDirections()) {
            BlockState blockState;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockState = this.defaultBlockState()
                        .setValue(FACE, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR)
                        .setValue(FACING, ctx.getHorizontalDirection());
            } else {
                blockState = this.defaultBlockState().setValue(FACE, AttachFace.WALL).setValue(FACING, direction.getOpposite());
            }

            if (blockState.canSurvive(ctx.getLevel(), ctx.getClickedPos())) {
                return blockState;
            }
        }

        return null;
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE);
    }
}
