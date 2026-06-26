package com.sp.sounds.entity;

import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.init.ModSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

public class SmilerGlitchSoundInstance extends AbstractTickableSoundInstance {
    private final Player player;
    private final PlayerComponent component;

    public SmilerGlitchSoundInstance(Player player) {
        super(ModSounds.SMILER_GLITCH, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.player = player;
        this.component = InitializeComponents.PLAYER.get(player);
        this.looping = true;
        this.delay = 0;
        this.volume = 1.0F;
        this.relative = true;
    }

    @Override
    public void tick() {
        if(this.player.isRemoved()){
            this.stop();
        }

        this.volume = this.component.getGlitchTimer() + 0.1f;
    }
}
