package com.sp.init;

import com.sp.SPBRevamped;
import com.sp.world.levels.BackroomsLevel;
import com.sp.world.levels.WorldRepresentingBackroomsLevel;
import com.sp.world.levels.custom.*;
import com.sp.world.levels.custom.vanilla_representing.OverworldRepresentingBackroomsLevel;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.dimension.DimensionType;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;
import java.util.Map;
import java.util.Optional;

public class BackroomsLevels {
    public static final ResourceKey<DimensionType> LEVEL0_DIM_TYPE = ResourceKey.create(Registries.DIMENSION_TYPE, Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level0_type"));
    public static final ResourceKey<Level> LEVEL0_WORLD_KEY = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level0"));
    public static final ResourceKey<Level> LEVEL1_WORLD_KEY = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level1"));
    public static final ResourceKey<Level> LEVEL2_WORLD_KEY = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level2"));
    public static final ResourceKey<Level> POOLROOMS_WORLD_KEY = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "poolrooms"));
    public static final ResourceKey<Level> INFINITE_FIELD_WORLD_KEY = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "infinite_field"));
    public static final ResourceKey<Level> LEVEL324_WORLD_KEY = ResourceKey.create(Registries.DIMENSION, Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "level324"));

    public static final BackroomsLevel LEVEL0_BACKROOMS_LEVEL = new Level0BackroomsLevel();
    public static final BackroomsLevel LEVEL1_BACKROOMS_LEVEL = new Level1BackroomsLevel();
    public static final BackroomsLevel LEVEL2_BACKROOMS_LEVEL = new Level2BackroomsLevel();
    public static final BackroomsLevel POOLROOMS_BACKROOMS_LEVEL = new PoolroomsBackroomsLevel();
    public static final BackroomsLevel INFINITE_FIELD_BACKROOMS_LEVEL = new InfiniteGrassBackroomsLevel();
    public static final BackroomsLevel OVERWORLD_REPRESENTING_BACKROOMS_LEVEL = new OverworldRepresentingBackroomsLevel();
    public static final BackroomsLevel LEVEL324_BACKROOMS_LEVEL = new Level324Backroomslevel();

    public static List<BackroomsLevel> BACKROOMS_LEVELS = new ArrayList<>();

    public static void init() {
        BACKROOMS_LEVELS.add(LEVEL0_BACKROOMS_LEVEL);
        BACKROOMS_LEVELS.add(LEVEL1_BACKROOMS_LEVEL);
        BACKROOMS_LEVELS.add(LEVEL2_BACKROOMS_LEVEL);
        BACKROOMS_LEVELS.add(POOLROOMS_BACKROOMS_LEVEL);
        BACKROOMS_LEVELS.add(INFINITE_FIELD_BACKROOMS_LEVEL);
        BACKROOMS_LEVELS.add(OVERWORLD_REPRESENTING_BACKROOMS_LEVEL);
        BACKROOMS_LEVELS.add(LEVEL324_BACKROOMS_LEVEL);

        for (BackroomsLevel backroomsLevel : BACKROOMS_LEVELS) {
            backroomsLevel.register();
        }
    }

    public static boolean isInBackroomsLevel(Level world, BackroomsLevel level) {
        return getLevel(world).map(backroomsLevel -> backroomsLevel.equals(level)).orElse(false);
    }

    public static Optional<BackroomsLevel> getLevel(Level world) {
        for (BackroomsLevel backroomsLevel : BACKROOMS_LEVELS) {
            if (backroomsLevel.getWorldKey().equals(world.dimension())) {
                return Optional.of(backroomsLevel);
            }
        }

        return Optional.empty();
    }

    public static boolean isInBackrooms(ResourceKey<Level> world){
        return BACKROOMS_LEVELS.stream().anyMatch(level -> level.getWorldKey().equals(world) && !(level instanceof WorldRepresentingBackroomsLevel));
    }

    public static Vec3 getCurrentLevelsOrigin(ResourceKey<Level> world) {
        for (BackroomsLevel backroomsLevel : BACKROOMS_LEVELS) {
            if (backroomsLevel.getWorldKey().equals(world)) {
                return backroomsLevel.getSpawnPos();
            }
        }

        return null;
    }

    public static Map<String, ResourceKey<Level>> definitions = Map.of(
            "LEVEL0",         LEVEL0_WORLD_KEY,
            "LEVEL1",         LEVEL1_WORLD_KEY,
            "LEVEL2",         LEVEL2_WORLD_KEY,
            "LEVEL324",       LEVEL324_WORLD_KEY,
            "POOLROOMS",      POOLROOMS_WORLD_KEY,
            "INFINITE_FIELD", INFINITE_FIELD_WORLD_KEY
    );

    public static Optional<BackroomsLevel> getById(String levelId) {
        for (BackroomsLevel backroomsLevel : BACKROOMS_LEVELS) {
            if (backroomsLevel.getLevelId().equals(levelId)) {
                return Optional.of(backroomsLevel);
            }
        }

        return Optional.empty();
    }
}
