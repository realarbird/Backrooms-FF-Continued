package com.sp.mixin.soundphysicsremasteredcompat;

import com.llamalad7.mixinextras.sugar.Local;
import com.sonicether.soundphysics.config.AllowedSoundConfig;
import com.sp.init.ModSounds;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Pseudo;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Map;

@Mixin(AllowedSoundConfig.class)
@Pseudo
public class AllowedSoundsConfigMixin {

    @Inject(method = "createDefaultMap", at = @At("RETURN"), remap = false)
    private void ignoreSoundPhysics(CallbackInfoReturnable<Map<String, Boolean>> cir, @Local Map<String, Boolean> map) {
        map.put(ModSounds.INTERCOM_BASIC1.location().toString(), false);
        map.put(ModSounds.INTERCOM_BASIC2.location().toString(), false);
        map.put(ModSounds.INTERCOM_FRIEND.location().toString(), false);
        map.put(ModSounds.INTERCOM_REVERSED.location().toString(), false);

        map.put(ModSounds.LIGHTS_OUT.location().toString(), false);
        map.put(ModSounds.LIGHTS_ON.location().toString(), false);
        map.put(ModSounds.LIGHT_BLINK.location().toString(), false);

        map.put(ModSounds.CREEPY_MUSIC1.location().toString(), false);
        map.put(ModSounds.CREEPY_MUSIC2.location().toString(), false);
        map.put(ModSounds.FAR_CROWD.location().toString(), false);

        map.put(ModSounds.LEVEL1_AMBIENCE1.location().toString(), false);
        map.put(ModSounds.LEVEL1_AMBIENCE2.location().toString(), false);
        map.put(ModSounds.LEVEL1_AMBIENCE3.location().toString(), false);
        map.put(ModSounds.LEVEL1_AMBIENCE4.location().toString(), false);

        map.put(ModSounds.SMILER_AMBIENCE.location().toString(), false);
        map.put(ModSounds.SMILER_GLITCH.location().toString(), false);

        map.put(ModSounds.CREAKING1.location().toString(), false);
        map.put(ModSounds.CREAKING2.location().toString(), false);
        map.put(ModSounds.LEVEL2_AMBIENCE.location().toString(), false);
        map.put(ModSounds.AMBIENCE.location().toString(), false);

        map.put(ModSounds.SKINWALKER_AMBIENCE.location().toString(), false);
        map.put(ModSounds.SKINWALKER_BONE_CRACK.location().toString(), false);
        map.put(ModSounds.SKINWALKER_CHASE.location().toString(), false);
        map.put(ModSounds.SKINWALKER_FOOTSTEP.location().toString(), false);
        map.put(ModSounds.SKINWALKER_NOTICE.location().toString(), false);
        map.put(ModSounds.SKINWALKER_BONE_CRACK_LONG.location().toString(), false);
        map.put(ModSounds.SKINWALKER_RELEASE.location().toString(), false);
        map.put(ModSounds.SKINWALKER_REVEAL.location().toString(), false);
        map.put(ModSounds.SKINWALKER_SNIFF.location().toString(), false);

        map.put(ModSounds.POOLROOMS_SPLASH1.location().toString(), false);
        map.put(ModSounds.POOLROOMS_SPLASH2.location().toString(), false);
        map.put(ModSounds.POOLROOMS_DRIP1.location().toString(), false);
        map.put(ModSounds.POOLROOMS_DRIP2.location().toString(), false);

        map.put(ModSounds.MIDNIGHT_TRANSITION.location().toString(), false);
        map.put(ModSounds.SUNSET_TRANSITION.location().toString(), false);
        map.put(ModSounds.SUNSET_TRANSITION_END.location().toString(), false);

        map.put(ModSounds.POOLROOMS_AMBIENCE_NOON.location().toString(), false);
        map.put(ModSounds.POOLROOMS_AMBIENCE_SUNSET.location().toString(), false);

        map.put(ModSounds.INFINITE_GRASS_AMBIENCE.location().toString(), false);
        map.put(ModSounds.INFINITE_GRASS_SOUNDEVENT.location().toString(), false);
        map.put(ModSounds.INFINITE_GRASS_SOUNDEVENT_FAR.location().toString(), false);

        map.put(ModSounds.WINDTUNNEL_GRASS_AMBIENCE.location().toString(), false);

        map.put(ModSounds.EMERGENCY_LIGHT_ALARM.location().toString(), false);

        map.put(ModSounds.WALKER_FOOTSTEP.location().toString(), false);
    }
}
