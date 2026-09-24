/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth;

import java.util.Optional;

/** The client's copy of the last payloads, each with the moment it arrived, so a bar never draws
 * what a server that went quiet last said. Free of client classes so the payload handlers can
 * live in common code; the HUD reads it. */
public final class Bearings {
    /** How long a payload is trusted: three cycles of the slowest default cadence. */
    static final long MAX_AGE_MILLIS = 3000;
    private static Payloads.Players players;
    private static Payloads.Locations locations;
    private static long playersAt, locationsAt;
    private Bearings() {}
    public static synchronized void players(Payloads.Players payload) { players = payload; playersAt = System.currentTimeMillis(); }
    public static synchronized void locations(Payloads.Locations payload) { locations = payload; locationsAt = System.currentTimeMillis(); }
    /** effects: the last players payload while it is fresh. */
    public static synchronized Optional<Payloads.Players> players() {
        return players != null && System.currentTimeMillis() - playersAt <= MAX_AGE_MILLIS ? Optional.of(players) : Optional.empty();
    }
    /** effects: the last places payload while it is fresh. */
    public static synchronized Optional<Payloads.Locations> locations() {
        return locations != null && System.currentTimeMillis() - locationsAt <= MAX_AGE_MILLIS ? Optional.of(locations) : Optional.empty();
    }
    /** effects: forgets everything; on leaving a server. */
    public static synchronized void clear() { players = null; locations = null; }
}
