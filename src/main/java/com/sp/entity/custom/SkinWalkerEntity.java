package com.sp.entity.custom;

import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.cca_stuff.SkinWalkerComponent;
import com.sp.cca_stuff.WorldEvents;
import com.sp.clientWrapper.ClientWrapper;
import com.sp.entity.ai.SlightlyBetterMobNavigation;
import com.sp.entity.ai.goals.*;
import com.sp.entity.ik.components.IKAnimatable;
import com.sp.entity.ik.components.IKModelComponent;
import com.sp.init.BackroomsLevels;
import com.sp.init.ModSounds;
import com.sp.sounds.entity.SkinWalkerChaseSoundInstance;
import com.sp.util.Easings;
import com.sp.world.levels.BackroomsLevelWithLights;
import com.sp.world.levels.custom.Level0BackroomsLevel;
import com.geckolib.animatable.GeoEntity;
import com.geckolib.animatable.GeoAnimatable;
import com.geckolib.animatable.instance.AnimatableInstanceCache;
import com.geckolib.animatable.manager.AnimatableManager;
import com.geckolib.animation.AnimationController;
import com.geckolib.animation.RawAnimation;
import com.geckolib.animation.object.LoopType;
import com.geckolib.animation.object.PlayState;
import com.geckolib.util.GeckoLibUtil;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.sounds.SoundEvent;
import net.minecraft.sounds.SoundSource;
import net.minecraft.util.Mth;
import net.minecraft.util.RandomSource;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.EntityType;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.attributes.AttributeSupplier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.ai.control.LookControl;
import net.minecraft.world.entity.ai.targeting.TargetingConditions;
import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.UUID;
import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

public class SkinWalkerEntity extends Monster implements GeoEntity, GeoAnimatable, IKAnimatable<SkinWalkerEntity> {
    private final AnimatableInstanceCache cache = GeckoLibUtil.createInstanceCache(this);
    public static final RawAnimation TRANSITION = RawAnimation.begin().then("transition", LoopType.PLAY_ONCE);
    public SkinWalkerComponent component;
    public List<IKModelComponent<SkinWalkerEntity>> components = new ArrayList<>();
    private final int maxSuspicion;
    private Entity prevTarget;
    private int ticks;
    private int trueFormTime;

    public SkinWalkerChaseSoundInstance chaseSoundInstance;

    public SkinWalkerEntity(EntityType<? extends Monster> entityType, Level world) {
        super(entityType, world);
        this.navigation = new SlightlyBetterMobNavigation(this, world);
        this.lookControl = new SkinWalkerLookControl(this);
        this.component = InitializeComponents.SKIN_WALKER.get(this);
        this.component.setTargetPlayerUUID(this.getTargetPlayer(world));
        this.component.setSneaking(false);
        this.maxSuspicion = 1800 + (900 * (world.players().size() - 1));

        this.setUpLimbs();
    }

    protected void setUpLimbs() {
        this.addComponent(component.getIKComponent());
    }

    private UUID getTargetPlayer(Level world) {
        WorldEvents events = InitializeComponents.EVENTS.get(world);
        if(events.getActiveSkinwalkerTarget() != null){
            return events.getActiveSkinwalkerTarget().getUUID();
        }

        List<? extends Player> players = world.players();
        int rand;
        if(players.size() <= 1){
            rand = 0;
        } else {
            rand = RandomSource.create().nextIntBetweenInclusive(0, players.size() - 1);
        }

        if (players.isEmpty()) {
            return null;
        }

        return players.get(rand).getUUID();
    }

    public static AttributeSupplier.Builder createSkinWalkerAttributes() {
        return Monster.createMonsterAttributes()
                .add(Attributes.MAX_HEALTH, 10000F)
                .add(Attributes.FOLLOW_RANGE, 1000.0F)
                .add(Attributes.MOVEMENT_SPEED, 0.32f)
                .add(Attributes.ATTACK_DAMAGE, 12.0f);
    }


