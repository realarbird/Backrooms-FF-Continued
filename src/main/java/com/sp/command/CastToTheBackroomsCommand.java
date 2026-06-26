package com.sp.command;

import com.mojang.brigadier.CommandDispatcher;
import com.sp.SPBRevamped;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.init.ModSounds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class CastToTheBackroomsCommand {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(
                Commands.literal("casttothebackrooms")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> execute(
                                                EntityArgument.getPlayers(context, "targets")
                                        )
                                )
                        )
        );
    }

    private static int execute(Collection<ServerPlayer> targets) {
        if (!targets.isEmpty()) {
            for (ServerPlayer serverPlayer : targets) {
                PlayerComponent component = InitializeComponents.PLAYER.get(serverPlayer);
                ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();
                ScheduledExecutorService executorService2 = Executors.newSingleThreadScheduledExecutor();

                //First set them to noclip and play the sound
                executorService.schedule(() -> {
                    SPBRevamped.sendCameraShakePacket(serverPlayer, 1.5, 2.5);
                    component.setShouldNoClip(true);
                    component.sync();
                    SPBRevamped.sendPersonalPlaySoundPacket(serverPlayer, ModSounds.NO_ESCAPE, 1.0f, 1.0f);
                    executorService.shutdown();
                }, 10000, TimeUnit.MILLISECONDS);

                //Half a second later tp to backrooms and stop the sound
                executorService2.schedule(() -> {
                    component.suffocationTimer = 40;
                    executorService2.shutdown();
                }, 10300, TimeUnit.MILLISECONDS);
            }
            return 1;
        } else {
            return -1;
        }

    }

}
