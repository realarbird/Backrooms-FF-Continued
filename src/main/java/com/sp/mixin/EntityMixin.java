package com.sp.mixin;

import com.sp.init.BackroomsLevels;
import com.sp.init.ModDamageTypes;
import com.sp.init.ModSounds;
import com.sp.world.levels.custom.PoolroomsBackroomsLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Entity.class)
public abstract class EntityMixin {

    @Shadow private Level level;

    @Shadow public abstract boolean isInWater();

    @Shadow public abstract boolean hurtServer(ServerLevel level, DamageSource source, float amount);

    @Inject(method = "baseTick", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;updateSwimming()V"))
    private void acidWater(CallbackInfo ci) {

        BackroomsLevels.getLevel(level).ifPresent(backroomsLevel -> {
            if (!(backroomsLevel instanceof PoolroomsBackroomsLevel poolrooms)) {
                return;
            }

            if(!poolrooms.isNoon()){
                if(this.isInWater() && this.level instanceof ServerLevel serverLevel){
                    this.hurtServer(serverLevel, ModDamageTypes.of(level, ModDamageTypes.ACID_WATER), 1.0f);
                }
            }
        });
    }

    @Inject(method = "getSwimSound", at = @At("RETURN"), cancellable = true)
    private void newSwimSound(CallbackInfoReturnable<SoundEvent> cir){
        cir.setReturnValue(ModSounds.SWIM);
    }

    @ModifyArg(method = "waterSwimSound()V", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;playSwimSound(F)V"))
    private float sfxLouder(float volume){
        return volume + 0.1f;
    }


}
