package com.sp.sounds;

import com.sp.init.ModSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class WindTunnelAmbienceSoundInstance extends AbstractTickableSoundInstance {
    private final Player player;

    public WindTunnelAmbienceSoundInstance(Player player) {
        super(ModSounds.WINDTUNNEL_GRASS_AMBIENCE, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.85F;
        this.relative = true;
    }

    @Override
    public void tick() {
        if(this.player.isRemoved()){
            this.stop();
        }
    }
}
