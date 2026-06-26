package com.sp.mixin.skinstolen;

import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.entity.client.SkinWalkerCapturedFlavorText;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Gui;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(Minecraft.class)
public class MinecraftClientMixin {

    // handleInputEvents -> handleKeybinds; the inventory open now routes through Gui.setScreen
    // (the sole setScreen call in handleKeybinds, hence ordinal 0).
    @Redirect(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/gui/Gui;setScreen(Lnet/minecraft/client/gui/screens/Screen;)V", ordinal = 0))
    private void disableInventory(Gui instance, Screen screen){
        Minecraft client = Minecraft.getInstance();
        if(client.player != null) {
            PlayerComponent component = InitializeComponents.PLAYER.get(client.player);
            if (!component.hasBeenCaptured()) {
                instance.setScreen(screen);
            } else {
                SkinWalkerCapturedFlavorText.triedToOpenInventory = true;
            }
        }
    }
}
