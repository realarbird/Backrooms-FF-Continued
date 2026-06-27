package com.sp.block.entity;

import com.sp.clientWrapper.ClientWrapper;
import com.sp.init.ModBlockEntities;
import com.sp.sounds.EmergencyAlarmSoundInstance;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

// PORT: phase6 (A4) — Veil deferred lights removed. Illumination now comes from the block's own vanilla
// light level (see ModBlocks.EMERGENCY_LIGHT). This BE keeps only the (non-Veil) emergency-alarm sound +
// state booleans. // PORT: delta — the Veil version swept two rotating red AreaLights + a red PointLight
// when RED_LIGHT was active; the steady vanilla light level cannot reproduce the rotating red sweep.
// PORT: phase6e — ClientWrapper.tickEmergencyLight (the alarm driver) is still Veil/Yarn and is rebuilt in 6e.
public class EmergencyLightBlockEntity extends BlockEntity {
    public final float randomOffset;
    public boolean playingEmergencyAlarm = false;

    public EmergencyAlarmSoundInstance emergencyAlarmSoundInstance;

    public EmergencyLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EMERGENCY_LIGHT_BLOCK_ENTITY, pos, state);
        this.randomOffset = RandomSource.create().nextFloat() * 180;
    }

    public void tick(Level world, BlockPos pos, BlockState state) {
        if (!world.isClientSide()) {
            return;
        }

        ClientWrapper.tickEmergencyLight(world, pos, state, this);
    }

    public void setEmergencyAlarm(boolean b) {
        this.playingEmergencyAlarm = b;
    }
}
