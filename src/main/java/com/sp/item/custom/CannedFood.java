package com.sp.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class CannedFood extends Item {

    public CannedFood(Properties settings) {
        super(settings);
    }

    // PORT: delta - 26.2 tooltip API (see Backshroom): appendTooltip(...List<Text>...) ->
    // appendHoverText(ItemStack, Item.TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag).
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(Component.translatable("item.spb-revamped.canned_food.tooltip").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
