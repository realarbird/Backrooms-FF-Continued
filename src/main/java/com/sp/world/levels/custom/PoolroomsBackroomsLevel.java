package com.sp.world.levels.custom;

import com.sp.SPBRevamped;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.init.BackroomsLevels;
import com.sp.world.events.poolrooms.PoolroomsAmbience;
import com.sp.world.events.poolrooms.PoolroomsSunset;
import com.sp.world.generation.chunk_generator.PoolroomsChunkGenerator;
import com.sp.world.levels.BackroomsLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.network.chat.Component;
import net.minecraft.ChatFormatting;
import net.minecraft.world.phys.Vec3;

import java.util.ArrayList;
import java.util.List;

public class PoolroomsBackroomsLevel extends BackroomsLevel {
    public float timeOfDay = 0;
    public boolean sunsetTransitioning = false;

    public PoolroomsBackroomsLevel() {
        super("poolrooms", PoolroomsChunkGenerator.CODEC, new RoomCount(1), new Vec3(16, 106, 16), BackroomsLevels.POOLROOMS_WORLD_KEY);
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

        this.registerEvent("sunset", PoolroomsSunset::new);
        this.registerEvent("abience", PoolroomsAmbience::new);

        this.registerTransition((world, playerComponent, from) -> {
            List<LevelTransition> playerList = new ArrayList<>();

            if (from instanceof PoolroomsBackroomsLevel && playerComponent.player.level().getMaxLocalRawBrightness(playerComponent.player.blockPosition()) == 0 && playerComponent.player.position().y < 60 && playerComponent.player.position().y > 52) {
                playerList.add(getInfiniteFieldTransition(playerComponent));
            }

            return playerList;

        }, this.getLevelId() + "->" + BackroomsLevels.INFINITE_FIELD_BACKROOMS_LEVEL.getLevelId());
    }

    private LevelTransition getInfiniteFieldTransition(PlayerComponent playerComponent) {
        return new LevelTransition(
                1,
                (teleport, tick) -> {
                    if (!teleport.playerComponent().player.level().isClientSide()) {
                        SPBRevamped.sendBlackScreenPacket((ServerPlayer) teleport.playerComponent().player, 60, true, false);
                    }
                },
                new CrossDimensionTeleport(
                        playerComponent,
                        BackroomsLevels.INFINITE_FIELD_BACKROOMS_LEVEL.getSpawnPos(),
                        this,
                        BackroomsLevels.INFINITE_FIELD_BACKROOMS_LEVEL
                ),
                (teleport, tick) -> {});
    }

    @Override
    public int nextEventDelay() {
        return random.nextInt(800, 1000);
    }

    public boolean isNoon() {
        return timeOfDay != 0.25 && timeOfDay != 0.75;
    }

    public float getTimeOfDay() {
        return timeOfDay;
    }

    @Override
    public BoolTextPair allowsTorch() {
        return new BoolTextPair(false, Component.translatable("spb-revamped.flashlight.wet1").append(Component.translatable("spb-revamped.flashlight.wet2").withStyle(ChatFormatting.RED)));
    }

    @Override
    public boolean hasVanillaLighting() {
        return true;
    }

    public void setTimeOfDay(float timeOfDay) {
        this.justChanged();
        this.timeOfDay = timeOfDay;
    }

    public boolean isSunsetTransitioning() {
        return sunsetTransitioning;
    }

    public void setSunsetTransitioning(boolean sunsetTransitioning) {
        this.justChanged();
        this.sunsetTransitioning = sunsetTransitioning;
    }

    @Override
    public void writeToNbt(CompoundTag nbt) {
        nbt.putFloat("timeOfDay", timeOfDay);
        nbt.putBoolean("sunsetTransitioning", sunsetTransitioning);
    }

    @Override
    public void readFromNbt(CompoundTag nbt) {
        this.timeOfDay = nbt.getFloatOr("timeOfDay", 0f);
        this.sunsetTransitioning = nbt.getBooleanOr("sunsetTransitioning", false);
    }

    @Override
    public void transitionOut(CrossDimensionTeleport crossDimensionTeleport) {
        crossDimensionTeleport.playerComponent().player.fallDistance = 0;
    }

    @Override
    public void transitionIn(CrossDimensionTeleport crossDimensionTeleport) {

    }
}
