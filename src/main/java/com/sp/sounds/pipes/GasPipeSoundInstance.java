package com.sp.sounds.pipes;

import com.sp.init.BackroomsLevels;
import com.sp.init.ModSounds;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;

public class GasPipeSoundInstance extends AbstractTickableSoundInstance {
    private final Player player;

    public GasPipeSoundInstance(Player player) {
        super(ModSounds.GAS_PIPE, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.x = 1.5;
        this.y = 22.5;
        this.z = player.getZ();
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.1F;
    }

    @Override
    public void tick() {
        this.z = player.getZ();
        ResourceKey<Level> level = this.player.level().dimension();
        if(level != BackroomsLevels.LEVEL2_WORLD_KEY || this.player.isRemoved()){
            this.stop();
        }
    }
}
