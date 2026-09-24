/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.domain;

import org.junit.jupiter.api.Test;
import java.util.List;
import static org.junit.jupiter.api.Assertions.*;

/** Partitions: fewer items than the cap; more (the nearest kept, in order); duplicates by key
 * (the first kept); equal distances keep their order; a cap of zero; a negative cap. */
final class RosterTest {
    private record Item(String key, double distance) {}
    @Test void nearestFirstUpToTheCap() {
        var items = List.of(new Item("c", 30), new Item("a", 10), new Item("b", 20), new Item("d", 40));
        assertEquals(List.of(new Item("a", 10), new Item("b", 20), new Item("c", 30)), Roster.select(items, Item::key, Item::distance, 3));
        assertEquals(4, Roster.select(items, Item::key, Item::distance, 10).size());
        assertEquals(List.of(), Roster.select(items, Item::key, Item::distance, 0));
    }
    @Test void duplicatesAndTies() {
        var items = List.of(new Item("a", 30), new Item("a", 10), new Item("b", 30), new Item("c", 30));
        assertEquals(List.of(new Item("a", 30), new Item("b", 30), new Item("c", 30)), Roster.select(items, Item::key, Item::distance, 5), "the first of a key is kept and ties keep their order");
    }
    @Test void invariants() {
        assertThrows(IllegalArgumentException.class, () -> Roster.select(List.<Item>of(), Item::key, Item::distance, -1));
        assertThrows(UnsupportedOperationException.class, () -> Roster.select(List.of(new Item("a", 1)), Item::key, Item::distance, 1).clear());
    }
}
