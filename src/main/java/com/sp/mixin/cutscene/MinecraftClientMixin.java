package com.sp.mixin.cutscene;

import com.sp.SPBRevampedClient;
import net.minecraft.client.Minecraft;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Per-frame cutscene tick (NEW-INJECTION). Ported Yarn→mojmap, Phase 6f.
 *
 * <p>PORT: delta — {@code MinecraftClient.render}→{@code Minecraft.runTick(boolean)}; the per-frame
 * render anchor {@code GameRenderer.render(FJZ)} became {@code GameRenderer.render(DeltaTracker, boolean)}.
 * The cutscene manager is still ticked once per rendered frame (for smooth camera-path interpolation),
 * just hung off the new anchor. Same outcome.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "runTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"))
    private void doCameraCutscene(boolean tick, CallbackInfo ci){
        SPBRevampedClient.getCutsceneManager().tick();
    }
}
