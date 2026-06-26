package com.sp.mixin;

import com.sp.init.BackroomsLevels;
import net.minecraft.core.particles.ParticleOptions;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Redirect(method = "checkFallDamage", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerLevel;sendParticles(Lnet/minecraft/core/particles/ParticleOptions;DDDIDDDD)I"))
    private <T extends ParticleOptions> int noFallParticles(ServerLevel instance, T particle, double x, double y, double z, int count, double deltaX, double deltaY, double deltaZ, double speed){
        if(!BackroomsLevels.isInBackrooms(instance.dimension())){
            return instance.sendParticles(particle, x, y, z, count, deltaX, deltaY, deltaZ, speed);
        }

        return 0;

    }
}
