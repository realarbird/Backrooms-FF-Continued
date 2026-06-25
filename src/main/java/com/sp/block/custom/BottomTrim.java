package com.sp.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.core.Direction;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

@SuppressWarnings("deprecation")
public class BottomTrim extends HorizontalDirectionalBlock {
    public static final MapCodec<BottomTrim> CODEC = simpleCodec(BottomTrim::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.HORIZONTAL_FACING;
    public static final IntegerProperty SIDES = IntegerProperty.create("sides", 1, 4);

    private static final VoxelShape SHAPE_SOUTH = Block.box(0.0, 3.0, 0.0, 16.0, 12.0, 1.0);
    private static final VoxelShape SHAPE_NORTH = Block.box(0.0, 3.0, 15.0, 16.0, 12.0, 16.0);
    private static final VoxelShape SHAPE_WEST = Block.box(15.0, 3.0, 0.0, 16.0, 12.0, 16.0);
    private static final VoxelShape SHAPE_EAST = Block.box(0.0, 3.0, 0.0, 1.0, 12.0, 16.0);


    public BottomTrim(Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState blockState = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (blockState.is(this)) {
            return blockState.setValue(SIDES, Math.min(4, blockState.getValue(SIDES) + 1)).setValue(FACING, blockState.getValue(FACING));
        } else {
            return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
        }
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
            case UP:
            case DOWN:
            case SOUTH:
            default: {
                return Block.box(0.0, 0.0, 0.0, 16.0, 2.0, 1.0);
            }
            case NORTH: {
                return Block.box(0.0, 0.0, 15.0, 16.0, 2.0, 16.0);
            }
            case WEST : {
                return Block.box(15.0, 0.0, 0.0, 16.0, 2.0, 16.0);
            }
            case EAST : {
                return Block.box(0.0, 0.0, 0.0, 1.0, 2.0, 16.0);
            }
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return !context.isSecondaryUseActive() && context.getItemInHand().is(this.asItem()) && state.getValue(SIDES) < 4 || super.canBeReplaced(state, context);
    }

    @Override
    public BlockState mirror(BlockState state, Mirror mirror) {
        return state.rotate(mirror.getRotation(state.getValue(FACING)));
    }

    @Override
    public BlockState rotate(BlockState state, Rotation rotation) {
        return state.setValue(FACING, rotation.rotate(state.getValue(FACING)));
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING);
        builder.add(SIDES);
    }

}
