/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.client;

import com.chunkworks.azimuth.Azimuth;
import com.chunkworks.azimuth.AzimuthConfig;
import com.chunkworks.azimuth.Bearings;
import com.chunkworks.azimuth.Payloads;
import com.chunkworks.azimuth.domain.BarLayout;
import com.chunkworks.azimuth.domain.BarPosition;
import com.chunkworks.azimuth.domain.Bearing;
import com.chunkworks.azimuth.domain.Fade;
import com.mojang.blaze3d.systems.RenderSystem;
import net.minecraft.client.DeltaTracker;
import net.minecraft.client.Minecraft;
import net.minecraft.client.gui.GuiGraphics;
import net.minecraft.client.gui.components.PlayerFaceRenderer;
import net.minecraft.client.resources.DefaultPlayerSkin;
import net.minecraft.core.registries.BuiltInRegistries;
import net.minecraft.resources.ResourceLocation;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import java.util.*;

/** The bar. Drawn as a HUD layer above the boss overlay, so the boss bars of this frame have
 * already reported their heights and the bar can sit below them. Everything about where a
 * marker goes and how faint it is comes from the domain; this only paints, with the sprites
 * under {@code textures/gui/sprites} (drawn by {@code devtools/art/sprites.py}).
 *
 * <p>The look: a 102 by 10 outlined bar with a notch at the viewer's heading; north, east, south
 * and west as coloured badges; a player within the fade as their head in a black frame, beyond it
 * as a small outlined dot; a place as its item over an outlined dot in the place's colour; and
 * whatever lies outside the view as one chevron at the nearer end, in the colour of the nearest
 * marker hidden that way (a stack of one per marker hid each other and cluttered the end, in the
 * booth). Markers keep {@link #MARGIN} pixels clear at each end for the chevrons. The tinted
 * sprites are white with a black outline, so the colour multiplies in and the outline stays
 * black. */
public final class AzimuthHud {
    static final int BAR_WIDTH = 102, BAR_HEIGHT = 10, HALF_WIDTH = 51, MARGIN = 6, MARKER = 8;
    private static final int TEXT = 0xFFE8E8E8, PLAYER = 0xF3DFA1;
    private static final ResourceLocation BAR = Azimuth.id("bar"), NOTCH = Azimuth.id("notch"), FRAME = Azimuth.id("frame"),
            DOT = Azimuth.id("dot"), PLAYER_DOT = Azimuth.id("player_dot"), FAR = Azimuth.id("far"),
            CHEVRON_LEFT = Azimuth.id("chevron_left"), CHEVRON_RIGHT = Azimuth.id("chevron_right");
    private static final float[] DIRECTION_YAWS = { 180f, -90f, 0f, 90f };
    private static final ResourceLocation[] BADGES = { Azimuth.id("badge_n"), Azimuth.id("badge_e"), Azimuth.id("badge_s"), Azimuth.id("badge_w") };
    /** The lowest boss bar's bottom edge this frame, reported by the boss overlay before we draw. */
    private static int bossBarsBottom, lastBossBarsBottom, lastTop;
    private AzimuthHud() {}

    /** effects: remembers how low the boss bars reach this frame. */
    public static void onBossBar(CustomizeGuiOverlayEvent.BossEventProgress event) {
        bossBarsBottom = Math.max(bossBarsBottom, event.getY() + event.getIncrement());
    }
    /** effects: how low the boss bars reached in the last frame drawn, 0 when none showed. */
    public static int lastBossBarsBottom() { return lastBossBarsBottom; }
    /** effects: the bar's top edge in the last frame drawn. */
    public static int lastTop() { return lastTop; }
    /** effects: draws the bar for this frame and consumes the boss-bar height. */
    public static void draw(GuiGraphics g, DeltaTracker delta) {
        lastBossBarsBottom = bossBarsBottom;
        int below = AzimuthConfig.BELOW_BOSS_BARS.get() ? bossBarsBottom : 0;
        bossBarsBottom = 0;
        var mc = Minecraft.getInstance();
        if (!AzimuthConfig.ENABLED.get() || mc.options.hideGui || mc.player == null || mc.level == null) return;
        float scale = AzimuthConfig.SCALE.get().floatValue();
        int width = Math.round(BAR_WIDTH * scale);
        int left = BarPosition.left(g.guiWidth(), width, AzimuthConfig.OFFSET_X.get());
        int top = BarPosition.top(AzimuthConfig.OFFSET_Y.get(), below);
        lastTop = top;
        var layout = new BarLayout(HALF_WIDTH - MARGIN, AzimuthConfig.VIEW_ANGLE.get().floatValue() / 2f);
        var player = mc.player;
        float yaw = player.getYRot();

        // Every vanilla layer brackets itself in blending and the boss overlay before us turns it
        // off at its end; without this every sprite drew at full strength whatever its alpha (the
        // booth's far dot measured pure cream).
        RenderSystem.enableBlend();
        RenderSystem.defaultBlendFunc();
        g.pose().pushPose();
        g.pose().translate(left, top, 0);
        g.pose().scale(scale, scale, 1);
        g.blitSprite(BAR, 0, 0, BAR_WIDTH, BAR_HEIGHT);
        g.blitSprite(NOTCH, HALF_WIDTH - 1, 0, 3, BAR_HEIGHT);
        if (AzimuthConfig.SHOW_DIRECTIONS.get()) for (int i = 0; i < 4; i++) {
            var place = layout.place(Bearing.toward(DIRECTION_YAWS[i], yaw));
            if (place.clamped()) continue;
            g.blitSprite(BADGES[i], HALF_WIDTH + place.x() - 3, 2, 7, 7);
        }
        var edges = new Edges();
        Bearings.locations().ifPresent(payload -> drawLocations(g, mc, payload, layout, player.getX(), player.getZ(), yaw, edges));
        Bearings.players().ifPresent(payload -> drawPlayers(g, mc, payload, layout, player.getX(), player.getZ(), yaw, edges));
        edges.draw(g);
        if (AzimuthConfig.SHOW_COORDINATES.get()) {
            var text = player.getBlockX() + " " + player.getBlockY() + " " + player.getBlockZ();
            g.drawCenteredString(mc.font, text, HALF_WIDTH, BAR_HEIGHT + 2, TEXT);
        }
        g.pose().popPose();
        RenderSystem.disableBlend();
    }

