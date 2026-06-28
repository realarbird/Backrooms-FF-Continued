package com.sp.mixin.rain;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sp.init.BackroomsLevels;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;

/**
 * Force permanent rain/thunder in Level324 (NEW-INJECTION). Ported Yarn→mojmap, Phase 6f.
 *
 * <p>PORT: delta — the original wrapped {@code LevelProperties.getRainTime/isRaining/getThunderTime/
 * isThundering}. In 26.2 the weather state was removed from {@code LevelProperties}
 * ({@code PrimaryLevelData}) — none of those four methods exist there anymore. Weather now lives on
 * {@code Level} itself ({@code isRaining()}, {@code isThundering()}, {@code getRainLevel(float)},
 * {@code getThunderLevel(float)}), inherited by both {@code ClientLevel} and {@code ServerLevel}.
 *
 * <p>Retargeted to {@code Level}: when the level is Level324 we force {@code isRaining}/{@code isThundering}
 * to {@code true} and the rain/thunder gradient to full ({@code 1.0f}). This subsumes the old rain/thunder
 * *time* forcing (those were just the countdown timers gating the boolean state — now moot). The
 * dimension check ({@code isInBackroomsLevel}) replaces the old {@code EnvType.CLIENT} guard and works on
 * both sides; behaviour for the player (always-raining Level324) is unchanged.
 */
@Mixin(Level.class)
public abstract class AlwaysRainLevelPropertiesMixin {

    @WrapMethod(method = "isRaining")
    private boolean spbrevamped$isRaining(Operation<Boolean> original) {
        if (BackroomsLevels.isInBackroomsLevel((Level) (Object) this, BackroomsLevels.LEVEL324_BACKROOMS_LEVEL)) {
            return true;
        }
        return original.call();
    }

    @WrapMethod(method = "isThundering")
    private boolean spbrevamped$isThundering(Operation<Boolean> original) {
        if (BackroomsLevels.isInBackroomsLevel((Level) (Object) this, BackroomsLevels.LEVEL324_BACKROOMS_LEVEL)) {
            return true;
        }
        return original.call();
    }

    @WrapMethod(method = "getRainLevel")
    private float spbrevamped$getRainLevel(float partialTick, Operation<Float> original) {
        if (BackroomsLevels.isInBackroomsLevel((Level) (Object) this, BackroomsLevels.LEVEL324_BACKROOMS_LEVEL)) {
            return 1.0f;
        }
        return original.call(partialTick);
    }

    @WrapMethod(method = "getThunderLevel")
    private float spbrevamped$getThunderLevel(float partialTick, Operation<Float> original) {
        if (BackroomsLevels.isInBackroomsLevel((Level) (Object) this, BackroomsLevels.LEVEL324_BACKROOMS_LEVEL)) {
            return 1.0f;
        }
        return original.call(partialTick);
    }
}
