package com.sp.render.bird;

import com.sp.compat.modmenu.ConfigStuff;
import com.sp.entity.ik.util.MathUtil;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.core.particles.ParticleTypes;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

/**
 * Boids flocking logic for the birds (A17 KEEP). Pure CPU math — no Veil. Ported Yarn→mojmap.
 */
public class FlockManager {
    private static final List<Vec3> FLOCK_CENTERS = new ArrayList<>();
    private static final List<Vec3> FLOCK_VELOCITIES = new ArrayList<>();

    private static int lastFlockCount = 0;

    private static final double MAX_HORIZONTAL_DISTANCE = 100;

    public static void init() {
        Random random = new Random();

        lastFlockCount = ConfigStuff.birdQuality.getFlockCount();

        FLOCK_CENTERS.clear();
        FLOCK_VELOCITIES.clear();
        for (int i = 0; i < lastFlockCount; i++) {
            FLOCK_CENTERS.add(getCheckCoord().add(new Vec3((random.nextFloat() * MAX_HORIZONTAL_DISTANCE * 2) - MAX_HORIZONTAL_DISTANCE, 0, (random.nextFloat() * MAX_HORIZONTAL_DISTANCE * 2) - MAX_HORIZONTAL_DISTANCE)));
            FLOCK_VELOCITIES.add(new Vec3(random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1).normalize());
        }
    }

    public static void moveFlockCenterTowards(Vec3 target, int flockIndex, boolean shouldLerp) {
        if (FLOCK_CENTERS.size() <= flockIndex) {
            FLOCK_CENTERS.set(flockIndex, new Vec3(getCheckCoord().x, 60, getCheckCoord().z));
        }

        if (shouldLerp) {
            FLOCK_CENTERS.set(flockIndex, MathUtil.lerpVec3(10, FLOCK_CENTERS.get(flockIndex), target));
        } else {
            FLOCK_CENTERS.set(flockIndex, target);
        }

    }

    public static Vec3 getFlockCenter(int flockIndex) {
        if (FLOCK_CENTERS.size() <= flockIndex) {
            FLOCK_CENTERS.set(flockIndex, new Vec3(getCheckCoord().x, 60, getCheckCoord().z));
        }
        return FLOCK_CENTERS.get(flockIndex);
    }

    public static List<Vec3> getFlockCenters() {
        return FLOCK_CENTERS;
    }

    public static void tick() {
        if (lastFlockCount != ConfigStuff.birdQuality.getFlockCount()) {
            init();
        }

        int maxY = 60;
        int minY = 50;

        /*
        if (SPBRevampedClient.isInLevel(BackroomsLevels.LEVEL324_BACKROOMS_LEVEL)) {
            maxY = 90;
            minY = maxY - 10;
        }
         */

        boolean shouldLerp = true;
        Random random = new Random();
        for (int i = 0; i < FLOCK_CENTERS.size(); i++) {
            Vec3 flockCenter = getFlockCenter(i);
            Vec3 velocity = FLOCK_VELOCITIES.size() > i ? FLOCK_VELOCITIES.get(i) : new Vec3(random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1, random.nextFloat() * 2 - 1).normalize();
            Vec3 flockingTarget = getCheckCoord();

            Vec3 newPos = flockCenter.add(velocity.scale(0.3));

            Vec3 localPos = flockingTarget.subtract(newPos);

            if (newPos.y > maxY || newPos.y < minY) {
                velocity = new Vec3(velocity.x, -velocity.y, velocity.z);
                newPos = new Vec3(newPos.x, Math.min(Math.max(newPos.y, minY), maxY), newPos.z);
            }

            if (Math.abs(localPos.x) > MAX_HORIZONTAL_DISTANCE) {
                newPos = new Vec3(flockingTarget.x - (localPos.x > 0 ? -MAX_HORIZONTAL_DISTANCE : MAX_HORIZONTAL_DISTANCE), newPos.y, newPos.z);
                shouldLerp = false;
            }

            if (Math.abs(localPos.z) > MAX_HORIZONTAL_DISTANCE) {
                newPos = new Vec3(newPos.x, newPos.y, flockingTarget.z - (localPos.z > 0 ? -MAX_HORIZONTAL_DISTANCE : MAX_HORIZONTAL_DISTANCE));
                shouldLerp = false;
            }

            if (FabricLoader.getInstance().isDevelopmentEnvironment() && Minecraft.getInstance().level != null) {
                Minecraft.getInstance().level.addParticle(ParticleTypes.CAMPFIRE_COSY_SMOKE, newPos.x, newPos.y, newPos.z, 0, 0, 0);
            }

            velocity = velocity.normalize();

            /*
            if (SPBRevampedClient.isInLevel(BackroomsLevels.LEVEL324_BACKROOMS_LEVEL)) {
                velocity = velocity.scale(2);
            }
             */

            FLOCK_VELOCITIES.set(i, velocity);

            moveFlockCenterTowards(newPos, i, shouldLerp);
        }
    }

    private static Vec3 getCheckCoord() {
        if (Minecraft.getInstance() == null || Minecraft.getInstance().player == null) {
            return new Vec3(0, 60, 0);
        }
        return Minecraft.getInstance().player.position();
    }
}
