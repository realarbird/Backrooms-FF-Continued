package com.sp.mixin;

import com.llamalad7.mixinextras.sugar.Local;
import com.sp.block.SprintBlockSoundGroup;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.level.block.SoundType;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Entity.class)
public abstract class PlaySprintSoundMixin {

    @Shadow public abstract void playSound(SoundEvent sound, float volume, float pitch);

    @Redirect(method = "playStepSound", at = @At(value = "INVOKE", target = "Lnet/minecraft/world/entity/Entity;playSound(Lnet/minecraft/sounds/SoundEvent;FF)V"))
    private void playSprintSound(Entity instance, SoundEvent sound, float volume, float pitch, @Local SoundType blockSoundGroup){
        if(blockSoundGroup instanceof SprintBlockSoundGroup && instance.isSprinting()) {
            this.playSound(((SprintBlockSoundGroup) blockSoundGroup).getSprintingSound(), blockSoundGroup.getVolume(), blockSoundGroup.getPitch());
        } else {
            this.playSound(blockSoundGroup.getStepSound(), blockSoundGroup.getVolume() * 0.15f, blockSoundGroup.getPitch());
        }
    }

}
