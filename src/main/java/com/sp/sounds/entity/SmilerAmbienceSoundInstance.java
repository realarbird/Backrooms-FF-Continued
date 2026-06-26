package com.sp.sounds.entity;

import com.sp.init.BackroomsLevels;
import com.sp.init.ModSounds;
import com.sp.world.levels.BackroomsLevelWithLights;
import com.sp.world.levels.custom.Level1BackroomsLevel;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class SmilerAmbienceSoundInstance extends AbstractTickableSoundInstance {
    private final Player player;

    public SmilerAmbienceSoundInstance(Player player) {
        super(ModSounds.SMILER_AMBIENCE, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.5F;
        this.relative = true;
    }

    @Override
    public void tick() {
        if (!((BackroomsLevels.getLevel(player.level()).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL)) instanceof Level1BackroomsLevel level)) {
            return;
        }

        if(level.getLightState() != BackroomsLevelWithLights.LightState.BLACKOUT || this.player.isRemoved()){
            this.stop();
        }
    }
}
