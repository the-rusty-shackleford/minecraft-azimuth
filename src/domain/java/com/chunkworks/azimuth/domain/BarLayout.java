/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.domain;

/** The bar's mapping from a bearing to a pixel: the view spans {@code halfAngle} either side of
 * the viewer's facing across {@code halfWidth} pixels either side of the centre; bearings beyond
 * the view sit at the nearer edge, marked as clamped, so a player is never lost off the bar.
 * Immutable. RI: halfWidth > 0; 0 < halfAngle <= 180. */
public record BarLayout(int halfWidth, float halfAngle) {
    /** A marker's offset from the bar's centre and whether it was pinned to an edge. */
    public record Placement(int x, boolean clamped) {}
    public BarLayout {
        if (halfWidth <= 0) throw new IllegalArgumentException("halfWidth");
        if (!(halfAngle > 0) || halfAngle > 180) throw new IllegalArgumentException("halfAngle");
    }
    /** requires: -180 < relativeYaw <= 180; effects: the placement for the bearing, rounded away
     * from zero so the left and the right of the bar mirror each other pixel for pixel. */
    public Placement place(float relativeYaw) {
        if (relativeYaw > halfAngle) return new Placement(halfWidth, true);
        if (relativeYaw < -halfAngle) return new Placement(-halfWidth, true);
        float exact = relativeYaw / halfAngle * halfWidth;
        return new Placement((int) Math.signum(exact) * Math.round(Math.abs(exact)), false);
    }
}
