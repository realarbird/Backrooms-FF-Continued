package com.sp.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.arguments.StringArgumentType;
import com.sp.init.BackroomsLevels;
import com.sp.world.levels.BackroomsLevel;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.portal.TeleportTransition;
import net.minecraft.world.phys.Vec3;

import java.util.Optional;

public class LevelCommand {
    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(
                Commands.literal("level")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("level", StringArgumentType.word()).suggests(
                                (context, builder) -> {
                                    for (BackroomsLevel backroomsLevel : BackroomsLevels.BACKROOMS_LEVELS) {
                                        builder.suggest(backroomsLevel.getLevelId());
                                    }
                                    return builder.buildFuture();
                                }
                        ).executes((context -> {
                            String levelId = StringArgumentType.getString(context, "level");
                            Optional<BackroomsLevel> optionalBackroomsLevel = BackroomsLevels.getById(levelId);

                            if (optionalBackroomsLevel.isPresent()) {
                                BackroomsLevel backroomsLevel = optionalBackroomsLevel.get();

                                Entity entity = context.getSource().getEntityOrException();

                                if (entity instanceof Player player) {
                                    ServerLevel destination = context.getSource().getLevel().getServer().getLevel(backroomsLevel.getWorldKey());
                                    player.teleport(new TeleportTransition(destination, backroomsLevel.getSpawnPos(), Vec3.ZERO, 0, 90, TeleportTransition.DO_NOTHING));
                                }


                            }

                            return 1;
                        })))
        );
    }
}
