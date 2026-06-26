package com.sp.world.levels.custom;

import com.sp.SPBRevamped;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.init.BackroomsLevels;
import com.sp.world.events.generic.lights.LightLevelFlicker;
import com.sp.world.events.level1.Level1Ambience;
import com.sp.world.events.level1.Level1Blackout;
import com.sp.world.generation.chunk_generator.Level1ChunkGenerator;
import com.sp.world.levels.BackroomsLevel;
import com.sp.world.levels.BackroomsLevelWithLights;
import net.minecraft.world.level.block.state.BlockState;
import net.minecraft.world.level.block.Blocks;
import net.minecraft.world.entity.player.Player;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.ChunkPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.core.Vec3i;

import java.util.ArrayList;
import java.util.List;

public class Level1BackroomsLevel extends BackroomsLevel implements BackroomsLevelWithLights {
    private BackroomsLevelWithLights.LightState lightState = BackroomsLevelWithLights.LightState.ON;

    public Level1BackroomsLevel() {
        super("level1", Level1ChunkGenerator.CODEC, new RoomCount(6, 24, 24, 12, 24), new Vec3(6, 22, 3), BackroomsLevels.LEVEL1_WORLD_KEY);
    }

    @Override
    public void register() {
        super.register();

        this.registerEvent("blackout", Level1Blackout::new);
        this.registerEvent("flicker", LightLevelFlicker::new);
        this.registerEvent("ambience", Level1Ambience::new);

        this.registerTransition((world, playerComponent, from) -> {
            List<LevelTransition> playerList = new ArrayList<>();


            if (from instanceof Level1BackroomsLevel && playerComponent.player.position().y() <= 12 && playerComponent.player.onGround()) {
                for (Player player : playerComponent.player.level().players()) {
                    PlayerComponent otherPlayerComponent = InitializeComponents.PLAYER.get(player);
                    playerList.add(getLevel2Transition(otherPlayerComponent));
                }
            }

            return playerList;
        }, this.getLevelId() + "->" + BackroomsLevels.LEVEL2_BACKROOMS_LEVEL.getLevelId());

        /*
        this.registerTransition((world, playerComponent, from) -> {
            List<LevelTransition> playerList = new ArrayList<>();
            BlockState state = world.getBlockState(playerComponent.player.blockPosition().subtract(new Vec3i(0, 2, 0)));

            if (
                    from instanceof Level1BackroomsLevel &&
                    playerComponent.player.position().y() >= 26 &&
                    playerComponent.player.onGround() &&
                    state.getBlock() == Blocks.WOOL.blue()
            ) {
                for (Player player : playerComponent.player.level().players()) {
                    PlayerComponent otherPlayerComponent = InitializeComponents.PLAYER.get(player);
                    playerList.add(getLevel324Transition(otherPlayerComponent));
                }
            }

            return playerList;
        }, this.getLevelId() + "->" + BackroomsLevels.LEVEL324_BACKROOMS_LEVEL.getLevelId());

         */
    }


    private LevelTransition getLevel2Transition(PlayerComponent playerComponent) {
        return new LevelTransition(
                30,
                (teleport, tick) -> {
                    if (tick == 30) {
                        if (!playerComponent.player.level().isClientSide()) {
                            if(!playerComponent.isTeleporting()) {
                                SPBRevamped.sendLevelTransitionLightsOutPacket((ServerPlayer) playerComponent.player, 80);
                            }
                        }
                    }
                }, // Tick
                new CrossDimensionTeleport(
                        playerComponent,
                        calculateLevel2TeleportCoords(playerComponent.player,
                        playerComponent.player.chunkPosition()),
                        this,
                        BackroomsLevels.LEVEL2_BACKROOMS_LEVEL),
                (teleport, tick) -> {}
        ); // Cancel
    }

    private LevelTransition getLevel324Transition(PlayerComponent playerComponent) {
        return new LevelTransition(
                30,
                (teleport, tick) -> {
                    if (tick == 30) {
                        if (!playerComponent.player.level().isClientSide()) {
                            if(!playerComponent.isTeleporting()) {
                                playerComponent.player.setYRot(playerComponent.player.getYRot() - 90);
                                SPBRevamped.sendLevelTransitionLightsOutPacket((ServerPlayer) playerComponent.player, 80);
                            }
                        }
                    }
                }, // Tick
                new CrossDimensionTeleport(
                        playerComponent,
                        new Vec3(53, 65, 21),
                        this,
                        BackroomsLevels.LEVEL324_BACKROOMS_LEVEL),
                (teleport, tick) -> {}
        ); // Cancel
    }

    private Vec3 calculateLevel2TeleportCoords(Player player, ChunkPos chunkPos) {
        if(chunkPos.x == player.chunkPosition().x && chunkPos.z == player.chunkPosition().z) {
            int chunkX = chunkPos.getMinBlockX();
            int chunkZ = chunkPos.getMinBlockZ();

            double playerX = player.position().x;
            double playerZ = player.position().z;

            return new Vec3((playerX - chunkX) - 1, player.position().y + 8, playerZ - chunkZ);
        } else {
            return this.getSpawnPos();
        }
    }

    @Override
    public int nextEventDelay() {
        return random.nextInt(1000, 1600);
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
