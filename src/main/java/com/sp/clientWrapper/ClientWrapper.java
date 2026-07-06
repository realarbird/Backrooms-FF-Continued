package com.sp.clientWrapper;

import com.sp.ModKeyBinds;
import com.sp.SPBRevampedClient;
import com.sp.block.custom.EmergencyLightBlock;
import com.sp.block.custom.FluorescentLightBlock;
import com.sp.block.custom.ThinFluorescentLightBlock;
import com.sp.block.custom.TinyFluorescentLightBlock;
import com.sp.block.entity.EmergencyLightBlockEntity;
import com.sp.block.entity.FluorescentLightBlockEntity;
import com.sp.block.entity.ThinFluorescentLightBlockEntity;
import com.sp.block.entity.TinyFluorescentLightBlockEntity;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.compat.modmenu.ConfigStuff;
import com.sp.entity.client.SkinWalkerCapturedFlavorText;
import com.sp.entity.custom.SkinWalkerEntity;
import com.sp.entity.custom.SmilerEntity;
import com.sp.entity.ik.parts.sever_limbs.ServerLimb;
import com.sp.init.BackroomsLevels;
import com.sp.init.HelpfulHintManager;
import com.sp.init.ModSounds;
import com.sp.networking.C2S.TargetEntitySync;
import com.sp.render.SkinwalkerJumpscare;
import com.sp.render.gui.BackroomsHudOverlay;
import com.sp.sounds.*;
import com.sp.sounds.entity.SkinWalkerChaseSoundInstance;
import com.sp.sounds.entity.SmilerAmbienceSoundInstance;
import com.sp.sounds.entity.SmilerGlitchSoundInstance;
import com.sp.sounds.pipes.GasPipeSoundInstance;
import com.sp.sounds.pipes.WaterPipeSoundInstance;
import com.sp.world.levels.BackroomsLevel;
import com.sp.world.levels.BackroomsLevelWithLights;
import com.sp.world.levels.custom.Level1BackroomsLevel;
import com.sp.world.levels.custom.Level2BackroomsLevel;
import com.sp.world.levels.custom.PoolroomsBackroomsLevel;
import net.fabricmc.api.EnvType;
import net.fabricmc.fabric.api.client.networking.v1.ClientPlayNetworking;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.Minecraft;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.client.resources.sounds.SimpleSoundInstance;
import net.minecraft.client.sounds.SoundManager;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.resources.ResourceKey;
import net.minecraft.sounds.SoundSource;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.phys.Vec3;

import java.util.List;
import java.util.Optional;

/**
 * This class is just here to avoid dedicated server crashes.
 * Minecraft seems to crash even when a client class is present in a method without being called on the client.
 * This mostly happens with Sound Instances. (Veil lights are gone — see PORT note below.)
 *
 * <p>PORT: phase6 (A4) delta — every Veil deferred light (AreaLight/PointLight + VeilRenderSystem)
 * has been removed. The light block-entities (ported in 6d) no longer hold any light field, so the
 * fluorescent/emergency light-tick methods below are reduced to their non-Veil parts: the hum /
 * blink / alarm sound instances and the block-state bookkeeping. Illumination itself now comes from
 * each light block's vanilla light level + emissive texture. Delta: no real-time deferred point/area
 * lights, no rotating red emergency sweep, no per-light colour/brightness tuning.
 *
 * <p>PORT: phase6e-b — references to {@code SPBRevampedClient} (isInLevel, getCutsceneManager,
 * sendComponentSyncPacket, blackScreen) resolve once the client entrypoint is ported in 6e-b.
 */
