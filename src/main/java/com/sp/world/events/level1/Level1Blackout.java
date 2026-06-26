package com.sp.world.events.level1;

import com.sp.entity.custom.SmilerEntity;
import com.sp.init.BackroomsLevels;
import com.sp.init.ModEntities;
import com.sp.init.ModSounds;
import com.sp.world.events.AbstractEvent;
import com.sp.world.levels.BackroomsLevelWithLights;
import com.sp.world.levels.custom.Level1BackroomsLevel;
import net.minecraft.world.entity.player.Player;
import net.minecraft.core.BlockPos;
import net.minecraft.world.phys.Vec3;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntitySpawnReason;
import net.minecraft.world.level.Level;

import java.util.List;

public class Level1Blackout extends AbstractEvent {
    private int smilerSpawnDelay = 80;

    @Override
    public void init(Level world) {
        if (!((BackroomsLevels.getLevel(world).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL)) instanceof Level1BackroomsLevel level)) {
            return;
        }

        if(level.getLightState() != BackroomsLevelWithLights.LightState.BLACKOUT) {
            level.setLightState(BackroomsLevelWithLights.LightState.BLACKOUT);
            playSound(world, ModSounds.LIGHTS_OUT);
        }
    }

    @Override
    public void ticks(int ticks, Level world) {
        if (world.dimension() != BackroomsLevels.LEVEL1_WORLD_KEY) {
            return;
        }

        RandomSource random = RandomSource.create();

        List<? extends Player> playerList = world.players();

        this.smilerSpawnDelay--;

        if (this.smilerSpawnDelay >= 0) {
            return;
        }
        for (Player player : playerList) {
            int rand = player.getRandom().nextIntBetweenInclusive(1, 10);

            if (rand != 1) {
                continue;
            }

            SmilerEntity smiler = ModEntities.SMILER_ENTITY.create(world, EntitySpawnReason.EVENT);

            if (smiler == null) {
                continue;
            }

            BlockPos.MutableBlockPos mutable = new BlockPos.MutableBlockPos();
            float randomAngle = random.nextFloat() * 360.0f;
            Vec3 spawnPos = new Vec3(0, 0, 15).yRot(randomAngle).add(player.position());
            if (!world.getBlockState(mutable.set(spawnPos.x, spawnPos.y, spawnPos.z)).blocksMotion()) {
                smiler.snapTo(Math.floor(spawnPos.x) + 0.5f, spawnPos.y, Math.floor(spawnPos.z) + 0.5f, 0.0f, 0.0f);
                world.addFreshEntity(smiler);
                smilerSpawnDelay = 80;
            }
        }
    }

    @Override
    public void finish(Level world) {
        super.finish(world);

        if (!((BackroomsLevels.getLevel(world).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL)) instanceof Level1BackroomsLevel level)) {
            return;
        }

        level.setLightState(BackroomsLevelWithLights.LightState.ON);
        playSound(world, ModSounds.LIGHTS_ON);
    }


    @Override
    public int duration() {
        return 600;
    }
}
