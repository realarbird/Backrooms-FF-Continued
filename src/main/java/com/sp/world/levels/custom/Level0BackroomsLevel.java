package com.sp.world.levels.custom;

import com.sp.SPBRevamped;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.init.BackroomsLevels;
import com.sp.world.events.AbstractEvent;
import com.sp.world.events.generic.lights.LightLevelBlackout;
import com.sp.world.events.generic.lights.LightLevelFlicker;
import com.sp.world.events.level0.Level0IntercomBasic;
import com.sp.world.events.level0.Level0Music;
import com.sp.world.generation.chunk_generator.Level0ChunkGenerator;
import com.sp.world.levels.BackroomsLevel;
import com.sp.world.levels.BackroomsLevelWithLights;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class Level0BackroomsLevel extends BackroomsLevel implements BackroomsLevelWithLights {
    ///execute in spb-revamped:level0 run tp 1063 15 24

    private int blackoutCount = 0;
    private int intercomCount = 0;
    private LightState lightState = LightState.ON;

    public Level0BackroomsLevel() {
        super("level0", Level0ChunkGenerator.CODEC, new RoomCount(8), new Vec3(0, 21, 0), BackroomsLevels.LEVEL0_WORLD_KEY);
    }

    @Override
    public boolean rendersClouds() {
        return false;
    }

    @Override
    public boolean rendersSky() {
        return false;
    }

    @Override
    public void register() {
        super.register();
        this.registerEvent("blackout", LightLevelBlackout::new);
        this.registerEvent("flicker", LightLevelFlicker::new);
        this.registerEvent("intercom", Level0IntercomBasic::new);
        this.registerEvent("music", Level0Music::new);

        this.registerTransition((world, playerComponent, from) -> {
            List<LevelTransition> playerList = new ArrayList<>();

            if (from instanceof Level0BackroomsLevel && playerComponent.player.position().y() <= 11 && playerComponent.player.onGround()) {
                for (Player player : playerComponent.player.level().players()) {
                    PlayerComponent otherPlayerComponent = InitializeComponents.PLAYER.get(player);
                    playerList.add(getLevel1Transition(otherPlayerComponent));
                }
            }

            return playerList;
        }, this.getLevelId() + "->" + BackroomsLevels.LEVEL1_BACKROOMS_LEVEL.getLevelId());
    }

    private LevelTransition getLevel1Transition(PlayerComponent playerComponent) {
        return new LevelTransition(
            30,
            (teleport, tick) -> {
                if (!teleport.playerComponent().player.level().isClientSide() && tick == 30) {
                    if(!teleport.playerComponent().isTeleporting()) {
                        SPBRevamped.sendLevelTransitionLightsOutPacket((ServerPlayer) teleport.playerComponent().player, 80);
                    }
                }
            },
            new CrossDimensionTeleport(playerComponent,
                calculateLevel1TeleportCoords(
                    playerComponent.player,
                    playerComponent.player.chunkPosition()),
                this,
                BackroomsLevels.LEVEL1_BACKROOMS_LEVEL),
        (teleport, tick) -> {});
    }

    private Vec3 calculateLevel1TeleportCoords(Player player, ChunkPos chunkPos) {
        if(chunkPos.x == player.chunkPosition().x && chunkPos.z == player.chunkPosition().z) {
            int chunkX = chunkPos.getMinBlockX();
            int chunkZ = chunkPos.getMinBlockZ();

            double playerX = player.position().x;
            double playerZ = player.position().z;

            return new Vec3(playerX - chunkX, player.position().y + 15, playerZ - chunkZ);
        } else {
            return this.getSpawnPos();
        }
    }

    @Override
    public AbstractEvent getRandomEvent(Level world) {
        AbstractEvent activeEvent = super.getRandomEvent(world);

        if (activeEvent instanceof LightLevelBlackout) {
            this.blackoutCount++;
            if (this.blackoutCount > 2) {
                while (activeEvent instanceof LightLevelBlackout) {
                    activeEvent = super.getRandomEvent(world);
                }
            }
        }

        return activeEvent;
    }

    @Override
    public int nextEventDelay() {
        return random.nextInt(1000, 1500);
    }

    @Override
    public void writeToNbt(CompoundTag nbt) {
        nbt.putInt("blackoutCount", blackoutCount);
        nbt.putInt("intercomCount", intercomCount);
        nbt.putString("lightState", lightState.name());
    }

    @Override
    public void readFromNbt(CompoundTag nbt) {
        this.blackoutCount = nbt.getIntOr("blackoutCount", 0);
        this.intercomCount = nbt.getIntOr("intercomCount", 0);
        this.lightState = LightState.valueOf(nbt.getStringOr("lightState", "ON"));
    }

    @Override
    public void transitionOut(CrossDimensionTeleport crossDimensionTeleport) {
    }

    @Override
    public void transitionIn(CrossDimensionTeleport crossDimensionTeleport) {

    }

    public int getIntercomCount() {
        return intercomCount;
    }

    public void setIntercomCount(int intercomCount) {
        this.justChanged();
        this.intercomCount = intercomCount;
    }

    public void addIntercomCount() {
        this.justChanged();
        this.intercomCount++;
    }

    public void setLightState(LightState lightState) {
        this.justChanged();
        this.lightState = lightState;
    }

    public LightState getLightState() {
        return this.lightState;
    }
}
