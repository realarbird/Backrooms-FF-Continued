package com.sp.world.levels.custom;

import com.sp.SPBRevamped;
import com.sp.SPBRevampedClient;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.init.BackroomsLevels;
import com.sp.init.ModBlocks;
import com.sp.world.events.generic.lights.LightLevelFlicker;
import com.sp.world.events.level324.ScreechSoundEvent;
import com.sp.world.generation.chunk_generator.Level324ChunkGenerator;
import com.sp.world.levels.BackroomsLevel;
import com.sp.world.levels.BackroomsLevelWithLights;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec2;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class Level324Backroomslevel extends BackroomsLevel implements BackroomsLevelWithLights {
    private BackroomsLevelWithLights.LightState lightState = BackroomsLevelWithLights.LightState.ON;

    public Level324Backroomslevel() {
        super("level324", Level324ChunkGenerator.CODEC, new Vec3(52,65,21), BackroomsLevels.LEVEL324_WORLD_KEY);

        this.registerEvent("flicker", LightLevelFlicker::new);
        this.registerEvent("ambience", ScreechSoundEvent::new);

        this.registerTransition((world, playerComponent, from) -> {
            List<LevelTransition> playerList = new ArrayList<>();

            int exitRadius = SPBRevamped.getExitSpawnRadius(world);

            if (from instanceof Level324Backroomslevel &&
                    hasGrassBeneath(playerComponent) &&
                    playerComponent.player.position().distanceToSqr(new Vec3(0, 65, 0)) >= (double) ((exitRadius / 3) * (exitRadius / 3)) ) {
                playerList.add(getInfiniteFieldsTransition(playerComponent));
            }

            return playerList;
        }, this.getLevelId() + " -> " + BackroomsLevels.INFINITE_FIELD_BACKROOMS_LEVEL.getLevelId());

        this.registerTransition((world, playerComponent, from) -> {
            List<LevelTransition> playerList = new ArrayList<>();

            Vec2[] puddleLocations = new Vec2[]{
                    new Vec2(300.0f, 0.0f),
                    new Vec2(-300.0f, 0.0f),
                    new Vec2(0.0f, 300.0f),
                    new Vec2(0.0f, -300.0f),
                    new Vec2(150.0f, 150.0f),
                    new Vec2(150.0f, -150.0f),
                    new Vec2(-150.0f, 150.0f),
                    new Vec2(-150.0f, -150.0f),
                    new Vec2(100.0f, 200.0f),
                    new Vec2(100.0f, -200.0f),
                    new Vec2(-100.0f, 200.0f),
                    new Vec2(-100.0f, -200.0f),
                    new Vec2(200.0f, 100.0f),
                    new Vec2(-200.0f, 100.0f),
                    new Vec2(200.0f, -100.0f),
                    new Vec2(-200.0f, -100.0f)
            };

            if (from instanceof Level324Backroomslevel && playerComponent.player.getY() < 20) {
                for (Vec2 vec2f : puddleLocations) {
                    if (4 > vec2f.distanceToSqr(new Vec2((float) playerComponent.player.getX(), (float) playerComponent.player.getZ()))) {
                        playerList.add(getPoolRoomsTransition(playerComponent));
                    }
                }
            }

            return playerList;
        }, this.getLevelId() + " -> " + BackroomsLevels.POOLROOMS_BACKROOMS_LEVEL.getLevelId());
    }

    private static boolean hasGrassBeneath(PlayerComponent playerComponent) {
        return playerComponent.player.level().getBlockState(playerComponent.player.mainSupportingBlockPos.orElseGet(() ->
                playerComponent.player.blockPosition().subtract(new Vec3i(0,1,0)))).getBlock() == ModBlocks.RED_DIRT;
    }

    private LevelTransition getInfiniteFieldsTransition(PlayerComponent playerComponent) {
        return new LevelTransition(
                40,
                (teleport, tick) -> {
                    Level world = teleport.playerComponent().player.level();

                    if (world.isClientSide()) {
                        if (tick == 14) {
                            SPBRevampedClient.getCutsceneManager().blackScreen.showBlackScreen(20, true, false);
                        }
                        return;
                    }

                    if (tick == 20) {
                        teleport.playerComponent().setShouldNoClip(true);
                        teleport.playerComponent().sync();
                    }

                    if (tick == 14) {
                        SPBRevamped.sendBlackScreenPacket((ServerPlayer) teleport.playerComponent().player, 20, true, false);
                    }

                    //After the screen turns black THEN teleport
                    if (tick == 1) {
                        teleport.playerComponent().setShouldNoClip(false);
                        teleport.playerComponent().sync();
                    }
                }, // Tick
                new CrossDimensionTeleport(
                        playerComponent,
                        BackroomsLevels.INFINITE_FIELD_BACKROOMS_LEVEL.getSpawnPos(),
                        this,
                        BackroomsLevels.INFINITE_FIELD_BACKROOMS_LEVEL
                ),
                (teleport, tick) -> {
                    teleport.playerComponent().setShouldNoClip(false);
                    teleport.playerComponent().sync();
                }); // Cancel
    }

    private LevelTransition getPoolRoomsTransition(PlayerComponent playerComponent) {
        return new LevelTransition(
                10,
                (teleport, tick) -> {
                    Level world = teleport.playerComponent().player.level();
                    if (tick == 9) {
                        teleport.playerComponent().setShouldNoClip(true);
                        teleport.playerComponent().sync();
                    }

                    if (world.isClientSide()) {
                        if (tick == 4) {
                            SPBRevampedClient.getCutsceneManager().blackScreen.showBlackScreen(20, true, false);
                        }
                        return;
                    }

                    if (tick == 4) {
                        SPBRevamped.sendBlackScreenPacket((ServerPlayer) teleport.playerComponent().player, 20, true, false);
                    }

                    //After the screen turns black THEN teleport
                    if (tick == 1) {
                        teleport.playerComponent().setShouldNoClip(false);
                        teleport.playerComponent().sync();
                    }
                }, // Tick
                new CrossDimensionTeleport(
                        playerComponent,
                        BackroomsLevels.POOLROOMS_BACKROOMS_LEVEL.getSpawnPos(),
                        this,
                        BackroomsLevels.POOLROOMS_BACKROOMS_LEVEL
                ),
                (teleport, tick) -> {
                    teleport.playerComponent().setShouldNoClip(false);
                    teleport.playerComponent().sync();
                }); // Cancel
    }

    @Override
    public boolean rendersClouds() {
        return false;
    }

    @Override
    public boolean rendersSky() {
        return false;
    }

    public int nextEventDelay() {
        return random.nextInt(1000, 1200);
    }

    @Override
    public void writeToNbt(CompoundTag nbt) {
        nbt.putString("lightState", lightState.name());
    }

    @Override
    public void readFromNbt(CompoundTag nbt) {
        this.lightState = BackroomsLevelWithLights.LightState.valueOf(nbt.getStringOr("lightState", "ON"));

    }

    @Override
    public void transitionOut(CrossDimensionTeleport crossDimensionTeleport) {

    }

    @Override
    public void transitionIn(CrossDimensionTeleport crossDimensionTeleport) {

    }

    public void setLightState(BackroomsLevelWithLights.LightState lightState) {
        this.justChanged();
        this.lightState = lightState;
    }

    public BackroomsLevelWithLights.LightState getLightState() {
        return this.lightState;
    }
}
