package com.sp.block.custom;

import com.mojang.serialization.MapCodec;
import com.sp.block.entity.FluorescentLightBlockEntity;
import com.sp.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.world.item.context.BlockPlaceContext;
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
import org.jetbrains.annotations.Nullable;

public class FluorescentLightBlock extends BaseEntityBlock {
    public static final MapCodec<FluorescentLightBlock> CODEC = simpleCodec(FluorescentLightBlock::new);
    public static final BooleanProperty ON = BooleanProperty.create("on");
    public static final BooleanProperty COPY = BooleanProperty.create("copy");
    public static final BooleanProperty BLACKOUT = BooleanProperty.create("blackout");


    public FluorescentLightBlock(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(BLACKOUT, false).setValue(ON, true).setValue(COPY, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockState getStateForPlacement(BlockPlaceContext ctx) {
        return this.defaultBlockState().setValue(BLACKOUT, false).setValue(ON, true).setValue(COPY, false);
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        if(state.getValue(BLACKOUT) || !state.getValue(ON)){
            return RenderShape.MODEL;
        }
        else {
            return RenderShape.INVISIBLE;
        }
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.FLUORESCENT_LIGHT_BLOCK_ENTITY, (world1, pos, state1, blockEntity) -> blockEntity.tick(world1, pos, state1));
    }

    @Nullable
    @Override
    public BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new FluorescentLightBlockEntity(pos, state);
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(ON, COPY, BLACKOUT);
    }
}
