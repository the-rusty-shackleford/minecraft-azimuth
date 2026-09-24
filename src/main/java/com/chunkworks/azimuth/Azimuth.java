/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth;

import com.chunkworks.azimuth.api.AzimuthProviders;
import com.chunkworks.azimuth.provider.CompassBearings;
import com.chunkworks.azimuth.provider.DeathBearing;
import net.minecraft.resources.ResourceLocation;
import net.neoforged.bus.api.IEventBus;
import net.neoforged.fml.ModContainer;
import net.neoforged.fml.common.Mod;
import net.neoforged.fml.config.ModConfig;
import net.neoforged.neoforge.common.NeoForge;
import net.neoforged.neoforge.event.server.ServerStoppedEvent;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

/** Azimuth: a bar across the top of the screen that shows which way the other players are and
 * any place a provider points it at. The server gathers ({@link Cycle}), the client draws
 * ({@code client.AzimuthHud}), and other mods contribute places through
 * {@link com.chunkworks.azimuth.api.AzimuthProvider the AzimuthLocation protocol}. */
@Mod(Azimuth.ID)
public final class Azimuth {
    public static final String ID = "azimuth";
    public static final Logger LOGGER = LoggerFactory.getLogger("Azimuth");
    public static ResourceLocation id(String path) { return ResourceLocation.fromNamespaceAndPath(ID, path); }

    /** requires: the mod bus and container; effects: registers config, payloads, the built-in
     * providers and the server clock. */
    public Azimuth(IEventBus bus, ModContainer container) {
        container.registerConfig(ModConfig.Type.SERVER, AzimuthConfig.SERVER);
        container.registerConfig(ModConfig.Type.CLIENT, AzimuthConfig.CLIENT);
        bus.addListener(Payloads::register);
        AzimuthProviders.register(new CompassBearings());
        AzimuthProviders.register(new DeathBearing());
        NeoForge.EVENT_BUS.addListener(Cycle::tick);
        NeoForge.EVENT_BUS.addListener((ServerStoppedEvent e) -> Cycle.reset(e.getServer()));
    }
}
