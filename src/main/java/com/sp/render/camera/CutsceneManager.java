package com.sp.render.camera;

import com.sp.SPBRevampedClient;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.compat.modmenu.ConfigStuff;
import com.sp.init.BackroomsLevels;
import com.sp.init.ModSounds;
import com.sp.render.gui.BackroomsHudOverlay;
import com.sp.util.MathStuff;
import net.minecraft.client.Minecraft;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

/**
 * Level 0 fall cutscene (A16 KEEP). Gameplay/timing/black-screen logic preserved; ported Yarn→mojmap.
 *
 * <p>PORT: delta (A16) — the Veil camera-path layer ({@code foundry.veil.api.client.anim.Path/Frame/
 * Keyframe} + the {@code cutscene/PathAccessor} mixin) is DROPPED and replaced by the small in-repo
 * {@link CameraPath} keyframe container below. The original interpolation was a plain
 * segmented <i>linear</i> lerp over the keyframe positions/rotations
 * ({@code MathHelper.lerp(mod(timer*frames, 1), prev, current)}) — the per-keyframe easings and the
 * Veil tangent vectors were never actually applied — so {@link CameraPath} reproduces exactly that
 * (no Veil, no {@code Easings} needed for the path itself). Cutscene timing, triggers, sounds, the
 * fall/BackroomsBySP sequence and packet sync are unchanged.
 *
 * <p>PORT: delta — 26.2 removed {@code Options#hudHidden}; HUD suppression now goes through
 * {@link BackroomsHudOverlay#setHudHidden(boolean)} (the repo's established no-Veil HUD flag, read by
 * the 6f Gui mixin). {@code refreshPositionAndAngles}→{@code absSnapTo}, {@code cameraEntity}→
 * {@code setCameraEntity}, {@code PositionedSoundInstance.master}→{@code SimpleSoundInstance.forUI},
 * {@code pauseAll}/{@code resumeAll}→{@code pauseAllExcept()}/{@code resume()},
 * {@code getRegistryKey}→{@code dimension}.
 *
 * <p>PORT: phase6e-b — {@code SPBRevampedClient.blackScreen/youCantEscape/sendComponentSyncPacket}
 * are wired in the 6e-b dispatch.
 */
@SuppressWarnings("DataFlowIssue")
public class CutsceneManager {
    public boolean started;
    public boolean isPlaying;
    public boolean fall;
    private int prevLightRenderDistance;
    public boolean backroomsBySP;
    private long startTime;
    private final int duration;
    private final int duration2;
    public BlackScreen blackScreen;
    private Entity camera;
    private final CameraPath cameraPathPos;
    private final CameraPath cameraPathRotX;
    private final CameraPath cameraPathRotY;
    private final CameraPath cameraPathRotZ;
    public float cameraRotZ;
    private final Minecraft client;

    public CutsceneManager() {
        this.started = false;
        this.isPlaying = false;
        this.fall = false;
        this.backroomsBySP = false;
        this.camera = null;
        this.duration = 7000;
        this.duration2 = 5000;
        this.blackScreen = new BlackScreen();
        this.client = Minecraft.getInstance();
        this.cameraRotZ = 0;
        this.cameraPathPos = new CameraPath(List.of(
                new Keyframe(new Vec3(0.5, 220, 0.5), Vec3.ZERO),
                new Keyframe(new Vec3(0.5, 27, 0.5), Vec3.ZERO)
        ));
        this.cameraPathRotX = new CameraPath(List.of(
                new Keyframe(Vec3.ZERO, new Vec3(80, 0, 0)),
                new Keyframe(Vec3.ZERO, new Vec3(60, 0, 0)),
                new Keyframe(Vec3.ZERO, new Vec3(110, 0, 0))
        ));
        this.cameraPathRotY = new CameraPath(List.of(
                new Keyframe(Vec3.ZERO, new Vec3(0, 0, 0)),
                new Keyframe(Vec3.ZERO, new Vec3(0, 120, 0))
        ));
        this.cameraPathRotZ = new CameraPath(List.of(
                new Keyframe(Vec3.ZERO, new Vec3(0, 0, 20)),
                new Keyframe(Vec3.ZERO, new Vec3(0, 0, -20)),
                new Keyframe(Vec3.ZERO, new Vec3(0, 0, 0))
        ));
    }


