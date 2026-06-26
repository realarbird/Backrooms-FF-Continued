package com.sp.mixin.rain;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sp.SPBRevampedClient;
import com.sp.init.BackroomsLevels;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(LightningBolt.class)
public class LightningSkyLightingUpLightningEntity {
    @WrapMethod(method = "tick")
    public void spbrevamped$LightingUpSkyDuringLightningTick(Operation<Void> original) {
        LightningBolt thiz = ((LightningBolt) (Object) this);

        if (Minecraft.getInstance().player != null && Minecraft.getInstance().player.getY() > 20) {
            if (thiz.tickCount == 1) {
                SPBRevampedClient.isLightning = true;
            }

            if (thiz.tickCount >= 3) {
                SPBRevampedClient.isLightning = false;
            }

            original.call();
        } else {
            thiz.discard();
        }
    }

    @WrapMethod(method = "spawnFire")
    public void spbrevamped$LightingUpSkyDuringLightningSpawnFire(int spreadAttempts, Operation<Void> original) {
        LightningBolt thiz = ((LightningBolt) (Object) this);

        if (BackroomsLevels.isInBackroomsLevel(thiz.level(), BackroomsLevels.LEVEL324_BACKROOMS_LEVEL)) {
            return;
        }

        original.call(spreadAttempts);
    }
}
