package com.sp.render.post;

import com.sp.block.custom.EmergencyLightBlock;
import com.sp.block.custom.FluorescentLightBlock;
import com.sp.block.custom.ThinFluorescentLightBlock;
import com.sp.block.custom.TinyFluorescentLightBlock;
import com.sp.block.entity.CeilingLightBlockEntity;
import com.sp.block.entity.EmergencyLightBlockEntity;
import com.sp.block.entity.FluorescentLightBlockEntity;
import com.sp.block.entity.ThinFluorescentLightBlockEntity;
import com.sp.block.entity.TinyFluorescentLightBlockEntity;
import com.sp.compat.modmenu.ConfigStuff;
import com.sp.entity.custom.SmilerEntity;
import com.sp.init.BackroomsLevels;
import net.minecraft.client.Minecraft;
import net.minecraft.client.multiplayer.ClientLevel;
import net.minecraft.core.BlockPos;
import net.minecraft.resources.ResourceKey;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.chunk.LevelChunk;
import net.minecraft.world.level.chunk.status.ChunkStatus;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.Comparator;
import java.util.List;
import java.util.Map;

/**
 * Gathers the world-space light sources the Backrooms post chain relights the scene with
 * (see {@code backrooms_scene.fsh}'s SpbLights block). This restores the Veil-era deferred
 * point lights — per-pixel smooth pools of light from the fluorescent fixtures instead of the
 * per-block vanilla lightmap — on the vanilla 26.2 post pipeline.
 *
 * <p>The scan runs once per client tick ({@code SPBRevampedClient}'s END_CLIENT_TICK) and walks
 * the block-entity maps of the loaded chunks around the camera: every light fixture in this mod
 * is a {@code BlockEntity}, so no block iteration is needed. Positions are world-space here;
 * {@link BackroomsPostEffects} converts to camera-relative floats each frame.</p>
 *
 * <p>{@link #deferredLightingActive} is the master flag for the whole per-pixel lighting path.
 * It is written each frame by {@link BackroomsPostEffects#onFrameStart} and read by the
 * fullbright-lightmap mixin and {@code FlashlightRenderer}: when the chain is unavailable
 * (config off, shader failed to compile) everything falls back to vanilla block lighting
 * automatically.</p>
 */
public final class SpbLightGatherer {

    /** One world-space point light: position, color premultiplied by intensity, and reach. */
    public record PointLight(double x, double y, double z, float r, float g, float b, float radius) {
    }

    /**
     * Whether the deferred relight path is live this frame (post chain healthy + smooth lighting
     * enabled + camera in a deferred-lit dimension). Written by BackroomsPostEffects only.
     */
    public static volatile boolean deferredLightingActive = false;

    /** Set by the lightmap mixin so it can refresh the lightmap once when the force flag drops. */
    public static boolean lightmapWasForced = false;

    private static volatile List<PointLight> lights = List.of();

    private SpbLightGatherer() {
    }

    /**
     * The dimensions whose look is per-pixel lit (dark interiors lit by fixtures). The sky-lit
     * levels (Poolrooms, Infinite Field) keep vanilla lighting — their look comes from skylight,
     * which has no per-pixel stand-in without shadow maps.
     */
    public static boolean isDeferredDimension(ResourceKey<Level> dimension) {
        return dimension == BackroomsLevels.LEVEL0_WORLD_KEY
                || dimension == BackroomsLevels.LEVEL1_WORLD_KEY
                || dimension == BackroomsLevels.LEVEL2_WORLD_KEY
                || dimension == BackroomsLevels.LEVEL324_WORLD_KEY;
    }

    public static List<PointLight> getLights() {
        return lights;
    }

