/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.api;

import java.util.regex.Pattern;

/** The validators the protocol's records share. */
public final class Identifiers {
    private static final Pattern NAMESPACED = Pattern.compile("[a-z0-9_.-]+:[a-z0-9_./-]+");
    private Identifiers() {}
    /** effects: the value when it is a namespaced identifier of at most 160 characters; throws
     * IllegalArgumentException otherwise. */
    public static String namespaced(String value) {
        if (value == null || value.length() > 160 || !NAMESPACED.matcher(value).matches()) throw new IllegalArgumentException("expected a namespaced identifier: " + value);
        return value;
    }
    /** effects: the value when it is non-blank, at most {@code limit} characters and free of control
     * and formatting codes; throws IllegalArgumentException otherwise. */
    public static String text(String value, int limit) {
        if (value == null || value.isBlank() || value.length() > limit || value.chars().anyMatch(c -> Character.isISOControl(c) || c == '§'))
            throw new IllegalArgumentException("invalid text");
        return value;
    }
    /** effects: the coordinate when finite and within the world's envelope; throws otherwise. */
    public static double coordinate(double value) {
        if (!Double.isFinite(value) || Math.abs(value) > 30_000_000) throw new IllegalArgumentException("coordinate out of the world");
        return value;
    }
}
