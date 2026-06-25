package com.sp.block.custom;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.SimpleWaterloggedBlock;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.level.material.Fluids;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.Half;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.RandomSource;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.LevelReader;
import net.minecraft.world.level.ScheduledTickAccess;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class PoolTileSlopeBlock extends Block implements SimpleWaterloggedBlock {
    public static final EnumProperty<Direction> FACING = HorizontalDirectionalBlock.FACING;
    public static final EnumProperty<Half> HALF = BlockStateProperties.HALF;
    public static final BooleanProperty WATERLOGGED = BlockStateProperties.WATERLOGGED;

    private static final VoxelShape SHAPE_NORTH = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.box(0.0, 4.0, 4.0, 16.0, 8.0, 16.0),
            Block.box(0.0, 8.0, 8.0, 16.0, 12.0, 16.0),
            Block.box(0.0, 12.0, 12.0, 16.0, 16.0, 16.0)
    );
    private static final VoxelShape SHAPE_SOUTH = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.box(0.0, 4.0, 0.0, 16.0, 8.0, 12.0),
            Block.box(0.0, 8.0, 0.0, 16.0, 12.0, 8.0),
            Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 4.0)
    );
    private static final VoxelShape SHAPE_WEST = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.box(4.0, 4.0, 0.0, 16.0, 8.0, 16.0),
            Block.box(8.0, 8.0, 0.0, 16.0, 12.0, 16.0),
            Block.box(12.0, 12.0, 0.0, 16.0, 16.0, 16.0)
    );
    private static final VoxelShape SHAPE_EAST = Shapes.or(
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 16.0),
            Block.box(0.0, 4.0, 0.0, 12.0, 8.0, 16.0),
            Block.box(0.0, 8.0, 0.0, 8.0, 12.0, 16.0),
            Block.box(0.0, 12.0, 0.0, 4.0, 16.0, 16.0)
    );

    private static final VoxelShape SHAPE_TOP_NORTH = Shapes.or(
            Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0),
            Block.box(0.0, 8.0, 4.0, 16.0, 12.0, 16.0),
            Block.box(0.0, 4.0, 8.0, 16.0, 8.0, 16.0),
            Block.box(0.0, 0.0, 12.0, 16.0, 4.0, 16.0)
    );
    private static final VoxelShape SHAPE_TOP_SOUTH = Shapes.or(
            Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0),
            Block.box(0.0, 8.0, 0.0, 16.0, 12.0, 12.0),
            Block.box(0.0, 4.0, 0.0, 16.0, 8.0, 8.0),
            Block.box(0.0, 0.0, 0.0, 16.0, 4.0, 4.0)
    );
    private static final VoxelShape SHAPE_TOP_WEST = Shapes.or(
            Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0),
            Block.box(4.0, 8.0, 0.0, 16.0, 12.0, 16.0),
            Block.box(8.0, 4.0, 0.0, 16.0, 8.0, 16.0),
            Block.box(12.0, 0.0, 0.0, 16.0, 4.0, 16.0)
    );
    private static final VoxelShape SHAPE_TOP_EAST = Shapes.or(
            Block.box(0.0, 12.0, 0.0, 16.0, 16.0, 16.0),
            Block.box(0.0, 8.0, 0.0, 12.0, 12.0, 16.0),
            Block.box(0.0, 4.0, 0.0, 8.0, 8.0, 16.0),
            Block.box(0.0, 0.0, 0.0, 4.0, 4.0, 16.0)
    );



    public PoolTileSlopeBlock(Properties settings) {
        super(settings);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch (state.getValue(HALF)){
            case TOP:{
                switch (state.getValue(FACING)) {
                    case UP:
                    case DOWN:
                    case SOUTH:
                    default: return SHAPE_TOP_SOUTH;
                    case NORTH: return SHAPE_TOP_NORTH;
                    case WEST : return SHAPE_TOP_WEST;
                    case EAST : return SHAPE_TOP_EAST;
                }
            }
            case BOTTOM:
            default: {
                switch (state.getValue(FACING)) {
                    case UP:
                    case DOWN:
                    case SOUTH:
                    default: return SHAPE_SOUTH;
                    case NORTH: return SHAPE_NORTH;
                    case WEST : return SHAPE_WEST;
                    case EAST : return SHAPE_EAST;
                }
            }
        }
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        Direction direction = ctx.getClickedFace();
        BlockPos blockPos = ctx.getClickedPos();
        return this.defaultBlockState()
                .setValue(FACING, ctx.getHorizontalDirection().getOpposite())
                .setValue(HALF, direction != Direction.DOWN && (direction == Direction.UP || !(ctx.getClickLocation().y - (double)blockPos.getY() > 0.5)) ? Half.BOTTOM : Half.TOP)
                .setValue(WATERLOGGED,ctx.getLevel().getFluidState(ctx.getClickedPos()).is(Fluids.WATER));
    }

    @Override
    public FluidState getFluidState(BlockState state) {
        return state.getValue(WATERLOGGED) ? Fluids.WATER.getSource(false) : super.getFluidState(state);
    }

    @Override
    public BlockState updateShape(BlockState state, LevelReader world, ScheduledTickAccess tickAccess, BlockPos pos, Direction direction, BlockPos neighborPos, BlockState neighborState, RandomSource random) {
        if (state.getValue(WATERLOGGED)) {
            tickAccess.scheduleTick(pos, Fluids.WATER, Fluids.WATER.getTickDelay(world));
        }

        return super.updateShape(state, world, tickAccess, pos, direction, neighborPos, neighborState, random);
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
    public float getShadeBrightness(BlockState state, BlockGetter world, BlockPos pos) {
        return 1.0F;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, HALF, WATERLOGGED);
    }

}
