package com.sp.mixin.respawnsystem;

import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.llamalad7.mixinextras.injector.wrapoperation.WrapOperation;
import com.sp.init.BackroomsLevels;
import net.minecraft.core.GlobalPos;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;

import java.util.Optional;

/**
 * Backrooms respawn override. In 26.2 the per-step respawn API (getSpawnPointPosition /
 * findRespawnPosition / setSpawnPoint / refreshPositionAndAngles) is gone — PlayerList.respawn now
 * derives everything from a single {@code ServerPlayer.findRespawnPositionAndUseSpawnBlock(...)}
 * call that returns a {@link TeleportTransition}. We wrap that one call: when the player dies inside
 * the backrooms, respawn them at their last death position (clamped to the level origin if it fell
 * below y=0) within the same dimension, preserving their facing.
 */
@Mixin(net.minecraft.server.players.PlayerList.class)
public class PlayerManagerMixin {

    @WrapOperation(method = "respawn", at = @At(value = "INVOKE", target = "Lnet/minecraft/server/level/ServerPlayer;findRespawnPositionAndUseSpawnBlock(ZLnet/minecraft/world/level/portal/TeleportTransition$PostTeleportTransition;)Lnet/minecraft/world/level/portal/TeleportTransition;"))
    private TeleportTransition spbrevamped$backroomsRespawn(ServerPlayer instance, boolean useCharge, TeleportTransition.PostTeleportTransition postTeleport, Operation<TeleportTransition> original) {
        ServerLevel currentLevel = instance.level();

        if (BackroomsLevels.isInBackrooms(currentLevel.dimension())) {
            Optional<GlobalPos> lastDeathPos = instance.getLastDeathLocation();
            if (lastDeathPos.isPresent()) {
                Vec3 deathPos = Vec3.atCenterOf(lastDeathPos.get().pos());
                Vec3 respawnPos = deathPos.y < 0
                        ? BackroomsLevels.getCurrentLevelsOrigin(currentLevel.dimension())
                        : deathPos;

                return new TeleportTransition(currentLevel, respawnPos, Vec3.ZERO, instance.getYRot(), instance.getXRot(), postTeleport);
            }
        }

        return original.call(instance, useCharge, postTeleport);
    }
}
