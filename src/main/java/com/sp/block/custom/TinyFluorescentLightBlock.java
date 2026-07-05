package com.sp.block.custom;

import com.mojang.serialization.MapCodec;
import com.sp.block.entity.TinyFluorescentLightBlockEntity;
import com.sp.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.entity.BlockEntityTicker;
import net.minecraft.world.level.block.entity.BlockEntityType;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.state.StateDefinition;
import net.minecraft.world.level.block.state.properties.BooleanProperty;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.jetbrains.annotations.Nullable;

@SuppressWarnings("deprecation")
public class TinyFluorescentLightBlock extends BaseEntityBlock {
    public static final MapCodec<TinyFluorescentLightBlock> CODEC = simpleCodec(TinyFluorescentLightBlock::new);
    public static final BooleanProperty ON = BooleanProperty.create("on");
    public static final BooleanProperty COPY = BooleanProperty.create("copy");
    public static final BooleanProperty BLACKOUT = BooleanProperty.create("blackout");

    private static final VoxelShape FLOOR_X_AXIS_SHAPE = Block.box(6.0, 14.0, 6.0, 10.0, 16.0, 10.0);

    public TinyFluorescentLightBlock(Properties settings) {
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
        BlockState blockState;
        blockState = this.defaultBlockState()
                .setValue(ON, true)
                .setValue(BLACKOUT, false)
                .setValue(COPY, false);
        if (blockState.canSurvive(ctx.getLevel(), ctx.getClickedPos())) {
            return blockState;
        }

        return null;
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return FLOOR_X_AXIS_SHAPE;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ON, COPY, BLACKOUT);
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new TinyFluorescentLightBlockEntity(pos, state);
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
        return createTickerHelper(type, ModBlockEntities.TINY_FLUORESCENT_LIGHT_BLOCK_ENTITY, (world1, pos, state1, blockEntity) -> blockEntity.tick(world1, pos, state1));
    }
}
