package com.sp.sounds;

import com.sp.init.BackroomsLevels;
import com.sp.init.ModSounds;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

@Environment(EnvType.CLIENT)
public class AmbientSoundInstance extends AbstractTickableSoundInstance {
    private final Player player;

    public AmbientSoundInstance(Player player) {
        super(ModSounds.AMBIENCE, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.5F;
        this.relative = true;
    }

    @Override
    public void tick() {
        ResourceKey<Level> level = this.player.level().dimension();
        if((level != BackroomsLevels.LEVEL1_WORLD_KEY && level != BackroomsLevels.LEVEL2_WORLD_KEY) || this.player.isRemoved()){
            this.stop();
        }
    }
}
