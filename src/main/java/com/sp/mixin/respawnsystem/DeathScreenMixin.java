package com.sp.mixin.respawnsystem;

import com.sp.init.BackroomsLevels;
import net.minecraft.client.gui.components.Button;
import net.minecraft.client.gui.screens.DeathScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.Redirect;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

@Mixin(DeathScreen.class)
public abstract class DeathScreenMixin extends Screen {

    @Shadow private int delayTicker;

    @Shadow protected abstract void setButtonsActive(boolean active);

    protected DeathScreenMixin(Component title) {
        super(title);
    }

    @Unique private static boolean firstTimeDead = true;
    @Unique private int delay = 0;

    @Unique
    private boolean isInBackrooms(){
        if(this.minecraft.player != null){
            if(this.minecraft.player.level() != null){
                return BackroomsLevels.isInBackrooms(this.minecraft.player.level().dimension());
            }
        }

        return false;
    }


    //Remove the functionality of the respawn button
    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;", ordinal = 0))
    private Button.Builder disableRespawnButton(Component message, Button.OnPress onPress){
        if (this.isInBackrooms()) {
            return new Button.Builder(message, button -> {
                firstTimeDead = false;
                button.active = true;
            });
        }

        return new Button.Builder(message, onPress);
    }

    //Remove the functionality of the title screen button
    @Redirect(method = "init", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/components/Button;builder(Lnet/minecraft/network/chat/Component;Lnet/minecraft/client/gui/components/Button$OnPress;)Lnet/minecraft/client/gui/components/Button$Builder;", ordinal = 1))
    private Button.Builder disableTitleScreenButton(Component message, Button.OnPress onPress){
        if (this.isInBackrooms()) {
            return new Button.Builder(message, button -> {
                firstTimeDead = false;
                button.active = true;
            });
        }

        return new Button.Builder(message, onPress);
    }


    @Inject(method = "tick", at = @At("TAIL"))
    private void youAreNotDoneYet(CallbackInfo ci){
        if (this.isInBackrooms()) {
            this.setButtonsActive(true);
            if (!firstTimeDead) {
                delay++;
                if(delay == 80) {
                    this.minecraft.player.respawn();
                    this.minecraft.setScreenAndShow(null);
                }
            }
        }
    }

}
