package com.sp.render.gui;

import com.sp.SPBRevamped;
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
 * "The Backrooms" intro title + "by SpacePotato" credit, shown during the opening cutscene.
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
        // PORT: phase6e — gate on SPBRevampedClient.getCutsceneManager(): show only while
        // cutscene.backroomsBySP is true and the black-screen is NOT up. The cutscene manager is not ported
        // yet (Veil anim Path / MinecraftClient). Until then this is driven by the shared static flags
        // below, which 6e wires to the real cutscene state.
        if (!shouldShowIntro()) {
            return;
        }

        Minecraft client = Minecraft.getInstance();
        Font font = client.font;

        int width = graphics.guiWidth();
        int height = graphics.guiHeight();

        Component title = Component.translatable("intro.backrooms");
        Component credit = Component.translatable("intro.credit").append(Component.literal("SpacePotato").withStyle(ChatFormatting.GOLD));

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
    // PORT: phase6e — cutscene-state stub.
    // Replace with: SPBRevampedClient.getCutsceneManager().backroomsBySP
    //               && !SPBRevampedClient.getCutsceneManager().blackScreen.isBlackScreen
    // ----------------------------------------------------------------------------------------------------
    private static boolean shouldShowIntro() {
        return introActive && !introBlackScreen;
    }

    /** PORT: phase6e — set from cutscene.backroomsBySP. */
    public static volatile boolean introActive = false;
    /** PORT: phase6e — set from cutscene.blackScreen.isBlackScreen. */
    public static volatile boolean introBlackScreen = false;
}
