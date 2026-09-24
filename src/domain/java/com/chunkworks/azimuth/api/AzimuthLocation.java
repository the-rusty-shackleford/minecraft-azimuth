/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.api;

/** A place the bar can point at. Immutable, JDK-only.
 * <p>AF: {@code (provider, id)} is the place's identity, stable while it exists; {@code name} is
 * what a player is told it is; {@code dimension} and {@code x, y, z} are where it is; {@code icon}
 * is the item whose sprite marks it and {@code color} the RGB tint of the dot beneath the sprite.
 * <p>RI: provider, dimension and icon are namespaced identifiers; id is 1..160 characters; name is
 * 1..64 characters with no control or formatting codes; the coordinates are finite and within the
 * world; color has no bits above the low 24. */
public record AzimuthLocation(String provider, String id, String name, String dimension, double x, double y, double z, String icon, int color) {
    public AzimuthLocation {
        provider = Identifiers.namespaced(provider);
        id = Identifiers.text(id, 160);
        name = Identifiers.text(name, 64);
        dimension = Identifiers.namespaced(dimension);
        icon = Identifiers.namespaced(icon);
        x = Identifiers.coordinate(x);
        y = Identifiers.coordinate(y);
        z = Identifiers.coordinate(z);
        if ((color & 0xff000000) != 0) throw new IllegalArgumentException("color must be RGB");
    }
    /** A place's identity across providers. */
    public record Key(String provider, String id) {
        public Key { Identifiers.namespaced(provider); Identifiers.text(id, 160); }
    }
    public Key key() { return new Key(provider, id); }
    /** effects: the horizontal distance from the point to this place. */
    public double distanceTo(double fromX, double fromZ) {
        double dx = x - fromX, dz = z - fromZ;
        return Math.sqrt(dx * dx + dz * dz);
    }
}
