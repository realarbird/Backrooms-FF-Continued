package com.sp.sounds;

import com.sp.SPBRevampedClient;
import com.sp.block.custom.FluorescentLightBlock;
import com.sp.block.entity.FluorescentLightBlockEntity;
import com.sp.init.BackroomsLevels;
import com.sp.init.ModBlocks;
import com.sp.init.ModSounds;
import com.sp.world.levels.BackroomsLevelWithLights;
import com.sp.world.levels.custom.Level0BackroomsLevel;
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
public class FluorescentLightSoundInstance extends AbstractTickableSoundInstance {
    private final BlockEntity entity;
    private final Player player;

    public FluorescentLightSoundInstance(BlockEntity entity, Player player) {
        super(ModSounds.FLUORESCENT_LIGHT_HUM, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.x = (float) Vec3.atCenterOf(entity.getBlockPos()).x;
        this.y = (float) Vec3.atCenterOf(entity.getBlockPos()).y - 0.5;
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

        if (!((BackroomsLevels.getLevel(player.level()).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL)) instanceof Level0BackroomsLevel level)) {
            return;
        }

        if(world != null) {
            if (!this.entity.isRemoved() &&
                player.position().closerThan(Vec3.atCenterOf(this.entity.getBlockPos()), 16.0f) &&
                level.getLightState() != BackroomsLevelWithLights.LightState.BLACKOUT &&
                ((FluorescentLightBlockEntity) entity).getCurrentState() == ModBlocks.FLUORESCENT_LIGHT.defaultBlockState().setValue(FluorescentLightBlock.ON, true) &&
                !((FluorescentLightBlockEntity) entity).getCurrentState().getValue(FluorescentLightBlock.BLACKOUT) &&
                !SPBRevampedClient.blackScreen) // PORT: phase6 — SPBRevampedClient not yet ported
            {
                this.pitch = 1.0F;
                this.volume = 0.4F;
            } else {
                this.stop();
                ((FluorescentLightBlockEntity) entity).setPlayingSound(false);
            }
        }
    }
}
