package com.sp.block;

import com.sp.init.ModSounds;
import net.minecraft.world.level.block.SoundType;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundEvents;

public class SprintBlockSoundGroup extends SoundType {
    private final SoundEvent sprintSound;

    public static final SprintBlockSoundGroup SILENT = new SprintBlockSoundGroup(
            0.0f,
            0.0f,
            ModSounds.SILENCE,
            ModSounds.SILENCE,
            ModSounds.SILENCE,
            ModSounds.SILENCE,
            ModSounds.SILENCE,
            ModSounds.SILENCE
    );

    public static final SprintBlockSoundGroup CARPET = new SprintBlockSoundGroup(
            1.0f,
            1.0f,
            SoundEvents.WOOL_BREAK,
            ModSounds.CARPET_WALK,
            SoundEvents.WOOL_PLACE,
            ModSounds.SILENCE,
            ModSounds.CARPET_RUN,
            ModSounds.CARPET_RUN
    );

    public static final SprintBlockSoundGroup CONCRETE = new SprintBlockSoundGroup(
            1.0f,
            1.0f,
            SoundEvents.STONE_BREAK,
            ModSounds.CONCRETE_WALK,
            SoundEvents.STONE_PLACE,
            ModSounds.SILENCE,
            ModSounds.CONCRETE_RUN,
            ModSounds.CONCRETE_RUN
    );

    public static final SoundType WALL = new SoundType(
            1.0f,
            1.0f,
            SoundEvents.WOOD_BREAK,
            SoundEvents.WOOD_STEP,
            SoundEvents.WOOD_PLACE,
            ModSounds.SILENCE,
            SoundEvents.WOOD_FALL
    );

    public static final SoundType CEILING_TILE = new SoundType(
            1.0f,
            1.0f,
            SoundEvents.WOOD_BREAK,
            SoundEvents.WOOD_STEP,
            SoundEvents.WOOD_PLACE,
            ModSounds.SILENCE,
            SoundEvents.WOOD_FALL
    );

    public static final SoundType GRASS2 = new SprintBlockSoundGroup(
            1.0f,
            1.0f,
            SoundEvents.GRASS_BREAK,
            ModSounds.GRASS_WALK,
            SoundEvents.GRASS_PLACE,
            ModSounds.SILENCE,
            ModSounds.GRASS_RUN,
            ModSounds.GRASS_RUN
    );

    public SprintBlockSoundGroup(float volume, float pitch, SoundEvent breakSound, SoundEvent stepSound, SoundEvent placeSound, SoundEvent hitSound, SoundEvent fallSound, SoundEvent sprintSound) {
        super(volume, pitch, breakSound, stepSound, placeSound, hitSound, fallSound);
        this.sprintSound = sprintSound;
    }

    public SoundEvent getSprintingSound(){
        return this.sprintSound;
    }

}
