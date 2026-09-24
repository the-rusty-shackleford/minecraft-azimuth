/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.domain;

/** Where the bar sits on the screen: centred at the top, moved by the player's offsets, and
 * pushed down below whatever boss bars are showing so a pregeneration or a raid never hides it. */
public final class BarPosition {
    /** The bar's own margin from the top edge. */
    public static final int TOP = 4;
    /** The gap kept below the lowest boss bar. */
    public static final int GAP = 3;
    private BarPosition() {}
    /** requires: barWidth <= screenWidth; effects: the left edge that centres the bar, shifted. */
    public static int left(int screenWidth, int barWidth, int offsetX) {
        if (barWidth > screenWidth) throw new IllegalArgumentException("barWidth");
        return (screenWidth - barWidth) / 2 + offsetX;
    }
    /** effects: the top edge: the margin plus the offset, or the gap below the lowest boss bar
     * when that is lower ({@code bossBarsBottom} is 0 when no boss bar is showing). */
    public static int top(int offsetY, int bossBarsBottom) {
        int own = TOP + offsetY;
        return bossBarsBottom > 0 ? Math.max(own, bossBarsBottom + GAP) : own;
    }
}
