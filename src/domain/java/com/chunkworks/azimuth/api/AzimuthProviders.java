/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.api;

import java.util.List;
import java.util.concurrent.CopyOnWriteArrayList;

/** The registered providers, in registration order. Azimuth registers its own (compass and
 * death points) first; other mods register from their constructors. */
public final class AzimuthProviders {
    private static final List<AzimuthProvider> PROVIDERS = new CopyOnWriteArrayList<>();
    private AzimuthProviders() {}
    /** effects: adds the provider last; throws IllegalArgumentException on an id that is not
     * namespaced and IllegalStateException on a second provider with the same id. */
    public static void register(AzimuthProvider provider) {
        var id = Identifiers.namespaced(provider.id());
        for (var p : PROVIDERS) if (p.id().equals(id)) throw new IllegalStateException("an Azimuth provider named " + id + " is already registered");
        PROVIDERS.add(provider);
    }
    /** effects: every registered provider, in order. */
    public static List<AzimuthProvider> all() { return List.copyOf(PROVIDERS); }
    /** effects: forgets the provider with that id, if any; for tests. */
    public static void unregister(String id) { PROVIDERS.removeIf(p -> p.id().equals(id)); }
}
