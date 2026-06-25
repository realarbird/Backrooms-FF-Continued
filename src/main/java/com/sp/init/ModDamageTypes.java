package com.sp.init;

import com.sp.SPBRevamped;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageType;
import net.minecraft.world.level.Level;

public class ModDamageTypes {
    public static final ResourceKey<DamageType> ACID_WATER = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "acid_water"));
    public static final ResourceKey<DamageType> SMILER = ResourceKey.create(Registries.DAMAGE_TYPE, Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "smiler"));

    public static DamageSource of(Level world, ResourceKey<DamageType> key){
        return new DamageSource(world.registryAccess().lookupOrThrow(Registries.DAMAGE_TYPE).getOrThrow(key));
    }

}
