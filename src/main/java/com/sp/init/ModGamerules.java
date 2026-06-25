package com.sp.init;

import com.sp.SPBRevamped;
import net.fabricmc.fabric.api.gamerule.v1.GameRuleBuilder;
import net.minecraft.resources.Identifier;
import net.minecraft.world.level.gamerules.GameRule;
import net.minecraft.world.level.gamerules.GameRuleCategory;

public class ModGamerules {
    public static final GameRule<Boolean> STUCK_IN_BACKROOMS =
        GameRuleBuilder.forBoolean(false)
            .category(GameRuleCategory.PLAYER)
            .buildAndRegister(Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "stuck_in_the_backrooms"));

    public static void registerGamerules() {

    }
}
