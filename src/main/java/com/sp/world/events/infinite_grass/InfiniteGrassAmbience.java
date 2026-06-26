package com.sp.world.events.infinite_grass;

import com.sp.init.ModSounds;
import com.sp.world.events.AbstractEvent;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public class InfiniteGrassAmbience extends AbstractEvent {
    @Override
    public void init(Level world) {
        RandomSource random = RandomSource.create();
        boolean far = random.nextBoolean();

        if(far){
            playDistantSound(world, ModSounds.INFINITE_GRASS_SOUNDEVENT_FAR);
        } else {
            playSound(world, ModSounds.INFINITE_GRASS_SOUNDEVENT);
        }
    }

    @Override
    public int duration() {
        return 200;
    }
}
