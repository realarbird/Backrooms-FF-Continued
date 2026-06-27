package com.sp.render.gui;

import com.sp.SPBRevamped;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
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

        // Camera-effect master gate. PORT: phase6e — wired through a stub until SPBRevampedClient ports.
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

        // ---- (2) tiled animated grain ----------------------------------------------------------------
        drawGrain(graphics, w, h, delta);

        // ---- (3) scanlines ---------------------------------------------------------------------------
        drawScanlines(graphics, w, h);

        // ---- (4) vignette ----------------------------------------------------------------------------
        drawVignette(graphics, w, h);

        // ---- (5) chromatic-fringe approximation ------------------------------------------------------
        drawChromaticFringe(graphics, w, h);

        // ---- (6) camcorder UI ------------------------------------------------------------------------
        drawCamcorderUi(graphics, client.font, w, h, delta);

        // ---- (7) jumpscare faces ---------------------------------------------------------------------
        drawJumpscare(graphics, w, h);
    }

    // ----------------------------------------------------------------------------------------------------
    // Layer draws
    // ----------------------------------------------------------------------------------------------------

    /** Tiles vhs_noise.png across the screen, scrolling its UVs over time for a moving-grain look. */
    private void drawGrain(GuiGraphicsExtractor graphics, int w, int h, DeltaTracker delta) {
        float t = (System.currentTimeMillis() % 1000L) / 1000.0f;
        float scale = 2.0f; // how many noise tiles fit across the screen
        // Scroll the V coordinate to animate the grain.
        float v0 = t;
        float v1 = t + scale;
        // blit(Identifier, x, y, w, h, u0, u1, v0, v1) — u/v are normalized; values >1 wrap (texture repeats).
        graphics.blit(VHS_NOISE, 0, 0, w, h, 0.0f, scale, v0, v1);
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

    /** Draws the skinwalker jumpscare faces / escape text as full-screen blits when their flags are set. */
    private void drawJumpscare(GuiGraphicsExtractor graphics, int w, int h) {
        if (!jumpscare) {
            return;
        }
        if (creepyFace2) {
            // Late stage: jitter the face by the synced random offset for an unstable look.
            int dx = (int) ((faceRandX - 0.5f) * 16.0f);
            int dy = (int) ((faceRandY - 0.5f) * 16.0f);
            graphics.blit(CREEPY_FACE_2, dx, dy, w, h, 0.0f, 1.0f, 0.0f, 1.0f);
            graphics.blit(YOU_CANT_ESCAPE, 0, 0, w, h, 0.0f, 1.0f, 0.0f, 1.0f);
        } else if (creepyFace1) {
            graphics.blit(CREEPY_FACE_1, 0, 0, w, h, 0.0f, 1.0f, 0.0f, 1.0f);
        }
    }

    // ----------------------------------------------------------------------------------------------------
    // PORT: phase6e — state accessor stubs.
    // These read from SPBRevampedClient / ClientWrapper / the cutscene manager, none of which are ported
    // yet. Phase 6e should replace each body with the real source (noted per-method) and delete the stub
    // defaults. Until then the overlay renders with conservative defaults (enabled, not black, not escape).
    // ----------------------------------------------------------------------------------------------------

    /** PORT: phase6e — back with {@code SPBRevampedClient.shouldRenderCameraEffect()}. */
    private static boolean shouldRenderCameraEffect() {
        return cameraEffectEnabled;
    }

    /**
     * PORT: phase6e — back with
     * {@code SPBRevampedClient.blackScreen || (player.isInsideWall() && !cutscene.isPlaying) || component.isBeingReleased()}.
     */
    private static boolean isBlackScreen() {
        return blackScreen;
    }

    // Phase-6e-writable shared flags (wired by the client tick once SPBRevampedClient ports). Defaults
    // keep the overlay visible so 6c can be verified standalone.
    public static volatile boolean cameraEffectEnabled = true;
    public static volatile boolean blackScreen = false;
    /** PORT: phase6e — set from SPBRevampedClient.youCantEscape (used by the jumpscare stage). */
    public static volatile boolean youCantEscape = false;
}
