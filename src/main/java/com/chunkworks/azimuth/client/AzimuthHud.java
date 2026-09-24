/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.client;

import com.chunkworks.azimuth.AzimuthConfig;
import com.chunkworks.azimuth.Bearings;
import com.chunkworks.azimuth.Payloads;
import com.chunkworks.azimuth.domain.BarLayout;
import com.chunkworks.azimuth.domain.BarPosition;
import com.chunkworks.azimuth.domain.Bearing;
import com.chunkworks.azimuth.domain.Fade;
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
 * marker goes and how faint it is comes from the domain; this only paints. */
public final class AzimuthHud {
    static final int BAR_WIDTH = 102, BAR_HEIGHT = 10, HALF_WIDTH = 51, MARKER = 8;
    private static final int BAR = 0xA0101010, BAR_EDGE = 0xC0404040, TICK = 0xC0FFFFFF, TEXT = 0xFFE8E8E8, DIRECTION = 0xFFB0C4DE;
    private static final float[] DIRECTION_YAWS = { 180f, -90f, 0f, 90f };
    private static final String[] DIRECTION_NAMES = { "N", "E", "S", "W" };
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
        var layout = new BarLayout(HALF_WIDTH, AzimuthConfig.VIEW_ANGLE.get().floatValue() / 2f);
        var player = mc.player;
        float yaw = player.getYRot();

        g.pose().pushPose();
        g.pose().translate(left, top, 0);
        g.pose().scale(scale, scale, 1);
        g.fill(0, 0, BAR_WIDTH, BAR_HEIGHT, BAR);
        g.fill(0, 0, 1, BAR_HEIGHT, BAR_EDGE);
        g.fill(BAR_WIDTH - 1, 0, BAR_WIDTH, BAR_HEIGHT, BAR_EDGE);
        g.fill(HALF_WIDTH, 0, HALF_WIDTH + 1, 2, TICK);
        g.fill(HALF_WIDTH, BAR_HEIGHT - 2, HALF_WIDTH + 1, BAR_HEIGHT, TICK);
        if (AzimuthConfig.SHOW_DIRECTIONS.get()) for (int i = 0; i < 4; i++) {
            var place = layout.place(Bearing.toward(DIRECTION_YAWS[i], yaw));
            if (place.clamped()) continue;
            g.drawCenteredString(mc.font, DIRECTION_NAMES[i], HALF_WIDTH + place.x(), 1, DIRECTION);
        }
        Bearings.locations().ifPresent(payload -> drawLocations(g, mc, payload, layout, player.getX(), player.getZ(), yaw));
        Bearings.players().ifPresent(payload -> drawPlayers(g, mc, payload, layout, player.getX(), player.getZ(), yaw));
        if (AzimuthConfig.SHOW_COORDINATES.get()) {
            var text = player.getBlockX() + " " + player.getBlockY() + " " + player.getBlockZ();
            g.drawCenteredString(mc.font, text, HALF_WIDTH, BAR_HEIGHT + 2, TEXT);
        }
        g.pose().popPose();
    }

    /** effects: the other players' heads (or dots), the tracked entity's live position winning
     * over the server's last word, faded by distance, pinned to the ends when behind. */
    private static void drawPlayers(GuiGraphics g, Minecraft mc, Payloads.Players payload, BarLayout layout, double x, double z, float yaw) {
        var live = new HashMap<UUID, double[]>();
        for (var other : mc.level.players()) if (other != mc.player) live.put(other.getUUID(), new double[] { other.getX(), other.getZ() });
        for (var entry : payload.entries()) {
            var at = live.getOrDefault(entry.id(), new double[] { entry.x(), entry.z() });
            var bearing = Bearing.of(x, z, yaw, at[0], at[1]);
            float alpha = Fade.player(bearing.distance(), payload.fadeStart(), payload.fadeToMin(), payload.minAlpha());
            var place = layout.place(bearing.relativeYaw());
            if (place.clamped()) alpha *= 0.5f;
            int left = HALF_WIDTH + place.x() - MARKER / 2, top = (BAR_HEIGHT - MARKER) / 2;
            g.setColor(1, 1, 1, alpha);
            if (AzimuthConfig.HEADS.get()) PlayerFaceRenderer.draw(g, skin(mc, entry.id()), left, top, MARKER);
            else g.fill(left + 2, top + 2, left + MARKER - 2, top + MARKER - 2, withAlpha(0xF3DFA1, alpha));
            g.setColor(1, 1, 1, 1);
        }
    }
    /** effects: the places: a tinted dot under the item's sprite, fading in at the edge of range,
     * pinned to the ends when behind. */
    private static void drawLocations(GuiGraphics g, Minecraft mc, Payloads.Locations payload, BarLayout layout, double x, double z, float yaw) {
        for (var entry : payload.entries()) {
            var bearing = Bearing.of(x, z, yaw, entry.x(), entry.z());
            float alpha = Fade.location(bearing.distance(), payload.range(), payload.fade());
            if (alpha <= 0) continue;
            var place = layout.place(bearing.relativeYaw());
            if (place.clamped()) alpha *= 0.5f;
            int centre = HALF_WIDTH + place.x(), left = centre - MARKER / 2, top = (BAR_HEIGHT - MARKER) / 2;
            g.fill(left - 1, top - 1, left + MARKER + 1, top + MARKER + 1, withAlpha(entry.color(), alpha * 0.7f));
            var item = BuiltInRegistries.ITEM.get(ResourceLocation.tryParse(entry.icon()));
            if (item != Items.AIR) {
                var sprite = mc.getItemRenderer().getModel(new ItemStack(item), null, null, 0).getParticleIcon(net.neoforged.neoforge.client.model.data.ModelData.EMPTY);
                g.setColor(1, 1, 1, alpha);
                g.blit(left, top, 0, MARKER, MARKER, sprite);
                g.setColor(1, 1, 1, 1);
            }
        }
    }
    private static ResourceLocation skin(Minecraft mc, UUID id) {
        var info = mc.getConnection() == null ? null : mc.getConnection().getPlayerInfo(id);
        return info == null ? DefaultPlayerSkin.get(id).texture() : info.getSkin().texture();
    }
    private static int withAlpha(int rgb, float alpha) { return (Math.round(Math.max(0, Math.min(1, alpha)) * 255) << 24) | (rgb & 0xffffff); }
}
