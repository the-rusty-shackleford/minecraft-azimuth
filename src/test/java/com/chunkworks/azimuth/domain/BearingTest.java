/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.domain;

import org.junit.jupiter.api.Test;
import static org.junit.jupiter.api.Assertions.*;

/** Partitions: the target ahead, behind, left and right of a viewer facing south; a viewer facing
 * another way; a target on the viewer's own spot; angles that wrap past 180 either way; the
 * compass directions; the record's invariants. */
final class BearingTest {
    @Test void aroundAViewerFacingSouth() {
        assertEquals(0f, Bearing.of(0, 0, 0, 0, 10).relativeYaw(), 1e-4, "south is ahead");
        assertEquals(180f, Bearing.of(0, 0, 0, 0, -10).relativeYaw(), 1e-4, "north is behind");
        assertEquals(-90f, Bearing.of(0, 0, 0, 10, 0).relativeYaw(), 1e-4, "east is to the left");
        assertEquals(90f, Bearing.of(0, 0, 0, -10, 0).relativeYaw(), 1e-4, "west is to the right");
        assertEquals(-45f, Bearing.of(0, 0, 0, 10, 10).relativeYaw(), 1e-4);
        assertEquals(10.0, Bearing.of(0, 0, 0, 0, 10).distance(), 1e-9);
    }
    @Test void aViewerFacingWestSeesNorthOnTheRight() {
        assertEquals(90f, Bearing.of(5, 5, 90, 5, -5).relativeYaw(), 1e-4);
        assertEquals(0f, Bearing.of(5, 5, 90, -5, 5).relativeYaw(), 1e-4);
    }
    @Test void ownSpotIsAheadAtNoDistance() {
        assertEquals(new Bearing(0, 0), Bearing.of(3, 4, 137, 3, 4));
    }
    @Test void wrapping() {
        assertEquals(-170f, Bearing.wrap(190), 1e-4);
        assertEquals(170f, Bearing.wrap(-190), 1e-4);
        assertEquals(180f, Bearing.wrap(180), 1e-4);
        assertEquals(180f, Bearing.wrap(-180), 1e-4);
        assertEquals(0f, Bearing.wrap(720), 1e-4);
        assertEquals(175f, Bearing.of(0, 0, -175, 0, 10).relativeYaw(), 1e-4, "south lies 175 degrees to the right of a viewer at yaw -175");
        assertEquals(-175f, Bearing.of(0, 0, 175, 0, 10).relativeYaw(), 1e-4, "and to the left of one at yaw 175");
    }
    @Test void compassDirections() {
        assertEquals(180f, Bearing.toward(180, 0), 1e-4, "north is behind a viewer facing south");
        assertEquals(-90f, Bearing.toward(-90, 0), 1e-4, "east is left of a viewer facing south");
        assertEquals(0f, Bearing.toward(180, 180), 1e-4);
    }
    @Test void invariants() {
        assertThrows(IllegalArgumentException.class, () -> new Bearing(-180, 1));
        assertThrows(IllegalArgumentException.class, () -> new Bearing(181, 1));
        assertThrows(IllegalArgumentException.class, () -> new Bearing(0, -1));
    }
}
