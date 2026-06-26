package com.sp.mixin;

import com.sp.mixininterfaces.NewServerProperties;
import net.minecraft.server.dedicated.DedicatedServerProperties;
import net.minecraft.server.dedicated.Settings;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;

import java.util.Properties;

@Mixin(DedicatedServerProperties.class)
public abstract class ServerPropertiesHandlerMixin extends Settings<DedicatedServerProperties> implements NewServerProperties {
    public ServerPropertiesHandlerMixin(Properties properties) {
        super(properties);
    }

    @Unique private final int exitSpawnRadius = this.get("backrooms-exit-spawn-radius", 300);

    @Override
    public int getExitSpawnRadius() {
        return this.exitSpawnRadius;
    }

}
