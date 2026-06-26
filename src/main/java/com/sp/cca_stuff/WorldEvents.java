package com.sp.cca_stuff;

import com.sp.SPBRevamped;
import com.sp.entity.custom.SkinWalkerEntity;
import com.sp.init.BackroomsLevels;
import com.sp.init.ModEntities;
import com.sp.init.ModSounds;
import com.sp.sounds.voicechat.BackroomsVoicechatPlugin;
import com.sp.world.events.AbstractEvent;
import com.sp.world.levels.BackroomsLevel;
import com.sp.world.levels.BackroomsLevelWithLights;
import com.sp.world.levels.custom.Level0BackroomsLevel;
import net.minecraft.core.UUIDUtil;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySelector;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.storage.ValueInput;
import net.minecraft.world.level.storage.ValueOutput;
import org.ladysnake.cca.api.v3.component.sync.AutoSyncedComponent;
import org.ladysnake.cca.api.v3.component.tick.ServerTickingComponent;

import java.util.List;
import java.util.Map;
import java.util.Optional;
import java.util.UUID;

public class WorldEvents implements AutoSyncedComponent, ServerTickingComponent {
    private final Level world;

    private AbstractEvent activeEvent;
    public int ticks;
    private int delay;

    private static final UUID nullUUID = UUID.randomUUID();
    private UUID activeSkinwalkerTarget;
    public SkinWalkerEntity activeSkinWalkerEntity;

    public boolean done;
    private int tick;

    public WorldEvents(Level world) {
        this.world = world;
        this.ticks = 0;
        this.delay = 1800;
        this.activeSkinwalkerTarget = nullUUID;

        this.done = false;
    }

    public void setActiveEvent(AbstractEvent activeEvent) {
        this.activeEvent = activeEvent;
    }
    public AbstractEvent getActiveEvent() {
        return this.activeEvent;
    }

    public Player getActiveSkinwalkerTarget() {
        if(this.activeSkinwalkerTarget == null || this.activeSkinwalkerTarget.equals(nullUUID)){
            return null;
        }
        return this.world.getPlayerByUUID(this.activeSkinwalkerTarget);
    }
    public void setActiveSkinwalkerTarget(UUID uuid) {
        this.activeSkinwalkerTarget = uuid;
        this.sync();
    }

    public void sync() {
        InitializeComponents.EVENTS.sync(this.world);
    }

    @Override
    public void readData(ValueInput tag) {
        // BackroomsLevel still serialises against a CompoundTag (ported in Phase 3); bridge the CCA 8
        // ValueInput by extracting the stored CompoundTag and handing it to the matching level.
        CompoundTag levelData = tag.read("levelData", CompoundTag.CODEC).orElseGet(CompoundTag::new);
        for (BackroomsLevel level: BackroomsLevels.BACKROOMS_LEVELS) {
            if (this.world.dimension() == level.getWorldKey()) {
                level.readFromNbt(levelData);
            }
        }

        this.activeSkinwalkerTarget = tag.read("activeSkinwalkerTarget", UUIDUtil.CODEC).orElse(nullUUID);
        this.done = tag.getBooleanOr("skinwalkerDone", false);
    }

    @Override
    public void writeData(ValueOutput tag) {
        CompoundTag levelData = new CompoundTag();
        for (BackroomsLevel level: BackroomsLevels.BACKROOMS_LEVELS) {
            if (this.world.dimension() == level.getWorldKey()) {
                level.writeToNbt(levelData);
            }
        }
        tag.store("levelData", CompoundTag.CODEC, levelData);

        tag.store("activeSkinwalkerTarget", UUIDUtil.CODEC, this.activeSkinwalkerTarget);
        tag.putBoolean("skinwalkerDone", this.done);
    }

    @Override
    public void serverTick() {
        if (world != null && !world.players().isEmpty() && BackroomsLevels.isInBackrooms(world.dimension())) {
            ticks++;

            tickWorldEvents();
            //Start Looking for a player to take and take them when they're not talking and can't be seen
            tickSkinWalkerCapturing();

            shouldReleasePlayer();
        }

        shouldSync();
    }

    private void shouldReleasePlayer() {
        if (this.activeSkinWalkerEntity == null) {
            if (this.getActiveSkinwalkerTarget() != null) {
                ServerPlayer target = (ServerPlayer) this.getActiveSkinwalkerTarget();
                PlayerComponent targetComponent = InitializeComponents.PLAYER.get(target);

                if (targetComponent.hasBeenCaptured() || targetComponent.isBeingCaptured()) {
                    target.setGameMode(GameType.SURVIVAL);
                    targetComponent.setHasBeenCaptured(false);
                    targetComponent.setShouldBeMuted(false);
                    targetComponent.sync();
                    SPBRevamped.sendPersonalPlaySoundPacket(target, ModSounds.SKINWALKER_RELEASE, 1.0f, 1.0f);
                }

                this.activeSkinwalkerTarget = nullUUID;
            }

            return;
        }

        SkinWalkerComponent component = InitializeComponents.SKIN_WALKER.get(this.activeSkinWalkerEntity);

        if (!component.shouldBeginRelease()) {
            if (this.getActiveSkinwalkerTarget() != null) {
                ((ServerPlayer) this.getActiveSkinwalkerTarget()).setGameMode(GameType.SPECTATOR);
                ((ServerPlayer) this.getActiveSkinwalkerTarget()).setCamera(this.activeSkinWalkerEntity);
            }

            return;
        }

        BackroomsLevels.getLevel(world).ifPresent((backroomsLevel -> {
            if (backroomsLevel instanceof Level0BackroomsLevel level0BackroomsLevel) {
                PlayerComponent targetComponent = InitializeComponents.PLAYER.get(this.getActiveSkinwalkerTarget());
                ServerPlayer target = (ServerPlayer) this.getActiveSkinwalkerTarget();
                tick++;

                if (this.tick == 1) {
                    level0BackroomsLevel.setLightState(BackroomsLevelWithLights.LightState.FLICKER);
                }

                if (this.tick == 80) {
                    level0BackroomsLevel.setLightState(BackroomsLevelWithLights.LightState.OFF);

                    targetComponent.setBeingReleased(true);
                    targetComponent.sync();

                    SPBRevamped.sendPersonalPlaySoundPacket(target, ModSounds.SKINWALKER_RELEASE, 1.0f, 1.0f);

                    target.setGameMode(targetComponent.getPrevGameMode() != null ? targetComponent.getPrevGameMode() : GameType.SURVIVAL);
                    target.setCamera(target);

                    for (Player player : this.world.players()) {
                        PlayerComponent playerComponent = InitializeComponents.PLAYER.get(player);
                        playerComponent.setFlashLightOn(false);
                        playerComponent.sync();
                    }
                    this.activeSkinWalkerEntity.discard();
                }

                if (this.tick >= 105) {
                    level0BackroomsLevel.setLightState(BackroomsLevelWithLights.LightState.ON);
                    targetComponent.setBeingReleased(false);
                    targetComponent.setHasBeenCaptured(false);
                    targetComponent.setShouldBeMuted(false);
                    targetComponent.sync();
                    this.activeSkinWalkerEntity = null;
                }
            }
        }));
    }

