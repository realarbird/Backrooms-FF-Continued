package com.sp.world.events.poolrooms;

import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.WorldEvents;
import com.sp.init.BackroomsLevels;
import com.sp.init.ModSounds;
// PORT: phase6 - com.sp.render.PoolroomsDayCycle lives in the render package (not yet ported)
// import com.sp.render.PoolroomsDayCycle;
import com.sp.world.events.AbstractEvent;
import com.sp.world.levels.custom.PoolroomsBackroomsLevel;
import net.minecraft.world.level.Level;

public class PoolroomsSunset extends AbstractEvent {
    @Override
    public void init(Level world) {

        BackroomsLevels.getLevel(world).ifPresent(backroomsLevel -> {
            if (!(backroomsLevel instanceof PoolroomsBackroomsLevel level)) {
                return;
            }

            WorldEvents events = InitializeComponents.EVENTS.get(world);
            // PORT: phase6 - restore once com.sp.render.PoolroomsDayCycle is ported
            float currentDayTime = 0.0f; // PoolroomsDayCycle.getDayTime(world);
            level.setSunsetTransitioning(true);
            level.setTimeOfDay(level.getTimeOfDay() + 0.25f);
            events.sync();

            if(currentDayTime == 0.0) playSound(world, ModSounds.SUNSET_TRANSITION);
            else if (currentDayTime == 0.25 || currentDayTime == 0.5) playSound(world, ModSounds.MIDNIGHT_TRANSITION);
            else playSound(world, ModSounds.SUNSET_TRANSITION_END);
        });
    }

    @Override
    public void finish(Level world) {
        super.finish(world);

        BackroomsLevels.getLevel(world).ifPresent(backroomsLevel -> {
            if (backroomsLevel instanceof PoolroomsBackroomsLevel level) {
                WorldEvents events = InitializeComponents.EVENTS.get(world);

                level.setTimeOfDay(level.getTimeOfDay() >= 1.0f ? 0.0f : level.getTimeOfDay());
                level.setSunsetTransitioning(false);
                events.sync();
            }
        });
    }

    @Override
    public int duration() {
        return 200;
    }
}
