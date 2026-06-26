package com.sp.world.events.level2;

import com.sp.init.ModSounds;
import com.sp.world.events.AbstractEvent;
import com.sp.world.events.EventSounds;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public class Level2Ambience extends AbstractEvent {
    int duration = 200;

    @Override
    public void init(Level world) {
        RandomSource random = RandomSource.create();
        int rand = random.nextIntBetweenInclusive(1, 3);
        SoundEvent soundEvent;
        switch(rand){
            case 1: soundEvent = ModSounds.CREAKING1;
            break;
            case 2: soundEvent = ModSounds.CREAKING2;
            break;
            default: {
                soundEvent = ModSounds.LEVEL2_AMBIENCE;
                duration = 720;
            }
            break;
        }
        playLevel2Sound(world, soundEvent);
    }

    private void playLevel2Sound(Level world, SoundEvent soundEvent){
        EventSounds.playLevel2Sound(world, soundEvent);
    }

    @Override
    public int duration() {
        return duration;
    }
}
