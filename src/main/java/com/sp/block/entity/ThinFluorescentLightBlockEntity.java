package com.sp.block.entity;

import com.sp.block.custom.FluorescentLightBlock;
import com.sp.block.custom.ThinFluorescentLightBlock;
import com.sp.init.BackroomsLevels;
import com.sp.init.ModBlockEntities;
import com.sp.init.ModBlocks;
import com.sp.world.levels.BackroomsLevelWithLights;
// PORT: phase6 - Veil deferred rendering removed/reworked
//import foundry.veil.api.client.render.VeilRenderSystem;
//import foundry.veil.api.client.render.deferred.light.PointLight;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import static com.sp.clientWrapper.ClientWrapper.doClientSideThinFluorescentsTick;


public class ThinFluorescentLightBlockEntity extends BlockEntity {
    BlockState currentState;
    public boolean playingSound;
    public Object pointLight; // PORT: phase6 - was foundry.veil.api.client.render.deferred.light.PointLight
    public boolean prevOn;
    public final int randInt;
    public int ticks = 0;
    public final RandomSource random = RandomSource.create();

    public ThinFluorescentLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.THIN_FLUORESCENT_LIGHT_BLOCK_ENTITY, pos, state);
        java.util.Random random = new java.util.Random();

        this.currentState = state;
        this.playingSound = false;
        this.randInt = random.nextInt(1,8);
    }

    @Override
    public void setRemoved() {
        if (this.getLevel() != null && this.getLevel().isClientSide()){
            if(pointLight != null) {
                // PORT: phase6 - VeilRenderSystem.renderer().getDeferredRenderer().getLightRenderer().removeLight(pointLight);
                pointLight = null;
            }
        }

        super.setRemoved();
    }

    public void tick(Level world, BlockPos pos, BlockState state) {
        if (world.getBlockState(pos).getBlock() != ModBlocks.THIN_FLUORESCENT_LIGHT) {
            return;
        }

        Vec3 position = Vec3.atCenterOf(pos);
        RandomSource random = RandomSource.create();
        java.util.Random random1 = new java.util.Random();
        this.currentState = state;
        ticks++;

        if (!world.isClientSide()) {
            BlockState northState = world.getBlockState(pos.north());
            BlockState westState = world.getBlockState(pos.west());
            BlockState downState = world.getBlockState(pos.below());
            int northOWest = 0;

            if (northState.getBlock() == ModBlocks.THIN_FLUORESCENT_LIGHT) {
                northOWest = 1;
            } else if (westState.getBlock() == ModBlocks.THIN_FLUORESCENT_LIGHT) {
                northOWest = 2;
            } else if (downState.getBlock() == ModBlocks.THIN_FLUORESCENT_LIGHT) {
                northOWest = 3;
            }

            if (northOWest != 0) {
                if (northOWest == 1) {
                    world.setBlockAndUpdate(pos, northState.setValue(ThinFluorescentLightBlock.COPY, true));
                } else if (northOWest == 2) {
                    world.setBlockAndUpdate(pos, westState.setValue(ThinFluorescentLightBlock.COPY, true));
                } else {
                    world.setBlockAndUpdate(pos, downState.setValue(ThinFluorescentLightBlock.COPY, true));
                }
            } else {
                if (state.getValue(ThinFluorescentLightBlock.COPY)) {
                    world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(ThinFluorescentLightBlock.COPY, false));
                }

                //Turn off if Blackout Event is active
                boolean blackouted = false;

                if ((BackroomsLevels.getLevel(world)).orElse(BackroomsLevels.POOLROOMS_BACKROOMS_LEVEL) instanceof BackroomsLevelWithLights level) {
                    if (level.getLightState() == BackroomsLevelWithLights.LightState.BLACKOUT) {
                        blackouted = true;
                    }
                }

                if (blackouted) {
                    world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(ThinFluorescentLightBlock.BLACKOUT, true));
                    this.setPlayingSound(false);

                } else {
                    world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(ThinFluorescentLightBlock.BLACKOUT, false));
                }

                if ((BackroomsLevels.getLevel(world)).orElse(BackroomsLevels.POOLROOMS_BACKROOMS_LEVEL) instanceof BackroomsLevelWithLights level && level.getLightState() == BackroomsLevelWithLights.LightState.FLICKER && !state.getValue(ThinFluorescentLightBlock.BLACKOUT)) {
                    if (ticks % randInt == 0) {
                        int i = random.nextIntBetweenInclusive(1, 2);
                        if (i == 1) {
                            world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(ThinFluorescentLightBlock.ON, true));
                        } else {
                            world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(ThinFluorescentLightBlock.ON, false));
                        }
                    }
                } else {
                    if (!state.getValue(ThinFluorescentLightBlock.ON)) {
                        world.setBlockAndUpdate(pos, world.getBlockState(pos).setValue(ThinFluorescentLightBlock.ON, true));
                    }
                }
            }
        }

        if (world.isClientSide()) {
            doClientSideThinFluorescentsTick(world, pos, state, random1, position, this);
        }

        if (ticks > 100) {
            ticks = 1;
        }

        prevOn = world.getBlockState(pos).getValue(FluorescentLightBlock.ON);
    }

    public BlockState getCurrentState(){
        return this.currentState;
    }

    public boolean isPlayingSound() {
        return playingSound;
    }

    public void setPlayingSound(boolean playingSound) {
        this.playingSound = playingSound;
    }

}