    @Override
    protected void registerGoals() {
        this.targetSelector.addGoal(2, new SkinWalkerActiveTarget(this));
        this.targetSelector.addGoal(1, new FinalFormActiveTargetGoal(this));

        this.goalSelector.addGoal(5, new FinalFormWanderGoal(this, 1.0));
        this.goalSelector.addGoal(4, new SpeakGoal(this));
        this.goalSelector.addGoal(3, new FollowClosestPlayerGoal(this, 5, 15, 1.0f));
        this.goalSelector.addGoal(3, new ActNaturalGoal(this));
        this.goalSelector.addGoal(2, new FinalFormIdleGoal(this, 60, 60));
        this.goalSelector.addGoal(1, new FinalFormAttackGoal(this));
    }

    @Override
    public boolean killedEntity(ServerLevel world, LivingEntity other, DamageSource damageSource) {
        this.setTarget(null);
        this.getNavigation().stop();

        this.component.setShouldBeginRelease(true);
        return super.killedEntity(world, other, damageSource);
    }

    @Override
    public void tick() {
        if (this.component.getTargetPlayerUUID() == null) {
            this.component.setTargetPlayerUUID(this.getTargetPlayer(this.level()));
        }

        this.setInvulnerable(this.component.isInTrueForm());

        if (this.level().isClientSide() && this.component.isInTrueForm()) {
            this.tickComponentsServer(this);
        }

        if (!this.level().isClientSide()) {
            if (this.getTarget() != null) {
                if (!this.component.isChasing()) {
                    this.component.setChasing(true);
                }
            } else {
                if(this.component.isChasing()) {
                    this.component.setChasing(false);
                }
            }

            if (!this.component.isInTrueForm() && !this.component.shouldBeginReveal()) {
                //3600
                if (this.tickCount >= 2400 || this.component.getSuspicion() > this.maxSuspicion) {
                    this.component.setBeginReveal(true);
                }

                this.updateLookAtSuspicion();

                if (this.getTarget() != null && this.component.shouldLookAtTarget()) {
                    ((SkinWalkerLookControl)this.getLookControl()).lookAt(this.getTarget(), 3);
                }
            }

            if(this.component.isInTrueForm() && !this.component.shouldBeginRelease()){
                this.trueFormTime++;
                if(this.trueFormTime >= 1200){
                    this.component.setShouldBeginRelease(true);
                }
            }

            if (this.component.shouldBeginReveal()) {
                this.tickReveal();
            } else {
                if (this.ticks > 100) {
                    this.ticks = 0;
                }
            }
        }

        super.tick();
    }


    @Override
    protected @Nullable SoundEvent getAmbientSound() {
        return super.getAmbientSound();
    }

    public void tickReveal() {
        if (this.prevTarget == null){
            if (this.getTarget() != null)
                this.prevTarget = this.getTarget();
        }

        this.setTarget(null);
        this.ticks++;
        this.getNavigation().stop();


        BackroomsLevels.getLevel(this.level()).ifPresent((backroomsLevel -> {
            if (this.ticks == 9) {
                this.level().playSound(null, this, ModSounds.SKINWALKER_BONE_CRACK, SoundSource.HOSTILE, 10.0f, 1.0f);
            }

            if (this.ticks == 39) {
                this.level().playSound(null, this, ModSounds.SKINWALKER_BONE_CRACK_LONG, SoundSource.HOSTILE, 10.0f, 1.0f);
            }

            if (this.ticks == 99) {
                this.level().playSound(null, this, ModSounds.SKINWALKER_REVEAL, SoundSource.HOSTILE, 100.0f, 1.0f);
            }

            if (this.ticks == 110) {
                if (backroomsLevel instanceof Level0BackroomsLevel level) {
                    level.setLightState(BackroomsLevelWithLights.LightState.FLICKER);
                }
            }

            if (this.ticks == 195) {
                if (backroomsLevel instanceof Level0BackroomsLevel level) {
                    level.setLightState(BackroomsLevelWithLights.LightState.OFF);
                }

                for (Player player : this.level().players()) {
                    PlayerComponent playerComponent = InitializeComponents.PLAYER.get(player);
                    playerComponent.setFlashLightOn(false);
                    playerComponent.sync();
                }
            }

            if (this.ticks >= 220) {
                if (backroomsLevel instanceof Level0BackroomsLevel level) {
                    level.setLightState(BackroomsLevelWithLights.LightState.ON);
                }
                this.component.setBeginReveal(false);
                this.component.setTrueForm(true);

                if (this.prevTarget != null) {
                    this.beginTargeting((Player) this.prevTarget);
                    this.prevTarget = null;
                }

                this.ticks = 0;
            }
        }));
    }

