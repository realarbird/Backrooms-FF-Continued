package com.sp.mixin.rain;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sp.SPBRevampedClient;
import net.minecraft.client.Minecraft;
import net.minecraft.world.entity.LightningBolt;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Client half of the lightning handling: drives the {@code SPBRevampedClient.isLightning}
 * sky-flash flag and discards the (client-side) bolt when the local player is below y=20,
 * i.e. inside the Backrooms, so no flash leaks underground.
 *
 * <p>// PORT: delta — this wrap lived in {@link LightningSkyLightingUpLightningEntity} (common
 * mixins) and referenced {@link Minecraft} there, which crashes a DEDICATED server with
 * "Cannot load class net.minecraft.client.Minecraft in environment type SERVER" the moment a
 * lightning bolt ticks (found live on the first player join to the headless test server —
 * RCON-only sessions never spawned lightning near a player). Registered under the config's
 * {@code client} list instead; on a dedicated server the bolt now ticks vanilla server-side
 * (thunder audio may still play in the Backrooms on multiplayer — visual flash is what the
 * effect suppresses, and that stays client-side).
 */
@Mixin(LightningBolt.class)
public class LightningSkyFlashClientMixin {
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
}
