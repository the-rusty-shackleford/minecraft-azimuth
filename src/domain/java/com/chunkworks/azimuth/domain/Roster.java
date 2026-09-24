/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.domain;

import java.util.*;
import java.util.function.Function;
import java.util.function.ToDoubleFunction;

/** Which markers make the bar when there are more than it shows: the nearest, one per identity. */
public final class Roster {
    private Roster() {}
    /** requires: cap >= 0; effects: at most {@code cap} of the items, nearest first (stable for
     * equal distances), the first of any two sharing a key kept. */
    public static <T> List<T> select(Collection<T> items, Function<T, ?> key, ToDoubleFunction<T> distance, int cap) {
        if (cap < 0) throw new IllegalArgumentException("cap");
        var seen = new HashSet<Object>();
        var unique = new ArrayList<T>();
        for (var item : items) if (seen.add(key.apply(item))) unique.add(item);
        unique.sort(Comparator.comparingDouble(distance));
        return List.copyOf(unique.subList(0, Math.min(cap, unique.size())));
    }
}
