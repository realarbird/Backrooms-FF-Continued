package com.sp.command;

import com.mojang.brigadier.CommandDispatcher;
import com.mojang.brigadier.LiteralMessage;
import com.mojang.brigadier.exceptions.CommandSyntaxException;
import com.mojang.brigadier.exceptions.SimpleCommandExceptionType;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.WorldEvents;
import com.sp.init.BackroomsLevels;
import com.sp.world.events.AbstractEvent;
import com.sp.world.events.infinite_grass.InfiniteGrassAmbience;
import com.sp.world.events.generic.lights.LightLevelBlackout;
import com.sp.world.events.generic.lights.LightLevelFlicker;
import com.sp.world.events.level0.Level0IntercomBasic;
import com.sp.world.events.level0.Level0Music;
import com.sp.world.events.level1.Level1Ambience;
import com.sp.world.events.level1.Level1Blackout;
import com.sp.world.events.level2.Level2Warp;
import com.sp.world.events.poolrooms.PoolroomsAmbience;
import com.sp.world.events.poolrooms.PoolroomsSunset;
import net.minecraft.commands.CommandBuildContext;
import net.minecraft.commands.CommandSourceStack;
import net.minecraft.commands.Commands;
import net.minecraft.resources.ResourceKey;
import net.minecraft.world.level.Level;

public class EventCommand {
    private static final SimpleCommandExceptionType FLICKER_BLACKOUT_EXCEPTION = new SimpleCommandExceptionType(new LiteralMessage("Event only occurs in Level 0 and Level 1"));
    private static final SimpleCommandExceptionType ONLY_LEVEL0_EXCEPTION = new SimpleCommandExceptionType(new LiteralMessage("Event only occurs in Level 0"));
    private static final SimpleCommandExceptionType AMBIENCE_EXCEPTION = new SimpleCommandExceptionType(new LiteralMessage("Event only occurs in Level 1, Level 2, The Poolrooms, or the Infinite Field"));
    private static final SimpleCommandExceptionType WARP_EXCEPTION = new SimpleCommandExceptionType(new LiteralMessage("Event only occurs in Level 2"));
    private static final SimpleCommandExceptionType SUNSET_EXCEPTION = new SimpleCommandExceptionType(new LiteralMessage("Event only occurs in The Poolrooms"));

    public static void register(CommandDispatcher<CommandSourceStack> dispatcher, CommandBuildContext registryAccess, Commands.CommandSelection environment) {
        dispatcher.register(
                Commands.literal("backroomsevent")
                        .requires(Commands.hasPermission(Commands.LEVEL_GAMEMASTERS))
                        .then(Commands.literal("flicker")
                                .executes(context -> doFlicker(
                                                context.getSource()
                                        )
                                )
                        )
                        .then(Commands.literal("blackout")
                                .executes(context -> doBlackout(
                                                context.getSource()
                                        )
                                )
                        )
                        .then(Commands.literal("intercom")
                                .executes(context -> doIntercom(
                                                context.getSource()
                                        )
                                )
                        )
                        .then(Commands.literal("music")
                                .executes(context -> doMusic(
                                                context.getSource()
                                        )
                                )
                        )
                        .then(Commands.literal("ambience")
                                .executes(context -> doAmbience(
                                                context.getSource()
                                        )
                                )
                        )
                        .then(Commands.literal("warp")
                                .executes(context -> doWarp(
                                                context.getSource()
                                        )
                                )
                        )
                        .then(Commands.literal("sunset")
                                .executes(context -> doSunset(
                                                context.getSource()
                                        )
                                )
                        )
        );
    }

    private static int doFlicker(CommandSourceStack source) throws CommandSyntaxException {
        Level world = source.getLevel();
        ResourceKey<Level> registryKey = world.dimension();
        WorldEvents events = InitializeComponents.EVENTS.get(world);

        if (registryKey == BackroomsLevels.LEVEL0_WORLD_KEY) {
            LightLevelFlicker flicker = new LightLevelFlicker();
            setEvent(events, world, flicker);
            return 1;
        } else if (registryKey == BackroomsLevels.LEVEL1_WORLD_KEY) {
            LightLevelFlicker flicker = new LightLevelFlicker();
            setEvent(events, world, flicker);


            return 1;
        } else if (registryKey == BackroomsLevels.LEVEL324_WORLD_KEY) {
            LightLevelFlicker flicker = new LightLevelFlicker();
            setEvent(events, world, flicker);


            return 1;
        }

        throw FLICKER_BLACKOUT_EXCEPTION.create();
    }

