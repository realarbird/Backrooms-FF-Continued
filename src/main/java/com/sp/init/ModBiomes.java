package com.sp.init;

import com.sp.SPBRevamped;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.biome.Biome;

public class ModBiomes {
    public static final ResourceKey<Biome> BASE_BACKROOMS_BIOME = ResourceKey.create(Registries.BIOME, Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "base_backrooms_biome"));
}
