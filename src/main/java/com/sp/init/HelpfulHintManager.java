package com.sp.init;

import com.sp.ModKeyBinds;
import com.sp.compat.modmenu.ConfigStuff;
import net.minecraft.ChatFormatting;
import net.minecraft.client.player.LocalPlayer;
import net.minecraft.network.chat.Component;

public class HelpfulHintManager {
    private static final Component flashlightHint = Component.translatable("flashlight.hint", ModKeyBinds.toggleFlashlight.getTranslatedKeyMessage().plainCopy().withStyle(ChatFormatting.BOLD, ChatFormatting.UNDERLINE));
    private static final Component suffocateHint = Component.translatable("noclip.hint");

    public static boolean turnedOnFlashlight;
    public static boolean wentToBackrooms;

    //Not persistent after quitting the game but its good enough
    public static void sendMessages(LocalPlayer player){
        if (ConfigStuff.enableHint) {
            if (!turnedOnFlashlight) {
                player.sendSystemMessage(flashlightHint);
            }

            if (!wentToBackrooms) {
                player.sendSystemMessage(suffocateHint);
            }
        }
    }

    public static void disableFlashlightHint() {
        turnedOnFlashlight = true;
    }

    public static void disableSuffocateHint() {
        wentToBackrooms = true;
    }

}
