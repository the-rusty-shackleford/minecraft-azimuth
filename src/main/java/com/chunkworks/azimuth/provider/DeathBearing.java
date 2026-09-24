/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.provider;

import com.chunkworks.azimuth.api.AzimuthLocation;
import com.chunkworks.azimuth.api.AzimuthProvider;
import com.chunkworks.azimuth.api.AzimuthViewer;
import net.minecraft.world.item.Items;
import java.util.List;

/** Where the viewer last died, while they carry a recovery compass and died in this dimension:
 * the compass is what points there, so the bar shows it under the same condition. */
public final class DeathBearing implements AzimuthProvider {
    public static final String ID = "azimuth:death";
    @Override public String id() { return ID; }
    @Override public List<AzimuthLocation> bearings(AzimuthViewer viewer, double range) {
        var player = CompassBearings.player(viewer);
        if (player == null || player.getLastDeathLocation().isEmpty()) return List.of();
        if (!player.getInventory().contains(new net.minecraft.world.item.ItemStack(Items.RECOVERY_COMPASS))) return List.of();
        var death = player.getLastDeathLocation().get();
        if (!death.dimension().location().toString().equals(viewer.dimension())) return List.of();
        var pos = death.pos();
        return List.of(new AzimuthLocation(ID, "last", "Where you died", viewer.dimension(), pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, "minecraft:recovery_compass", 0x7fb2e5));
    }
}
