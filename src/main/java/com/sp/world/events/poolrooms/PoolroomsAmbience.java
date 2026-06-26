package com.sp.world.events.poolrooms;

import com.sp.init.ModSounds;
import com.sp.world.events.AbstractEvent;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public class PoolroomsAmbience extends AbstractEvent {

    @Override
    public void init(Level world) {
        RandomSource random = RandomSource.create();
        int rand = random.nextIntBetweenInclusive(1, 4);
        SoundEvent soundEvent = switch (rand) {
            case 1 -> ModSounds.POOLROOMS_SPLASH1;
            case 2 -> ModSounds.POOLROOMS_SPLASH2;
            case 3 -> ModSounds.POOLROOMS_DRIP1;
            default -> ModSounds.POOLROOMS_DRIP2;
        };

        playDistantSound(world, soundEvent);
    }

    @Override
    public int duration() {
        return 200;
    }
}
