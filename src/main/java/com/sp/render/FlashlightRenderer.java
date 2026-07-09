package com.sp.render;

import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.LightBlock;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.BlockHitResult;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

/**
 * Mac-safe flashlight rebuild (Phase 6 / effect A3).
 *
 * <p>Veil original projected a deferred {@code AreaLight} (a slerp-oriented spot, brightness/distance/angle)
 * per player along the look vector. Veil has no 26.2 build and macOS cannot run its GLSL deferred pipeline,
 * so the cone is replaced by a single moving vanilla light source: a transient {@link Blocks#LIGHT}
 * (LightBlock, {@link LightBlock#LEVEL} 0-15) placed in an air block a few blocks ahead along the camera
 * look-vector and re-placed each frame as the player turns/moves; the previous light block is cleared back
 * to air as it moves.
 *
 * <p>If LambDynamicLights is installed we defer entirely to it (it already lights the held item / player),
 * matching the original soft-dependency intent and avoiding a double light.
 *
 * <p>// PORT: delta — this is a moving point light, NOT a true cone/spot: no soft penumbra, no 25-block
 * throw, no per-other-player lights. Per MAC_SAFE_RENDERING.md (A3) the flashlight drops to self-only.
 * The {@code renderFlashlightForEveryPlayer} / {@code clearFlashlights} signatures are kept so the
 * SPBRevampedClient (phase 6e) tick wiring is unchanged.
 */
public class FlashlightRenderer {
    private final Minecraft client;
    private final RandomSource random;
    private final boolean lambDynLightsPresent;

    // The block position currently holding our transient flashlight light block (null = none placed).
    private BlockPos currentLightPos;

    public FlashlightRenderer() {
        this.client = Minecraft.getInstance();
        this.random = RandomSource.create();
        this.lambDynLightsPresent = FabricLoader.getInstance().isModLoaded("lambdynlights");
    }

    /**
     * Kept name/signature from the Veil version (called once per frame from SPBRevampedClient, phase 6e).
     * Self-only now: only the local player projects a flashlight light block.
     */
    public void renderFlashlightForEveryPlayer(float partialTicks) {
        // While the post chain relights the scene per pixel, the flashlight is a smooth shader
        // spotlight (backrooms_scene.fsh) — a vanilla light block would just re-add the blocky
        // per-block falloff this path exists to remove.
        if (com.sp.render.post.SpbLightGatherer.deferredLightingActive) {
            clearLightBlock(client.level);
            return;
        }

        // If LambDynamicLights handles dynamic lighting, defer to it entirely.
        if (lambDynLightsPresent) {
            return;
        }

        ClientLevel level = client.level;
        LocalPlayer player = client.player;
        if (level == null || player == null) {
            clearLightBlock(level);
            return;
        }

        PlayerComponent playerComponent = InitializeComponents.PLAYER.get(player);
        if (!playerComponent.isFlashLightOn()) {
            clearLightBlock(level);
            return;
        }

        // Project a point a few blocks ahead along the eye look-vector, raycast-clamped so the light
        // doesn't end up inside a wall.
        Vec3 eye = player.getEyePosition(partialTicks);
        Vec3 look = player.getViewVector(partialTicks);
        double throwDistance = 4.0;
        Vec3 end = eye.add(look.scale(throwDistance));

        BlockHitResult hit = level.clip(new ClipContext(
                eye, end, ClipContext.Block.COLLIDER, ClipContext.Fluid.NONE, player));

        Vec3 target;
        if (hit.getType() == HitResult.Type.BLOCK) {
            // Step back from the surface so the light sits in the air block in front of the wall.
            target = hit.getLocation().subtract(look.scale(0.6));
        } else {
            target = end;
        }

        BlockPos targetPos = BlockPos.containing(target);

        // Only place into a block we can safely replace (air or our own previously-placed light).
        if (!canPlaceLightAt(level, targetPos)) {
            // Fall back to the eye position's block if the projected spot is blocked.
            targetPos = player.blockPosition();
            if (!canPlaceLightAt(level, targetPos)) {
                clearLightBlock(level);
                return;
            }
        }

        // Flicker: occasionally drop the emitted level, like the Veil brightness dip, otherwise full 15.
        int lightLevel = LightBlock.MAX_LEVEL;
        if (random.nextInt(40) == 0) {
            lightLevel = 11 + random.nextInt(4); // 11..14, a subtle dip
        }

        if (!targetPos.equals(currentLightPos)) {
            clearLightBlock(level);
            placeLightBlock(level, targetPos, lightLevel);
            currentLightPos = targetPos.immutable();
        } else {
            // Same cell: refresh the level for the flicker without moving.
            placeLightBlock(level, targetPos, lightLevel);
        }
    }

    private boolean canPlaceLightAt(ClientLevel level, BlockPos pos) {
        BlockState state = level.getBlockState(pos);
        Block block = state.getBlock();
        return state.isAir() || block == Blocks.LIGHT;
    }

    private void placeLightBlock(ClientLevel level, BlockPos pos, int lightLevel) {
        BlockState light = Blocks.LIGHT.defaultBlockState().setValue(LightBlock.LEVEL, lightLevel);
        // Client-only cosmetic light: UPDATE_CLIENTS flag, no neighbour/physics notifications.
        level.setBlock(pos, light, net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
    }

    /** Removes the transient light block we placed (if any), restoring air. */
    private void clearLightBlock(ClientLevel level) {
        if (currentLightPos != null && level != null) {
            if (level.getBlockState(currentLightPos).getBlock() == Blocks.LIGHT) {
                level.setBlock(currentLightPos, Blocks.AIR.defaultBlockState(),
                        net.minecraft.world.level.block.Block.UPDATE_CLIENTS);
            }
        }
        currentLightPos = null;
    }

    /** Kept name/signature from the Veil version (called on world unload / spectator from SPBRevampedClient). */
    public void clearFlashlights() {
        clearLightBlock(client.level);
    }
}
