/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Partitions: a bearing at the centre, inside the view either side, exactly at each edge, beyond
 * each edge (clamped), and the full half turn; the record's invariants. */
final class BarLayoutTest {
    private static final BarLayout BAR = new BarLayout(51, 45);
    @Test void insideTheView() {
        assertEquals(new BarLayout.Placement(0, false), BAR.place(0));
        assertEquals(new BarLayout.Placement(26, false), BAR.place(22.5f));
        assertEquals(new BarLayout.Placement(-26, false), BAR.place(-22.5f), "the left mirrors the right at a half pixel");
        assertEquals(new BarLayout.Placement(51, false), BAR.place(45));
        assertEquals(new BarLayout.Placement(-51, false), BAR.place(-45));
    }
    @Test void beyondTheViewPinsToTheEdge() {
        assertEquals(new BarLayout.Placement(51, true), BAR.place(46));
        assertEquals(new BarLayout.Placement(-51, true), BAR.place(-100));
        assertEquals(new BarLayout.Placement(51, true), BAR.place(180));
    }
    @Test void invariants() {
        assertThrows(IllegalArgumentException.class, () -> new BarLayout(0, 45));
        assertThrows(IllegalArgumentException.class, () -> new BarLayout(51, 0));
        assertThrows(IllegalArgumentException.class, () -> new BarLayout(51, 181));
    }
}
