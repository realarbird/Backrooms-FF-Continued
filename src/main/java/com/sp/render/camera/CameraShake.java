package com.sp.render.camera;

import com.sp.SPBRevampedClient;
import com.sp.compat.modmenu.ConfigStuff;
import com.sp.util.MathStuff;
import net.minecraft.client.Camera;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.levelgen.synth.ImprovedNoise;
import net.minecraft.util.RandomSource;

/**
 * Perlin-noise camera shake (A15 KEEP) — pure math, no Veil. Ported Yarn→mojmap.
 *
 * <p>PORT: delta — 26.2's {@link Camera#setRotation} is {@code protected} and unreachable from this
 * package, so {@code tick(Camera)} no longer mutates the camera directly. Instead it reads the
 * camera's current yaw/pitch and stores the shaken target rotation in {@link #shakeYaw}/
 * {@link #shakePitch} (and the roll in {@link #cameraZRot}); the camera-update mixin
 * ({@code mixin/CameraMixin}, rebuilt in 6f) applies them via {@code setRotation}. The noise math is
 * unchanged.
 *
 * <p>PORT: delta — the Yarn per-tick speed proxy {@code (horizontalSpeed - prevHorizontalSpeed) * 6}
 * is replaced by {@code walkAnimation.speed(tickDelta) * 6} (26.2 dropped the {@code prevHorizontalSpeed}
 * field; the {@code WalkAnimationState} step speed is the closest equivalent). Frame duration uses
 * {@code DeltaTracker.getRealtimeDeltaTicks()} (replacing {@code getLastFrameDuration}).
 *
 * <p>PORT: phase6e-b — {@code SPBRevampedClient.getCutsceneManager()} is wired in the 6e-b dispatch.
 */
public class CameraShake {
    public double trauma;
    private double traumaGoal;
    public double noiseSpeed;
    private double noiseSpeedGoal;
    private double noiseY;
    private double amplitude;
    private final ImprovedNoise noiseSampler;
    private float cameraZRot;

    /** Target yaw/pitch (camera's current rotation + shake offset) for the 6f CameraMixin to apply. */
    public float shakeYaw;
    public float shakePitch;

    public CameraShake() {
        this.trauma = 0.1;
        this.noiseSpeed = 0.1;
        this.noiseY = 0;
        this.amplitude = 5;
        this.noiseSampler = new ImprovedNoise(RandomSource.create());
        this.cameraZRot = 0.0f;
    }

    public void tick(Camera camera) {
        this.shakeYaw = camera.yRot();
        this.shakePitch = camera.xRot();

        if (ConfigStuff.enableRealCamera && !SPBRevampedClient.getCutsceneManager().isPlaying) {
            float frameDelta = Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();
            if (this.noiseY >= 1000) {
                this.noiseY = 0;
            }

            Player player = Minecraft.getInstance().player;
            if (player != null) {
                // PORT: delta — WalkAnimationState step speed replaces (horizontalSpeed - prevHorizontalSpeed).
                float playerSpeed = player.walkAnimation.speed(1.0f) * 6;

                if (player.isFallFlying()) playerSpeed /= 6; // https://github.com/SpacePotatoee/MinecraftFoundFootage/issues/101

                this.traumaGoal = Mth.clamp(0.6 * playerSpeed, 0.5, 1.5f);
                this.noiseSpeedGoal = Mth.clamp(0.25 * playerSpeed, 0.1, 1.0f);
                this.amplitude = 4;

                this.trauma = Math.max(MathStuff.Lerp((float) this.trauma, (float) this.traumaGoal, 0.93f, frameDelta), 0.5);
                this.noiseSpeed = Math.max(MathStuff.Lerp((float) this.noiseSpeed, (float) this.noiseSpeedGoal, 0.93f, frameDelta), 0.1);

                this.noiseY += (this.noiseSpeed * frameDelta);

                double pitchOffset = this.amplitude * this.getShakeIntensity() * (this.noiseSampler.noise(1, this.noiseY, 0));
                double yawOffset = this.amplitude * this.getShakeIntensity() * (this.noiseSampler.noise(73, this.noiseY, 0));
                double rollOffset = this.amplitude * this.getShakeIntensity() * (this.noiseSampler.noise(146, this.noiseY, 0));

                this.shakeYaw = (float) (camera.yRot() + yawOffset);
                this.shakePitch = (float) (camera.xRot() + pitchOffset);
                this.cameraZRot = (float) rollOffset * 2;
            }
        }
    }

    private double getShakeIntensity() {
        return this.trauma * this.trauma;
    }

    public float getCameraZRot() {
        return this.cameraZRot;
    }
}
