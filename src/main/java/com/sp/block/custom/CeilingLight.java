package com.sp.block.custom;

import com.mojang.serialization.MapCodec;
import com.sp.block.entity.CeilingLightBlockEntity;
import com.sp.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
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

public class CeilingLight extends BaseEntityBlock {
    public static final MapCodec<CeilingLight> CODEC = simpleCodec(CeilingLight::new);
    public static final BooleanProperty STOPPED = BooleanProperty.create("stopped");

    public CeilingLight(Properties settings) {
        super(settings);
        this.registerDefaultState(this.defaultBlockState().setValue(STOPPED, false));
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new CeilingLightBlockEntity(pos, state);
    }

    @Nullable
    @Override
    public <T extends BlockEntity> BlockEntityTicker<T> getTicker(Level world, BlockState state, BlockEntityType<T> type) {
        return createTickerHelper(type, ModBlockEntities.CEILING_LIGHT_BLOCK_ENTITY, (world1, pos, state1, blockEntity) -> blockEntity.tick(world, pos, state));
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void createBlockStateDefinition(StateDefinition.Builder<Block, BlockState> builder) {
        builder.add(STOPPED);
    }
}
