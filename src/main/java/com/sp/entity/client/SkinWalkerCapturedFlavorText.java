package com.sp.entity.client;

import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Options;
import net.minecraft.network.chat.Component;
import net.minecraft.world.entity.player.Player;

/**
 * PORT: delta (1.20.1 -> 26.2) — pure mojmap rename. {@code MinecraftClient}->{@code Minecraft},
 * {@code GameOptions}->{@code Options}, {@code PlayerEntity}->{@code Player}, {@code Text}->{@code Component}
 * ({@code .formatted(...)} -> {@code .withStyle(...)}), {@code Formatting}->{@code ChatFormatting},
 * {@code player.sendMessage(Text)} -> {@code player.sendSystemMessage(Component)}, the option key fields
 * ({@code forwardKey}/etc.) -> {@code keyUp}/{@code keyDown}/{@code keyLeft}/{@code keyRight}/{@code keyJump}/
 * {@code keyShift}, and {@code KeyBinding.isPressed()} -> {@code KeyMapping.isDown()}.
 */
public class SkinWalkerCapturedFlavorText {
    private static final Minecraft client = Minecraft.getInstance();
    private static final Options options = client.options;
    private static boolean shownMovementText = false;
    public static boolean triedToOpenInventory = false;
    private static boolean shownInventoryText = false;
    public static boolean triedToLeave = false;
    private static boolean shownTriedToLeaveText = false;
    public static boolean triedToChat = false;
    private static boolean shownTriedToChatText = false;
    private static int textCount = 0;
    private static boolean shownTextTaunt = false;
    private static int tick = 0;


    public static void tickFlavorText(Player player) {
        if(isPressingMoveKeys()){
            if(!shownMovementText){
                player.sendSystemMessage(Component.translatable("skinwalker.flavor-text.move1").append(Component.translatable("skinwalker.flavor-text.move2").withStyle(ChatFormatting.RED)));
                 shownMovementText = true;
                textCount++;
            }
        }

        if(triedToOpenInventory) {
            if(!shownInventoryText) {
                player.sendSystemMessage(Component.translatable("skinwalker.flavor-text.inventory").withStyle(ChatFormatting.RED));
                shownInventoryText = true;
                textCount++;
            }
        }

        if(triedToLeave) {
            if(!shownTriedToLeaveText) {
                player.sendSystemMessage(Component.translatable("skinwalker.flavor-text.leave").withStyle(ChatFormatting.RED));
                shownTriedToLeaveText = true;
                textCount++;
            }
        }

        if(triedToChat) {
            if(!shownTriedToChatText) {
                player.sendSystemMessage(Component.translatable("skinwalker.flavor-text.chat").withStyle(ChatFormatting.RED));
                shownTriedToChatText = true;
                textCount++;
            }
        }

        if(!shownTextTaunt && !shownTriedToChatText) {
            if(textCount >= 2) {
                tick++;

                if(tick == 100){
                    player.sendSystemMessage(Component.translatable("skinwalker.flavor-text.taunt1").withStyle(ChatFormatting.RED));
                }

                if(tick == 135){
                    player.sendSystemMessage(Component.translatable("skinwalker.flavor-text.taunt2").withStyle(ChatFormatting.GOLD));

                }

                if(tick == 300){
                    player.sendSystemMessage(Component.translatable("skinwalker.flavor-text.taunt3").append(Component.translatable("skinwalker.flavor-text.taunt4")).withStyle(ChatFormatting.RED));
                    shownTextTaunt = true;
                }
            }
        }


    }

    private static boolean isPressingMoveKeys() {
        return options.keyUp.isDown() || options.keyDown.isDown() || options.keyLeft.isDown() || options.keyRight.isDown() || options.keyJump.isDown() || options.keyShift.isDown();
    }


}
