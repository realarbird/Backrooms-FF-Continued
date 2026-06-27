package com.sp.mixin.lightmap;

import com.sp.init.BackroomsLevels;
import com.sp.render.PoolroomsDayCycle;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.renderer.LightmapRenderStateExtractor;
import net.minecraft.client.renderer.state.LightmapRenderState;
import org.joml.Vector3f;
import org.joml.Vector3fc;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Poolrooms day-cycle sky tint (Phase 6 / effect A5).
 *
 * <p>This makes the Poolrooms skylight color follow {@link PoolroomsDayCycle} over its day/night cycle,
 * while leaving the Darkrooms (and every other dimension) at vanilla skylight color.
 *
 * <p>// PORT: delta — the 1.20.1 original was a {@code @Redirect} of {@code Vector3f.lerp(Vector3fc,F)}
 * (ordinal 0) inside {@code LightmapTextureManager.update}. In 26.2 the lightmap was refactored:
 * {@code LightmapTextureManager}/{@code LightTexture} no longer exist as a per-frame updater; the skylight
 * color is now computed in {@code LightmapRenderStateExtractor.extract(LightmapRenderState, float)} via
 * {@code EnvironmentAttributeProbe.getValue(EnvironmentAttributes.SKY_LIGHT_COLOR, partialTick)} packed as
 * an RGB24 int through {@code ARGB.vector3fFromRGB24} and stored on {@code LightmapRenderState.skyLightColor}.
 * There is NO {@code Vector3f.lerp} INVOKE left to redirect (verified via javap — the method contains zero
 * {@code lerp} calls). Retargeted to a TAIL {@code @Inject} that post-multiplies the already-computed
 * {@code skyLightColor} by the day-cycle tint for the Poolrooms dimension only. {@code skyLightColor} is the
 * concrete {@code Vector3f} returned by {@code vector3fFromRGB24}, so it is multiplied in place.
 */
@Mixin(LightmapRenderStateExtractor.class)
public class PoolroomsSkylightColor {

    @Inject(method = "extract", at = @At("TAIL"))
    private void poolroomsSkylightTint(LightmapRenderState state, float partialTick, CallbackInfo ci) {
        ClientLevel clientWorld = Minecraft.getInstance().level;
        if (clientWorld == null) {
            return;
        }

        if (clientWorld.dimension() != BackroomsLevels.POOLROOMS_WORLD_KEY) {
            return;
        }

        Vector3fc skyColor = state.skyLightColor;
        if (skyColor instanceof Vector3f mutable) {
            // Tint the day-night skylight color by the Poolrooms day-cycle color ratio.
            mutable.mul(PoolroomsDayCycle.getLightColor());
        } else {
            // Defensive: if the field ever holds a non-mutable Vector3fc, replace it with a tinted copy.
            Vector3f tinted = new Vector3f(skyColor).mul(PoolroomsDayCycle.getLightColor());
            state.skyLightColor = tinted;
        }
    }
}
