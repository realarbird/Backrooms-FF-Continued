package com.sp.world.events;

import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.level.Level;

public abstract class AbstractEvent {
    boolean done = false;

    public abstract void init(Level world);

    public void finish(Level world) {
        done = true;
    }

    public boolean isDone() {
        return done;
    }

    public abstract int duration();

    public void ticks(int ticks, Level world) {

    }

    protected static void playSound(Level world, SoundEvent soundEvent){
        EventSounds.playSound(world, soundEvent);
    }

    protected static void playSoundWithRandLocation(Level world, SoundEvent soundEvent, int yLevel, int range){
        EventSounds.playSoundWithRandLocation(world, soundEvent, yLevel, range);
    }

    protected static void playDistantSound(Level world, SoundEvent soundEvent){
        EventSounds.playDistantSound(world, soundEvent);
    }
}
