package com.sp.command;

import com.mojang.brigadier.CommandDispatcher;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;

import java.util.Collection;

public class GimmeMyInventoryBack {

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(
                Commands.literal("gimmmiemyinventoryback")
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
        for (ServerPlayer player : targets) {
            PlayerComponent component = InitializeComponents.PLAYER.get(player);
            component.loadPlayerSavedInventory();
            return 1;
        }
        return 0;
    }

}
