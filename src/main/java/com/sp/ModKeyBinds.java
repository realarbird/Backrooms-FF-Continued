package com.sp;

import com.mojang.blaze3d.platform.InputConstants;
// PORT: delta - Yarn used fabric-api's fabric-key-binding-api-v1 (KeyBindingHelper). In 26.2 that
// module is renamed fabric-key-mapping-api-v1; the helper class is
// net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper and the register method is
// registerKeyMapping(KeyMapping) (was registerKeyBinding(KeyBinding)).
import net.fabricmc.fabric.api.client.keymapping.v1.KeyMappingHelper;
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
        toggleFlashlight = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.spb-revamped.toggle_flashlight", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_R, KEYBINDS_CATEGORY));

        if (Minecraft.getInstance().getUser().getName().equals("SppacePotato") || Minecraft.getInstance().getUser().getName().equals("HerrChaotic") || FabricLoader.getInstance().isDevelopmentEnvironment()) {
            toggleEvent = KeyMappingHelper.registerKeyMapping(new KeyMapping("key.spb-revamped.toggle_event", InputConstants.Type.KEYSYM, GLFW.GLFW_KEY_SEMICOLON, KEYBINDS_CATEGORY));
        }
    }
}
