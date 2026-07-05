package com.sp.block.custom;

import com.mojang.serialization.MapCodec;
import com.sp.block.entity.ThinFluorescentLightBlockEntity;
import com.sp.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Mirror;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.Rotation;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.AttachFace;
import net.minecraft.world.level.block.state.properties.BlockStateProperties;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.level.block.state.properties.EnumProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class ThinFluorescentLightBlock extends BaseEntityBlock {
    public static final MapCodec<ThinFluorescentLightBlock> CODEC = simpleCodec(ThinFluorescentLightBlock::new);
    public static final BooleanProperty ON = BooleanProperty.create("on");
    public static final BooleanProperty COPY = BooleanProperty.create("copy");
    public static final BooleanProperty BLACKOUT = BooleanProperty.create("blackout");

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

    public ThinFluorescentLightBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(BLACKOUT, false).setValue(ON, true).setValue(COPY, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Nullable
    @Override
    public BlockState getStateForPlacement(BlockPlaceContext ctx) {
        for (Direction direction : ctx.getNearestLookingDirections()) {
            BlockState blockState;
            if (direction.getAxis() == Direction.Axis.Y) {
                blockState = this.defaultBlockState()
                        .setValue(FACE, direction == Direction.UP ? AttachFace.CEILING : AttachFace.FLOOR)
                        .setValue(FACING, ctx.getHorizontalDirection())
                        .setValue(ON, true)
                        .setValue(BLACKOUT, false)
                        .setValue(COPY, false);
            } else {
                blockState = this.defaultBlockState().setValue(FACE, AttachFace.WALL).setValue(FACING, direction.getOpposite()).setValue(ON, true).setValue(BLACKOUT, false).setValue(COPY, false);
            }

            if (blockState.canSurvive(ctx.getLevel(), ctx.getClickedPos())) {
                return blockState;
            }
        }

        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        switch (state.getValue(FACE)) {
            case FLOOR:
                switch (state.getValue(FACING).getAxis()) {
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
                switch (state.getValue(FACING).getAxis()) {
                    case X:
                        return CEILING_X_AXIS_SHAPE;
                    case Z:
                    default:
                        return CEILING_Z_AXIS_SHAPE;
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
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(FACING, FACE, ON, COPY, BLACKOUT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new ThinFluorescentLightBlockEntity(pos, state);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        // PORT FIX: lit look now comes from the blockstate JSON (Veil emissive BER dropped) — see
        // FluorescentLightBlock.getRenderShape.
        return RenderShape.MODEL;
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.THIN_FLUORESCENT_LIGHT_BLOCK_ENTITY, (world1, pos, state1, blockEntity) -> blockEntity.tick(world1, pos, state1));
    }

}
