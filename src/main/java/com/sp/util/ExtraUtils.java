package com.sp.util;

import net.minecraft.client.resources.sounds.SoundInstance;
import net.minecraft.client.sounds.SoundEngine;
import net.minecraft.resources.Identifier;

public class ExtraUtils {

    // PORT: delta (Yarn -> mojmap).
    //  - client.sound.SoundInstance      -> client.resources.sounds.SoundInstance
    //  - client.sound.SoundSystem        -> client.sounds.SoundEngine
    //  - util.Identifier                 -> resources.Identifier
    //  - SoundInstance#getId()           -> SoundInstance#getIdentifier()
    //  - SoundEngine#stop(SoundInstance) is public (unchanged behaviour).
    //
    // The iterated collection changed name: Yarn SoundSystem#sounds was a
    // Multimap<SoundSource, SoundInstance>; its 26.2 equivalent is SoundEngine#instanceBySource
    // (same shape: Multimap<SoundSource, SoundInstance>). Both the Yarn field and this one are
    // PRIVATE and require an access-widener. The 1.20.1 build already widened the Yarn field via
    // spb-revamped.accesswidener ("accessible field .../SoundSystem sounds ..."); the mojmap
    // equivalent entry is:
    //     accessible field net/minecraft/client/sounds/SoundEngine instanceBySource Lcom/google/common/collect/Multimap;
    // NOTE: the .accesswidener file is still fully in Yarn names and its accessWidenerPath is
    // commented out in build.gradle (it cannot be re-enabled until the remaining 6f mixin/render
    // targets are converted to mojmap, else loom remapping fails). Until then this private-field
    // access will not link at build time, mirroring the original's reliance on the widener.
    public static void stopAllOtherSounds(Identifier id, SoundEngine soundEngine){
        for (SoundInstance soundInstance : soundEngine.instanceBySource.values()) {
            if (!soundInstance.getIdentifier().equals(id)) {
                soundEngine.stop(soundInstance);
            }
        }
    }

}