    public void tick() {
        if (client.player != null && client.level != null) {
            PlayerComponent playerComponent = InitializeComponents.PLAYER.get(client.player);
            if (playerComponent.isDoingCutscene() && client.level.dimension() == BackroomsLevels.LEVEL0_WORLD_KEY) {
                this.pause();
                this.Fall();
                this.BackroomsBySP();
            } else {
                playerComponent.setDoingCutscene(false);
            }
            this.blackScreen.tick();
        }
    }

    private void pause() {
        if (!this.backroomsBySP && !this.fall) {
            if (!this.started) {
                this.blackScreen.showBlackScreen(60, true, false);
                this.startTime = System.currentTimeMillis();
                this.started = true;
            }
            float timer = (float) (System.currentTimeMillis() - this.startTime) / 2900;
            BackroomsHudOverlay.setHudHidden(true);
            if (timer >= 1.0) {
                this.fall = true;
            }
        }
    }

    private void Fall() {
        if (!this.backroomsBySP && this.fall) {
            if (!this.isPlaying) {
                this.prevLightRenderDistance = ConfigStuff.lightRenderDistance;
                ConfigStuff.lightRenderDistance = 1000;
                this.startTime = System.currentTimeMillis();
                this.isPlaying = true;
                client.getSoundManager().play(SimpleSoundInstance.forUI(ModSounds.FALLING, 1.0f));
            }
            float timer = (float) (System.currentTimeMillis() - this.startTime) / this.duration;
            if (this.camera == null) {
                this.initCamera();
            }
            if (timer >= 1.0) {
                this.blackScreen.showBlackScreen(50, true, false);
                this.backroomsBySP = true;
                this.startTime = System.currentTimeMillis() + 2500L;
                this.fall = false;
                camera.absSnapTo(3, 21, 1.5, 15, (float) 90);
                ConfigStuff.lightRenderDistance = this.prevLightRenderDistance;
            } else {
                BackroomsHudOverlay.setHudHidden(true);
                Vec3 newCameraPos = lerpedCameraPos(timer);
                Vec3 newCameraRot = lerpedCameraRot(timer);

                this.cameraRotZ = (float) newCameraRot.z;
                camera.absSnapTo(newCameraPos.x, newCameraPos.y, newCameraPos.z, (float) newCameraRot.y, (float) newCameraRot.x);
                client.setCameraEntity(camera);
            }
        }
    }

    private void BackroomsBySP() {
        if (this.backroomsBySP) {
            float timer = (float) (System.currentTimeMillis() - this.startTime) / this.duration2;

            if (timer >= 1.0) {
                this.blackScreen.showBlackScreen(40, true, false);
                this.reset();
            } else {
                BackroomsHudOverlay.setHudHidden(false);
                camera.absSnapTo(3, 21, 1.5, 5, (float) 83);
                this.cameraRotZ = 100;
                client.setCameraEntity(camera);
            }
        }
    }

    private Vec3 lerpedCameraRot(float timer) {
        double interpolateX = MathStuff.mod(timer * cameraPathRotX.size(), 1);
        double currentFrameRotX = cameraPathRotX.frameAtProgress(timer).rotation().x;
        double prevFrameRotX = cameraPathRotX.previousFrameAtProgress(timer).rotation().x;

        double interpolateY = MathStuff.mod(timer * cameraPathRotY.size(), 1);
        double currentFrameRotY = cameraPathRotY.frameAtProgress(timer).rotation().y;
        double prevFrameRotY = cameraPathRotY.previousFrameAtProgress(timer).rotation().y;

        double interpolateZ = MathStuff.mod(timer * cameraPathRotZ.size(), 1);
        double currentFrameRotZ = cameraPathRotZ.frameAtProgress(timer).rotation().z;
        double prevFrameRotZ = cameraPathRotZ.previousFrameAtProgress(timer).rotation().z;

        return new Vec3(
                Mth.lerp(interpolateX, prevFrameRotX, currentFrameRotX),
                Mth.lerp(interpolateY, prevFrameRotY, currentFrameRotY),
                Mth.lerp(interpolateZ, prevFrameRotZ, currentFrameRotZ)
        );
    }

    private Vec3 lerpedCameraPos(float timer) {
        double interpolatePos = MathStuff.mod(timer * cameraPathPos.size(), 1);
        Vec3 currentFramePos = cameraPathPos.frameAtProgress(timer).position();
        Vec3 prevFramePos = cameraPathPos.previousFrameAtProgress(timer).position();

        return new Vec3(
                Mth.lerp(interpolatePos, prevFramePos.x, currentFramePos.x),
                Mth.lerp(interpolatePos, prevFramePos.y, currentFramePos.y),
                Mth.lerp(interpolatePos, prevFramePos.z, currentFramePos.z)
        );
    }

