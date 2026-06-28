package com.sp.mixin;

import com.sp.SPBRevampedClient;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.gui.components.DebugScreenOverlay;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppress the F3 debug overlay for non-creative players in the backrooms (NEW-INJECTION).
 * Ported Yarn→mojmap, Phase 6f.
 *
 * <p>PORT: delta — {@code DebugHud}→{@code DebugScreenOverlay}; the immediate-mode {@code render(...)}
 * is gone — the debug overlay now feeds the deferred HUD via
 * {@code extractRenderState(GuiGraphicsExtractor)}. Cancelling that at HEAD prevents the overlay from
 * emitting any draw state. Same outcome (no F3 readout in the backrooms unless creative/spectator).
 */
@Mixin(DebugScreenOverlay.class)
public class DisableF3Mixin {

    @Inject(method = "extractRenderState(Lnet/minecraft/client/gui/GuiGraphicsExtractor;)V", at = @At("HEAD"), cancellable = true)
    private void disableF3(GuiGraphicsExtractor extractor, CallbackInfo ci){
        Player player = Minecraft.getInstance().player;

        if(player != null) {
            if (!player.isCreative() && !player.isSpectator() && SPBRevampedClient.isInBackrooms()) {
                ci.cancel();
            }
        }
    }

}
