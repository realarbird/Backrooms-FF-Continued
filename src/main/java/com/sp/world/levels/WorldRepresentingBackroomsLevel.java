package com.sp.world.levels;

import net.minecraft.resources.ResourceKey;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public abstract class WorldRepresentingBackroomsLevel extends BackroomsLevel {
    public WorldRepresentingBackroomsLevel(String levelId, Vec3 spawnPos, ResourceKey<Level> worldKey) {
        super(levelId, null, spawnPos, worldKey, "minecraft");
    }

    public WorldRepresentingBackroomsLevel(String levelId, Vec3 spawnPos, ResourceKey<Level> worldKey, String modId) {
        super(levelId, null, spawnPos, worldKey, modId);
    }

    @Override
    public void register() {

    }
}
