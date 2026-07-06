package com.sp.block.entity;

import com.sp.block.custom.FluorescentLightBlock;
import com.sp.init.BackroomsLevels;
import com.sp.init.ModBlockEntities;
import com.sp.init.ModBlocks;
import com.sp.world.levels.BackroomsLevelWithLights;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

import static com.sp.clientWrapper.ClientWrapper.doClientSideTick;

public class FluorescentLightBlockEntity extends BlockEntity {
    public BlockState currentState;
    public RandomSource random = RandomSource.create();
    public java.util.Random random1 = new java.util.Random();
    public boolean playingSound;
    public boolean prevOn;
    public final int randInt;
    public int ticks = 0;

    public FluorescentLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.FLUORESCENT_LIGHT_BLOCK_ENTITY, pos, state);

        this.playingSound = false;
        this.currentState = state;
        this.randInt = this.random.nextIntBetweenInclusive(1, 5);
    }

    @Override
    public void setRemoved() {
        super.setRemoved();

        // PORT: phase6 (A4) — Veil point light removed; illumination is now the block's vanilla light level.
        if (level != null && level.isClientSide()) {
            this.setPlayingSound(false);
        }
    }

    public void tick(Level world, BlockPos pos, BlockState state) {
        if (world.getBlockState(pos).getBlock() != ModBlocks.FLUORESCENT_LIGHT) {
            return;
        }

        ticks++;
        this.currentState = state;

        if (!world.isClientSide()) {
            //Set to ceiling tile if it can't be seen
            if (world.dimension() == BackroomsLevels.LEVEL0_WORLD_KEY) {
                if (world.getBlockState(pos.below()) != Blocks.AIR.defaultBlockState()) {
                    world.removeBlockEntity(pos);
                    // PORT: phase6 - LevelChunk.blockEntityNbts (pending NBT map) no longer public in 26.2
                    world.setBlockAndUpdate(pos, ModBlocks.CEILING_TILE.defaultBlockState());
                    return;
                }
            }


            BlockState northState = world.getBlockState(pos.north());
            BlockState westState = world.getBlockState(pos.west());
            int northOWest = 0;

            if (northState.getBlock() == ModBlocks.FLUORESCENT_LIGHT) {
                northOWest = 1;
            } else if (westState.getBlock() == ModBlocks.FLUORESCENT_LIGHT) {
                northOWest = 2;
            }

            if (northOWest != 0) {
                if (northOWest == 1) {
                    world.setBlockAndUpdate(pos, northState.setValue(FluorescentLightBlock.COPY, true));
                } else {
                    world.setBlockAndUpdate(pos, westState.setValue(FluorescentLightBlock.COPY, true));
                }
            } else {
                if (state.getValue(FluorescentLightBlock.COPY)) {
                    world.setBlockAndUpdate(pos, ModBlocks.FLUORESCENT_LIGHT.defaultBlockState().setValue(FluorescentLightBlock.COPY, false));
                }

                if (!((BackroomsLevels.getLevel(this.getLevel()).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL)) instanceof BackroomsLevelWithLights level)) {
                    return;
                }
                //Turn off if Blackout Event is active
                if (level.getLightState() == BackroomsLevelWithLights.LightState.BLACKOUT) {
                    world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(FluorescentLightBlock.BLACKOUT, true));
                } else if (world.getBlockState(pos).getValue(FluorescentLightBlock.BLACKOUT)) {
                    // Clear the blackout once the event ends (the thin light already did this;
                    // without it every full fixture that ever saw a blackout stayed dark forever —
                    // blackout=true forces lightLevel 0 — leaving whole regions of Level 0 unlit).
                    world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(FluorescentLightBlock.BLACKOUT, false));
                }

                if (level.getLightState() != BackroomsLevelWithLights.LightState.ON && state.getValue(FluorescentLightBlock.ON)) {
                    world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(FluorescentLightBlock.ON, false));
                }

                if (level.getLightState() == BackroomsLevelWithLights.LightState.FLICKER && !state.getValue(FluorescentLightBlock.BLACKOUT)) {
                    if (ticks % randInt == 0) {
                        boolean i = this.random.nextBoolean();
                        if (i) {
                            world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(FluorescentLightBlock.ON, true));
                        } else {
                            world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(FluorescentLightBlock.ON, false));
                        }
                    }
                } else {
                    if (!state.getValue(FluorescentLightBlock.ON) && level.getLightState() == BackroomsLevelWithLights.LightState.ON) {
                        world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(FluorescentLightBlock.ON, true));
                    }
                }
            }
        }


        if (world.isClientSide()) {
            doClientSideTick(world, pos, state, this);
        }

        if (ticks > 100) {
            ticks = 1;
        }

        prevOn = world.getBlockState(pos).getValue(FluorescentLightBlock.ON);
    }

    public boolean isPlayingSound() {
        return playingSound;
    }

    public void setPlayingSound(boolean playingSound) {
        this.playingSound = playingSound;
    }

    public BlockState getCurrentState(){
        return this.currentState;
    }

}