public class ClientWrapper {
    public static void skinWalkerPlayStepSound(ServerLimb limb) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            Minecraft client = Minecraft.getInstance();
            client.getSoundManager().play(new SimpleSoundInstance(ModSounds.SKINWALKER_FOOTSTEP, SoundSource.HOSTILE, 10.0f, 1.0f, limb.random, limb.pos.x, limb.pos.y, limb.pos.z));
        }
    }

    public static void walkerPlayStepSound(ServerLimb limb) {
        if (FabricLoader.getInstance().getEnvironmentType() == EnvType.CLIENT) {
            Minecraft client = Minecraft.getInstance();
            client.getSoundManager().play(new SimpleSoundInstance(ModSounds.WALKER_FOOTSTEP, SoundSource.HOSTILE, 10.0f, 1.0f, limb.random, limb.pos.x, limb.pos.y, limb.pos.z));
        }
    }

    public static void tickClientPlayerComponent(PlayerComponent playerComponent) {
        Minecraft client = Minecraft.getInstance();

        if (client.player != null && playerComponent.player == client.player) {
            SoundManager soundManager = client.getSoundManager();

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            //Get a list of all the smilers in the area and see if any of them can see you
            List<SmilerEntity> smilerEntityList = playerComponent.player.level().getEntitiesOfClass(SmilerEntity.class, playerComponent.player.getBoundingBox().inflate(15, 1, 15), livingEntity -> true);
            boolean isSeen = false;
            if (!smilerEntityList.isEmpty()) {
                for (SmilerEntity smiler : smilerEntityList) {
                    // PORT: Yarn MobEntity.canSee(Entity) -> mojmap Mob.getSensing().hasLineOfSight(Entity).
                    if (smiler.getSensing().hasLineOfSight(playerComponent.player)) {
                        playerComponent.setShouldGlitch(true);
                        isSeen = true;
                        break;
                    }
                }

            }

            if (!isSeen) {
                playerComponent.setShouldGlitch(false);
            }

            //Update smiler glitch effect
            if (playerComponent.shouldGlitch()) {
                playerComponent.glitchTick = Math.min(playerComponent.glitchTick + 1, 80);
                playerComponent.glitchTimer = Math.min((float) playerComponent.glitchTick / 80, 1.0f);

                if (!soundManager.isActive(playerComponent.GlitchAmbience)) {
                    playerComponent.GlitchAmbience = new SmilerGlitchSoundInstance(playerComponent.player);
                    soundManager.play(playerComponent.GlitchAmbience);
                }

                if (playerComponent.glitchTimer >= 0.25f) {
                    if (!playerComponent.shouldInflictGlitchDamage) {
                        playerComponent.shouldInflictGlitchDamage = true;
                        SPBRevampedClient.sendComponentSyncPacket(true, "glitch");
                    }
                }

            } else if (!playerComponent.isTeleportingToPoolrooms() && (!(SPBRevampedClient.isInLevel(BackroomsLevels.LEVEL324_BACKROOMS_LEVEL) && playerComponent.player.level().getBlockState(playerComponent.player.blockPosition().relative(Direction.DOWN, 2)).getBlock() == Blocks.WOOL.green()))) {
                playerComponent.glitchTick = Math.max(playerComponent.glitchTick - 1, 0);
                playerComponent.glitchTimer = Math.max((float) playerComponent.glitchTick / 80, 0.0f);

                if (playerComponent.glitchTimer <= 0) {
                    if (soundManager.isActive(playerComponent.GlitchAmbience)) {
                        soundManager.stop(playerComponent.GlitchAmbience);
                    }
                }

                if (playerComponent.glitchTimer <= 0.75f) {
                    if (playerComponent.shouldInflictGlitchDamage) {
                        playerComponent.shouldInflictGlitchDamage = false;
                        SPBRevampedClient.sendComponentSyncPacket(false, "glitch");
                    }
                }
            }

            if (SPBRevampedClient.isInLevel(BackroomsLevels.LEVEL324_BACKROOMS_LEVEL) && playerComponent.player.level().getBlockState(playerComponent.player.blockPosition().relative(Direction.DOWN, 2)).getBlock() == Blocks.WOOL.green()) {
                playerComponent.glitchTick = Math.min(playerComponent.glitchTick + 4, 120);
                playerComponent.glitchTimer = (float) playerComponent.glitchTick / 30;

                if (!soundManager.isActive(playerComponent.GlitchAmbience)) {
                    playerComponent.GlitchAmbience = new SmilerGlitchSoundInstance(playerComponent.player);
                    soundManager.play(playerComponent.GlitchAmbience);
                }


                if (playerComponent.glitchTimer >= 3) {
                    if (SPBRevampedClient.isInLevel(BackroomsLevels.LEVEL324_BACKROOMS_LEVEL) && playerComponent.player.level().getBlockState(playerComponent.player.blockPosition().relative(Direction.DOWN, 3)).getBlock() == Blocks.WOOL.red()) {
                        playerComponent.player.teleportTo(playerComponent.player.getX(), playerComponent.player.getY() - 65, playerComponent.player.getZ());
                    }
                }
            }


            //Teleporting to poolrooms Glitch
            if (playerComponent.isTeleportingToPoolrooms()) {
                playerComponent.glitchTick = Math.min(playerComponent.glitchTick + 1, 120);
                playerComponent.glitchTimer = Math.min((float) playerComponent.glitchTick / 120, 1.0f);

                if (!soundManager.isActive(playerComponent.GlitchAmbience)) {
                    playerComponent.GlitchAmbience = new SmilerGlitchSoundInstance(playerComponent.player);
                    soundManager.play(playerComponent.GlitchAmbience);
                }
            }

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            //Sync Target Entity for updating SkinWalker suspicion
            if (playerComponent.getTargetEntity() != client.crosshairPickEntity) {
                playerComponent.setTargetEntity(client.crosshairPickEntity);

                // PORT: phase5 networking — Yarn PacketByteBuf channel send replaced by the 26.2
                // CustomPacketPayload (TargetEntitySync); -1 means "no target".
                int targetId = playerComponent.getTargetEntity() != null ? playerComponent.getTargetEntity().getId() : -1;
                ClientPlayNetworking.send(new TargetEntitySync(targetId));
            }

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            // Skinwalker jumpscare timeline — drives the creepy-face overlay flags on BackroomsHudOverlay
            // while the player is being captured. PORT FIX: doJumpscare() existed but was never called, so
            // the timeline never ran and a capture just showed the black-screen with no face.
            if (playerComponent.isBeingCaptured()) {
                SkinwalkerJumpscare.doJumpscare(client, playerComponent);
            } else if (BackroomsHudOverlay.jumpscare) {
                // Capture ended early (e.g. interrupted) — clear any lingering jumpscare flags.
                SkinwalkerJumpscare.reset();
            }

            //Flavor text while being controlled by the SkinWalker
            if (playerComponent.hasBeenCaptured() && !playerComponent.isBeingCaptured()) {
                SkinWalkerCapturedFlavorText.tickFlavorText(playerComponent.player);
            }

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            //Client side stuff for level 0 -> 1 and 1 -> 2 and so on.

            Optional<BackroomsLevel> backroomsLevel = BackroomsLevels.getLevel(playerComponent.player.level());

            if (backroomsLevel.isPresent()) {
                BackroomsLevel level = backroomsLevel.get();

                List<BackroomsLevel.LevelTransition> teleports = level.checkForTransition(playerComponent, playerComponent.player.level());

                if (!teleports.isEmpty() && playerComponent.currentTransition == null) {
                    playerComponent.currentTransition = teleports.get(0);
                }

                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

                //Flashlight

                if (ModKeyBinds.toggleFlashlight.consumeClick() && !SPBRevampedClient.getCutsceneManager().isPlaying && !SPBRevampedClient.getCutsceneManager().blackScreen.isBlackScreen && !playerComponent.hasBeenCaptured && !playerComponent.isBeingCaptured()) {
                    playerComponent.player.playSound(ModSounds.FLASHLIGHT_CLICK, 0.5f, 1);
                    if (level.allowsTorch().value()) {
                        playerComponent.setFlashLightOn(!playerComponent.isFlashLightOn());
                        HelpfulHintManager.disableFlashlightHint();

                        if (!playerComponent.player.isSpectator()) {
                            SPBRevampedClient.sendComponentSyncPacket(playerComponent.isFlashLightOn(), "flashlight");
                        }
                    } else {
                        playerComponent.setFlashLightOn(false);
                        playerComponent.player.sendSystemMessage(level.allowsTorch().string());
                    }
                } else if (playerComponent.hasBeenCaptured && playerComponent.isBeingCaptured()) {
                    if (playerComponent.isFlashLightOn()) {
                        playerComponent.setFlashLightOn(false);

                        SPBRevampedClient.sendComponentSyncPacket(playerComponent.isFlashLightOn(), "flashlight");
                    }
                }

                if (!level.allowsTorch().value()) {
                    if (playerComponent.isFlashLightOn()) {
                        SPBRevampedClient.sendComponentSyncPacket(false, "flashlight");
                    }
                    playerComponent.setFlashLightOn(false);
                }

                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
                ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            }


            if (playerComponent.currentTransition != null) {
                if (playerComponent.getTeleportingTimer() == -1) {
                    playerComponent.setTeleportingTimer(playerComponent.currentTransition.duration());
                }

                if (playerComponent.getTeleportingTimer() == 0) {
                    playerComponent.currentTransition.teleport().to().transitionOut(playerComponent.currentTransition.teleport());
                    playerComponent.currentTransition.teleport().to().transitionIn(playerComponent.currentTransition.teleport());
                    playerComponent.currentTransition = null;
                }
            }

            if (playerComponent.getTeleportingTimer() >= 1) {
                if (playerComponent.currentTransition != null) {
                    playerComponent.currentTransition.callback().tick(playerComponent.currentTransition.teleport(), playerComponent.getTeleportingTimer());
                }
                playerComponent.setTeleportingTimer(playerComponent.getTeleportingTimer() - 1);
            }

            ////AMBIENCE////
            ResourceKey<Level> levelKey = playerComponent.player.level().dimension();

            if ((levelKey == BackroomsLevels.LEVEL1_WORLD_KEY || levelKey == BackroomsLevels.LEVEL2_WORLD_KEY) && !soundManager.isActive(playerComponent.DeepAmbience)) {
                playerComponent.DeepAmbience = new AmbientSoundInstance(playerComponent.player);
                soundManager.play(playerComponent.DeepAmbience);
            }

            if (levelKey == BackroomsLevels.LEVEL2_WORLD_KEY && !soundManager.isActive(playerComponent.WaterPipeAmbience) && !soundManager.isActive(playerComponent.GasPipeAmbience)) {
                playerComponent.WaterPipeAmbience = new WaterPipeSoundInstance(playerComponent.player);
                playerComponent.GasPipeAmbience = new GasPipeSoundInstance(playerComponent.player);

                soundManager.play(playerComponent.WaterPipeAmbience);
                soundManager.play(playerComponent.GasPipeAmbience);
            }

            if ((BackroomsLevels.getLevel(playerComponent.player.level()).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL))
                    instanceof Level2BackroomsLevel level) {
                if (levelKey == BackroomsLevels.LEVEL2_WORLD_KEY && !soundManager.isActive(playerComponent.WarpAmbience) && level.isWarping()) {
                    playerComponent.WarpAmbience = new CreakingSoundInstance(playerComponent.player);
                    soundManager.play(playerComponent.WarpAmbience);
                }
            }

            if ((BackroomsLevels.getLevel(playerComponent.player.level()).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL))
                    instanceof PoolroomsBackroomsLevel level) {
                if (level.isNoon() && !soundManager.isActive(playerComponent.PoolroomsNoonAmbience)) {
                    playerComponent.PoolroomsNoonAmbience = new PoolroomsNoonAmbienceSoundInstance(playerComponent.player);
                    soundManager.play(playerComponent.PoolroomsNoonAmbience);
                }
            }

            if ((BackroomsLevels.getLevel(playerComponent.player.level()).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL))
                    instanceof PoolroomsBackroomsLevel level) {
                if (!level.isNoon() && !soundManager.isActive(playerComponent.PoolroomsSunsetAmbience)) {
                    playerComponent.PoolroomsSunsetAmbience = new PoolroomsSunsetAmbienceSoundInstance(playerComponent.player);
                    soundManager.play(playerComponent.PoolroomsSunsetAmbience);
                }
            }

            if ((BackroomsLevels.getLevel(playerComponent.player.level()).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL))
                    instanceof Level1BackroomsLevel level) {
                if (level.getLightState() == BackroomsLevelWithLights.LightState.BLACKOUT && !soundManager.isActive(playerComponent.SmilerAmbience)) {
                    playerComponent.SmilerAmbience = new SmilerAmbienceSoundInstance(playerComponent.player);
                    soundManager.play(playerComponent.SmilerAmbience);
                }
            }

            if ((levelKey == BackroomsLevels.INFINITE_FIELD_WORLD_KEY) && !soundManager.isActive(playerComponent.WindAmbience)) {
                playerComponent.WindAmbience = new InfiniteGrassAmbienceSoundInstance(playerComponent.player);
                soundManager.play(playerComponent.WindAmbience);
            }

            if ((levelKey == BackroomsLevels.LEVEL324_WORLD_KEY) && !soundManager.isActive(playerComponent.WindAmbience) && playerComponent.player.getY() > 20) {
                playerComponent.WindAmbience = new InfiniteGrassAmbienceSoundInstance(playerComponent.player);
                if (soundManager.isActive(playerComponent.WindTunnelAmbience)) {
                    soundManager.stop(playerComponent.WindTunnelAmbience);
                }
                soundManager.play(playerComponent.WindAmbience);
            }

            if ((levelKey == BackroomsLevels.LEVEL324_WORLD_KEY) && !soundManager.isActive(playerComponent.WindTunnelAmbience) && playerComponent.player.getY() < 20) {
                playerComponent.WindTunnelAmbience = new WindTunnelAmbienceSoundInstance(playerComponent.player);
                if (soundManager.isActive(playerComponent.WindAmbience)) {
                    soundManager.stop(playerComponent.WindAmbience);
                }
                soundManager.play(playerComponent.WindTunnelAmbience);
            }

            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////
            ////////////////////////////////////////////////////////////////////////////////////////////////////////////////////////

            //Level0 Cutscene
            if (playerComponent.player.isInWall() && playerComponent.player.level().dimension() == Level.OVERWORLD && !playerComponent.isDoingCutscene()) {
                playerComponent.suffocationTimer++;
                if (playerComponent.suffocationTimer >= 40) {
                    playerComponent.setDoingCutscene(true);
                    playerComponent.suffocationTimer = 0;
                }
            }

        }
    }

    public static void onRemoveSkinWalkerClientSide(SkinWalkerEntity entity) {
        if (entity.chaseSoundInstance != null && entity.level().isClientSide()) {
            Minecraft.getInstance().getSoundManager().stop(entity.chaseSoundInstance);
        }
    }

    public static void handleSkinWalkerEntityClientSide(SkinWalkerEntity entity) {
        Minecraft client = Minecraft.getInstance();
        if (!client.getSoundManager().isActive(entity.chaseSoundInstance)) {
            entity.chaseSoundInstance = new SkinWalkerChaseSoundInstance(entity);
            client.getSoundManager().play(entity.chaseSoundInstance);
        }
    }

    // PORT: phase6 (A4) — Veil red AreaLight sweep + PointLight removed (the BE no longer holds them).
    // Reduced to the emergency-alarm sound + state bookkeeping; illumination is the block's vanilla
    // light level (see ModBlocks.EMERGENCY_LIGHT). Delta: no rotating red light sweep.
    public static void tickEmergencyLight(Level world, BlockPos pos, BlockState state, EmergencyLightBlockEntity block) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;

        if (player == null) {
            return;
        }

        if (state.getValue(EmergencyLightBlock.RED_LIGHT)) {
            if (!block.playingEmergencyAlarm) {
                block.emergencyAlarmSoundInstance = new EmergencyAlarmSoundInstance(block, player);
                client.getSoundManager().play(block.emergencyAlarmSoundInstance);
                block.setEmergencyAlarm(true);
            }
            return;
        }

        if (block.playingEmergencyAlarm) {
            client.getSoundManager().stop(block.emergencyAlarmSoundInstance);
            block.emergencyAlarmSoundInstance = null;
            block.setEmergencyAlarm(false);
        }
    }

    // PORT: phase6 (A4) — Veil PointLight add/remove removed; reduced to the LIGHT_BLINK + hum sounds
    // and the playing-sound flag. Illumination is the block's vanilla light level.
    public static void doClientSideThinFluorescentsTick(Level world, BlockPos pos, BlockState state, java.util.Random random1, Vec3 position, ThinFluorescentLightBlockEntity block) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null) {
            Vec3 playerPos = player.position();
            double distance;

            if (world.dimension() == BackroomsLevels.LEVEL2_WORLD_KEY) {
                distance = Math.min(ConfigStuff.getLightRenderDistance(), 32);
            } else {
                distance = ConfigStuff.getLightRenderDistance();
            }

            boolean withinDistance = pos.closerToCenterThan(playerPos, distance);

            if (withinDistance) {
                if (!state.getValue(ThinFluorescentLightBlock.COPY) && pos.closerToCenterThan(playerPos, 15.0f)) {
                    if (block.prevOn != world.getBlockState(pos).getValue(ThinFluorescentLightBlock.ON)) {
                        Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(ModSounds.LIGHT_BLINK, SoundSource.AMBIENT, 0.2F, random1.nextFloat(0.9f, 1.1f), block.random, pos));
                    }
                }

                if (!state.getValue(ThinFluorescentLightBlock.COPY) && state.getValue(ThinFluorescentLightBlock.ON) && !state.getValue(ThinFluorescentLightBlock.BLACKOUT)) {
                    if (!block.isPlayingSound() && pos.closerToCenterThan(playerPos, 15.0f) && !SPBRevampedClient.blackScreen) {
                        Minecraft.getInstance().getSoundManager().play(new ThinFluorescentLightSoundInstance(block, player));
                        block.setPlayingSound(true);
                    }
                }
            }
        }
    }


    // PORT: phase6 (A4) — same as the thin variant: Veil PointLight removed, sound + state only.
    public static void doClientSideTinyFluorescentsTick(Level world, BlockPos pos, BlockState state, java.util.Random random1, Vec3 position, TinyFluorescentLightBlockEntity block) {
        LocalPlayer player = Minecraft.getInstance().player;

        if (player != null) {
            Vec3 playerPos = player.position();
            double distance;

            if (world.dimension() == BackroomsLevels.LEVEL2_WORLD_KEY) {
                distance = Math.min(ConfigStuff.getLightRenderDistance(), 32);
            } else {
                distance = ConfigStuff.getLightRenderDistance();
            }

            boolean withinDistance = pos.closerToCenterThan(playerPos, distance);

            if (withinDistance) {
                // 26.2 compares block-state properties by identity: this must read the TINY block's
                // own COPY/ON/BLACKOUT instances — the thin block's same-named properties threw
                // "property does not exist" and crashed the client the first time a tiny light
                // (e.g. Level 324's gas station) ticked in render distance (same bug class as the
                // thin/tiny ON fix in the block entities).
                if (!state.getValue(TinyFluorescentLightBlock.COPY) && pos.closerToCenterThan(playerPos, 15.0f)) {
                    if (block.prevOn != world.getBlockState(pos).getValue(TinyFluorescentLightBlock.ON)) {
                        Minecraft.getInstance().getSoundManager().play(new SimpleSoundInstance(ModSounds.LIGHT_BLINK, SoundSource.AMBIENT, 0.2F, random1.nextFloat(0.9f, 1.1f), block.random, pos));
                    }
                }

                if (!state.getValue(TinyFluorescentLightBlock.COPY) && state.getValue(TinyFluorescentLightBlock.ON) && !state.getValue(TinyFluorescentLightBlock.BLACKOUT)) {
                    if (!block.isPlayingSound() && pos.closerToCenterThan(playerPos, 15.0f) && !SPBRevampedClient.blackScreen) {
                        Minecraft.getInstance().getSoundManager().play(new TinyFluorescentLightSoundInstance(block, player));
                        block.setPlayingSound(true);
                    }
                }
            }
        }
    }

    // PORT: phase6 (A4) — Veil PointLight removed; reduced to the LIGHT_BLINK + hum sounds and the
    // playing-sound flag. Illumination is the block's vanilla light level.
    public static void doClientSideTick(Level world, BlockPos pos, BlockState state, FluorescentLightBlockEntity block) {
        if (!world.isClientSide()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        Player player = client.player;

        if (player != null) {

            if (!state.getValue(FluorescentLightBlock.COPY)) {
                if (pos.closerToCenterThan(player.position(), 20)) {
                    if (block.prevOn != world.getBlockState(pos).getValue(FluorescentLightBlock.ON)) {
                        client.getSoundManager().play(new SimpleSoundInstance(ModSounds.LIGHT_BLINK, SoundSource.AMBIENT, 0.1F, block.random1.nextFloat(0.9f, 1.1f), block.random, pos));
                    }
                }
            }

            Vec3 playerPos = player.position();
            boolean withinDistance = pos.closerToCenterThan(playerPos, ConfigStuff.getLightRenderDistance());
            if (withinDistance) {
                if (!state.getValue(FluorescentLightBlock.COPY) &&
                        state.getValue(FluorescentLightBlock.ON) &&
                        !state.getValue(FluorescentLightBlock.BLACKOUT)) {
                    if (!block.isPlayingSound() && pos.closerToCenterThan(playerPos, 16.0f) && !state.getValue(FluorescentLightBlock.BLACKOUT) && !SPBRevampedClient.blackScreen) {
                        Minecraft.getInstance().getSoundManager().play(new FluorescentLightSoundInstance(block, player));
                        block.setPlayingSound(true);
                    }
                } else {
                    block.setPlayingSound(false);
                }
            }
        }
    }

}
