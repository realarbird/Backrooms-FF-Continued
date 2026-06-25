package com.sp.block.custom;

import com.mojang.serialization.MapCodec;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.phys.shapes.Shapes;
import net.minecraft.world.level.BlockGetter;

@SuppressWarnings("deprecation")
public class PoolTileWall extends HorizontalDirectionalBlock {
    public static final MapCodec<PoolTileWall> CODEC = simpleCodec(PoolTileWall::new);
    public static final IntegerProperty TYPE = IntegerProperty.create("type", 0, 1);

    public PoolTileWall(Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACING)) {
            case UP:
            case DOWN:
            case SOUTH:
            default: {
                switch (state.getValue(TYPE)){
                    case 1: return Shapes.or(
                            Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 6.4),
                            Block.box(0.0, 0.0, 0.0, 6.4, 16.0, 16.0)
                    );
                    default: return Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 6.4);
                }
            }
            case NORTH: {
                switch (state.getValue(TYPE)){
                    case 1: return Shapes.or(
                            Block.box(0.0, 0.0, 9.6, 16.0, 16.0, 16.0),
                            Block.box(9.6, 0.0, 0.0, 16.0, 16.0, 16.0)
                    );
                    default: return Block.box(0.0, 0.0, 9.6, 16.0, 16.0, 16.0);
                }

            }
            case WEST : {
                switch (state.getValue(TYPE)){
                    case 1: return Shapes.or(
                            Block.box(9.6, 0.0, 0.0, 16.0, 16.0, 16.0),
                            Block.box(0.0, 0.0, 0.0, 16.0, 16.0, 6.4)
                    );
                    default: return Block.box(9.6, 0.0, 0.0, 16.0, 16.0, 16.0);
                }
            }
            case EAST : {
                switch (state.getValue(TYPE)){
                    case 1: return Shapes.or(
                            Block.box(0.0, 0.0, 0.0, 6.4, 16.0, 16.0),
                            Block.box(0.0, 0.0, 9.6, 16.0, 16.0, 16.0)
                    );
                    default: return Block.box(0.0, 0.0, 0.0, 6.4, 16.0, 16.0);
                }
            }
        }
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState blockState = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (blockState.is(this)) {
            return blockState.setValue(TYPE, Math.min(1, blockState.getValue(TYPE) + 1)).setValue(FACING, blockState.getValue(FACING));
        } else {
            return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
        }
    }

    @Override
    public boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return !context.isSecondaryUseActive() && context.getItemInHand().is(this.asItem()) && state.getValue(TYPE) < 1 || super.canBeReplaced(state, context);
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
        builder.add(FACING, TYPE);
    }
}
