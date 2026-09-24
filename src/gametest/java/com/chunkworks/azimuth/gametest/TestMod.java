/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.gametest;

import net.neoforged.fml.common.Mod;
import net.neoforged.fml.loading.FMLEnvironment;

@Mod("azimuth_gametest")
public final class TestMod {
    /** effects: on a client, registers the booth's boss-bar hider now, at construction, the way
     * the mods it stands in for register theirs: before Azimuth's client setup. */
    public TestMod() { if (FMLEnvironment.dist.isClient()) BossBarHider.register(); }
}
