package com.sp.mixininterfaces;

import net.minecraft.client.OptionInstance;

public interface VideoModeOptionAccessor {

    OptionInstance<Integer> getNormalVideoMode();
    OptionInstance<Integer> getVHSVVideoMode();

}
