package com.sp.mixin.worldgen;

import com.sp.world.generation.BackroomsGenBuffer;
import net.minecraft.core.BlockPos;
import net.minecraft.server.level.WorldGenRegion;
import net.minecraft.world.level.block.entity.BlockEntity;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.material.FluidState;
import net.minecraft.world.ticks.BlackholeTickAccess;
import net.minecraft.world.ticks.LevelTickAccess;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

/**
 * Redirects a {@link WorldGenRegion}'s block I/O into the active {@link BackroomsGenBuffer} while the
 * Backrooms generators run chunk-locally.
 *
 * <p>The Backrooms generators place whole megarooms/mazes spanning several chunks from one anchor chunk's
 * pass. 26.2 only commits writes within {@code blockStateWriteRadius} (=1 at FEATURES) and drops the rest,
 * which left every Backrooms level with void gaps at region edges. The fix runs each generator into a
 * per-anchor in-memory buffer (see {@link BackroomsGenBuffer}); these injects make the generators' reads and
 * writes hit that buffer instead of the real region. When no buffer is {@link BackroomsGenBuffer#active()}
 * (i.e. always, for vanilla dimensions), every call passes straight through untouched.</p>
 *
 * <p>The intercepted set is exactly what 26.2's {@code StructureTemplate.placeInWorld} (and the generators
 * themselves) touch, verified against the decompiled class: {@code getBlockState}, {@code getFluidState}
 * (the waterlogging check — missing this was the far-chunk-warning flood of the first attempt),
 * {@code getBlockEntity} (template NBT loading, e.g. loot crates), {@code setBlock}, and the two tick
 * schedulers (reached via {@code LiquidBlockContainer.placeLiquid} for waterlogged pool blocks).</p>
 */
@Mixin(WorldGenRegion.class)
public abstract class BackroomsWorldGenRegionMixin {

    @Inject(method = "getBlockState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/state/BlockState;",
            at = @At("HEAD"), cancellable = true)
    private void spbRevamped$bufferedGetBlockState(BlockPos pos, CallbackInfoReturnable<BlockState> cir) {
        BackroomsGenBuffer buffer = BackroomsGenBuffer.active();
        if (buffer != null) {
            cir.setReturnValue(buffer.get(pos));
        }
    }

    @Inject(method = "getFluidState(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/material/FluidState;",
            at = @At("HEAD"), cancellable = true)
    private void spbRevamped$bufferedGetFluidState(BlockPos pos, CallbackInfoReturnable<FluidState> cir) {
        BackroomsGenBuffer buffer = BackroomsGenBuffer.active();
        if (buffer != null) {
            cir.setReturnValue(buffer.get(pos).getFluidState());
        }
    }

    @Inject(method = "setBlock(Lnet/minecraft/core/BlockPos;Lnet/minecraft/world/level/block/state/BlockState;II)Z",
            at = @At("HEAD"), cancellable = true)
    private void spbRevamped$bufferedSetBlock(BlockPos pos, BlockState state, int flags, int recursion,
                                              CallbackInfoReturnable<Boolean> cir) {
        BackroomsGenBuffer buffer = BackroomsGenBuffer.active();
        if (buffer != null) {
            buffer.put(pos.immutable(), state);
            cir.setReturnValue(true);
        }
    }

    @Inject(method = "getBlockEntity(Lnet/minecraft/core/BlockPos;)Lnet/minecraft/world/level/block/entity/BlockEntity;",
            at = @At("HEAD"), cancellable = true)
    private void spbRevamped$bufferedGetBlockEntity(BlockPos pos, CallbackInfoReturnable<BlockEntity> cir) {
        BackroomsGenBuffer buffer = BackroomsGenBuffer.active();
        if (buffer != null) {
            // Detached instance so placeInWorld can load template NBT (loot tables); transferred to the
            // real block entity when the buffered slice is copied out.
            cir.setReturnValue(buffer.getOrCreateBlockEntity(pos));
        }
    }

    @Inject(method = "getBlockTicks()Lnet/minecraft/world/ticks/LevelTickAccess;",
            at = @At("HEAD"), cancellable = true)
    private void spbRevamped$bufferedBlockTicks(CallbackInfoReturnable<LevelTickAccess<?>> cir) {
        if (BackroomsGenBuffer.active() != null) {
            cir.setReturnValue(BlackholeTickAccess.emptyLevelList());
        }
    }

    @Inject(method = "getFluidTicks()Lnet/minecraft/world/ticks/LevelTickAccess;",
            at = @At("HEAD"), cancellable = true)
    private void spbRevamped$bufferedFluidTicks(CallbackInfoReturnable<LevelTickAccess<?>> cir) {
        if (BackroomsGenBuffer.active() != null) {
            cir.setReturnValue(BlackholeTickAccess.emptyLevelList());
        }
    }
}
