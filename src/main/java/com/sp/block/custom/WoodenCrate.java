package com.sp.block.custom;

import com.mojang.serialization.MapCodec;
import com.sp.block.entity.WoodenCrateBlockEntity;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.Container;
import net.minecraft.world.Containers;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.BaseEntityBlock;
import net.minecraft.world.level.block.RenderShape;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import org.jetbrains.annotations.Nullable;

public class WoodenCrate extends BaseEntityBlock {
    public static final MapCodec<WoodenCrate> CODEC = simpleCodec(WoodenCrate::new);

    public WoodenCrate(Properties settings) {
        super(settings);
    }

    @Override
    protected MapCodec<? extends BaseEntityBlock> codec() {
        return CODEC;
    }

    @Override
    protected InteractionResult useWithoutItem(BlockState state, Level world, BlockPos pos, Player player, BlockHitResult hit) {
        if (world.isClientSide()) {
            return InteractionResult.SUCCESS;
        } else {
            BlockEntity blockEntity = world.getBlockEntity(pos);
            if (blockEntity instanceof WoodenCrateBlockEntity) {
                player.openMenu((WoodenCrateBlockEntity) blockEntity);
            }

            return InteractionResult.CONSUME;
        }
    }

    @Override
    protected RenderShape getRenderShape(BlockState state) {
        return RenderShape.MODEL;
    }

    @Override
    protected void affectNeighborsAfterRemoval(BlockState state, ServerLevel world, BlockPos pos, boolean movedByPiston) {
        BlockEntity blockEntity = world.getBlockEntity(pos);
        if (blockEntity instanceof Container) {
            Containers.dropContents(world, pos, (Container) blockEntity);
        }
        super.affectNeighborsAfterRemoval(state, world, pos, movedByPiston);
    }

    @Override
    public @Nullable BlockEntity newBlockEntity(BlockPos pos, BlockState state) {
        return new WoodenCrateBlockEntity(pos, state);
    }
}
