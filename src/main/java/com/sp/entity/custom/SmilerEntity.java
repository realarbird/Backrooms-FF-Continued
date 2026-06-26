package com.sp.entity.custom;

import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.cca_stuff.SmilerComponent;
import com.sp.init.BackroomsLevels;
import com.sp.world.levels.BackroomsLevelWithLights;
import com.sp.world.levels.custom.Level1BackroomsLevel;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.util.RandomSource;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.goal.target.NearestAttackableTargetGoal;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.Vec3;

import java.util.List;

public class SmilerEntity extends Mob {
    private final SmilerComponent component;
    private int finalTicks;
    private float liveTime;

    public SmilerEntity(EntityType<? extends Mob> entityType, Level world) {
        super(entityType, world);
        this.component = InitializeComponents.SMILER.get(this);

        if(!world.isClientSide()){
            RandomSource random = RandomSource.create();
            this.component.setRandomTexture(random.nextIntBetweenInclusive(1,3));
            this.component.sync();
        }
        this.finalTicks = 20;
        this.liveTime = 100;
    }

    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(1, new NearestAttackableTargetGoal<>(this, Player.class, 0, true, false, null));
    }

    @Override
    public void tick() {
        if(!this.level().isClientSide()) {
            if(!this.component.shouldDisappear()) {
                if (this.level().getNearestPlayer(this, 15) != null) {
                    ServerLevel serverLevel = (ServerLevel) this.level();
                    TargetingConditions conditions = TargetingConditions.forNonCombat().range(15);
                    List<Player> playerList = serverLevel.getEntitiesOfClass(Player.class, this.getBoundingBox().inflate(15, 1, 15),
                            player -> conditions.test(serverLevel, this, player));

                    for (Player player : playerList) {
                        if (this.shouldDisappear(player)) {
                            this.component.setShouldDisappear(true);
                            this.component.sync();
                            break;
                        }
                    }
                } else {
                    this.component.setShouldDisappear(true);
                    this.component.sync();
                }

                if (this.liveTime > 0) {
                    this.liveTime--;
                } else {
                    this.component.setShouldDisappear(true);
                    this.component.sync();
                }
            }



            if(this.component.shouldDisappear()) {
                this.finalTicks--;
                if(this.finalTicks <= 0){
                    this.discard();
                }
            }

            if (((BackroomsLevels.getLevel(this.level()).orElse(BackroomsLevels.OVERWORLD_REPRESENTING_BACKROOMS_LEVEL)) instanceof Level1BackroomsLevel level)) {
                if (level.getLightState() != BackroomsLevelWithLights.LightState.BLACKOUT) {
                    this.discard();
                }
            }
        }


        super.tick();
    }

    private boolean shouldDisappear(Player player){
        PlayerComponent playerComponent = InitializeComponents.PLAYER.get(player);
        return playerComponent.isFlashLightOn() &&
                this.isPlayerStaring(player);
    }

    //From Enderman. Don't need anything too fancy
    private boolean isPlayerStaring(Player player) {
        Vec3 vec3d = player.getViewVector(1.0F).normalize();
        Vec3 vec3d2 = new Vec3(this.getX() - player.getX(), this.getEyeY() - player.getEyeY(), this.getZ() - player.getZ());
        double d = vec3d2.length();
        vec3d2 = vec3d2.normalize();
        double e = vec3d.dot(vec3d2);
        return e > 1.0 - 0.35 / d && player.hasLineOfSight(this);
    }

    public static AttributeSupplier.Builder createSmilerAttributes(){
        return Mob.createMobAttributes()
                .add(Attributes.MAX_HEALTH, 1000)
                .add(Attributes.MOVEMENT_SPEED, 0)
                .add(Attributes.KNOCKBACK_RESISTANCE, 1000);
    }


}
