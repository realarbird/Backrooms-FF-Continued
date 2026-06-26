package com.sp.sounds.entity;

import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.SkinWalkerComponent;
import com.sp.entity.custom.SkinWalkerEntity;
import com.sp.init.ModSounds;
import com.sp.util.Easings;
import net.minecraft.client.resources.sounds.AbstractTickableSoundInstance;
import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.sounds.SoundSource;

public class SkinWalkerChaseSoundInstance extends AbstractTickableSoundInstance {
    private final SkinWalkerEntity entity;
    private final SkinWalkerComponent component;
    private boolean beginFade;
    private int ticksToFade = 80;

    public SkinWalkerChaseSoundInstance(SkinWalkerEntity entity) {
        super(ModSounds.SKINWALKER_CHASE, SoundSource.HOSTILE, SoundInstance.createUnseededRandom());
        this.entity = entity;
        this.component = InitializeComponents.SKIN_WALKER.get(entity);
        this.setPosition(entity);
        this.looping = true;
        this.delay = 0;
        this.volume = 100.0f;
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
        if(this.entity.isRemoved()){
            this.beginFade = true;
        } else if(!this.component.isChasing()){
            this.beginFade = true;
        }

        if(this.component.isChasing() && this.beginFade){
            this.beginFade = false;
            this.ticksToFade = 80;
        }

        if(this.beginFade){
            this.ticksToFade--;
            this.volume = 10 * Easings.Easing.easeInSine.ease((float) this.ticksToFade/80);

            if(ticksToFade <= 0) {
                this.stop();
            }
        }

        this.setPosition(this.entity);
    }

    private void setPosition(SkinWalkerEntity entity){
        this.x = entity.getX();
        this.y = entity.getY();
        this.z = entity.getZ();
    }
}