    private void tickSkinWalkerCapturing() {
        if (!(BackroomsLevels.getLevel(world).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL) instanceof Level0BackroomsLevel level0BackroomsLevel)) {
            return;
        }

        if (level0BackroomsLevel.getIntercomCount() < 2 || world.players().size() <= 1) {
            return;
        }

        if (done || this.world.dimension() != BackroomsLevels.LEVEL0_WORLD_KEY) {
            return;
        }

        if(this.activeSkinWalkerEntity != null){
            return;
        }
        //Thank goodness for https://stackoverflow.com/questions/2776176/get-minvalue-of-a-mapkey-double
        Map.Entry<UUID, Float> min = null;
        for (Map.Entry<UUID, Float> entry : BackroomsVoicechatPlugin.speakingTime.entrySet()) {
            if (min == null || min.getValue() > entry.getValue()) {
                min = entry;

            }
        }

        if (min != null) {
            Player target = this.world.getPlayerByUUID(min.getKey());
            if (target != null && target.isAlive()) {
                this.setActiveSkinwalkerTarget(target.getUUID());
            }
        }

        if (this.getActiveSkinwalkerTarget() == null) {
            return;
        }

        Player target = this.getActiveSkinwalkerTarget();
        PlayerComponent targetComponent = InitializeComponents.PLAYER.get(target);

        if (targetComponent.isSpeaking()) {
            return;
        }

        // 26.2: World.getPlayers(TargetPredicate, source, Box) is removed. The original ignored distance
        // scaling and line-of-sight while excluding creative/spectator within 50 blocks; getEntitiesOfClass
        // with the inflated box + EntitySelector.NO_CREATIVE_OR_SPECTATOR reproduces that.
        List<Player> playerEntityList = target.level().getEntitiesOfClass(
                Player.class,
                target.getBoundingBox().inflate(50),
                EntitySelector.NO_CREATIVE_OR_SPECTATOR);

        boolean seen = false;
        for (Player player : playerEntityList) {
            if (player != target) {
                PlayerComponent playerComponent = InitializeComponents.PLAYER.get(player);
                if (playerComponent.canSeeActiveSkinWalkerTarget()) {
                    seen = true;
                    break;
                }
            }
        }

        if (seen) {
            return;
        }
        //Take em
        SkinWalkerEntity skinWalkerEntity = ModEntities.SKIN_WALKER_ENTITY.create(this.world, EntitySpawnReason.EVENT);
        if (skinWalkerEntity == null) {
            return;
        }

        skinWalkerEntity.snapTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
        skinWalkerEntity.setDeltaMovement(target.getDeltaMovement());
        this.world.addFreshEntity(skinWalkerEntity);
        this.activeSkinWalkerEntity = skinWalkerEntity;

        targetComponent.setPrevGameMode(((ServerPlayer) target).gameMode());
        targetComponent.setBeingCaptured(true);
        targetComponent.setHasBeenCaptured(true);
        targetComponent.setShouldBeMuted(true);
        targetComponent.sync();

        ((ServerPlayer) target).setGameMode(GameType.SPECTATOR);
        ((ServerPlayer) target).setCamera(skinWalkerEntity);
        this.done = true;
        this.sync();
    }

    private void tickWorldEvents() {
        if (activeEvent == null) {
            this.delay--;
            if (this.delay > 0) {
                return;
            }

            this.delay = 0;

            Optional<BackroomsLevel> currentDimension = BackroomsLevels.getLevel(world);

            if (currentDimension.isEmpty()) {
                return;
            }

            this.activeEvent = currentDimension.get().getRandomEvent(world);
            activeEvent.init(this.world);
            ticks = 0;
            this.delay = currentDimension.get().nextEventDelay();

            return;
        }

        if (activeEvent.duration() <= ticks) {
            activeEvent.finish(this.world);
            if (activeEvent.isDone()) activeEvent = null;
        } else {
            activeEvent.ticks(ticks, this.world);
        }
    }

    private void shouldSync() {
        boolean sync = false;

        for (BackroomsLevel backroomsLevel : BackroomsLevels.BACKROOMS_LEVELS) {
            if (backroomsLevel.shouldSync()) {
                sync = true;
                break;
            }
        }

        if (sync){
            this.sync();
        }
    }
}
