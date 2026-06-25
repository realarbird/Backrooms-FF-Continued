package com.sp.init;

import com.sp.SPBRevamped;
import com.sp.entity.custom.SkinWalkerEntity;
import com.sp.entity.custom.SmilerEntity;
import com.sp.entity.custom.WalkerEntity;
import net.minecraft.core.Registry;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.core.registries.Registries;
import net.minecraft.resources.Identifier;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.MobCategory;

public class ModEntities {

    public static final EntityType<SkinWalkerEntity> SKIN_WALKER_ENTITY = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "skin_walker"),
            EntityType.Builder.of(SkinWalkerEntity::new, MobCategory.MONSTER).sized(0.6f, 2.0f).build(entityKey("skin_walker")));

    public static final EntityType<SmilerEntity> SMILER_ENTITY = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "smiler"),
            EntityType.Builder.of(SmilerEntity::new, MobCategory.MONSTER).sized(0.6f, 2.0f).build(entityKey("smiler")));

    public static final EntityType<WalkerEntity> WALKER_ENTITY = Registry.register(BuiltInRegistries.ENTITY_TYPE,
            Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "walker"),
            EntityType.Builder.of(WalkerEntity::new, MobCategory.MONSTER).sized(2.0f, 2.0f).build(entityKey("walker")));

    private static ResourceKey<EntityType<?>> entityKey(String name) {
        return ResourceKey.create(Registries.ENTITY_TYPE, Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, name));
    }
}
