/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.domain;

/** Where a target lies relative to a viewer: how far round from the viewer's facing, and how far
 * away. Immutable.
 * <p>AF: {@code relativeYaw} is the turn from the viewer's facing to the target, in degrees,
 * negative to the left and positive to the right, in (-180, 180]; {@code distance} is horizontal.
 * <p>RI: -180 < relativeYaw <= 180; distance >= 0. The game's yaw convention: 0 faces south
 * (+z), 90 faces west (-x), so a target's own yaw is atan2(-dx, dz). */
public record Bearing(float relativeYaw, double distance) {
    public Bearing {
        if (relativeYaw <= -180 || relativeYaw > 180 || Float.isNaN(relativeYaw)) throw new IllegalArgumentException("relativeYaw");
        if (distance < 0 || Double.isNaN(distance)) throw new IllegalArgumentException("distance");
    }
    /** effects: the bearing of the target from a viewer standing at (viewerX, viewerZ) facing
     * {@code viewerYaw}; a target at the viewer's own spot is dead ahead at no distance. */
    public static Bearing of(double viewerX, double viewerZ, float viewerYaw, double targetX, double targetZ) {
        double dx = targetX - viewerX, dz = targetZ - viewerZ;
        double distance = Math.sqrt(dx * dx + dz * dz);
        if (distance < 1e-6) return new Bearing(0, 0);
        double targetYaw = Math.toDegrees(Math.atan2(-dx, dz));
        return new Bearing(wrap(targetYaw - viewerYaw), distance);
    }
    /** effects: the bearing of a compass direction given as the yaw that faces it (north 180,
     * east -90, south 0, west 90) from a viewer facing {@code viewerYaw}. */
    public static float toward(float directionYaw, float viewerYaw) { return wrap(directionYaw - viewerYaw); }
    /** effects: the angle brought into (-180, 180]. */
    public static float wrap(double degrees) {
        double d = degrees % 360;
        if (d > 180) d -= 360;
        if (d <= -180) d += 360;
        return (float) d;
    }
}
