package com.sp.world.events.level324;

import com.sp.init.ModSounds;
import com.sp.world.events.AbstractEvent;
import net.minecraft.world.level.Level;

public class ScreechSoundEvent extends AbstractEvent {
    @Override
    public void init(Level world) {
        long playerOverLand = world.players().stream().filter(player -> player.getY() > 60).count();

        if (playerOverLand == 0) {
            playDistantSound(world, ModSounds.SCREECH_SOUNDEVENT_FAR);
        }
    }

    @Override
    public int duration() {
        return 200;
    }
}
