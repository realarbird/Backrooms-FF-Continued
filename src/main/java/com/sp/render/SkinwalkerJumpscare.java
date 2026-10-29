package com.sp.render;

import com.sp.SPBRevampedClient;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.init.ModSounds;
import com.sp.render.gui.BackroomsHudOverlay;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.RandomSource;

/**
 * Skinwalker jumpscare timeline.
 *
 * <p>Port note (Phase 6c, Mac-safe rebuild): the original drove a full-screen creepy-face overlay by
 * poking Veil {@code ShaderProgram} uniforms ({@code Jumpscare}, {@code CreepyFace1}, {@code CreepyFace2},
 * {@code Rand}) on the {@code POST_VHS} program. Veil/GLSL is removed on 26.2 + macOS. The timeline,
 * sound and packet-sync logic below are kept verbatim; the uniform pokes are replaced by static flags on
 * {@link BackroomsHudOverlay}, which composites the faces as plain {@code blit} draws in the HUD layer.</p>
 */
public class SkinwalkerJumpscare {
    private static long startTime;
    private static boolean started = false;
    private static final RandomSource random = RandomSource.create(13);
    private static final RandomSource random2 = RandomSource.create(8767);

    private static SimpleSoundInstance jumpScareSound;

    /**
     * Advances the jumpscare timeline for the current frame. Call every frame while
     * {@link PlayerComponent#isBeingCaptured()} is true. Drives {@link BackroomsHudOverlay} flags instead
     * of Veil uniforms.
     */
    public static void doJumpscare(Minecraft client, PlayerComponent component) {
        if (!started) {
            startTime = System.currentTimeMillis();
            started = true;
            if (client.player != null) {
                jumpScareSound = new SimpleSoundInstance(ModSounds.JUMPSCARE, SoundSource.HOSTILE, 1.0f, 1.0f, client.player.getRandom(), client.player.blockPosition());
                client.getSoundManager().play(jumpScareSound);
            }
        }

        // PORT: phase7 — silence all other sounds during the jumpscare. The original called
        // ExtraUtils.stopAllOtherSounds(jumpScareSound.getIdentifier(), soundEngine). On 26.2 the
        // SoundManager#soundEngine field and SoundEngine#instanceToChannel map are private and need an
        // accessor mixin (sound subsystem, Phase 7). Re-wire the suppression there.

        // PORT: phase6f — hide the vanilla HUD for the duration. The Yarn Options#hudHidden field is gone
        // in 26.2 (the hide-gui boolean is now a per-frame parameter to Gui#extractRenderState). Drive it
        // through the shared client flag; a Gui#extractRenderState mixin reads it to suppress the HUD.
        BackroomsHudOverlay.setHudHidden(true);

        BackroomsHudOverlay.jumpscare = true;

        long currentTime = (System.currentTimeMillis() - startTime);

        if (currentTime >= 2000 && currentTime < 12000) {
            BackroomsHudOverlay.creepyFace1 = true;
        }

        if (currentTime >= 12000) {
            BackroomsHudOverlay.creepyFace2 = true;
            BackroomsHudOverlay.faceRandX = random.nextFloat();
            BackroomsHudOverlay.faceRandY = random2.nextFloat();
        }

        if (currentTime >= 14000) {
            component.setBeingCaptured(false);
            BackroomsHudOverlay.setHudHidden(false);
            started = false;
            startTime = 0L;

            // Tell the server the jumpscare is over. While this sync was commented out the server kept
            // isBeingCaptured=true, so every later component sync (sprint stamina, flashlight, release,
            // relog) re-sent it and the face replayed over and over (issues #1, #2).
            SPBRevampedClient.sendComponentSyncPacket(component.isBeingCaptured(), "beingCaptured");

            reset();
        }
    }

    /** Clears all jumpscare HUD flags. Called when the timeline ends or the capture state is cleared. */
    public static void reset() {
        // Reset the timeline too, so a later capture replays from the start (otherwise the stale
        // started/startTime would make doJumpscare immediately hit the end and skip the face).
        started = false;
        startTime = 0L;
        BackroomsHudOverlay.jumpscare = false;
        BackroomsHudOverlay.creepyFace1 = false;
        BackroomsHudOverlay.creepyFace2 = false;
        BackroomsHudOverlay.faceRandX = 0.0f;
        BackroomsHudOverlay.faceRandY = 0.0f;
    }
}
