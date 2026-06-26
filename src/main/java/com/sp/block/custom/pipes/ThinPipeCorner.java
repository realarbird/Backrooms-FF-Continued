package com.sp.block.custom.pipes;

import com.mojang.serialization.MapCodec;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.HorizontalDirectionalBlock;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.level.block.state.properties.IntegerProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;

@SuppressWarnings("deprecation")
public class ThinPipeCorner extends HorizontalDirectionalBlock {
    public static final MapCodec<ThinPipeCorner> CODEC = simpleCodec(ThinPipeCorner::new);
    public static final EnumProperty<Direction> FACING = BlockStateProperties.FACING;
    public static final IntegerProperty TYPE = IntegerProperty.create("type", 0, 1);

    private static final VoxelShape SHAPE_SOUTH = Block.box(0.0, 0.0, 0.0, 15.0, 16.0, 1.0);
    private static final VoxelShape SHAPE_NORTH = Block.box(0.0, 0.0, 15.0, 15.0, 16.0, 16.0);
    private static final VoxelShape SHAPE_EAST = Block.box(0.0, 0.0, 0.0, 1.0, 16.0, 15.0);
    private static final VoxelShape SHAPE_WEST = Block.box(15.0, 0.0, 0.0, 16.0, 16.0, 15.0);
    private static final VoxelShape CEILING = Block.box(0.0, 15.0, 0.0, 15.0, 16.0, 15.0);

    public ThinPipeCorner(Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends HorizontalDirectionalBlock> codec() {
        return CODEC;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return CEILING;
    }

    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        BlockState blockState = ctx.getLevel().getBlockState(ctx.getClickedPos());
        if (blockState.getBlock() == this) {
            return blockState.setValue(TYPE, Math.min(1, blockState.getValue(TYPE) + 1)).setValue(FACING, blockState.getValue(FACING));
        } else {
            return this.defaultBlockState().setValue(FACING, ctx.getHorizontalDirection().getOpposite());
        }
    }

    @Override
    protected boolean canBeReplaced(BlockState state, BlockPlaceContext context) {
        return !context.isSecondaryUseActive() && context.getItemInHand().getItem() == this.asItem() && state.getValue(TYPE) < 1 || super.canBeReplaced(state, context);
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
