/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.gametest;

import net.neoforged.bus.api.EventPriority;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.common.NeoForge;

/** Stands in for a mod that hides a boss bar the way Block Factory's Bosses does for every
 * tracked warden: a listener registered at mod construction, at LOWEST priority, that cancels
 * the bar's draw. It is registered before Azimuth's own listener, which is added at client
 * setup; a listener at NORMAL priority ran before this and counted the bar it was about to
 * hide, which is how 1.0.0 sat low under empty sky (D-0004). Client only. */
public final class BossBarHider {
    /** Whether the stand-in is hiding boss bars this frame. */
    static volatile boolean hiding;
    private BossBarHider() {}
    static void register() {
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, false, CustomizeGuiOverlayEvent.BossEventProgress.class, e -> { if (hiding) e.setCanceled(true); });
    }
}
