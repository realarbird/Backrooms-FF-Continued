package com.sp.mixin.collision;

import com.llamalad7.mixinextras.injector.wrapmethod.WrapMethod;
import com.llamalad7.mixinextras.injector.wrapoperation.Operation;
import com.sp.util.CollisionHelper;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;

@Mixin(Entity.class)
public abstract class PlatformCollisionMixin {
    // If re-enabled: Yarn `adjustMovementForCollisions(Vec3d)` is `collide(Vec3)` (private) in 26.2.
//    @WrapMethod(method = "collide(Lnet/minecraft/world/phys/Vec3;)Lnet/minecraft/world/phys/Vec3;")
//    private Vec3 collide(Vec3 movement, Operation<Vec3> original) {
//        Entity entity = (Entity) (Object) this;
//
//        Vec3 adjustedMovement = movement;
//
//        if (entity != null) {
//            if (movement.y != 0) {
//                double yAxisCollision = CollisionHelper.getCollisionOffset(entity, adjustedMovement);
//
//                adjustedMovement = adjustedMovement.add(0, yAxisCollision, 0);
//            }
//
//            return original.call(adjustedMovement);
//        }
//
//        return original.call(movement);
//    }
}
