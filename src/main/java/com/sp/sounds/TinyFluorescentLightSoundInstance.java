package com.sp.sounds;

import com.sp.SPBRevampedClient;
import com.sp.block.custom.TinyFluorescentLightBlock;
import com.sp.block.entity.TinyFluorescentLightBlockEntity;
import com.sp.init.BackroomsLevels;
import com.sp.init.ModSounds;
import com.sp.world.levels.BackroomsLevelWithLights;
import com.sp.world.levels.custom.Level0BackroomsLevel;
import com.sp.world.levels.custom.Level1BackroomsLevel;
import net.fabricmc.api.EnvType;
import net.fabricmc.api.Environment;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.phys.Vec3;

@Environment(EnvType.CLIENT)
public class TinyFluorescentLightSoundInstance extends AbstractTickableSoundInstance {
    private final BlockEntity entity;
    private final Player player;

    public TinyFluorescentLightSoundInstance(BlockEntity entity, Player player) {
        super(ModSounds.FLUORESCENT_LIGHT_HUM2, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.x = (float) Vec3.atCenterOf(entity.getBlockPos()).x;
        this.y = (float) Vec3.atCenterOf(entity.getBlockPos()).y;
        this.z = (float) Vec3.atCenterOf(entity.getBlockPos()).z;
        this.entity = entity;
        this.player = player;
    }

    @Override
    public boolean canStartSilent() {
        return true;
    }

    @Override
    public boolean canPlaySound() {
        return true;
    }

    @Override
    public void tick() {
        Level world = this.entity.getLevel();

        if(world != null) {
            boolean blackedOut = false;

            if ((BackroomsLevels.getLevel(world).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL)) instanceof Level0BackroomsLevel level) {
                blackedOut = level.getLightState() == BackroomsLevelWithLights.LightState.BLACKOUT;
            }

            if ((BackroomsLevels.getLevel(world).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL)) instanceof Level1BackroomsLevel level) {
                blackedOut = level.getLightState() == BackroomsLevelWithLights.LightState.BLACKOUT;
            }

            if (!this.entity.isRemoved() &&
                    player.position().closerThan(Vec3.atCenterOf(entity.getBlockPos()), 15.0f) &&
                    !blackedOut &&
                    // 26.2 compares block-state properties by identity — must be the TINY block's
                    // own ON/BLACKOUT (the thin block's instances threw and crashed the client
                    // once this sound started ticking).
                    ((TinyFluorescentLightBlockEntity) entity).getCurrentState().getValue(TinyFluorescentLightBlock.ON) &&
                    !((TinyFluorescentLightBlockEntity) entity).getCurrentState().getValue(TinyFluorescentLightBlock.BLACKOUT) &&
                    !SPBRevampedClient.blackScreen) {
                this.pitch = 1.0F;
                this.volume = 0.2F;
            } else {
                this.stop();
                ((TinyFluorescentLightBlockEntity) entity).setPlayingSound(false);
            }
        }
    }
}
