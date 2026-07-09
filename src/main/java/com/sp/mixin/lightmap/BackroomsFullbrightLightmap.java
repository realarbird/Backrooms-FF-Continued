package com.sp.mixin.lightmap;

import com.sp.render.post.SpbLightGatherer;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Fullbright lightmap for the per-pixel-lit Backrooms levels.
 *
 * <p>While {@link SpbLightGatherer#deferredLightingActive} (post chain healthy + smooth lighting
 * on + camera in Level 0/1/2/324) the vanilla lightmap is forced to white: the world renders as
 * pure albedo and ALL lighting — fixture point lights, flashlight cone, ambient floor — is applied
 * per pixel by {@code backrooms_scene.fsh}. This is what kills the per-block lightmap banding.
 *
 * <p>The lightmap shader ({@code minecraft:core/lightmap}) computes
 * {@code max(AmbientColor, nightVision) + sky + block} then clamps to 1 — setting
 * {@code ambientColor} to white saturates every cell. {@code darknessEffectScale} is zeroed
 * because it is subtracted AFTER that clamp and would re-darken the albedo unevenly.
 *
 * <p>If the flag is off (chain failed to load, config disabled, other dimension) nothing is
 * touched — vanilla block lighting is the automatic fallback.
 */
@Mixin(LightmapRenderStateExtractor.class)
public class BackroomsFullbrightLightmap {

    private static final Vector3fc SPB_FULLBRIGHT = new Vector3f(1.0f, 1.0f, 1.0f);

    @Inject(method = "extract", at = @At("TAIL"))
    private void spbForceFullbrightLightmap(LightmapRenderState state, float partialTick, CallbackInfo ci) {
        boolean forced = SpbLightGatherer.deferredLightingActive;

        if (forced) {
            state.ambientColor = SPB_FULLBRIGHT;
            state.darknessEffectScale = 0.0f;
            state.needsUpdate = true;
        } else if (SpbLightGatherer.lightmapWasForced) {
            // One refresh on the frame the force drops, so the white texture doesn't linger.
            state.needsUpdate = true;
        }

        SpbLightGatherer.lightmapWasForced = forced;
    }
}
