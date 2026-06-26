package com.sp.sounds;

import com.sp.SPBRevampedClient;
import com.sp.init.BackroomsLevels;
import com.sp.init.ModSounds;
import com.sp.world.levels.custom.Level2BackroomsLevel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class CreakingSoundInstance extends AbstractTickableSoundInstance {
    private final Player player;

    public CreakingSoundInstance(Player player) {
        super(ModSounds.LEVEL2_WARP_CREAKING_LOOP, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.9F;
        this.relative = true;
    }

    @Override
    public void tick() {
        if (!((BackroomsLevels.getLevel(player.level()).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL)) instanceof Level2BackroomsLevel level)) {
            return;
        }

        // PORT: phase6 — SPBRevampedClient (client/render wrapper) not yet ported
        if(this.player.isRemoved() || (!level.isWarping() && SPBRevampedClient.finishedWarp(player.level()))) {
            if(!level.isWarping()){
                this.volume -= 0.01f;
            }else {
                this.volume = 0.0f;
                this.stop();
            }

            if(this.volume <= 0.0f){
                this.stop();
            }

        }
    }
}