    private void updateLookAtSuspicion() {
        ServerLevel serverLevel = (ServerLevel) this.level();
        HashSet<Player> otherPlayers = new HashSet<>(this.level().players());
        TargetingConditions conditions = TargetingConditions.DEFAULT;
        List<Player> players = serverLevel.getEntitiesOfClass(Player.class,
                new AABB(this.position(), this.position().add(15, 15, 15)).move(-7.5, -7.5, -7.5),
                player -> conditions.test(serverLevel, this, player));
        players.forEach(otherPlayers::remove);

        for (Player player : players) {
            PlayerComponent playerComponent = InitializeComponents.PLAYER.get(player);
            Entity targetEntity = playerComponent.getTargetEntity();

            if(targetEntity != null) {
                if (targetEntity.equals(this)) {
                    if (playerComponent.getSkinWalkerLookDelay() <= 0) {
                        this.component.addSuspicion();
                    } else {
                        playerComponent.subtractSkinWalkerLookDelay();
                    }
                } else {
                    playerComponent.setSkinWalkerLookDelay(60);
                }
            } else {
                playerComponent.setSkinWalkerLookDelay(60);
            }
        }

        for(Player player : otherPlayers){
            PlayerComponent playerComponent = InitializeComponents.PLAYER.get(player);
            playerComponent.setSkinWalkerLookDelay(60);
        }
    }

    public void noticePlayer(Player player){
        component.setNoticing(true);
        ScheduledExecutorService executorService = Executors.newSingleThreadScheduledExecutor();

        this.playSound(ModSounds.SKINWALKER_NOTICE, 10.0f, 1.0f);
        this.getLookControl().setLookAt(player, 360, 360);

        executorService.schedule(() ->{
            this.level().broadcastEntityEvent(this, (byte) 123);
            this.setTarget(player);
            component.setNoticing(false);
            executorService.shutdown();
        }, 4300, TimeUnit.MILLISECONDS);
    }

    public void beginTargeting(Player player) {
        this.getLookControl().setLookAt(player, 360, 360);
        this.setTarget(player);
        this.level().broadcastEntityEvent(this, (byte) 123);
    }

    @Override
    public void handleEntityEvent(byte status) {
        if(status == (byte) 123 && this.level().isClientSide()){
            ClientWrapper.handleSkinWalkerEntityClientSide(this);
        }
        super.handleEntityEvent(status);
    }

    @Override
    public void onClientRemoval() {
        if (this.level().isClientSide()) {
            ClientWrapper.onRemoveSkinWalkerClientSide(this);
        }
    }

    @Override
    public boolean hurtServer(ServerLevel world, DamageSource source, float amount) {
        boolean bl = super.hurtServer(world, source, amount);
        if (this.level().isClientSide()) {
            return false;
        } else {
            if (bl && source.getEntity() instanceof Player) {
                this.component.addSuspicion(100);
            }

            return bl;
        }
    }

