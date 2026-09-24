/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Partitions, players: nearer than the start, at the start, between (monotone, the curve's
 * midpoint), at the floor's distance, beyond it, a step when start equals the floor's distance;
 * places: inside the full zone, at the band's start, inside the band, at the range, beyond it,
 * no band; bad parameters. */
final class FadeTest {
    @Test void playersEaseToAFloorAndStayThere() {
        assertEquals(1f, Fade.player(10, 50, 125, 0.4f));
        assertEquals(1f, Fade.player(50, 50, 125, 0.4f));
        float mid = Fade.player(87.5, 50, 125, 0.4f);
        assertTrue(mid < 1 && mid > 0.4f, "between full and the floor: " + mid);
        assertEquals(1 - 0.6 * Math.pow(0.5, 1.65), mid, 1e-6);
        assertTrue(Fade.player(70, 50, 125, 0.4f) > Fade.player(100, 50, 125, 0.4f), "monotone");
        assertEquals(0.4f, Fade.player(125, 50, 125, 0.4f));
        assertEquals(0.4f, Fade.player(10_000, 50, 125, 0.4f));
        assertEquals(0.4f, Fade.player(51, 50, 50, 0.4f), "a step when the band is empty");
    }
    @Test void placesFadeInAtTheEdgeOfRange() {
        assertEquals(1f, Fade.location(10, 256, 64));
        assertEquals(1f, Fade.location(192, 256, 64));
        assertEquals(0.5f, Fade.location(224, 256, 64), 1e-6);
        assertEquals(0f, Fade.location(256, 256, 64));
        assertEquals(0f, Fade.location(300, 256, 64));
        assertEquals(1f, Fade.location(255, 256, 0), "no band: full until the range");
    }
    @Test void badParameters() {
        assertThrows(IllegalArgumentException.class, () -> Fade.player(1, 125, 50, 0.4f));
        assertThrows(IllegalArgumentException.class, () -> Fade.player(1, 50, 125, 1.5f));
        assertThrows(IllegalArgumentException.class, () -> Fade.location(1, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> Fade.location(1, 256, 300));
    }
}
