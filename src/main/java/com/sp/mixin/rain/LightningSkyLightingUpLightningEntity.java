package com.sp.mixin.rain;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sp.init.BackroomsLevels;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Server-safe half of the lightning handling: suppresses lightning fire spread on Level 324.
 *
 * <p>// PORT: delta — the {@code tick} wrap that drove the client sky-flash flag (and referenced
 * client-only {@code Minecraft}) moved to {@link LightningSkyFlashClientMixin} in the config's
 * {@code client} list; keeping it here crashed dedicated servers on the first lightning tick.
 */
@Mixin(LightningBolt.class)
public class LightningSkyLightingUpLightningEntity {
    @WrapMethod(method = "spawnFire")
    public void spbrevamped$LightingUpSkyDuringLightningSpawnFire(int spreadAttempts, Operation<Void> original) {
        LightningBolt thiz = ((LightningBolt) (Object) this);

        if (BackroomsLevels.isInBackroomsLevel(thiz.level(), BackroomsLevels.LEVEL324_BACKROOMS_LEVEL)) {
            return;
        }

        original.call(spreadAttempts);
    }
}