    // PORT: phase6 - vanilla head-turn hook moved into BodyRotationControl in 26.2; the 360-degree
    // freedom this method provided is now achieved via getMaxHeadYRot/getMaxHeadXRot returning 360.
    // protected float turnHead(float bodyRotation, float headRotation) {
    //     if (this.attackAnim > 0.0F) {
    //         bodyRotation = this.getYHeadRot();
    //     }
    //     float f = Mth.wrapDegrees(bodyRotation - this.yBodyRot);
    //     this.yBodyRot += f * 0.3F;
    //     float g = Mth.wrapDegrees(this.getYHeadRot() - this.yBodyRot);
    //     if (Math.abs(g) > 50.0F) {
    //         this.yBodyRot = this.yBodyRot + (g - (float)(Mth.sign(g) * 50));
    //     }
    //
    //     boolean bl = g < -90.0F || g >= 90.0F;
    //     if (bl) {
    //         headRotation *= -1.0F;
    //     }
    //
    //     return headRotation;
    // }

    @Override
    public int getMaxHeadYRot() {
        return 360;
    }

    @Override
    public int getMaxHeadXRot() {
        return 360;
    }

    //GECKO LIB STUFF
    @Override
    public void registerControllers(AnimatableManager.ControllerRegistrar controllers) {
        controllers.add(new AnimationController<>("controller", 10, test -> {
            if (this.component.shouldBeginReveal()) {
                return test.setAndContinue(TRANSITION);
            }
            else {
                return PlayState.STOP;
            }
        }));
    }

    @Override
    public AnimatableInstanceCache getAnimatableInstanceCache() {return this.cache;}

    @Override
    public List<IKModelComponent<SkinWalkerEntity>> getComponents() {
        return this.components;
    }

    @Override
    public double getSize() {
        return 1;
    }

    @SuppressWarnings("InnerClassMayBeStatic")
    public class SkinWalkerLookControl extends LookControl{
        private int maxLookAtTimer = 5;
        private final Easings.Easing easing = Easings.Easing.easeInOutCubic;

        public SkinWalkerLookControl(Mob entity) {
            super(entity);
        }

        public void lookAt(Entity entity, int lookTimer){
            this.lookAt(new Vec3(entity.getX(), getLookingHeightFor(entity), entity.getZ()), lookTimer);
        }

        public void lookAt(Vec3 vec3d, int lookTimer){
            this.wantedX = vec3d.x;
            this.wantedY = vec3d.y;
            this.wantedZ = vec3d.z;
            this.yMaxRotSpeed = (float)this.mob.getMaxHeadYRot();
            this.xMaxRotAngle = (float)this.mob.getMaxHeadXRot();
            this.lookAtCooldown = lookTimer;
            this.maxLookAtTimer = lookTimer;
//            this.easing;
        }

        @Override
        public void tick() {
            if (this.lookAtCooldown > 0) {
                this.lookAtCooldown--;
                this.getYRotD().ifPresent(yaw -> this.mob.yHeadRot = this.changeAngle2(this.mob.yHeadRot, yaw, this.yMaxRotSpeed));
                this.getXRotD().ifPresent(pitch -> this.mob.setXRot(this.changeAngle2(this.mob.getXRot(), pitch, this.xMaxRotAngle)));
            } else {
                this.mob.yHeadRot = Mth.rotateIfNecessary(this.mob.yHeadRot, this.mob.yBodyRot, 10.0F);
            }

            this.clampHeadRotationToBody();
        }

        private float changeAngle2(float from, float to, float max){
            float f = Mth.degreesDifference(from, to);
            float g = Mth.clamp(f, -max, max);
            return from + (g * this.easing.ease(1 - ((float) this.lookAtCooldown / maxLookAtTimer)));
        }

        private static double getLookingHeightFor(Entity entity) {
            return entity instanceof LivingEntity ? entity.getEyeY() : (entity.getBoundingBox().minY + entity.getBoundingBox().maxY) / 2.0;
        }
    }
}
