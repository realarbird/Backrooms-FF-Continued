package com.sp.world.levels;

public interface BackroomsLevelWithLights {
    LightState getLightState();

    void setLightState(LightState lightState);

    enum LightState {
        ON,
        OFF,
        FLICKER,
        BLACKOUT
    }
}