    public void reset() {
        this.isPlaying = false;
        this.started = false;
        this.fall = false;
        this.backroomsBySP = false;
        if (this.camera != null) {
            this.camera.remove(Entity.RemovalReason.DISCARDED);
            this.camera = null;
        }
        BackroomsHudOverlay.setHudHidden(false);
        this.startTime = 0L;
        // PORT: client.player is null when reset() fires on disconnect — guard the player/component access.
        if (client.player != null) {
            client.setCameraEntity(client.player);
            PlayerComponent playerComponent = InitializeComponents.PLAYER.get(client.player);
            playerComponent.setDoingCutscene(false);
            SPBRevampedClient.sendComponentSyncPacket(playerComponent.isDoingCutscene(), "cutscene");
        }
    }

    private void initCamera() {
        this.camera = new ItemEntity(client.level, 1.5, 300, 1.5, ItemStack.EMPTY);
        this.camera.absSnapTo(1.5, 300, 1.5, 0, 90);
    }

    /**
     * A single camera keyframe: a position and a rotation (yaw/pitch/roll packed into a {@link Vec3}).
     * PORT: replaces Veil {@code anim.Keyframe} — only position + rotation are used by the linear lerp.
     */
    public record Keyframe(Vec3 position, Vec3 rotation) {
    }

    /**
     * Minimal in-repo replacement for Veil {@code anim.Path} + {@code PathAccessor} (A16).
     *
     * <p>Reproduces the original behaviour exactly: progress in {@code [0,1]} is split into
     * {@code frames.size()} equal segments; {@code frameAtProgress} picks the frame at
     * {@code floor(progress * size)} (clamped) and {@code previousFrameAtProgress} the one before it.
     * Callers do the actual linear interpolation between the two frames.
     */
    private static final class CameraPath {
        private final List<Keyframe> frames;

        CameraPath(List<Keyframe> frames) {
            this.frames = frames;
        }

        int size() {
            return frames.size();
        }

        Keyframe frameAtProgress(double progress) {
            int index = (int) (frames.size() * progress);
            if (index < 0) {
                return frames.get(0);
            }
            if (index >= frames.size()) {
                return frames.get(frames.size() - 1);
            }
            return frames.get(index);
        }

        Keyframe previousFrameAtProgress(double progress) {
            int index = (int) (frames.size() * progress) - 1;
            if (index < 0) {
                return frames.get(0);
            }
            if (index >= frames.size()) {
                return frames.get(frames.size() - 1);
            }
            return frames.get(index);
        }
    }


    public class BlackScreen {
        public boolean isBlackScreen;
        public boolean noEscape;
        private long duration;
        private long startTime;
        private boolean shouldPauseSounds;

        //Duration in ticks
        public BlackScreen() {
            this.startTime = 0L;
            this.isBlackScreen = false;
            this.duration = 0;
        }

        public void showBlackScreen(int time, boolean shouldPauseSounds, boolean noEscape) {
            this.duration = time * 50L;
            this.isBlackScreen = true;
            this.noEscape = noEscape;
            this.startTime = System.currentTimeMillis();
            this.shouldPauseSounds = shouldPauseSounds;
            BackroomsHudOverlay.setHudHidden(true);
        }

        //Tick
        public void tick() {
            if (isBlackScreen) {
                Minecraft client = Minecraft.getInstance();
                float timer = (float) (System.currentTimeMillis() - this.startTime) / this.duration;

                if (timer >= 1.0) {
                    SPBRevampedClient.blackScreen = false;
                    SPBRevampedClient.youCantEscape = false;
                    this.isBlackScreen = false;
                    BackroomsHudOverlay.setHudHidden(false);
                    this.startTime = 0;
                    client.getSoundManager().resume();
                } else {
                    if (shouldPauseSounds) {
                        client.getSoundManager().pauseAllExcept();
                    }
                    if (noEscape) {
                        SPBRevampedClient.youCantEscape = true;
                    }
                    BackroomsHudOverlay.setHudHidden(true);
                    SPBRevampedClient.blackScreen = true;
                }
            }
        }
    }
}
