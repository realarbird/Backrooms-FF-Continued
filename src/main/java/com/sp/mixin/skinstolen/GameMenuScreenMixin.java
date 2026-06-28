package com.sp.mixin.skinstolen;

import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.entity.client.SkinWalkerCapturedFlavorText;
import net.minecraft.ChatFormatting;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(PauseScreen.class)
public abstract class GameMenuScreenMixin extends Screen {

    protected GameMenuScreenMixin(Component title) {
        super(title);
    }

    // You can't escape. GameMenuScreen.initWidgets -> PauseScreen.createPauseMenu; ButtonWidget -> Button.
    // javap-verified: createPauseMenu() has exactly 3 Button.builder(Component,OnPress) INVOKEs (ordinals
    // 0/1/2); the Feedback/Report/Bug-report buttons in between use SpriteIconButton.builder and don't
    // count. By the Component loaded before each: ordinal 0 = RETURN_TO_GAME, ordinal 1 = OPTIONS, ordinal
    // 2 = CommonComponents.disconnectButtonLabel(isLocalServer) -> the "leave"/disconnect button. So the
    // disconnect button is ordinal 2.
    @Redirect(method = "createPauseMenu", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;", ordinal = 2))
    private Button.Builder noEscape(Component message, Button.OnPress onPress){
        Minecraft client1 = Minecraft.getInstance();
        if(client1.player != null) {
            PlayerComponent component = InitializeComponents.PLAYER.get(client1.player);

            if (component.hasBeenCaptured()) {
                return new Button.Builder(Component.translatable("skinwalker.flavor-text.leave").withStyle(ChatFormatting.RED), button -> {
                    button.active = true;
                    SkinWalkerCapturedFlavorText.triedToLeave = true;
                });
            }
        }

        return new Button.Builder(message, onPress);
    }
}
