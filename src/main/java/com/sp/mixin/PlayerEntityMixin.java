package com.sp.mixin;

import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.cca_stuff.WorldEvents;
import com.sp.init.BackroomsLevels;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.damagesource.DamageTypes;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(Player.class)
public abstract class PlayerEntityMixin extends Entity {

    protected PlayerEntityMixin(EntityType<? extends LivingEntity> entityType, Level world) {
        super(entityType, world);
    }

    // 26.2: Player.tick() writes `this.noPhysics = this.isSpectator()` at its head, and
    // the field ref's owner in the constant pool is Player (not Entity). Target Player here
    // or Mixin scans 0 targets and the injection fails at apply time.
    @Inject(method = "tick", at = @At(value = "FIELD", target = "Lnet/minecraft/world/entity/player/Player;noPhysics:Z", ordinal = 0, shift = At.Shift.AFTER))
    private void enableNoclip(CallbackInfo ci) {
        PlayerComponent playerComponent = InitializeComponents.PLAYER.get(this);

        if (playerComponent.shouldNoClip()) {
            this.noPhysics = playerComponent.shouldNoClip();
        }
    }

    // The Backrooms have no food: exhaustion (sprinting, jumping, healing, the hunger effect —
    // every vanilla drain funnels through this method) is cancelled while the player is in any
    // Backrooms dimension, so the hunger bar never depletes there. Eating still restores as normal.
    @Inject(method = "causeFoodExhaustion", at = @At("HEAD"), cancellable = true)
    private void noHungerInBackrooms(float exhaustion, CallbackInfo ci) {
        if (BackroomsLevels.isInBackrooms(this.level().dimension())) {
            ci.cancel();
        }
    }

    // Potential fix for: https://github.com/SpacePotatoee/MinecraftFoundFootage/issues/85
    // IDK tho. I am just throwing shit at the wall to see what sticks.
    @Inject(method = "die", at = @At("HEAD"))
    public void onDeath(DamageSource damageSource, CallbackInfo ci) {
        PlayerComponent playerComponent = InitializeComponents.PLAYER.get(this);

        Player player = (Player) (Object) this;

        WorldEvents events = InitializeComponents.EVENTS.get(player.level());

        if (damageSource.is(DamageTypes.FELL_OUT_OF_WORLD)) {
            if (BackroomsLevels.getLevel(player.level()).isPresent()) {
                player.setPos(0, 100, 0);
            } else {
                player.setPos(BackroomsLevels.getLevel(player.level()).get().getSpawnPos());
            }
        }

        if (playerComponent.hasBeenCaptured() || playerComponent.isBeingCaptured() || events.activeSkinWalkerEntity.getTarget() == player) {
            if (this.level() instanceof ServerLevel) {
                ServerPlayer serverPlayer = (ServerPlayer) player;

                serverPlayer.setCamera(serverPlayer);
            }

            events.activeSkinWalkerEntity.discard();
            events.activeSkinWalkerEntity = null;

            playerComponent.setBeingCaptured(false);
            playerComponent.setBeingReleased(false);
            playerComponent.setShouldNoClip(false);
            playerComponent.setHasBeenCaptured(false);
            playerComponent.setShouldBeMuted(false);
            playerComponent.sync();
        }
    }
}
