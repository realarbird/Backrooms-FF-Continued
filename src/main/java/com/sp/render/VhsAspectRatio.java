package com.sp.render;

import com.google.common.collect.Lists;
import com.mojang.blaze3d.platform.VideoMode;
import net.minecraft.client.OptionInstance;

import java.util.List;
import java.util.Optional;

// PORT: delta (Yarn -> mojmap).
//  - client.option.SimpleOption<T> -> client.OptionInstance<T>
//  - client.util.VideoMode         -> com.mojang.blaze3d.platform.VideoMode
public class VhsAspectRatio {

    public static List<VideoMode> vhsAspectRatiosList = Lists.<VideoMode>newArrayList();
    public static List<VideoMode> normalVideoModesList = Lists.<VideoMode>newArrayList();

    public static OptionInstance<Integer> normalVideoMode;
    public static OptionInstance<Integer> vhsVideoMode;

    public static Optional<VideoMode> currentNormalVideoMode;
    public static Optional<VideoMode> currentVhsVideoMode;

}
