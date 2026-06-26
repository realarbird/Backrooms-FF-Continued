package com.sp.mixin;

import net.minecraft.core.Vec3i;
import net.minecraft.world.level.block.entity.StructureBlockEntity;
import net.minecraft.world.level.storage.ValueInput;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(StructureBlockEntity.class)
public class StructureBlockEntityMixin {
    @Shadow
    private Vec3i structureSize;

    @Inject(method = "loadAdditional", at = @At("RETURN"))
    public void readNbt(ValueInput nbt, CallbackInfo ci) {
        int l = nbt.getIntOr("sizeX", 0);
        int m = nbt.getIntOr("sizeY", 0);
        int n = nbt.getIntOr("sizeZ", 0);
        this.structureSize = new Vec3i(l, m, n);
    }
}
