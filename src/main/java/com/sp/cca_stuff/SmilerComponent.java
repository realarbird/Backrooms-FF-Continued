package com.sp.cca_stuff;

import com.sp.entity.custom.SmilerEntity;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ClientTickingComponent;

public class SmilerComponent implements AutoSyncedComponent, ClientTickingComponent {
    private final SmilerEntity smiler;
    private Integer randomTexture;
    private boolean shouldDisappear;
    private float opacity;
    private float maxOpacity;
    private int tick;

    public SmilerComponent(SmilerEntity smiler) {
        this.smiler = smiler;
        this.shouldDisappear = false;
        this.opacity = 0.0f;
        this.tick = 20;
        this.randomTexture = 1;
    }


    public void setRandomTexture(Integer randomTexture) {
        this.randomTexture = randomTexture;
    }
    public int getRandomTexture(){
        return this.randomTexture;
    }

    public boolean shouldDisappear() {
        return shouldDisappear;
    }
    public void setShouldDisappear(boolean shouldDisappear) {
        this.shouldDisappear = shouldDisappear;
    }

    public float getOpacity() {
        return opacity;
    }

    public void sync() {
        InitializeComponents.SMILER.sync(this.smiler);
    }

    @Override
    public void readData(ValueInput tag) {
        this.randomTexture = tag.getIntOr("randomTexture", 1);
        this.shouldDisappear = tag.getBooleanOr("shouldDisappear", false);
    }

    @Override
    public void writeData(ValueOutput tag) {
        tag.putInt("randomTexture", this.randomTexture);
        tag.putBoolean("shouldDisappear", this.shouldDisappear);
    }

    @Override
    public void clientTick() {
        if(this.smiler.tickCount <= 30){
            this.opacity = Math.min((float) this.smiler.tickCount / 30, 1.0f);
            this.maxOpacity = this.opacity;
        }

        if(this.shouldDisappear()) {
            this.tick--;
            this.opacity = ((float) this.tick / 30) * this.maxOpacity;

            if(this.tick <= 0) {
                this.tick = 30;
                this.shouldDisappear = false;
            }
        }
    }

}
