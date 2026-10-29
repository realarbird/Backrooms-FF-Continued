package com.sp.render.gui;

import com.sp.SPBRevamped;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;

/**
 * Mac-safe VHS / camcorder HUD overlay (Phase 6c, effect A1 + A2 + black-screen part of A15).
 *
 * <p>Replaces the deleted Veil GLSL post chain ({@code POST_VHS}, {@code everything}, {@code glitch},
 * {@code bloom}, {@code highlights}, {@code mixed}) and the {@code hudandresolution/InGameHudMixin}
 * overlay role with a single Fabric {@link HudElement} registered via
 * {@link HudElementRegistry#addLast(Identifier, HudElement)}. It composites, in order:</p>
 * <ol>
 *   <li>black-screen fill (camera-cut / in-wall / release beats);</li>
 *   <li>tiled animated grain ({@code textures/shaders/vhs_noise.png});</li>
 *   <li>horizontal scanlines (translucent {@code fill} rows);</li>
 *   <li>edge vignette ({@code fillGradient} on all four edges);</li>
 *   <li>a 2-px chromatic-fringe approximation (offset translucent red/blue edge fills);</li>
 *   <li>the camcorder UI: blinking REC dot + label, timestamp, battery readout;</li>
 *   <li>the skinwalker jumpscare faces ({@code creepy_face1/2.png}) and {@code you_cant_escape.png}.</li>
 * </ol>
 *
 * <p>Delta vs original: no true per-pixel bloom, lens distortion or full-screen chromatic warp; these are
 * approximated with overlays. Drawing uses {@link GuiGraphicsExtractor} primitives only — no GLSL.</p>
 */
public class BackroomsHudOverlay implements HudElement {

