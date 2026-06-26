package com.sp.block.entity;

import com.sp.clientWrapper.ClientWrapper;
import com.sp.init.ModBlockEntities;
import com.sp.sounds.EmergencyAlarmSoundInstance;
// PORT: phase6 - Veil deferred rendering removed/reworked
//import foundry.veil.api.client.render.VeilRenderSystem;
//import foundry.veil.api.client.render.deferred.light.AreaLight;
//import foundry.veil.api.client.render.deferred.light.PointLight;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;

public class EmergencyLightBlockEntity extends BlockEntity {
    public final float randomOffset;
    public boolean initEmergencyLights = false;
    public boolean playingEmergencyAlarm = false;
    public boolean initNormalLights = false;

    public EmergencyAlarmSoundInstance emergencyAlarmSoundInstance;
    public Object areaLight1; // PORT: phase6 - was foundry.veil.api.client.render.deferred.light.AreaLight
    public Object areaLight2; // PORT: phase6 - was foundry.veil.api.client.render.deferred.light.AreaLight
    public Object pointLight;  // PORT: phase6 - was foundry.veil.api.client.render.deferred.light.PointLight

    public EmergencyLightBlockEntity(BlockPos pos, BlockState state) {
        super(ModBlockEntities.EMERGENCY_LIGHT_BLOCK_ENTITY, pos, state);
        this.randomOffset = RandomSource.create().nextFloat() * 180;
    }

    @Override
    public void setRemoved() {
        if (this.initEmergencyLights){
            this.removeEmergencyLights();
        }

        if(this.initNormalLights){
            this.removeNormalLights();
        }

        super.setRemoved();
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

    public void removeEmergencyLights() {
        if (this.initEmergencyLights && this.level.isClientSide()) {
            // PORT: phase6 - VeilRenderSystem.renderer().getDeferredRenderer().getLightRenderer().removeLight(this.areaLight1);
            this.areaLight1 = null;
            // PORT: phase6 - VeilRenderSystem.renderer().getDeferredRenderer().getLightRenderer().removeLight(this.areaLight2);
            this.areaLight2 = null;
            // PORT: phase6 - VeilRenderSystem.renderer().getDeferredRenderer().getLightRenderer().removeLight(this.pointLight);
            this.pointLight = null;
            this.initEmergencyLights = false;
        }
    }

    public void removeNormalLights() {
        if(this.initNormalLights && this.level.isClientSide()) {
            // PORT: phase6 - VeilRenderSystem.renderer().getDeferredRenderer().getLightRenderer().removeLight(this.pointLight);
            this.pointLight = null;
            this.initNormalLights = false;
        }
    }
}
