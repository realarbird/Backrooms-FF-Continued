package com.sp.world.events;

import net.minecraft.world.level.Level;

public class EmptyEvent extends AbstractEvent {
    @Override
    public void init(Level world) {

        done = true;
    }

    @Override
    public int duration() {
        return 0;
    }
}
