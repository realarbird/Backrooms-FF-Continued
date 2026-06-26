package com.sp.mixin.skinstolen;

import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import com.sp.entity.client.SkinWalkerCapturedFlavorText;
import net.minecraft.client.gui.screens.ChatScreen;
import net.minecraft.client.gui.screens.Screen;
import net.minecraft.client.multiplayer.ClientPacketListener;
import net.minecraft.network.chat.Component;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Redirect;

@Mixin(ChatScreen.class)
public abstract class ChatScreenMixin extends Screen{

    protected ChatScreenMixin(Component title) {
        super(title);
    }

    @Redirect(method = "handleChatInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendChat(Ljava/lang/String;)V"))
    private void noOneCanHearYouScream(ClientPacketListener instance, String content){
        PlayerComponent component = InitializeComponents.PLAYER.get(this.minecraft.player);

        if(!component.hasBeenCaptured()){
            instance.sendChat(content);
        } else {
            SkinWalkerCapturedFlavorText.triedToChat = true;
        }
    }

    @Redirect(method = "handleChatInput", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/multiplayer/ClientPacketListener;sendCommand(Ljava/lang/String;)V"))
    private void noOneCanHearYouScream2(ClientPacketListener instance, String content){
        PlayerComponent component2 = InitializeComponents.PLAYER.get(this.minecraft.player);

        if(!component2.hasBeenCaptured() || content.contains("release")){
            instance.sendCommand(content);
        } else {
            SkinWalkerCapturedFlavorText.triedToChat = true;
        }
    }
}