    private static int doBlackout(CommandSourceStack source) throws CommandSyntaxException {
        Level world = source.getLevel();
        ResourceKey<Level> registryKey = world.dimension();
        WorldEvents events = InitializeComponents.EVENTS.get(world);

        if (registryKey == BackroomsLevels.LEVEL0_WORLD_KEY) {
            LightLevelBlackout blackout = new LightLevelBlackout();
            setEvent(events, world, blackout);

            return 1;
        } else if (registryKey == BackroomsLevels.LEVEL1_WORLD_KEY) {
            Level1Blackout blackout = new Level1Blackout();
            setEvent(events, world, blackout);

            return 1;
        }

        throw FLICKER_BLACKOUT_EXCEPTION.create();
    }

    private static int doIntercom(CommandSourceStack source) throws CommandSyntaxException {
        Level world = source.getLevel();
        ResourceKey<Level> registryKey = world.dimension();
        WorldEvents events = InitializeComponents.EVENTS.get(world);

        if (registryKey == BackroomsLevels.LEVEL0_WORLD_KEY) {
            Level0IntercomBasic intercom = new Level0IntercomBasic();
            setEvent(events, world, intercom);
            return 1;
        }

        throw ONLY_LEVEL0_EXCEPTION.create();
    }

    private static int doMusic(CommandSourceStack source) throws CommandSyntaxException {
        Level world = source.getLevel();
        ResourceKey<Level> registryKey = world.dimension();
        WorldEvents events = InitializeComponents.EVENTS.get(world);

        if (registryKey == BackroomsLevels.LEVEL0_WORLD_KEY) {
            Level0Music music = new Level0Music();
            setEvent(events, world, music);
            return 1;
        }

        throw ONLY_LEVEL0_EXCEPTION.create();
    }

    private static int doAmbience(CommandSourceStack source) throws CommandSyntaxException {
        Level world = source.getLevel();
        ResourceKey<Level> registryKey = world.dimension();
        WorldEvents events = InitializeComponents.EVENTS.get(world);

        if (registryKey == BackroomsLevels.POOLROOMS_WORLD_KEY) {
            PoolroomsAmbience ambience = new PoolroomsAmbience();
            setEvent(events, world, ambience);
            return 1;
        } else if (registryKey == BackroomsLevels.LEVEL1_WORLD_KEY) {
            Level1Ambience ambience = new Level1Ambience();
            setEvent(events, world, ambience);

            return 1;
        } else if (registryKey == BackroomsLevels.LEVEL2_WORLD_KEY) {
            Level1Ambience ambience = new Level1Ambience();
            setEvent(events, world, ambience);

            return 1;
        } else if (registryKey == BackroomsLevels.INFINITE_FIELD_WORLD_KEY) {
            InfiniteGrassAmbience ambience = new InfiniteGrassAmbience();
            setEvent(events, world, ambience);

            return 1;
        }

        throw AMBIENCE_EXCEPTION.create();
    }

    private static int doWarp(CommandSourceStack source) throws CommandSyntaxException {
        Level world = source.getLevel();
        ResourceKey<Level> registryKey = world.dimension();
        WorldEvents events = InitializeComponents.EVENTS.get(world);

        if (registryKey == BackroomsLevels.LEVEL2_WORLD_KEY) {
            Level2Warp warp = new Level2Warp();
            setEvent(events, world, warp);
            return 1;
        }

        throw WARP_EXCEPTION.create();
    }

    private static int doSunset(CommandSourceStack source) throws CommandSyntaxException {
        Level world = source.getLevel();
        ResourceKey<Level> registryKey = world.dimension();
        WorldEvents events = InitializeComponents.EVENTS.get(world);

        if (registryKey == BackroomsLevels.POOLROOMS_WORLD_KEY) {
            PoolroomsSunset sunset = new PoolroomsSunset();
            setEvent(events, world, sunset);
            return 1;
        }

        throw SUNSET_EXCEPTION.create();
    }

    private static void setEvent(WorldEvents events, Level world, AbstractEvent activeEvent) {
        if (events.getActiveEvent() != null) {
            events.getActiveEvent().finish(world);
        }
        events.setActiveEvent(activeEvent);
        activeEvent.init(world);
        events.ticks = 0;
    }

}
