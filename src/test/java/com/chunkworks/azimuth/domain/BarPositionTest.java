/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Partitions: centred with and without an offset; no boss bar; a boss bar lower than the bar's
 * own place; a boss bar higher than an offset bar; a bar wider than the screen. */
final class BarPositionTest {
    @Test void centredLeft() {
        assertEquals(269, BarPosition.left(640, 102, 0));
        assertEquals(279, BarPosition.left(640, 102, 10));
        assertThrows(IllegalArgumentException.class, () -> BarPosition.left(100, 102, 0));
    }
    @Test void topFollowsTheBossBars() {
        assertEquals(4, BarPosition.top(0, 0));
        assertEquals(14, BarPosition.top(10, 0));
        assertEquals(31, BarPosition.top(0, 28), "below one boss bar at y 12 of height 16");
        assertEquals(50, BarPosition.top(46, 28), "an offset already lower than the boss bar stays");
    }
}
