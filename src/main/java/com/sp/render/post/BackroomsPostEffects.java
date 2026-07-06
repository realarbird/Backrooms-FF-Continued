package com.sp.render.post;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
import com.mojang.blaze3d.framegraph.FrameGraphBuilder;
import com.mojang.blaze3d.pipeline.RenderTarget;
import com.mojang.blaze3d.systems.RenderSystem;
import com.sp.SPBRevamped;
import com.sp.SPBRevampedClient;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.compat.modmenu.ConfigStuff;
import com.sp.init.BackroomsLevels;
import com.sp.mixin.render.PostChainAccessor;
import com.sp.mixin.render.PostPassAccessor;
import com.sp.render.PoolroomsDayCycle;
import com.sp.render.gui.BackroomsHudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.renderer.GameRenderer;
import net.minecraft.client.renderer.LevelTargetBundle;
import net.minecraft.client.renderer.PostChain;
import net.minecraft.client.renderer.PostPass;
import net.minecraft.client.renderer.state.level.CameraRenderState;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import org.joml.Matrix4f;
import org.joml.Vector3f;
import org.lwjgl.system.MemoryStack;

import java.nio.ByteBuffer;
import java.util.Map;

/**
 * Drives the Backrooms post-effect chain ({@code assets/spb-revamped/post_effect/backrooms.json})
 * on the vanilla 26.2 post pipeline. This restores the Veil-era full-screen shader look (VHS
 * camcorder distortion/noise, bloom, per-level fog incl. Level 1 knee fog, poolrooms procedural
 * sky, level-2/skinwalker glitch) that Phase 6 originally dropped — rebuilt on vanilla
 * {@code PostChain}, which is Mac-safe (it is the same GL 3.3 pipeline vanilla itself runs on
 * macOS; no Veil, no compute/geometry shaders).
 *
 * <p>{@code GameRendererPostFxMixin} calls {@link #onFrameStart} at the head of
 * {@code GameRenderer.renderLevel} — after the frame's {@link CameraRenderState} is extracted —
 * to write the per-frame uniforms; {@code LevelRendererPostFxMixin} then adds the chain to the
 * level frame graph (NOT the {@code GameRenderer.setPostEffect} slot: that one is processed after
 * the hand pass has cleared the main depth buffer, which starves every depth-driven pass — fog,
 * SSAO, knee fog, sky detection. See that mixin's javadoc).</p>
 *
 * <p>Dynamic uniforms: vanilla {@code PostPass} bakes JSON uniform blocks into immutable
 * {@code GpuBuffer}s (usage {@code USAGE_UNIFORM} only). On first touch each pass's
 * {@code SpbDynamics} buffer is replaced (via {@link PostPassAccessor}) with one created
 * {@code USAGE_UNIFORM | USAGE_COPY_DST}, which is then rewritten in place every frame — the
 * same pattern vanilla's {@code GlobalSettingsUniform} uses. Resource reloads rebuild the chain
 * with a fresh read-only buffer; the usage check re-swaps it automatically.</p>
 */
