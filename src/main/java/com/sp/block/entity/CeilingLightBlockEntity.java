package com.sp.block.entity;

import com.sp.init.ModBlockEntities;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// PORT: phase6 (A4) — Veil deferred AreaLight removed. The ceiling fixture now illuminates via the
// block's own vanilla light level (see ModBlocks.CEILINGLIGHT lightLevel), so this BE no longer manages
// any per-tick light source. The kept fields (random/ticks/on) are retained for the (already-disabled)
// flicker behavior reference; the tick body is now a no-op for lighting. // PORT: delta — no flicker.
public class CeilingLightBlockEntity extends BlockEntity {
    float brightness;
    float angle;
    int ticks;
    RandomSource random = RandomSource.create();
    int randomInt;
    boolean on;

    public CeilingLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.CEILING_LIGHT_BLOCK_ENTITY, pos, state);
        this.randomInt = random.nextIntBetweenInclusive(1, 4);
        this.on = true;
    }

    public void tick(Level world, BlockPos pos, BlockState state) {
        // Lighting is handled by the block's vanilla light level; nothing to do per-tick.
    }
}
