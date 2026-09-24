/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.domain;

/** How much of a marker is drawn at a distance. Players never vanish: they ease from full to a
 * floor and stay there. Places are the other way round: nothing far away, easing in as the
 * viewer comes within range, full inside it. */
public final class Fade {
    private Fade() {}
    /** requires: 0 <= fadeStart <= fadeToMin, 0 <= minAlpha <= 1; effects: 1 up to fadeStart,
     * easing (exponent 1.65, the curve players are used to) to minAlpha at fadeToMin, minAlpha
     * beyond; when fadeStart equals fadeToMin the drop is a step. */
    public static float player(double distance, double fadeStart, double fadeToMin, float minAlpha) {
        if (fadeStart < 0 || fadeToMin < fadeStart || minAlpha < 0 || minAlpha > 1) throw new IllegalArgumentException("fade");
        if (distance <= fadeStart) return 1;
        if (distance >= fadeToMin) return minAlpha;
        double t = (distance - fadeStart) / (fadeToMin - fadeStart);
        return (float) (1 - (1 - minAlpha) * Math.pow(t, 1.65));
    }
    /** requires: fadeToMin >= 0; effects: whether a player this far is past the fade, where the
     * bar stops drawing their head and marks them as a far dot instead: at fadeToMin and beyond,
     * the same distance from which {@link #player} answers the floor. */
    public static boolean far(double distance, double fadeToMin) {
        if (fadeToMin < 0) throw new IllegalArgumentException("fade");
        return distance >= fadeToMin;
    }
    /** requires: range > 0, 0 <= band <= range; effects: 0 beyond range, rising in a straight
     * line over the last {@code band} blocks to 1 at range - band and anywhere nearer. */
    public static float location(double distance, double range, double band) {
        if (!(range > 0) || band < 0 || band > range) throw new IllegalArgumentException("range");
        if (distance >= range) return 0;
        if (band == 0 || distance <= range - band) return 1;
        return (float) ((range - distance) / band);
    }
}
