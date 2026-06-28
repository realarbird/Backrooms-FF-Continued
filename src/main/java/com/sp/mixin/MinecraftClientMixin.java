package com.sp.mixin;

import com.sp.SPBRevampedClient;
import com.sp.networking.callbacks.ClientConnectionEvents;
import net.minecraft.client.CameraType;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.screens.ProgressScreen;
import net.minecraft.client.gui.screens.Screen;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.ModifyArg;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;

/**
 * Misc client hooks (NEW-INJECTION). Ported Yarn→mojmap, Phase 6f.
 *
 * <p>PORT: delta — {@code MinecraftClient}→{@code Minecraft}.
 * <ul>
 *   <li><b>disableF5</b> — {@code handleInputEvents}→{@code handleKeybinds}; the perspective toggle now
 *       calls {@code Options.setCameraType(CameraType)} ({@code Perspective}→{@code CameraType}). Forces
 *       first-person while a cutscene plays. Same outcome.</li>
 *   <li><b>onDisconnect</b> — {@code disconnect(Screen)}→{@code disconnect(Screen, boolean)}; the login-
 *       disconnect callback fires unless we're going through a {@code ProgressScreen}. Same outcome.</li>
 *   <li><b>DROPPED: the {@code run}→{@code render(Z)} "re-enable veil:deferred resource pack" injection</b>
 *       — Veil is removed (no GLSL/deferred pipeline on macOS), so there is no {@code veil:deferred}
 *       pack to re-enable. Delta: none (the pack no longer exists).</li>
 * </ul>
 */
@Mixin(Minecraft.class)
public abstract class MinecraftClientMixin {

    @ModifyArg(method = "handleKeybinds", at = @At(value = "INVOKE", target = "Lnet/minecraft/client/Options;setCameraType(Lnet/minecraft/client/CameraType;)V"))
    private CameraType disableF5(CameraType cameraType){
        if(SPBRevampedClient.getCutsceneManager().isPlaying) {
            return CameraType.FIRST_PERSON;
        }

        return cameraType;
    }

    @Inject(method = "disconnect(Lnet/minecraft/client/gui/screens/Screen;Z)V", at = @At("HEAD"))
    private void onDisconnect(Screen screen, boolean keepResourcePacks, CallbackInfo ci){
        if(!(screen instanceof ProgressScreen)) {
            ClientConnectionEvents.DISCONNECT.invoker().onLoginDisconnect((Minecraft) (Object) this);
        }
    }
}
