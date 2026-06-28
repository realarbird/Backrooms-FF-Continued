package com.sp.render.gui;

import com.sp.SPBRevamped;
import com.sp.render.camera.CutsceneManager;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElement;
import net.fabricmc.fabric.api.client.rendering.v1.hud.HudElementRegistry;
import net.minecraft.ChatFormatting;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.Font;
import net.minecraft.client.gui.GuiGraphicsExtractor;
import net.minecraft.network.chat.Component;
import net.minecraft.resources.Identifier;
import org.joml.Matrix3x2fStack;

/**
 * "The Backrooms" intro title + "A Mod By ARBird" credit, shown during the opening cutscene.
 *
 * <p>Port note (Phase 6c, effect A14): migrated from Fabric {@code HudRenderCallback} + {@code DrawContext}
 * to a {@link HudElement} drawing via {@link GuiGraphicsExtractor}. The matrix push/translate/scale is now
 * done through {@code graphics.pose()} ({@link Matrix3x2fStack}). {@code Text}/{@code Formatting} became
 * {@code Component}/{@link ChatFormatting}.</p>
 */
public class TitleText implements HudElement {
    public static final Identifier ID = Identifier.fromNamespaceAndPath(SPBRevamped.MOD_ID, "intro_title");

    public TitleText() {
    }

    /** Registers the intro title in the HUD layer stack. Call from the client init entrypoint. */
    public static void register() {
        HudElementRegistry.addLast(ID, new TitleText());
    }

    @Override
    public void extractRenderState(GuiGraphicsExtractor graphics, DeltaTracker delta) {
        // Show only during the opening cutscene's backroomsBySP beat, while the black-screen is NOT up
        // (backed by SPBRevampedClient.getCutsceneManager() — see shouldShowIntro()).
        if (!shouldShowIntro()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        Font font = client.font;

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        Component title = Component.translatable("intro.backrooms");
        Component credit = Component.translatable("intro.credit").append(Component.literal("ARBird").withStyle(ChatFormatting.GOLD));

        Matrix3x2fStack pose = graphics.pose();
        pose.pushMatrix();
        pose.translate((float) (width / 2), (float) (height / 2));

        // Title at 2.5x.
        pose.pushMatrix();
        pose.scale(2.5f, 2.5f);
        int tw = font.width(title);
        int th = font.lineHeight;
        graphics.text(font, title, -tw / 2, -th / 2, 0xFFFFFFFF, true);
        pose.popMatrix();

        // Credit at 1x, below the title.
        pose.pushMatrix();
        int cw = font.width(credit);
        graphics.text(font, credit, -cw / 2, 10, 0xFFFFFFFF, true);
        pose.popMatrix();

        pose.popMatrix();
    }

    // ----------------------------------------------------------------------------------------------------
    // Cutscene-state gate (Phase 6e-b). Backed directly by the cutscene manager:
    //   SPBRevampedClient.getCutsceneManager().backroomsBySP && !...blackScreen.isBlackScreen
    // (SPBRevampedClient also mirrors these onto introActive/introBlackScreen each client tick.)
    // ----------------------------------------------------------------------------------------------------
    private static boolean shouldShowIntro() {
        CutsceneManager cutscene = com.sp.SPBRevampedClient.getCutsceneManager();
        return cutscene.backroomsBySP && !cutscene.blackScreen.isBlackScreen;
    }

    // Mirrored each client tick by SPBRevampedClient (kept for any external readers).
    public static volatile boolean introActive = false;
    public static volatile boolean introBlackScreen = false;
}
