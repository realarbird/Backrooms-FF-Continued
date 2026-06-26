package com.sp.sounds;

import com.sp.init.BackroomsLevels;
import com.sp.init.ModSounds;
import com.sp.world.levels.custom.PoolroomsBackroomsLevel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;

@Environment(EnvType.CLIENT)
public class PoolroomsNoonAmbienceSoundInstance extends AbstractTickableSoundInstance {
    private final Player player;

    public PoolroomsNoonAmbienceSoundInstance(Player player) {
        super(ModSounds.POOLROOMS_AMBIENCE_NOON, SoundSource.AMBIENT, SoundInstance.createUnseededRandom());
        this.player = player;
        this.looping = true;
        this.delay = 0;
        this.volume = 0.5F;
        this.relative = true;
    }

    @Override
    public void tick() {
        if (!((BackroomsLevels.getLevel(player.level()).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL)) instanceof PoolroomsBackroomsLevel level)) {
            return;
        }

        if(!level.isNoon() || this.player.isRemoved()){
            this.stop();
            this.looping = true;
        }
    }
}
