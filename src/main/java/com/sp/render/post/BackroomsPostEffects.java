package com.sp.render.post;

import com.mojang.blaze3d.buffers.GpuBuffer;
import com.mojang.blaze3d.buffers.Std140Builder;
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
 * <p>Called once per frame from {@code GameRendererPostFxMixin} at the head of
 * {@code GameRenderer.renderLevel} — after the frame's {@link CameraRenderState} is extracted and
 * before {@code GameRenderer.render} processes {@code postEffectId} — so the chain is activated
 * and its per-frame uniforms written in the same frame they are used.</p>
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

    /** std140 size of the SpbDynamics block: 2 * mat4 (64) + 4 * vec4 (16). */
    private static final int DYNAMICS_SIZE = 2 * 64 + 4 * 16;

    /** Matches the shader's LDR bloom combine; the Veil chain's HDR bloom had no single knob. */
    private static final float BLOOM_STRENGTH = 0.35f;

    private BackroomsPostEffects() {
    }

    /**
     * The per-frame hook only runs while a level is rendering; without this, leaving a world
     * would strand the chain active over the title screen.
     */
    public static void onDisconnect(Minecraft client) {
        if (client.gameRenderer != null && CHAIN_ID.equals(client.gameRenderer.currentPostEffect())) {
            client.gameRenderer.clearPostEffect();
        }
        BackroomsHudOverlay.postShaderActive = false;
    }

    public static void onFrameStart(GameRenderer gameRenderer) {
        Minecraft client = Minecraft.getInstance();

        boolean active = client.level != null && client.player != null
                && SPBRevampedClient.shouldRenderCameraEffect();

        Identifier current = gameRenderer.currentPostEffect();
        if (!active) {
            if (CHAIN_ID.equals(current)) {
                gameRenderer.clearPostEffect();
            }
            BackroomsHudOverlay.postShaderActive = false;
            return;
        }

        if (!CHAIN_ID.equals(current)) {
            gameRenderer.setPostEffect(CHAIN_ID); // access-widened; also sets effectActive
        }

        PostChain chain = client.getShaderManager().getPostChain(CHAIN_ID, LevelTargetBundle.MAIN_TARGETS);
        if (chain == null) {
            // Shader failed to compile/parse on this system — GameRenderer will skip processing
            // too; fall back to the Phase-6c HUD approximation (grain/scanlines) so the player
            // still gets the camcorder feel.
            BackroomsHudOverlay.postShaderActive = false;
            return;
        }
        BackroomsHudOverlay.postShaderActive = true;

        CameraRenderState cam = gameRenderer.gameRenderState().levelRenderState.cameraRenderState;
        if (!cam.initialized || cam.projectionMatrix == null || cam.viewRotationMatrix == null) {
            return;
        }

        Matrix4f invProj = new Matrix4f(cam.projectionMatrix).invert();
        Matrix4f invViewRot = new Matrix4f(cam.viewRotationMatrix).invert();

        // ---- Per-level look (fog / sky / knee fog), matching the Veil chain's per-dimension defines ----
        Vector3f fogColor = new Vector3f();
        float fogMode = 0.0f;     // 0 off, 1 linear, 2 exp
        float fogStart = 0.0f;
        float fogEnd = 90.0f;
        float fogDensity = 0.0f;
        float skyMode = 0.0f;     // 0 off, 1 procedural clouds, 2 solid black
        float kneeFog = 0.0f;
        float sunsetTimer = 0.0f;
        Vector3f sunDir = new Vector3f(0.0f, 1.0f, 0.0f);

        ResourceKey<Level> dim = client.level.dimension();
        if (dim == BackroomsLevels.LEVEL0_WORLD_KEY) {
            // Not in the original chain (its depth came from deferred lights/SSAO, both dropped):
            // a soft warm exponential falloff stands in for the lost distance darkening.
            if (ConfigStuff.enableLevelFog) {
                fogMode = 2.0f;
                fogDensity = 0.028f;
                fogColor.set(0.10f, 0.09f, 0.055f);
            }
        } else if (dim == BackroomsLevels.LEVEL1_WORLD_KEY) {
            kneeFog = ConfigStuff.enableLevel1Fog ? 1.0f : 0.0f;
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
        } else if (dim == BackroomsLevels.LEVEL324_WORLD_KEY) {
            // vhs/mixed.fsh LEVEL324: linear_fog(-10, 90, black) under a black sky.
            if (ConfigStuff.enableLevelFog) {
                fogMode = 1.0f;
                fogStart = -10.0f;
                fogEnd = 90.0f;
                fogColor.set(0.0f, 0.0f, 0.0f);
            }
            skyMode = 2.0f;
        } else if (dim == BackroomsLevels.POOLROOMS_WORLD_KEY) {
            skyMode = 1.0f;
            sunsetTimer = PoolroomsDayCycle.getDayTime(client.level);
            float sunAngle = (float) Math.toRadians(PoolroomsDayCycle.getSunAngle());
            sunDir.set((float) Math.cos(sunAngle), (float) Math.sin(sunAngle), 0.05f).normalize();
        }
        // level2 / non-backrooms dimensions: VHS + bloom only.

        float glitchTime = 0.0f;
        PlayerComponent playerComponent = InitializeComponents.PLAYER.get(client.player);
        if (playerComponent != null) {
            glitchTime = Math.max(0.0f, Math.min(1.0f, playerComponent.getGlitchTimer()));
        }

        float distortion = ConfigStuff.VHSDistortionMultiplier;
        float bloom = ConfigStuff.enableBloom ? BLOOM_STRENGTH : 0.0f;

        writeDynamics(chain, invProj, invViewRot,
                fogColor, fogMode, fogStart, fogEnd, fogDensity, skyMode,
                sunsetTimer, kneeFog, glitchTime, distortion, sunDir, bloom);
    }

    private static void writeDynamics(PostChain chain, Matrix4f invProj, Matrix4f invViewRot,
                                      Vector3f fogColor, float fogMode, float fogStart, float fogEnd,
                                      float fogDensity, float skyMode, float sunsetTimer, float kneeFog,
                                      float glitchTime, float distortion, Vector3f sunDir, float bloom) {
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
                    .get();

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
}
