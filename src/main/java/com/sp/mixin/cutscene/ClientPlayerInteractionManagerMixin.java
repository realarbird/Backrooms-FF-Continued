package com.sp.mixin.cutscene;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sp.SPBRevampedClient;
import net.minecraft.client.multiplayer.MultiPlayerGameMode;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(MultiPlayerGameMode.class)
public class ClientPlayerInteractionManagerMixin {
    @WrapMethod(method = "attack")
    public void attackEntity(Player player, Entity target, Operation<Void> original) {
        if (SPBRevampedClient.getCutsceneManager().isPlaying) {
            return;
        }

        original.call(player, target);
    }
}