    /** Called once per client tick. Cheap: only BE-map walks over the nearby loaded chunks. */
    public static void tick(Minecraft client) {
        ClientLevel level = client.level;
        Entity camEntity = client.getCameraEntity() != null ? client.getCameraEntity() : client.player;
        if (level == null || camEntity == null || !isDeferredDimension(level.dimension())) {
            lights = List.of();
            return;
        }

        Vec3 camPos = camEntity.position();
        int radiusBlocks = Mth.clamp(ConfigStuff.getLightRenderDistance(), 24, 96);
        int chunkRadius = (radiusBlocks >> 4) + 1;
        int camChunkX = Mth.floor(camPos.x) >> 4;
        int camChunkZ = Mth.floor(camPos.z) >> 4;
        double maxDistSq = (double) radiusBlocks * radiusBlocks;

        ArrayList<PointLight> gathered = new ArrayList<>(64);

        for (int cx = camChunkX - chunkRadius; cx <= camChunkX + chunkRadius; cx++) {
            for (int cz = camChunkZ - chunkRadius; cz <= camChunkZ + chunkRadius; cz++) {
                LevelChunk chunk = (LevelChunk) level.getChunkSource().getChunk(cx, cz, ChunkStatus.FULL, false);
                if (chunk == null) {
                    continue;
                }
                for (Map.Entry<BlockPos, BlockEntity> entry : chunk.getBlockEntities().entrySet()) {
                    BlockPos pos = entry.getKey();
                    if (camPos.distanceToSqr(pos.getX() + 0.5, pos.getY() + 0.5, pos.getZ() + 0.5) > maxDistSq) {
                        continue;
                    }
                    PointLight light = fromBlockEntity(level, pos, entry.getValue());
                    if (light != null) {
                        gathered.add(light);
                    }
                }
            }
        }

        // The Smiler's glowing face is emissive in the original — as albedo it would vanish into
        // the relit darkness, so it carries its own small light (which also spills onto walls).
        for (Entity entity : level.entitiesForRendering()) {
            if (entity instanceof SmilerEntity smiler) {
                Vec3 eye = smiler.getEyePosition();
                if (camPos.distanceToSqr(eye) <= maxDistSq) {
                    gathered.add(new PointLight(eye.x, eye.y, eye.z, 0.85f, 0.92f, 1.0f, 7.0f));
                }
            }
        }

        if (gathered.size() > BackroomsPostEffects.MAX_POINT_LIGHTS) {
            gathered.sort(Comparator.comparingDouble(l -> camPos.distanceToSqr(l.x(), l.y(), l.z())));
            gathered.subList(BackroomsPostEffects.MAX_POINT_LIGHTS, gathered.size()).clear();
        }

        lights = gathered;
    }

    private static PointLight fromBlockEntity(ClientLevel level, BlockPos pos, BlockEntity be) {
        BlockState state = level.getBlockState(pos);

        if (be instanceof FluorescentLightBlockEntity) {
            if (!isOn(state, FluorescentLightBlock.ON, FluorescentLightBlock.BLACKOUT)) {
                return null;
            }
            // Tube hangs at the block's bottom face; the light source sits just below it so the
            // fixture's own face gets positive incidence and the ceiling around it stays grazed.
            return new PointLight(pos.getX() + 0.5, pos.getY() - 0.3, pos.getZ() + 0.5,
                    0.78f, 0.72f, 0.56f, 11.0f);
        }
        if (be instanceof ThinFluorescentLightBlockEntity) {
            if (!isOn(state, ThinFluorescentLightBlock.ON, ThinFluorescentLightBlock.BLACKOUT)) {
                return null;
            }
            return new PointLight(pos.getX() + 0.5, pos.getY() - 0.25, pos.getZ() + 0.5,
                    0.62f, 0.68f, 0.74f, 10.0f);
        }
        if (be instanceof TinyFluorescentLightBlockEntity) {
            if (!isOn(state, TinyFluorescentLightBlock.ON, TinyFluorescentLightBlock.BLACKOUT)) {
                return null;
            }
            return new PointLight(pos.getX() + 0.5, pos.getY() - 0.2, pos.getZ() + 0.5,
                    0.68f, 0.62f, 0.50f, 8.0f);
        }
        if (be instanceof CeilingLightBlockEntity) {
            return new PointLight(pos.getX() + 0.5, pos.getY() - 0.25, pos.getZ() + 0.5,
                    0.72f, 0.68f, 0.58f, 10.0f);
        }
        if (be instanceof EmergencyLightBlockEntity) {
            if (state.hasProperty(EmergencyLightBlock.RED_LIGHT) && state.getValue(EmergencyLightBlock.RED_LIGHT)) {
                // The original was a rotating red sweep (Veil area light); a slow pulse stands in.
                float pulse = 0.75f + 0.45f * (float) Math.sin(level.getGameTime() * 0.35);
                return new PointLight(pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5,
                        0.85f * pulse, 0.10f * pulse, 0.06f * pulse, 9.0f);
            }
            return new PointLight(pos.getX() + 0.5, pos.getY() + 0.3, pos.getZ() + 0.5,
                    0.55f, 0.50f, 0.42f, 8.0f);
        }
        return null;
    }

    private static boolean isOn(BlockState state,
                                net.minecraft.world.level.block.state.properties.BooleanProperty on,
                                net.minecraft.world.level.block.state.properties.BooleanProperty blackout) {
        return state.hasProperty(on) && state.hasProperty(blackout)
                && state.getValue(on) && !state.getValue(blackout);
    }
}
