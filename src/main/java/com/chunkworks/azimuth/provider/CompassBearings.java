/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.provider;

import com.chunkworks.azimuth.Cycle;
import com.chunkworks.azimuth.api.AzimuthLocation;
import com.chunkworks.azimuth.api.AzimuthProvider;
import com.chunkworks.azimuth.api.AzimuthViewer;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import java.util.ArrayList;
import java.util.List;

/** Every lodestone compass the viewer carries points at its lodestone: a compass in the
 * inventory or the offhand whose tracker targets the viewer's dimension puts that lodestone on
 * the bar. The colour tells compasses apart by slot. */
public final class CompassBearings implements AzimuthProvider {
    public static final String ID = "azimuth:compass";
    private static final int[] COLORS = { 0xd94f4f, 0xe0a13d, 0xe6d84a, 0x63c060, 0x4fb7d9, 0x5f6fe0, 0xb066d6, 0xe07bb5 };
    @Override public String id() { return ID; }
    @Override public List<AzimuthLocation> bearings(AzimuthViewer viewer, double range) {
        var player = player(viewer);
        if (player == null) return List.of();
        var out = new ArrayList<AzimuthLocation>();
        var inventory = player.getInventory();
        for (int slot = 0; slot < inventory.getContainerSize(); slot++) consider(viewer, inventory.getItem(slot), slot, out);
        return List.copyOf(out);
    }
    private static void consider(AzimuthViewer viewer, ItemStack stack, int slot, List<AzimuthLocation> out) {
        if (!stack.is(Items.COMPASS)) return;
        var tracker = stack.get(DataComponents.LODESTONE_TRACKER);
        if (tracker == null || tracker.target().isEmpty()) return;
        var target = tracker.target().get();
        if (!target.dimension().location().toString().equals(viewer.dimension())) return;
        var pos = target.pos();
        out.add(new AzimuthLocation(ID, "slot/" + slot, "Lodestone", viewer.dimension(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, "minecraft:compass", COLORS[slot % COLORS.length]));
    }
    /** effects: the viewer as a server player, or null when no server runs or they are not online. */
    static ServerPlayer player(AzimuthViewer viewer) {
        var server = Cycle.server();
        return server == null ? null : server.getPlayerList().getPlayer(viewer.player());
    }
}
