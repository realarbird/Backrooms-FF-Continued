package com.sp.world.events;

import net.minecraft.world.entity.player.Player;
import net.minecraft.network.protocol.game.ClientboundSoundPacket;
import net.minecraft.core.Holder;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.sounds.SoundSource;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.core.BlockPos;
import net.minecraft.util.RandomSource;
import net.minecraft.world.level.Level;

public class EventSounds {

    public static void playSound(Level world, SoundEvent soundEvent){
        for(Player player : world.players()){
            BlockPos playerPos = player.blockPosition();
            ((ServerPlayer) player).connection.send(
                    new ClientboundSoundPacket(
                            Holder.direct(soundEvent),
                            SoundSource.AMBIENT,
                            playerPos.getX(),
                            playerPos.getY(),
                            playerPos.getZ(),
                            100.0f,
                            1.0f,
                            player.getRandom().nextLong()
                    )
            );
        }
    }

    public static void playSoundWithRandLocation(Level world, SoundEvent soundEvent, int yLevel, int range){
        for(Player player : world.players()){
            if(player instanceof ServerPlayer) {
                for (int i = 0; i < 5; i++) {
                    RandomSource random = RandomSource.create();
                    BlockPos playerPos = player.blockPosition();

                    int randXOffset = random.nextIntBetweenInclusive(-range, range);
                    int randZOffset = random.nextIntBetweenInclusive(-range, range);

                    ((ServerPlayer) player).connection.send(
                            new ClientboundSoundPacket(
                                    Holder.direct(soundEvent),
                                    SoundSource.AMBIENT,
                                    playerPos.getX() + randXOffset,
                                    yLevel,
                                    playerPos.getZ() + randZOffset,
                                    100.0f,
                                    1.0f,
                                    player.getRandom().nextLong()
                            )
                    );
                }
            }
        }
    }

    public static void playDistantSound(Level world, SoundEvent soundEvent){
        RandomSource random = RandomSource.create();

        int randXOffset = random.nextIntBetweenInclusive(-300, 300);
        int randZOffset = random.nextIntBetweenInclusive(-300, 300);

        for(Player player : world.players()){
            BlockPos playerPos = player.blockPosition();
            ((ServerPlayer) player).connection.send(
                    new ClientboundSoundPacket(
                            Holder.direct(soundEvent),
                            SoundSource.AMBIENT,
                            playerPos.getX() + randXOffset,
                            playerPos.getY(),
                            playerPos.getZ() + randZOffset,
                            1000.0f,
                            1.0f,
                            player.getRandom().nextLong()
                    )
            );
        }
    }

    public static void playLevel2Sound(Level world, SoundEvent soundEvent) {
        RandomSource random = RandomSource.create();
        int rand = random.nextIntBetweenInclusive(1, 2);

        for(Player player : world.players()){
            BlockPos playerPos = player.blockPosition();

            if(rand == 1) {
                ((ServerPlayer) player).connection.send(
                        new ClientboundSoundPacket(
                                Holder.direct(soundEvent),
                                SoundSource.AMBIENT,
                                playerPos.getX(),
                                playerPos.getY(),
                                playerPos.getZ() + 200,
                                1000.0f,
                                1.0f,
                                player.getRandom().nextLong()
                        )
                );
            } else {
                ((ServerPlayer) player).connection.send(
                        new ClientboundSoundPacket(
                                Holder.direct(soundEvent),
                                SoundSource.AMBIENT,
                                playerPos.getX(),
                                playerPos.getY(),
                                playerPos.getZ() - 200,
                                1000.0f,
                                1.0f,
                                player.getRandom().nextLong()
                        )
                );
            }
        }
    }

}