    public static final Identifier ID = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "vhs_overlay");

    private static final Identifier VHS_NOISE = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "textures/shaders/vhs_noise.png");

    /** vhs_noise.png native size (square). */
    private static final int NOISE_TEX = 256;
    /**
     * Grain tint — low-alpha white so the noise composites as a faint translucent shimmer over the world.
     * The simple {@code blit(...)} overload hardcodes an opaque (-1) tint, which painted the entire
     * colourful noise texture solid over the screen; routing through the colour-aware overload with this
     * tint is what makes it a grain overlay instead of a full-screen fill. Alpha 0x18 ≈ 9% (matches the
     * scanline strength); bump the alpha byte for heavier grain.
     */
    private static final int GRAIN_TINT = 0x18FFFFFF;
    private static final Identifier CREEPY_FACE_1 = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "textures/shaders/creepy_face1.png");
    private static final Identifier CREEPY_FACE_2 = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "textures/shaders/creepy_face2.png");
    private static final Identifier YOU_CANT_ESCAPE = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "textures/shaders/you_cant_escape.png");

    // ---- Jumpscare flags (written by com.sp.render.SkinwalkerJumpscare; read here) -----------------
    public static volatile boolean jumpscare = false;
    public static volatile boolean creepyFace1 = false;
    public static volatile boolean creepyFace2 = false;
    public static volatile float faceRandX = 0.0f;
    public static volatile float faceRandY = 0.0f;

    // ---- Shared client flags --------------------------------------------------------------------------
    // PORT: phase6f — vanilla HUD suppression. 26.2 has no Options#hudHidden; a Gui#extractRenderState
    // mixin should read this flag and pass hideGui=true. Set by jumpscare/cutscene/black-screen beats.
    private static volatile boolean hudHidden = false;

    public static void setHudHidden(boolean value) { hudHidden = value; }
    public static boolean isHudHidden() { return hudHidden; }

    private float blinkTimer = 0.0f;
    private boolean recOn = true;

    /** Registers the overlay in the HUD layer stack. Call from the client init entrypoint. */
    public static void register() {
        HudElementRegistry.addLast(ID, new BackroomsHudOverlay());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        if (client.player == null || client.level == null) {
            return;
        }

        // Camera-effect master gate (SPBRevampedClient.shouldRenderCameraEffect()).
        if (!shouldRenderCameraEffect()) {
            return;
        }

        int w = graphics.guiWidth();
        int h = graphics.guiHeight();

        // ---- (1) black-screen ------------------------------------------------------------------------
        if (isBlackScreen()) {
            graphics.fill(0, 0, w, h, 0xFF000000);
            // jumpscare faces still draw over black; everything else is hidden by the black fill.
            drawJumpscare(graphics, w, h);
            return;
        }

        // (2)-(5): grain/scanlines/fringe were the Mac-safe stand-ins for the Veil VHS shader.
        // When the real post chain (BackroomsPostEffects) is running, the shader does noise and
        // chromatic aberration properly — only the vignette overlay stays (the shader chain
        // computes but never applies one, faithful to the original vhs_post.fsh).
        if (!postShaderActive) {
            // ---- (2) tiled animated grain ------------------------------------------------------------
            drawGrain(graphics, w, h, delta);

            // ---- (3) scanlines -----------------------------------------------------------------------
            drawScanlines(graphics, w, h);
        }

        // ---- (4) vignette ----------------------------------------------------------------------------
        drawVignette(graphics, w, h);

        // ---- (5) chromatic-fringe approximation ------------------------------------------------------
        if (!postShaderActive) {
            drawChromaticFringe(graphics, w, h);
        }

        // ---- (6) camcorder UI ------------------------------------------------------------------------
        drawCamcorderUi(graphics, client.font, w, h, delta);

        // ---- (7) jumpscare faces ---------------------------------------------------------------------
        drawJumpscare(graphics, w, h);
    }

    // ----------------------------------------------------------------------------------------------------
    // Layer draws
    // ----------------------------------------------------------------------------------------------------

    /**
     * Tiles vhs_noise.png across the screen at low opacity, scrolling its UVs over time for moving grain.
     *
     * <p>Uses the colour-aware blit overload
     * {@code blit(pipeline, id, x, y, uOffset, vOffset, w, h, regionW, regionH, texW, texH, color)} so the
     * grain is tinted by {@link #GRAIN_TINT} (low-alpha white) and composites translucently. The simpler
     * {@code blit(id, x, y, w, h, u0, u1, v0, v1)} overload bakes an opaque (-1) tint, which drew the whole
     * colourful noise texture solid over the world.</p>
     *
     * <p>region = 2× the texture (uOffset/regionW chosen to reproduce the original u∈[0,2], v∈[t,t+2] UVs)
     * so the noise tiles ~2× across the screen and scrolls one period per second.</p>
     */
    private void drawGrain(GuiGraphicsExtractor graphics, int w, int h, DeltaTracker delta) {
        float t = (System.currentTimeMillis() % 1000L) / 1000.0f;
        float vOffsetPx = t * NOISE_TEX; // scroll V: v0 = vOffset/texH = t, v1 = (vOffset+regionH)/texH = t+2
        graphics.blit(
                RenderPipelines.GUI_TEXTURED, VHS_NOISE,
                0, 0,                          // x, y
                0.0f, vOffsetPx,               // uOffset, vOffset (px)
                w, h,                          // dest width, height (full screen)
                NOISE_TEX * 2, NOISE_TEX * 2,  // regionW, regionH (2× tile)
                NOISE_TEX, NOISE_TEX,          // texW, texH
                GRAIN_TINT);                   // low-alpha white → translucent grain
    }

    /** Draws faint translucent horizontal scanline rows every 3 px. */
    private void drawScanlines(GuiGraphicsExtractor graphics, int w, int h) {
        int lineColor = 0x18000000; // ~9% black
        for (int y = 0; y < h; y += 3) {
            graphics.fill(0, y, w, y + 1, lineColor);
        }
    }

    /** Edge vignette via four directional gradient fills fading to transparent toward the centre. */
    private void drawVignette(GuiGraphicsExtractor graphics, int w, int h) {
        int band = Math.max(24, Math.min(w, h) / 6);
        int dark = 0xB0000000; // opaque-ish black at the very edge
        int clear = 0x00000000;
        // top (dark -> clear downward)
        graphics.fillGradient(0, 0, w, band, dark, clear);
        // bottom (clear -> dark downward)
        graphics.fillGradient(0, h - band, w, h, clear, dark);
        // left and right approximated with thinner vertical bands of mid-strength black.
        graphics.fillGradient(0, 0, band / 2, h, 0x60000000, 0x60000000);
        graphics.fillGradient(w - band / 2, 0, w, h, 0x60000000, 0x60000000);
    }

    /** Cheap chromatic aberration: thin red fill on the left edge, blue on the right edge. */
    private void drawChromaticFringe(GuiGraphicsExtractor graphics, int w, int h) {
        graphics.fill(0, 0, 2, h, 0x30FF0000);
        graphics.fill(w - 2, 0, w, h, 0x300000FF);
    }

    /** REC dot + label (blinking), timestamp and battery readout, camcorder-corner style. */
    private void drawCamcorderUi(GuiGraphicsExtractor graphics, Font font, int w, int h, DeltaTracker delta) {
        // Blink the REC indicator roughly twice a second.
        blinkTimer += delta.getGameTimeDeltaTicks();
        if (blinkTimer >= 10.0f) {
            blinkTimer = 0.0f;
            recOn = !recOn;
        }

        int margin = 8;
        if (recOn) {
            // red REC dot
            graphics.fill(margin, margin, margin + 7, margin + 7, 0xFFFF1111);
            graphics.text(font, "REC", margin + 11, margin, 0xFFFFFFFF, true);
        }

        // Timestamp (top-right). Uses a translation key so resource packs/locales can restyle it.
        Component timestamp = Component.translatable("vhs.timestamp");
        int tw = font.width(timestamp);
        graphics.text(font, timestamp, w - tw - margin, margin, 0xFFFFFFFF, true);

        // Battery readout (bottom-right).
        Component battery = Component.translatable("vhs.battery");
        int bw = font.width(battery);
        graphics.text(font, battery, w - bw - margin, h - font.lineHeight - margin, 0xFFFFFFFF, true);
    }

    /**
     * Draws the skinwalker jumpscare faces / escape text as full-screen blits when their flags are set.
     *
     * <p>These PNGs are stored upside down: the original sampled them in the VHS post shader, whose texture
     * coordinates start at the bottom-left. The GUI's origin is top-left, so v runs 1 → 0 here to draw them
     * upright like the original did.</p>
     */
    private void drawJumpscare(GuiGraphicsExtractor graphics, int w, int h) {
        if (!jumpscare) {
            return;
        }
        if (creepyFace2) {
            // Late stage: jitter the face by the synced random offset for an unstable look.
            int dx = (int) ((faceRandX - 0.5f) * 16.0f);
            int dy = (int) ((faceRandY - 0.5f) * 16.0f);
            graphics.blit(CREEPY_FACE_2, dx, dy, w, h, 0.0f, 1.0f, 1.0f, 0.0f);
            graphics.blit(YOU_CANT_ESCAPE, 0, 0, w, h, 0.0f, 1.0f, 1.0f, 0.0f);
        } else if (creepyFace1) {
            graphics.blit(CREEPY_FACE_1, 0, 0, w, h, 0.0f, 1.0f, 1.0f, 0.0f);
        }
    }

    // ----------------------------------------------------------------------------------------------------
    // State accessors (Phase 6e-b: wired to SPBRevampedClient).
    // The master camera-effect gate reads SPBRevampedClient directly (single source of truth, valid even
    // before the first client tick). The black-screen / you-cant-escape / camera-effect flags are mirrored
    // each client tick by SPBRevampedClient's END_CLIENT_TICK handler (see SPBRevampedClient#onInitializeClient):
    //   blackScreen        = SPBRevampedClient.blackScreen || (player.isInWall() && !cutscene.isPlaying) || component.isBeingReleased()
    //   youCantEscape      = SPBRevampedClient.youCantEscape
    //   cameraEffectEnabled= SPBRevampedClient.shouldRenderCameraEffect()
    // ----------------------------------------------------------------------------------------------------

    private static boolean shouldRenderCameraEffect() {
        return com.sp.SPBRevampedClient.shouldRenderCameraEffect();
    }

    /** Black-screen state, composited by the client tick (see class header). */
    private static boolean isBlackScreen() {
        return blackScreen;
    }

    // Shared flags mirrored each client tick by SPBRevampedClient. The defaults keep the overlay
    // renderable if read before the first tick.
    public static volatile boolean cameraEffectEnabled = true;
    public static volatile boolean blackScreen = false;
    public static volatile boolean youCantEscape = false;
    /** True while the real Backrooms post chain is processing frames (set by BackroomsPostEffects). */
    public static volatile boolean postShaderActive = false;
}
