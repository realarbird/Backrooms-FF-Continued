package com.sp.block.entity;

import com.sp.block.custom.TinyFluorescentLightBlock;
import com.sp.init.ModBlockEntities;
import com.sp.init.ModBlocks;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static com.sp.clientWrapper.ClientWrapper.doClientSideTinyFluorescentsTick;


public class TinyFluorescentLightBlockEntity extends BlockEntity {
    BlockState currentState;
    public boolean playingSound;
    public boolean prevOn;
    public final int randInt;
    public int ticks = 0;
    public final RandomSource random = RandomSource.create();

    public TinyFluorescentLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.TINY_FLUORESCENT_LIGHT_BLOCK_ENTITY, pos, state);
        java.util.Random random = new java.util.Random();

        this.currentState = state;
        this.playingSound = false;
        this.randInt = random.nextInt(1,8);
    }

    @Override
    public void setRemoved() {
        // PORT: phase6 (A4) — Veil point light removed; illumination is now the block's vanilla light level.
        super.setRemoved();
    }

    public void tick(Level world, BlockPos pos, BlockState state) {
        if (world.getBlockState(pos).getBlock() != ModBlocks.TINY_FLUORESCENT_LIGHT) {
            return;
        }

        Vec3 position = Vec3.atCenterOf(pos);
        java.util.Random random1 = new java.util.Random();


        if (world.isClientSide()) {
            doClientSideTinyFluorescentsTick(world, pos, state, random1, position, this);
        }

        if (ticks > 100) {
            ticks = 1;
        }

        // 26.2 compares block-state properties by identity, so this must read the tiny block's OWN
        // ON property — reading the full FluorescentLightBlock.ON (a different "on" instance) threw
        // "property does not exist" and crashed the tick.
        prevOn = world.getBlockState(pos).getValue(TinyFluorescentLightBlock.ON);
    }

    public BlockState getCurrentState() {
        return this.currentState;
    }

    public boolean isPlayingSound() {
        return playingSound;
    }

    public void setPlayingSound(boolean playingSound) {
        this.playingSound = playingSound;
    }
}
