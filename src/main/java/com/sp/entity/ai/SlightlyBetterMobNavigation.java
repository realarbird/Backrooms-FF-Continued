package com.sp.entity.ai;

import com.sp.entity.custom.SkinWalkerEntity;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.Mob;
import net.minecraft.world.entity.ai.navigation.GroundPathNavigation;
import net.minecraft.world.level.ClipContext;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.HitResult;
import net.minecraft.world.phys.Vec3;

public class SlightlyBetterMobNavigation extends GroundPathNavigation {

    public SlightlyBetterMobNavigation(Mob mobEntity, Level world) {
        super(mobEntity, world);
    }

    @Override
    public void tick() {
        LivingEntity livingEntity = this.mob.getTarget();
        if(livingEntity != null) {
            if (this.canSee(this.mob, livingEntity)) {
                if (!this.isDone() || ((SkinWalkerEntity)this.mob).component.isActive()) {
                    // adjustTargetY has no mojmap equivalent; move toward the target's actual Y.
                    this.mob.getMoveControl().setWantedPosition(livingEntity.getX(), livingEntity.getY(), livingEntity.getZ(), this.speedModifier);
                    return;
                }
            }
        }
        super.tick();
    }

    private boolean canSee(Entity entity1, Entity entity2) {
        if (entity2.level() != entity1.level()) {
            return false;
        } else {
            Vec3 vec3d = new Vec3(entity1.getX(), entity1.getY() + 0.05, entity1.getZ());
            Vec3 vec3d2 = new Vec3(entity2.getX(), entity2.getEyeY(), entity2.getZ());
            return entity1.level().clip(new ClipContext(
                    vec3d,
                    vec3d2,
                    ClipContext.Block.COLLIDER,
                    ClipContext.Fluid.NONE,
                    entity1)).getType()
                    == HitResult.Type.MISS;
        }
    }
}
