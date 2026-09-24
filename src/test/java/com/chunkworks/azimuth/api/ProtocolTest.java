/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.api;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.Test;
import java.util.List;
import java.util.UUID;
import static org.junit.jupiter.api.Assertions.*;

/** Partitions: a valid location and its key and distance; each invariant of a location (provider,
 * id, name, dimension, icon, each coordinate, color); a viewer and its invariants; the registry
 * refusing a bad id and a duplicate, keeping order, and forgetting. */
final class ProtocolTest {
    private static final AzimuthLocation BELL = new AzimuthLocation("magicalmap:atlas", "villagedeed:villages/structure:8", "Plains Village", "minecraft:overworld", 136, 64, 8, "minecraft:bell", 0xe5b85b);
    @AfterEach void forget() { AzimuthProviders.unregister("test:a"); AzimuthProviders.unregister("test:b"); }

    @Test void aLocationKnowsItsKeyAndDistance() {
        assertEquals(new AzimuthLocation.Key("magicalmap:atlas", "villagedeed:villages/structure:8"), BELL.key());
        assertEquals(5.0, BELL.distanceTo(133, 4), 1e-9);
    }
    @Test void locationInvariants() {
        assertThrows(IllegalArgumentException.class, () -> new AzimuthLocation("atlas", "x", "n", "minecraft:overworld", 0, 0, 0, "minecraft:bell", 0));
        assertThrows(IllegalArgumentException.class, () -> new AzimuthLocation("a:b", "", "n", "minecraft:overworld", 0, 0, 0, "minecraft:bell", 0));
        assertThrows(IllegalArgumentException.class, () -> new AzimuthLocation("a:b", "x", "§cred", "minecraft:overworld", 0, 0, 0, "minecraft:bell", 0));
        assertThrows(IllegalArgumentException.class, () -> new AzimuthLocation("a:b", "x", "n", "overworld", 0, 0, 0, "minecraft:bell", 0));
        assertThrows(IllegalArgumentException.class, () -> new AzimuthLocation("a:b", "x", "n", "minecraft:overworld", 0, 0, 0, "bell", 0));
        assertThrows(IllegalArgumentException.class, () -> new AzimuthLocation("a:b", "x", "n", "minecraft:overworld", Double.NaN, 0, 0, "minecraft:bell", 0));
        assertThrows(IllegalArgumentException.class, () -> new AzimuthLocation("a:b", "x", "n", "minecraft:overworld", 0, 0, 40_000_000, "minecraft:bell", 0));
        assertThrows(IllegalArgumentException.class, () -> new AzimuthLocation("a:b", "x", "n", "minecraft:overworld", 0, 0, 0, "minecraft:bell", 0xff000000));
        assertThrows(IllegalArgumentException.class, () -> new AzimuthLocation("a:b", "x", "a".repeat(65), "minecraft:overworld", 0, 0, 0, "minecraft:bell", 0));
    }
    @Test void viewer() {
        var id = UUID.randomUUID();
        assertEquals(id, new AzimuthViewer(id, "minecraft:overworld", 1, 2, 3).player());
        assertThrows(NullPointerException.class, () -> new AzimuthViewer(null, "minecraft:overworld", 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new AzimuthViewer(id, "nowhere", 0, 0, 0));
        assertThrows(IllegalArgumentException.class, () -> new AzimuthViewer(id, "minecraft:overworld", Double.POSITIVE_INFINITY, 0, 0));
    }
    @Test void registry() {
        AzimuthProvider a = provider("test:a"), b = provider("test:b");
        AzimuthProviders.register(a);
        AzimuthProviders.register(b);
        assertEquals(List.of(a, b), AzimuthProviders.all().subList(AzimuthProviders.all().size() - 2, AzimuthProviders.all().size()));
        assertThrows(IllegalStateException.class, () -> AzimuthProviders.register(provider("test:a")));
        assertThrows(IllegalArgumentException.class, () -> AzimuthProviders.register(provider("bad id")));
        AzimuthProviders.unregister("test:a");
        assertFalse(AzimuthProviders.all().contains(a));
    }
    private static AzimuthProvider provider(String id) {
        return new AzimuthProvider() {
            @Override public String id() { return id; }
            @Override public List<AzimuthLocation> bearings(AzimuthViewer viewer, double range) { return List.of(); }
        };
    }
}
