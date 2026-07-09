package com.sp.render.camera;

import com.sp.SPBRevampedClient;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.compat.modmenu.ConfigStuff;
import com.sp.util.MathStuff;
import net.minecraft.client.Minecraft;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec2;

/**
 * Camera roll from yaw-spin + strafe (A15 KEEP) — pure math, no Veil. Ported Yarn→mojmap.
 *
 * <p>PORT: delta — {@code getYaw}→{@code getYRot}, {@code getVelocity}→{@code getDeltaMovement},
 * {@code Vec2f}→{@code Vec2}, frame duration via {@code DeltaTracker.getRealtimeDeltaTicks()}
 * (replacing {@code getLastFrameDuration}). Roll math unchanged.
 *
 * <p>PORT: phase6e-b — {@code SPBRevampedClient.getCameraShake()} is wired in the 6e-b dispatch.
 */
public class CameraRoll {
    static float prevYaw;
    static float rotAmount;
    static float spinRoll;

    static float strafeRoll;

    public static float doCameraRoll(Player player, float tickDelta) {
        if (player != null) {
            float yaw = player.getYRot(tickDelta);
            float lastFrameDuration = Minecraft.getInstance().getDeltaTracker().getRealtimeDeltaTicks();

            //Yaw roll
            rotAmount += yaw - prevYaw;
            spinRoll = MathStuff.Lerp(spinRoll, (rotAmount * 0.1f) * ConfigStuff.lookRollMultiplier, 0.8f, lastFrameDuration);
            rotAmount = MathStuff.Lerp(rotAmount, 0, 0.5f, lastFrameDuration);
            spinRoll = Mth.clamp(spinRoll, -20.0f, 20.0f);

            //Strafe Roll
            Vec2 velocity2D = MathStuff.get2DRelativeRotation(player.getDeltaMovement(), 360.0f - player.getYRot());
            strafeRoll = MathStuff.Lerp(strafeRoll, (-velocity2D.x * 5) * ConfigStuff.strafeRollMultiplier, 0.8f, lastFrameDuration);
            strafeRoll = Mth.clamp(strafeRoll, -20.0f, 20.0f);

            prevYaw = yaw;
        }
        return spinRoll + strafeRoll + warpRoll(player, tickDelta) + SPBRevampedClient.getCameraShake().getCameraZRot();
    }

    /**
     * Level-2 warp camera tumble (reference: SpacePotato's found-footage video). While the warp is
     * active {@code warpTimer} ramps 0..1 (client-side, in ClientWrapper); scale a big, disorienting
     * multi-frequency roll by it so the view tumbles like the video's fisheye warp, then settles as the
     * warp ends. Full-intensity amplitude per the requested look (~±35°, peaks to ~±50°). World game
     * time (always advancing) drives the oscillation.
     */
    private static float warpRoll(Player player, float tickDelta) {
        if (player == null) {
            return 0.0f;
        }
        PlayerComponent pc = InitializeComponents.PLAYER.get(player);
        if (pc == null || pc.getWarpTimer() <= 0.0f) {
            return 0.0f;
        }
        double t = player.level().getGameTime() + tickDelta;
        double tumble = Math.sin(t * 0.28) * 26.0 + Math.sin(t * 0.093) * 16.0 + Math.sin(t * 0.5) * 8.0;
        return pc.getWarpTimer() * (float) tumble;
    }
}
