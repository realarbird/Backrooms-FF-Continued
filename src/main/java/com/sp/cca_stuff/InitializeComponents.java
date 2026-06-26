package com.sp.cca_stuff;

import com.sp.SPBRevamped;
import com.sp.entity.custom.SkinWalkerEntity;
import com.sp.entity.custom.SmilerEntity;
import net.minecraft.resources.Identifier;
import org.ladysnake.cca.api.v3.component.ComponentKey;
import org.ladysnake.cca.api.v3.component.ComponentRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentFactoryRegistry;
import org.ladysnake.cca.api.v3.entity.EntityComponentInitializer;
import org.ladysnake.cca.api.v3.entity.RespawnCopyStrategy;
import org.ladysnake.cca.api.v8.level.LevelComponentFactoryRegistry;
import org.ladysnake.cca.api.v8.level.LevelComponentInitializer;

public class InitializeComponents implements EntityComponentInitializer, LevelComponentInitializer {
    public static final ComponentKey<PlayerComponent> PLAYER = ComponentRegistry.getOrCreate(Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "player"), PlayerComponent.class);
    public static final ComponentKey<WorldEvents> EVENTS = ComponentRegistry.getOrCreate(Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "events"), WorldEvents.class);
    public static final ComponentKey<SkinWalkerComponent> SKIN_WALKER = ComponentRegistry.getOrCreate(Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "skw"), SkinWalkerComponent.class);
    public static final ComponentKey<SmilerComponent> SMILER = ComponentRegistry.getOrCreate(Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "smi"), SmilerComponent.class);


    @Override
    public void registerEntityComponentFactories(EntityComponentFactoryRegistry registry) {
        registry.registerForPlayers(PLAYER, PlayerComponent::new, RespawnCopyStrategy.ALWAYS_COPY);
        registry.registerFor(SkinWalkerEntity.class, SKIN_WALKER, SkinWalkerComponent::new);
        registry.registerFor(SmilerEntity.class, SMILER, SmilerComponent::new);
    }

    @Override
    public void registerLevelComponentFactories(LevelComponentFactoryRegistry registry) {
        registry.register(EVENTS, WorldEvents::new);
    }
}
