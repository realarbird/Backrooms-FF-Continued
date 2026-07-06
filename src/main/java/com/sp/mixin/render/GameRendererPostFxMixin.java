package com.sp.mixin.render;

import com.sp.render.post.BackroomsPostEffects;
import net.minecraft.client.renderer.GameRenderer;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Activates the Backrooms post chain and writes its per-frame uniforms at the head of
 * renderLevel: the frame's CameraRenderState is already extracted here, and GameRenderer.render
 * processes postEffectId right after renderLevel returns — so matrices are same-frame fresh.
 */
@Mixin(GameRenderer.class)
public abstract class GameRendererPostFxMixin {

    @Inject(method = "renderLevel", at = @At("HEAD"))
    private void spbRevamped$updateBackroomsPostChain(CallbackInfo ci) {
        BackroomsPostEffects.onFrameStart((GameRenderer) (Object) this);
    }
}
