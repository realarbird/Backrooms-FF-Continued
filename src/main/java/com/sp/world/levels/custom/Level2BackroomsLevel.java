package com.sp.world.levels.custom;

import com.sp.SPBRevamped;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.init.BackroomsLevels;
import com.sp.world.events.level2.Level2Ambience;
import com.sp.world.events.level2.Level2Warp;
import com.sp.world.generation.chunk_generator.Level2ChunkGenerator;
import com.sp.world.levels.BackroomsLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

import java.util.ArrayList;
import java.util.List;

public class Level2BackroomsLevel extends BackroomsLevel {
    private boolean isWarping = false;

    public Level2BackroomsLevel() {
        super("level2", Level2ChunkGenerator.CODEC, new Vec3(0.5, 20, 8), BackroomsLevels.LEVEL2_WORLD_KEY);

        this.registerEvent("warp", Level2Warp::new);
        this.registerEvent("abience", Level2Ambience::new);

        this.registerTransition((world, playerComponent, from) -> {
            List<LevelTransition> playerList = new ArrayList<>();

            int exitRadius = SPBRevamped.getExitSpawnRadius(world);

            if (from instanceof Level2BackroomsLevel && Math.abs(playerComponent.player.position().z()) >= exitRadius) {
                playerList.add(getPoolRoomsTransition(playerComponent));
            }

            return playerList;
        }, "level2 -> poolrooms");
    }

    private LevelTransition getPoolRoomsTransition(PlayerComponent playerComponent) {
        return new LevelTransition(
                110,
                (teleport, tick) -> {
                    Level world = teleport.playerComponent().player.level();

                    if (world.isClientSide()) {
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
    public int nextEventDelay() {
        return random.nextInt(500, 800);
    }

    public boolean isWarping() {
        return isWarping;
    }

    public void setWarping(boolean warping) {
        isWarping = warping;
    }

    @Override
    public void writeToNbt(CompoundTag nbt) {
        nbt.putBoolean("isWarping", isWarping);
    }

    @Override
    public void readFromNbt(CompoundTag nbt) {
        this.isWarping = nbt.getBooleanOr("isWarping", false);
    }

    @Override
    public void transitionOut(CrossDimensionTeleport crossDimensionTeleport) {

    }

    @Override
    public void transitionIn(CrossDimensionTeleport crossDimensionTeleport) {

    }
}