    /** effects: the other players, the tracked entity's live position winning over the server's
     * last word: a framed head fading by distance, a small dot once beyond the fade, a plain dot
     * when heads are off, and a chevron at the end when outside the view. */
    private static void drawPlayers(GuiGraphics g, Minecraft mc, Payloads.Players payload, BarLayout layout, double x, double z, float yaw, Edges edges) {
        var live = new HashMap<UUID, double[]>();
        for (var other : mc.level.players()) if (other != mc.player) live.put(other.getUUID(), new double[] { other.getX(), other.getZ() });
        // Payloads come nearest first; drawn farthest first, so where markers overlap the
        // nearest is on top.
        for (var entry : payload.entries().reversed()) {
            var at = live.getOrDefault(entry.id(), new double[] { entry.x(), entry.z() });
            var bearing = Bearing.of(x, z, yaw, at[0], at[1]);
            float alpha = Fade.player(bearing.distance(), payload.fadeStart(), payload.fadeToMin(), payload.minAlpha());
            var place = layout.place(bearing.relativeYaw());
            if (place.clamped()) { edges.add(place, bearing.distance(), PLAYER, alpha); continue; }
            int centre = HALF_WIDTH + place.x();
            if (!AzimuthConfig.HEADS.get()) tinted(g, PLAYER_DOT, centre - 3, 2, 7, 7, PLAYER, alpha);
            else if (Fade.far(bearing.distance(), payload.fadeToMin())) tinted(g, FAR, centre - 2, 3, 5, 5, PLAYER, alpha);
            else {
                g.setColor(1, 1, 1, alpha);
                g.blitSprite(FRAME, centre - 5, 0, 10, 10);
                PlayerFaceRenderer.draw(g, skin(mc, entry.id()), centre - 4, 1, MARKER);
                g.setColor(1, 1, 1, 1);
                // The face renderer turns blending off after the hat layer; everything after a
                // head drew at full strength until this (the booth's far dot measured pure cream).
                RenderSystem.enableBlend();
            }
        }
    }
    /** effects: the places: the item's sprite over an outlined dot in the place's colour, fading
     * in at the edge of range, a chevron at the end when outside the view. */
    private static void drawLocations(GuiGraphics g, Minecraft mc, Payloads.Locations payload, BarLayout layout, double x, double z, float yaw, Edges edges) {
        for (var entry : payload.entries().reversed()) {
            var bearing = Bearing.of(x, z, yaw, entry.x(), entry.z());
            float alpha = Fade.location(bearing.distance(), payload.range(), payload.fade());
            if (alpha <= 0) continue;
            var place = layout.place(bearing.relativeYaw());
            if (place.clamped()) { edges.add(place, bearing.distance(), entry.color(), alpha); continue; }
            int centre = HALF_WIDTH + place.x();
            tinted(g, DOT, centre - 5, 0, 10, 10, entry.color(), alpha);
            var item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(entry.icon()));
            if (item != Items.AIR) {
                var sprite = mc.getItemRenderer().getModel(new ItemStack(item), null, null, 0).getParticleIcon(net.neoforged.neoforge.client.model.data.ModelData.EMPTY);
                g.setColor(1, 1, 1, alpha);
                g.blit(centre - MARKER / 2, 1, 0, MARKER, MARKER, sprite);
                g.setColor(1, 1, 1, 1);
            }
        }
    }
    /** The nearest marker hidden beyond each end this frame, as its colour and strength; drawn
     * as one chevron per end. */
    private static final class Edges {
        private record Mark(double distance, int rgb, float alpha) {}
        private Mark left, right;
        void add(BarLayout.Placement place, double distance, int rgb, float alpha) {
            var mark = new Mark(distance, rgb, alpha);
            if (place.x() < 0) { if (left == null || distance < left.distance()) left = mark; }
            else if (right == null || distance < right.distance()) right = mark;
        }
        void draw(GuiGraphics g) {
            if (left != null) tinted(g, CHEVRON_LEFT, 1, 2, 6, 7, left.rgb(), left.alpha());
            if (right != null) tinted(g, CHEVRON_RIGHT, BAR_WIDTH - 7, 2, 6, 7, right.rgb(), right.alpha());
        }
    }
    /** effects: the sprite at the place, its white multiplied by the colour at the strength. */
    private static void tinted(GuiGraphics g, ResourceLocation sprite, int x, int y, int width, int height, int rgb, float alpha) {
        g.setColor(((rgb >> 16) & 0xff) / 255f, ((rgb >> 8) & 0xff) / 255f, (rgb & 0xff) / 255f, Math.max(0, Math.min(1, alpha)));
        g.blitSprite(sprite, x, y, width, height);
        g.setColor(1, 1, 1, 1);
    }
    private static ResourceLocation skin(Minecraft mc, UUID id) {
        var info = mc.getConnection() == null ? null : mc.getConnection().getPlayerInfo(id);
        return info == null ? DefaultPlayerSkin.get(id).texture() : info.getSkin().texture();
    }
}
