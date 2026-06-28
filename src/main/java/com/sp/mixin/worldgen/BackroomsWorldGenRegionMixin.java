package com.sp.mixin.worldgen;

import com.sp.world.generation.chunk_generator.BackroomsChunkGenerator;
import net.minecraft.server.level.GenerationChunkHolder;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.util.StaticCache2D;
import org.spongepowered.asm.mixin.Final;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Lets the Backrooms chunk generators place rooms/mazes that span several chunks from a single chunk's
 * decoration pass.
 *
 * <p>PORT FIX (void Backrooms / death-loop): the generators run from
 * {@code BackroomsChunkGenerator.applyBiomeDecoration} (the FEATURES step) and {@code setBlock} at chunk
 * offsets up to the room/maze size (e.g. the spawn megaroom spans ±32 blocks = ±2 chunks). 26.2's
 * {@link WorldGenRegion} enforces a {@code blockStateWriteRadius} of 1 and silently DROPS any write outside
 * it (logging "Detected setBlock in a far chunk"), which left every Backrooms level as empty void — players
 * fell out of the world into a respawn-at-death-position loop, and with no blocks (and no skylight/ambient
 * light) the screen was pitch black. The original 1.20.1 mod ran its generation from a custom
 * {@code ChunkStatus} task whose region permitted these wide writes; that hook was dropped in the port.
 *
 * <p>This restores the capability the safe way: for Backrooms generators only, treat any chunk that is
 * actually present in the region's loaded cache as writable/readable. The FEATURES step loads a margin of 8
 * chunks, so the room/maze writes (distance ≤ ~2) are always in-cache; {@link StaticCache2D#contains} guards
 * against ever touching an unloaded chunk (no worldgen crash), and vanilla dimensions are left untouched.
 */
@Mixin(WorldGenRegion.class)
public abstract class BackroomsWorldGenRegionMixin {
    @Shadow @Final private ServerLevel level;
    @Shadow @Final private StaticCache2D<GenerationChunkHolder> cache;

    @Unique
    private boolean spbRevamped$wideWritesAllowed(int chunkX, int chunkZ) {
        return this.level.getChunkSource().getGenerator() instanceof BackroomsChunkGenerator
                && this.cache.contains(chunkX, chunkZ);
    }

    @Inject(method = "isWithinWriteZone(II)Z", at = @At("HEAD"), cancellable = true)
    private void spbRevamped$allowBackroomsWideWrites(int chunkX, int chunkZ, CallbackInfoReturnable<Boolean> cir) {
        if (this.spbRevamped$wideWritesAllowed(chunkX, chunkZ)) {
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "warnIfReadOutsideWriteZone(II)V", at = @At("HEAD"), cancellable = true)
    private void spbRevamped$silenceBackroomsReadWarn(int chunkX, int chunkZ, CallbackInfo ci) {
        if (this.spbRevamped$wideWritesAllowed(chunkX, chunkZ)) {
            ci.cancel();
        }
    }
}
