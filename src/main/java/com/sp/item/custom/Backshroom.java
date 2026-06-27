package com.sp.item.custom;

import net.minecraft.ChatFormatting;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.TooltipFlag;
import net.minecraft.world.item.component.TooltipDisplay;

import java.util.function.Consumer;

public class Backshroom extends Item {

    public Backshroom(Properties settings) {
        super(settings);
    }

    // PORT: delta - 26.2 reworked the tooltip API. Yarn's
    // appendTooltip(ItemStack, World, List<Text>, TooltipContext) became
    // appendHoverText(ItemStack, Item.TooltipContext, TooltipDisplay, Consumer<Component>, TooltipFlag):
    // lines are now pushed through a Consumer<Component> instead of being added to a List, and the
    // text-style helpers renamed (Text.translatable->Component.translatable, .formatted(Formatting)->
    // .withStyle(ChatFormatting), which is varargs so both styles go in one call).
    @Override
    public void appendHoverText(ItemStack stack, Item.TooltipContext context, TooltipDisplay tooltipDisplay, Consumer<Component> tooltipAdder, TooltipFlag flag) {
        super.appendHoverText(stack, context, tooltipDisplay, tooltipAdder, flag);
        tooltipAdder.accept(Component.translatable("item.spb-revamped.backshroom.tooltip").withStyle(ChatFormatting.DARK_PURPLE, ChatFormatting.ITALIC));
    }
}
