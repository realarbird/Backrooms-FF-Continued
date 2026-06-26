package com.sp.world.events.level1;

import com.sp.init.ModSounds;
import com.sp.world.events.AbstractEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public class Level1Ambience extends AbstractEvent {
    @Override
    public void init(Level world) {
        RandomSource random = RandomSource.create();
        int rand = random.nextIntBetweenInclusive(1, 4);
        SoundEvent soundEvent = switch (rand) {
            case 1 -> ModSounds.LEVEL1_AMBIENCE1;
            case 2 -> ModSounds.LEVEL1_AMBIENCE2;
            case 3 -> ModSounds.LEVEL1_AMBIENCE3;
            default -> ModSounds.LEVEL1_AMBIENCE4;
        };

        playDistantSound(world, soundEvent);
    }

    @Override
    public int duration() {
        return 1200;
    }
}
