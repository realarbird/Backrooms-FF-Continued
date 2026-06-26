package com.sp.sounds;

import com.sp.SPBRevampedClient;
import com.sp.block.entity.EmergencyLightBlockEntity;
import com.sp.init.BackroomsLevels;
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
public class EmergencyAlarmSoundInstance extends AbstractTickableSoundInstance {
    private final BlockEntity entity;
    private final Player player;

    public EmergencyAlarmSoundInstance(BlockEntity entity, Player player) {
        super(ModSounds.EMERGENCY_LIGHT_ALARM, SoundSource.BLOCKS, SoundInstance.createUnseededRandom());
        this.x = (float) Vec3.atCenterOf(entity.getBlockPos()).x;
        this.y = (float) Vec3.atCenterOf(entity.getBlockPos()).y;
        this.z = (float) Vec3.atCenterOf(entity.getBlockPos()).z;
        this.entity = entity;
        this.player = player;
        this.pitch = 1.0F;
        this.volume = 3.0F;
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

        BackroomsLevels.getLevel(world).ifPresent((backroomsLevel -> {
            if (backroomsLevel instanceof Level0BackroomsLevel level) {
                if(world != null) {
                    if (!this.entity.isRemoved() &&
                            player.position().closerThan(Vec3.atCenterOf(this.entity.getBlockPos()), 80.0f) &&
                            level.getLightState() != BackroomsLevelWithLights.LightState.BLACKOUT &&
                            !SPBRevampedClient.blackScreen) // PORT: phase6 — SPBRevampedClient not yet ported
                    {
                        this.pitch = 1.0F;
                        this.volume = 10.0F;
                    } else {
                        this.stop();
                        ((EmergencyLightBlockEntity) entity).setEmergencyAlarm(false);
                    }
                }
            }
        }));
    }
}
