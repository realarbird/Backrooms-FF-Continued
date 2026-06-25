package com.sp;

import com.mojang.blaze3d.platform.InputConstants;
import net.fabricmc.fabric.api.client.keybinding.v1.KeyBindingHelper;
import net.fabricmc.loader.api.FabricLoader;
import net.minecraft.client.KeyMapping;
import net.minecraft.client.Minecraft;
import net.minecraft.resources.Identifier;
import org.lwjgl.glfw.GLFW;

public class ModKeyBinds {
    // 26.2: keybinding categories are registered KeyMapping.Category objects (keyed by Identifier),
    // not raw category strings as in 1.20.1.
    public static final KeyMapping.Category KEYBINDS_CATEGORY =
            KeyMapping.Category.register(Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "keybinds"));

    public static KeyMapping toggleFlashlight;
    public static KeyMapping toggleEvent;

    public static void initializeKeyBinds() {
        toggleFlashlight = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.spb-revamped.toggle_flashlight", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KEYBINDS_CATEGORY));

        if (Minecraft.getInstance().getUser().getName().equals("SppacePotato") || Minecraft.getInstance().getUser().getName().equals("HerrChaotic") || FabricLoader.getInstance().isDevelopmentEnvironment()) {
            toggleEvent = KeyBindingHelper.registerKeyBinding(new KeyMapping("key.spb-revamped.toggle_event", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_SEMICOLON, KEYBINDS_CATEGORY));
        }
    }
}
