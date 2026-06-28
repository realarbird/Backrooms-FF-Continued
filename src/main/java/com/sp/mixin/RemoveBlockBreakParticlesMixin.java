package com.sp.mixin;

import com.sp.SPBRevampedClient;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Suppress block-break crack particles while the camcorder effect is active (NEW-INJECTION).
 * Ported Yarn→mojmap, Phase 6f.
 *
 * <p>PORT: delta — the target moved off the particle manager: {@code ParticleManager} (now
 * {@code ParticleEngine}) no longer has {@code addBlockBreakingParticles(BlockPos, Direction)}. In 26.2
 * the per-face destroy crack particles are spawned by {@code Level.addDestroyBlockEffect(BlockPos,
 * BlockState)} (overridden on {@code ClientLevel}). Cancelling that at HEAD suppresses them, matching the
 * original. Delta: the old hook keyed on the broken face ({@code Direction}); the new one keys on the
 * block state — functionally identical for whole-block break suppression.
 */
@Mixin(ClientLevel.class)
public class RemoveBlockBreakParticlesMixin {

    @Inject(method = "addDestroyBlockEffect", at = @At("HEAD"), cancellable = true)
    private void cancel(BlockPos pos, BlockState state, CallbackInfo ci) {
        if (SPBRevampedClient.shouldRenderCameraEffect()) {
            ci.cancel();
        }
    }
}
