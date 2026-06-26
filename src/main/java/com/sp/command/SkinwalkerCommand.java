package com.sp.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.sp.SPBRevamped;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.cca_stuff.WorldEvents;
import com.sp.entity.custom.SkinWalkerEntity;
import com.sp.init.ModEntities;
import com.sp.init.ModSounds;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.commands.arguments.EntityArgument;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.GameType;

import java.util.Collection;

public class SkinwalkerCommand {
    private static final SimpleCommandExceptionType TOO_MANY_TARGETS = new SimpleCommandExceptionType(new LiteralMessage("Can only apply to 1 target"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(
                Commands.literal("skinwalker")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> execute(
                                                context.getSource(),
                                                EntityArgument.getPlayers(context, "targets")
                                        )
                                )
                        )
        );

        dispatcher.register(
                Commands.literal("release")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.argument("targets", EntityArgument.players())
                                .executes(context -> release(
                                                context.getSource(),
                                                EntityArgument.getPlayers(context, "targets")
                                        )
                                )
                        )
        );
    }

    private static int execute(CommandSourceStack source, Collection<ServerPlayer> targets) throws CommandSyntaxException {
        if (targets.size() > 1) {
            throw TOO_MANY_TARGETS.create();
        }

        for (ServerPlayer target : targets) {
            WorldEvents events = InitializeComponents.EVENTS.get(source.getLevel());
            events.setActiveSkinwalkerTarget(target.getUUID());
            SkinWalkerEntity skinWalkerEntity = ModEntities.SKIN_WALKER_ENTITY.create(source.getLevel(), EntitySpawnReason.EVENT);

            if (skinWalkerEntity != null) {
                PlayerComponent targetComponent = InitializeComponents.PLAYER.get(target);
                skinWalkerEntity.snapTo(target.getX(), target.getY(), target.getZ(), target.getYRot(), target.getXRot());
                skinWalkerEntity.setDeltaMovement(target.getDeltaMovement());
                source.getLevel().addFreshEntity(skinWalkerEntity);
                events.activeSkinWalkerEntity = skinWalkerEntity;

                targetComponent.setPrevGameMode(target.gameMode());
                targetComponent.setBeingCaptured(true);
                targetComponent.setHasBeenCaptured(true);
                targetComponent.setShouldBeMuted(true);
                targetComponent.sync();

                target.setGameMode(GameType.SPECTATOR);
                target.setCamera(skinWalkerEntity);
            }
        }
        return 1;
    }

    private static int release(CommandSourceStack source, Collection<ServerPlayer> targets) throws CommandSyntaxException {
        if (targets.size() > 1) {
            throw TOO_MANY_TARGETS.create();
        }

        for (ServerPlayer target : targets) {
            PlayerComponent targetComponent = InitializeComponents.PLAYER.get(target);
            WorldEvents events = InitializeComponents.EVENTS.get(source.getLevel());

            targetComponent.setHasBeenCaptured(false);
            targetComponent.setShouldBeMuted(false);
            targetComponent.sync();

            target.setGameMode(targetComponent.getPrevGameMode() != null ? targetComponent.getPrevGameMode() : GameType.SURVIVAL);
            target.setCamera(target);
            events.activeSkinWalkerEntity.discard();
            events.activeSkinWalkerEntity = null;

            SPBRevamped.sendPersonalPlaySoundPacket(target, ModSounds.SKINWALKER_RELEASE, 1.0f, 1.0f);
        }
        return 1;
    }

}
