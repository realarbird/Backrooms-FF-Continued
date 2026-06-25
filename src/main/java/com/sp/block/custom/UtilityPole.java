package com.sp.block.custom;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.shapes.CollisionContext;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.shapes.VoxelShape;
import net.minecraft.world.level.BlockGetter;

public class UtilityPole extends Block {
    private final int size;

    public UtilityPole(Properties settings, int size) {
        super(settings);
        this.size = Math.min(size, 8);
    }

    @Override
    public VoxelShape getShape(BlockState state, BlockGetter world, BlockPos pos, CollisionContext context) {
        return Block.box(size, 0, size, 16-size, 16, 16-size);
    }
}
