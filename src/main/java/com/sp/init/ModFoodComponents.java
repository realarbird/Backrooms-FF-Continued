package com.sp.init;

import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.effect.MobEffects;
import net.minecraft.world.food.FoodProperties;
import net.minecraft.world.item.component.Consumable;
import net.minecraft.world.item.component.Consumables;
import net.minecraft.world.item.consume_effects.ApplyStatusEffectsConsumeEffect;

public class ModFoodComponents {
    // 26.2: FoodProperties no longer carries status effects (nutrition/saturation/canAlwaysEat only).
    // The on-eat effects moved to the item's Consumable component via ApplyStatusEffectsConsumeEffect.
    public static final FoodProperties CANNED_FOOD = new FoodProperties.Builder().nutrition(5).saturationModifier(0.8f).build();
    public static final FoodProperties BACKSHROOM = new FoodProperties.Builder().nutrition(3).saturationModifier(0.4f).build();

    public static final Consumable CANNED_FOOD_CONSUMABLE = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.POISON, 200, 2), 0.05f))
            .build();
    public static final Consumable BACKSHROOM_CONSUMABLE = Consumables.defaultFood()
            .onConsume(new ApplyStatusEffectsConsumeEffect(new MobEffectInstance(MobEffects.NAUSEA, 200, 2), 0.1f))
            .build();
}
