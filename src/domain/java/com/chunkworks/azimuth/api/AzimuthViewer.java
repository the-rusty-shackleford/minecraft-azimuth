/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.api;

import java.util.Objects;
import java.util.UUID;

/** Who is asking for bearings, and where they stand. Immutable, JDK-only. Azimuth tells a
 * provider nothing else about the viewer: what a viewer carries or may see is the provider's own
 * business to check, on the server, by the player's id.
 * <p>RI: player non-null, dimension a namespaced identifier, coordinates finite and in the world. */
public record AzimuthViewer(UUID player, String dimension, double x, double y, double z) {
    public AzimuthViewer {
        Objects.requireNonNull(player, "player");
        dimension = Identifiers.namespaced(dimension);
        x = Identifiers.coordinate(x);
        y = Identifiers.coordinate(y);
        z = Identifiers.coordinate(z);
    }
}
