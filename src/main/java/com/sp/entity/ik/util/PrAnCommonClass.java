package com.sp.entity.ik.util;

import java.util.logging.Logger;

public class PrAnCommonClass {
    public static Logger LOGGER = Logger.getLogger("PrAn");
    // PORT/BUGFIX: must default to false. throwInDevOnly() is meant to hard-fail only in a dev
    // environment; SPBRevamped flips this to true when FabricLoader.isDevelopmentEnvironment(). The old
    // `true` default was never cleared in production, so every "bone not found" (which the IK code is
    // written to tolerate via `if (getBone(...).isEmpty()) return;`) became a fatal crash in shipped
    // builds instead of a graceful skip. Keep this false so those defensive guards actually work.
    public static boolean isDev = false;
    public static boolean shouldRenderDebugLegs = false;

    public static void throwInDevOnly(RuntimeException exception) {
        if (isDev) {
            throw exception;
        }
    }

    public static void init() {
    }
}
