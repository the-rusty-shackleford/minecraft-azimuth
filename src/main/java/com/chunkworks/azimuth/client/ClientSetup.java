/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.client;

import com.chunkworks.azimuth.Azimuth;
import com.chunkworks.azimuth.Bearings;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.EventPriority;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.fml.event.lifecycle.FMLClientSetupEvent;
import net.neoforged.neoforge.client.event.ClientPlayerNetworkEvent;
import net.neoforged.neoforge.client.event.CustomizeGuiOverlayEvent;
import net.neoforged.neoforge.client.event.RegisterGuiLayersEvent;
import net.neoforged.neoforge.client.gui.VanillaGuiLayers;
import net.neoforged.neoforge.common.NeoForge;

/** The client's registrations: the bar as a HUD layer above the boss overlay, the boss-bar
 * listener that tells it how low to sit, and forgetting the last server's word on leaving. */
@EventBusSubscriber(modid = Azimuth.ID, value = Dist.CLIENT)
public final class ClientSetup {
    private ClientSetup() {}
    @SubscribeEvent public static void layers(RegisterGuiLayersEvent event) { event.registerAbove(VanillaGuiLayers.BOSS_OVERLAY, Azimuth.id("bar"), AzimuthHud::draw); }
    @SubscribeEvent public static void setup(FMLClientSetupEvent event) {
        // Last, and told about cancelled bars too, so the bar sits below what vanilla actually
        // draws: a mod that hides or restyles a boss bar cancels this event (Block Factory's
        // Bosses does, at LOWEST, for every warden a player tracks), and counting it pushed the
        // bar down under nothing (D-0004).
        NeoForge.EVENT_BUS.addListener(EventPriority.LOWEST, true, CustomizeGuiOverlayEvent.BossEventProgress.class, AzimuthHud::onBossBar);
        NeoForge.EVENT_BUS.addListener((ClientPlayerNetworkEvent.LoggingOut e) -> Bearings.clear());
    }
}