public final class BackroomsPostEffects {
    public static final Identifier CHAIN_ID = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "backrooms");

    /** std140 size of the SpbDynamics block: 2 * mat4 (64) + 7 * vec4 (16). */
    private static final int DYNAMICS_SIZE = 2 * 64 + 7 * 16;

    /** Matches the shader's LDR bloom combine; the Veil chain's HDR bloom had no single knob. */
    private static final float BLOOM_STRENGTH = 0.35f;

    private BackroomsPostEffects() {
    }

    /** Set per frame by {@link #onFrameStart}; read by the LevelRenderer frame-graph hook. */
    private static boolean chainActiveThisFrame = false;

    /**
     * The per-frame hook only runs while a level is rendering; without this, leaving a world
     * would strand the HUD flag on over the title screen.
     */
    public static void onDisconnect(Minecraft client) {
        chainActiveThisFrame = false;
        BackroomsHudOverlay.postShaderActive = false;
    }

    /**
     * Called from {@code LevelRendererPostFxMixin} while the level frame graph is being built —
     * the only place the chain sees the live world depth buffer (see that mixin's javadoc).
     */
    public static void addToLevelFrame(FrameGraphBuilder frameGraphBuilder, LevelTargetBundle targets) {
        if (!chainActiveThisFrame) {
            return;
        }
        Minecraft client = Minecraft.getInstance();
        PostChain chain = client.getShaderManager().getPostChain(CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
        if (chain == null) {
            return;
        }
        RenderTarget main = client.gameRenderer.mainRenderTarget();
        chain.addToFrame(frameGraphBuilder, main.width, main.height, targets);
    }

    public static void onFrameStart(GameRenderer gameRenderer) {
        Minecraft client = Minecraft.getInstance();

        boolean active = client.level != null && client.player != null
                && SPBRevampedClient.shouldRenderCameraEffect();

        if (!active) {
            chainActiveThisFrame = false;
            BackroomsHudOverlay.postShaderActive = false;
            return;
        }

        PostChain chain = client.getShaderManager().getPostChain(CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
        if (chain == null) {
            // Shader failed to compile/parse on this system — fall back to the Phase-6c HUD
            // approximation (grain/scanlines) so the player still gets the camcorder feel.
            chainActiveThisFrame = false;
            BackroomsHudOverlay.postShaderActive = false;
            return;
        }
        chainActiveThisFrame = true;
        BackroomsHudOverlay.postShaderActive = true;

        CameraRenderState cam = gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
        if (!cam.initialized || cam.projectionMatrix == null || cam.viewRotationMatrix == null) {
            return;
        }

        Matrix4f invProj = new Matrix4f(cam.projectionMatrix).invert();
        Matrix4f invViewRot = new Matrix4f(cam.viewRotationMatrix).invert();

        // ---- Per-level look (fog / sky / knee fog / grade), matching the Veil chain's
        // per-dimension defines plus depth-only stand-ins for its deferred tonal character ----
        Vector3f fogColor = new Vector3f();
        float fogMode = 0.0f;     // 0 off, 1 linear, 2 exp
        float fogStart = 0.0f;
        float fogEnd = 90.0f;
        float fogDensity = 0.0f;
        float skyMode = 0.0f;     // 0 off, 1 procedural clouds, 2 solid black
        float kneeFog = 0.0f;
        float sunsetTimer = 0.0f;
        Vector3f sunDir = new Vector3f(0.0f, 1.0f, 0.0f);

        // Neutral-ish defaults for non-Backrooms dimensions (the VHS chain runs everywhere):
        // a touch of film S-curve and ambient occlusion, no tint.
        Vector3f tint = new Vector3f(1.0f, 1.0f, 1.0f);
        float saturation = 1.0f;
        float sCurve = 0.15f;
        float lift = 0.0f;
        float aoStrength = 0.45f;
        float rayStrength = 0.0f;
        float bloomStrength = BLOOM_STRENGTH;
        // The Veil composite ran `color *= blur(SSAO) * 2.0` — a 2x exposure lift over the raw
        // lightmap. Without a boost the fog + S-curve crush unlit Backrooms areas to void black.
        float exposure = 1.0f;

        ResourceKey<Level> dim = client.level.dimension();
        if (dim == BackroomsLevels.LEVEL0_WORLD_KEY) {
            // Not in the original chain (its depth came from deferred lights/SSAO, both dropped):
            // a warm exponential falloff into near-black stands in for the lost distance darkening.
            if (ConfigStuff.enableLevelFog) {
                fogMode = 2.0f;
                fogDensity = 0.030f;
                fogColor.set(0.034f, 0.028f, 0.012f);
            }
            tint.set(1.08f, 1.03f, 0.85f); // sickly warm yellow
            saturation = 1.12f;
            sCurve = 0.32f;
            aoStrength = 0.75f;
            bloomStrength = 0.45f;
            exposure = 1.9f;
        } else if (dim == BackroomsLevels.LEVEL1_WORLD_KEY) {
            kneeFog = ConfigStuff.enableLevel1Fog ? 1.0f : 0.0f;
            // Parking-garage haze above the knee fog.
            if (ConfigStuff.enableLevelFog) {
                fogMode = 2.0f;
                fogDensity = 0.020f;
                fogColor.set(0.30f, 0.32f, 0.34f);
            }
            tint.set(0.94f, 0.97f, 1.03f); // cold damp concrete
            saturation = 0.88f;
            sCurve = 0.28f;
            lift = 0.01f;
            aoStrength = 0.75f;
            exposure = 1.55f;
        } else if (dim == BackroomsLevels.LEVEL2_WORLD_KEY) {
            // Tight service corridors falling off into darkness.
            if (ConfigStuff.enableLevelFog) {
                fogMode = 2.0f;
                fogDensity = 0.045f;
                fogColor.set(0.012f, 0.014f, 0.020f);
            }
            tint.set(0.90f, 0.95f, 1.06f);
            saturation = 0.85f;
            sCurve = 0.38f;
            aoStrength = 0.85f;
            exposure = 1.6f;
        } else if (dim == BackroomsLevels.INFINITE_FIELD_WORLD_KEY) {
            // vhs/mixed.fsh INFINITE_FIELD: linear_fog(-10, 90, 0.8 grey) + procedural sky.
            if (ConfigStuff.enableLevelFog) {
                fogMode = 1.0f;
                fogStart = -10.0f;
                fogEnd = 90.0f;
                fogColor.set(0.8f, 0.8f, 0.8f);
            }
            skyMode = 1.0f;
            sunsetTimer = 0.02f; // permanent late morning
            sunDir.set(0.3f, 0.9f, 0.2f).normalize();
            tint.set(1.02f, 1.05f, 0.97f);
            saturation = 1.08f;
            sCurve = 0.18f;
            lift = 0.04f;      // lifted blacks: hazy overexposed film
            aoStrength = 0.35f;
            rayStrength = 0.35f;
            exposure = 1.1f;
        } else if (dim == BackroomsLevels.LEVEL324_WORLD_KEY) {
            // vhs/mixed.fsh LEVEL324: linear_fog(-10, 90, black) under a black sky.
            if (ConfigStuff.enableLevelFog) {
                fogMode = 1.0f;
                fogStart = -10.0f;
                fogEnd = 90.0f;
                fogColor.set(0.0f, 0.0f, 0.0f);
            }
            skyMode = 2.0f;
            tint.set(0.96f, 0.97f, 1.02f);
            saturation = 0.90f;
            sCurve = 0.35f;
            aoStrength = 0.65f;
            exposure = 1.35f;
        } else if (dim == BackroomsLevels.POOLROOMS_WORLD_KEY) {
            skyMode = 1.0f;
            sunsetTimer = PoolroomsDayCycle.getDayTime(client.level);
            float sunAngle = (float) Math.toRadians(PoolroomsDayCycle.getSunAngle());
            sunDir.set((float) Math.cos(sunAngle), (float) Math.sin(sunAngle), 0.05f).normalize();
            tint.set(1.05f, 1.01f, 0.94f); // warm sunlit water
            saturation = 1.12f;
            sCurve = 0.24f;
            lift = 0.02f;
            aoStrength = 0.55f;
            bloomStrength = 0.5f;
            // Volumetric-light stand-in; off during the night band of the day cycle.
            boolean night = sunsetTimer > 0.32f && sunsetTimer < 0.68f;
            rayStrength = (night || !ConfigStuff.enableVolumetricLight) ? 0.0f : 0.55f;
            exposure = 1.15f;
        }

        // ---- Sun screen position for the god-ray march (only when rays are on) ----
        float sunU = 0.5f;
        float sunV = 0.5f;
        float sunOnScreen = 0.0f;
        if (rayStrength > 0.0f) {
            Vector3f sunView = new Matrix4f(cam.viewRotationMatrix).transformDirection(new Vector3f(sunDir));
            if (sunView.z < -0.01f) { // in front of the camera (view space looks down -z)
                org.joml.Vector4f clip = new Matrix4f(cam.projectionMatrix)
                        .transform(new org.joml.Vector4f(sunView.x, sunView.y, sunView.z, 0.0f));
                sunU = clip.x / clip.w * 0.5f + 0.5f;
                sunV = clip.y / clip.w * 0.5f + 0.5f;
                // Fade the effect out as the sun leaves the screen instead of hard-cutting.
                float edge = Math.min(Math.min(sunU, 1.0f - sunU), Math.min(sunV, 1.0f - sunV));
                sunOnScreen = Math.max(0.0f, Math.min(1.0f, (edge + 0.3f) / 0.3f));
            }
        }

        float glitchTime = 0.0f;
        PlayerComponent playerComponent = InitializeComponents.PLAYER.get(client.player);
        if (playerComponent != null) {
            glitchTime = Math.max(0.0f, Math.min(1.0f, playerComponent.getGlitchTimer()));
        }

        float distortion = ConfigStuff.VHSDistortionMultiplier;
        float bloom = ConfigStuff.enableBloom ? bloomStrength : 0.0f;

        try (MemoryStack stack = MemoryStack.stackPush()) {
            // Order/types must match the SpbDynamics block in shaders/include/spb_post.glsl
            // and the default value list in post_effect/backrooms.json.
            ByteBuffer data = Std140Builder.onStack(stack, DYNAMICS_SIZE)
                    .putMat4f(invProj)
                    .putMat4f(invViewRot)
                    .putVec4(fogColor.x, fogColor.y, fogColor.z, fogMode)
                    .putVec4(fogStart, fogEnd, fogDensity, skyMode)
                    .putVec4(sunsetTimer, kneeFog, glitchTime, distortion)
                    .putVec4(sunDir.x, sunDir.y, sunDir.z, bloom)
                    .putVec4(tint.x, tint.y, tint.z, saturation)
                    .putVec4(sCurve, lift, aoStrength, rayStrength)
                    .putVec4(sunU, sunV, sunOnScreen, exposure)
                    .get();

            writeDynamics(chain, data);
        }
    }

    private static void writeDynamics(PostChain chain, ByteBuffer data) {
        for (PostPass pass : ((PostChainAccessor) chain).spbRevamped$getPasses()) {
            Map<String, GpuBuffer> uniforms = ((PostPassAccessor) pass).spbRevamped$getCustomUniforms();
            GpuBuffer buffer = uniforms.get("SpbDynamics");
            if (buffer == null) {
                continue;
            }

            if ((buffer.usage() & GpuBuffer.USAGE_COPY_DST) == 0) {
                // First frame after (re)load: replace the pass's baked read-only buffer with a
                // writable one. The pass closes whatever is in the map on chain teardown.
                GpuBuffer writable = RenderSystem.getDevice().createBuffer(
                        () -> "spb-revamped backrooms post dynamics",
                        GpuBuffer.USAGE_UNIFORM | GpuBuffer.USAGE_COPY_DST,
                        data);
                uniforms.put("SpbDynamics", writable);
                buffer.close();
            } else {
                RenderSystem.getDevice().createCommandEncoder().writeToBuffer(buffer.slice(), data);
            }

            data.rewind();
        }
    }
}
