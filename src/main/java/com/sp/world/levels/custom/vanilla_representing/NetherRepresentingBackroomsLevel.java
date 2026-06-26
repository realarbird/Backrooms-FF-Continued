package com.sp.world.levels.custom.vanilla_representing;

import com.sp.world.levels.WorldRepresentingBackroomsLevel;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.level.Level;

public class NetherRepresentingBackroomsLevel extends WorldRepresentingBackroomsLevel {
    public NetherRepresentingBackroomsLevel() {
        super("nether", new Vec3(0,0,0), Level.NETHER);
    }

    @Override
    public int nextEventDelay() {
        return 0;
    }

    @Override
    public void writeToNbt(CompoundTag nbt) {

    }

    @Override
    public void readFromNbt(CompoundTag nbt) {

    }

    @Override
    public void transitionOut(CrossDimensionTeleport crossDimensionTeleport) {

    }

    @Override
    public void transitionIn(CrossDimensionTeleport crossDimensionTeleport) {
        crossDimensionTeleport.playerComponent().loadPlayerSavedInventory();
    }
}
