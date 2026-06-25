package com.sp.util;

/**
 * Standalone easing functions, replacing the removed Veil
 * {@code foundry.veil.api.client.util.Easings}.
 *
 * <p>Only the easings actually referenced by this codebase are provided. Each is exposed as a
 * static {@link Easing} constant so existing call sites such as {@code Easings.Easing.linear}
 * keep working unchanged.
 */
public final class Easings {

    private Easings() {
    }

    /**
     * A single-argument easing function mapping a normalized progress {@code t} in {@code [0, 1]}
     * to an eased value.
     */
    @FunctionalInterface
    public interface Easing {
        float ease(float t);

        Easing linear = t -> t;

        Easing easeInSine = t -> (float) (1.0 - Math.cos((t * Math.PI) / 2.0));

        Easing easeInOutSine = t -> (float) (-(Math.cos(Math.PI * t) - 1.0) / 2.0);

        Easing easeInOutQuad = t -> t < 0.5f
                ? 2.0f * t * t
                : (float) (1.0 - Math.pow(-2.0 * t + 2.0, 2.0) / 2.0);

        Easing easeInOutCubic = t -> t < 0.5f
                ? 4.0f * t * t * t
                : (float) (1.0 - Math.pow(-2.0 * t + 2.0, 3.0) / 2.0);

        Easing easeInCirc = t -> (float) (1.0 - Math.sqrt(1.0 - Math.pow(t, 2.0)));

        Easing easeOutCirc = t -> (float) Math.sqrt(1.0 - Math.pow(t - 1.0, 2.0));
    }
}
