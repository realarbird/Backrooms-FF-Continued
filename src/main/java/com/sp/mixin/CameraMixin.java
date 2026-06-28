package com.sp.mixin;

import com.sp.SPBRevampedClient;
import com.sp.compat.modmenu.ConfigStuff;
import com.sp.render.camera.CameraRoll;
import com.sp.render.camera.CameraShake;
import net.minecraft.client.Camera;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.gen.Invoker;

/**
 * Mac-safe camera shake / roll (A15). Ported Yarn→mojmap, Phase 6f.
 *
 * <p>PORT: delta — the original applied shake by mutating the camera in {@code Camera.update(...,float)}
 * and applied roll (a {@code POSITIVE_Z} view-matrix rotation) in {@code GameRenderer.renderWorld}.
 * Both of those host methods are gone in 26.2 (the deferred pipeline only has
 * {@code Camera.update(DeltaTracker)} and {@code GameRenderer.renderLevel(DeltaTracker)}; the immediate
 * {@code renderWorld}/{@code bobView} are gone — {@code GameRendererMixin} was dropped in 6f).
 *
 * <p>This re-homes BOTH effects onto {@code Camera.update(DeltaTracker)} @TAIL:
 * <ul>
 *   <li><b>Shake</b> — {@link CameraShake#tick(Camera)} samples {@code shakeYaw}/{@code shakePitch};
 *       we apply them via the protected {@code Camera.setRotation(float,float)} (reached with an
 *       {@code @Invoker} — no access-widener entry needed). {@code setRotation} rebuilds the rotation
 *       quaternion + forward/up/left vectors and marks the view matrices dirty.</li>
 *   <li><b>Roll</b> — {@code setRotation} can only express yaw+pitch (its Z term is hardcoded 0), so the
 *       view tilt is applied by post-multiplying the camera's live rotation quaternion
 *       ({@code Camera.rotation()} returns the mutable field) by a local-Z rotation. Because
 *       {@code setRotation} already marked {@code matrixPropertiesDirty}, the next
 *       {@code getViewRotationMatrix} (during the render pass, after this update) rebuilds the view
 *       matrix from the rolled quaternion — reproducing the old POSITIVE_Z view-matrix tilt. The
 *       forward/up/left vectors keep the un-rolled orientation, matching the original (which rolled only
 *       the render matrix, not the camera basis).</li>
 * </ul>
 * Roll degrees come from {@link CameraRoll#doCameraRoll(Player, float)} (which already folds in the
 * shake's Z component). Gated by {@code enableRealCamera && !cutscene} exactly as the old code was.
 */
@Mixin(Camera.class)
public abstract class CameraMixin {

    @Invoker("setRotation")
    protected abstract void spbRevamped$setRotation(float yaw, float pitch);

    @Inject(method = "update", at = @At("TAIL"))
    private void cameraShake(DeltaTracker deltaTracker, CallbackInfo ci) {
        if (!SPBRevampedClient.shouldRenderCameraEffect()) {
            return;
        }

        Camera camera = (Camera) (Object) this;
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;

        if (player == null) {
            return;
        }

        CameraShake cameraShake = SPBRevampedClient.getCameraShake();

        if (ConfigStuff.enableRealCamera
                && !SPBRevampedClient.getCutsceneManager().isPlaying
                && client.getCameraEntity() == player) {

            // Shake: reads camera.yRot()/xRot() internally and offsets via Perlin noise.
            cameraShake.tick(camera);
            spbRevamped$setRotation(cameraShake.shakeYaw, cameraShake.shakePitch);

            // Roll: post-rotate the view rotation quaternion about its local forward (Z) axis.
            float tickDelta = deltaTracker.getGameTimeDeltaPartialTick(true);
            float rollDegrees = CameraRoll.doCameraRoll(player, tickDelta);
            camera.rotation().rotateZ(rollDegrees * Mth.DEG_TO_RAD);
        }
    }
}
