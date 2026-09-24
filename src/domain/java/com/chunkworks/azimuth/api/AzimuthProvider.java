/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.api;

import java.util.List;

/** A source of places for the bar. Register one with {@link AzimuthProviders#register} from a mod
 * constructor. Azimuth asks every provider on the server thread once per cycle per viewer, keeps
 * only the places in the viewer's dimension within range, caps them nearest first, and sends them
 * to that viewer's client; the client fades each by its distance. Players are not a provider's
 * business: the bar shows them on its own.
 * <p>Spec for implementations: answer with an immutable list of at most a few dozen places, all in
 * the viewer's dimension and within {@code range} blocks of the viewer where that is cheap to
 * know; never load or generate chunks, never mutate the world, and leave out anything the viewer
 * may not see. A provider that throws is logged once and skipped until it answers again. */
public interface AzimuthProvider {
    /** effects: this provider's namespaced id, the first half of every key it issues. */
    String id();
    /** effects: the places this provider wants on the viewer's bar. */
    List<AzimuthLocation> bearings(AzimuthViewer viewer, double range);
}
