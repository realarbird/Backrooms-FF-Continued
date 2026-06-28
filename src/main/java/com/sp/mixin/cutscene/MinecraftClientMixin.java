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
 * <p>PORT: delta — {@code MinecraftClient.render}→{@code Minecraft.renderFrame(boolean)}; the per-frame
 * render anchor {@code GameRenderer.render(FJZ)} became {@code GameRenderer.render(DeltaTracker, boolean)}.
 * In 26.2 the world-render call moved one level down: {@code runTick(boolean)} now delegates to
 * {@code renderFrame(boolean)}, and the lone {@code GameRenderer.render(DeltaTracker,boolean)} INVOKE
 * lives inside {@code renderFrame} (verified via javap: 0 such calls in {@code runTick}, exactly 1 in
 * {@code renderFrame}). Host method retargeted to {@code renderFrame(boolean)} so the cutscene manager is
 * still ticked once per rendered frame (for smooth camera-path interpolation), hung off the same render
 * INVOKE. Same outcome.
 */
@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    @Inject(method = "renderFrame", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/renderer/GameRenderer;render(Lnet/minecraft/client/DeltaTracker;Z)V"))
    private void doCameraCutscene(boolean tick, CallbackInfo ci){
        SPBRevampedClient.getCutsceneManager().tick();
    }
}
