package com.sp.render.gui;

import com.sp.SPBRevamped;
import com.sp.cca_stuff.InitializeComponents;
import com.sp.cca_stuff.PlayerComponent;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.client.renderer.RenderPipelines;
import net.minecraft.resources.Identifier;
import net.minecraft.util.Mth;
import net.minecraft.world.entity.player.Player;

/**
 * Out-of-breath stamina indicator, centred on screen, fading in/out with the player's stamina.
 *
 * <p>Port note (Phase 6c, effect A14): migrated from Fabric {@code HudRenderCallback} + {@code DrawContext}
 * to a {@link HudElement} drawing via {@link GuiGraphicsExtractor}. 26.2's {@code GuiGraphicsExtractor}
 * has no {@code setShaderColor}; the old per-draw alpha is baked into the blit colour (ARGB) instead, and
 * the old matrix push/translate/scale becomes direct integer placement of the (scaled) 0.2x icon.</p>
 */
public class StaminaBar implements HudElement {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "stamina_bar");
    private static final Identifier STAMINA_ICONS = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "textures/gui/stamina.png");

    // stamina.png is 64x64; the original drew it at 0.2x scale centred on screen.
    private static final int TEX = 64;
    private static final int FULL_W = 44;
    private static final int FULL_H = 64;

    private Long fadeStart;
    private float fadeTimer;

    /** Registers the stamina bar in the HUD layer stack. Call from the client init entrypoint. */
    public static void register() {
        HudElementRegistry.addLast(ID, new StaminaBar());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        Minecraft client = Minecraft.getInstance();
        Player player = client.player;
        if (player == null) {
            return;
        }

        PlayerComponent component = InitializeComponents.PLAYER.get(player);

        // 0.2x scale: a 64px icon becomes ~13px; centre it on screen.
        int cx = graphics.guiWidth() / 2;
        int cy = graphics.guiHeight() / 2;
        int w = Math.round(FULL_W * 0.2f);
        int h = Math.round(FULL_H * 0.2f);

        if (component.getStamina() < 300) {
            this.fadeStart = null;
            this.fadeTimer = 0.0f;

            float normalizedStamina = 1.0f - (float) component.getStamina() / 300.0f;
            int offset = Mth.floor(normalizedStamina * FULL_H);
            int offsetScaled = Math.round(offset * 0.2f);

            int color = argb(0.25f);
            // Background "frame": region (u=44,v=0) (matches old drawTexture(tex, w/2, -h/2, w, 0, 64, h)).
            blit(graphics, STAMINA_ICONS, cx + w / 2, cy - h / 2, FULL_W, 0, w, h, color);
            // Foreground "fill": region (u=0,v=offset), drawn shifted down by the (scaled) offset.
            blit(graphics, STAMINA_ICONS, cx + w / 2 + 1, cy - h / 2 + offsetScaled, 0, offset, w, h - offsetScaled, color);
        } else if (this.fadeTimer < 1.0f) {
            if (this.fadeStart == null) {
                this.fadeStart = System.currentTimeMillis();
            }
            this.fadeTimer = Math.min((float) (System.currentTimeMillis() - this.fadeStart) / 1000.0f, 1.0f);

            int color = argb(0.25f * (1.0f - this.fadeTimer));
            blit(graphics, STAMINA_ICONS, cx + w / 2, cy - h / 2, FULL_W, 0, w, h, color);
            blit(graphics, STAMINA_ICONS, cx + w / 2 + 1, cy - h / 2, 0, 0, w, h, color);
        }
    }

    /** White tint with the given alpha, as 0xAARRGGBB. */
    private static int argb(float alpha) {
        int a = Mth.clamp(Math.round(alpha * 255.0f), 0, 255);
        return (a << 24) | 0x00FFFFFF;
    }

    /**
     * Region blit at {@code (x,y)} of size {@code (w,h)} sourced from texture coords {@code (u,v)} of the
     * 64x64 sheet, tinted with {@code color}. Uses the {@code blit(pipeline, id, x, y, u, v, w, h, texW,
     * texH, color)} overload (the 26.2 successor to {@code DrawContext.drawTexture}).
     */
    private static void blit(GuiGraphicsExtractor graphics, Identifier id, int x, int y, float u, float v, int w, int h, int color) {
        if (w <= 0 || h <= 0) {
            return;
        }
        graphics.blit(RenderPipelines.GUI_TEXTURED, id, x, y, u, v, w, h, TEX, TEX, color);
    }
}
