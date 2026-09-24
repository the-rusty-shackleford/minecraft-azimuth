/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth;

import net.neoforged.neoforge.common.ModConfigSpec;

/** The server's rules for what is sent and how it fades, and the client's choices for how the
 * bar looks. The fade rules travel with every payload, so a client needs no copy of the server
 * file. */
public final class AzimuthConfig {
    private AzimuthConfig() {}
    public static final ModConfigSpec SERVER, CLIENT;
    public static final ModConfigSpec.IntValue PLAYER_CADENCE, PLAYER_MAX, LOCATION_CADENCE, LOCATION_MAX;
    public static final ModConfigSpec.DoubleValue PLAYER_FADE_START, PLAYER_FADE_TO_MIN, PLAYER_MIN_ALPHA, LOCATION_RANGE, LOCATION_FADE;
    public static final ModConfigSpec.BooleanValue ENABLED, SHOW_DIRECTIONS, SHOW_COORDINATES, HEADS, BELOW_BOSS_BARS;
    public static final ModConfigSpec.DoubleValue SCALE, VIEW_ANGLE;
    public static final ModConfigSpec.IntValue OFFSET_X, OFFSET_Y;
    static {
        var server = new ModConfigSpec.Builder();
        server.push("players");
        PLAYER_CADENCE = server.comment("How often, in ticks, each player is sent where the others are.", "Default: 10").defineInRange("cadence_ticks", 10, 1, 200);
        PLAYER_MAX = server.comment("The most players shown on a bar, nearest first.", "Default: 16").defineInRange("max", 16, 0, 64);
        PLAYER_FADE_START = server.comment("Players nearer than this, in blocks, are drawn in full.", "Default: 50").defineInRange("fade_start", 50.0, 0.0, 100000.0);
        PLAYER_FADE_TO_MIN = server.comment("Players this far, in blocks, and beyond are drawn at the faded opacity.", "Default: 125").defineInRange("fade_to_min", 125.0, 0.0, 100000.0);
        PLAYER_MIN_ALPHA = server.comment("The opacity a far player fades to; they never vanish.", "Default: 0.4").defineInRange("min_alpha", 0.4, 0.0, 1.0);
        server.pop();
        server.push("locations");
        LOCATION_CADENCE = server.comment("How often, in ticks, the places providers know are gathered and sent.", "Default: 20").defineInRange("cadence_ticks", 20, 1, 400);
        LOCATION_MAX = server.comment("The most places shown on a bar, nearest first.", "Default: 16").defineInRange("max", 16, 0, 64);
        LOCATION_RANGE = server.comment("Places farther than this, in blocks, are not shown.", "Default: 256").defineInRange("range", 256.0, 1.0, 100000.0);
        LOCATION_FADE = server.comment("Places fade in over this many blocks inside the range.", "Default: 64").defineInRange("fade", 64.0, 0.0, 100000.0);
        server.pop();
        SERVER = server.build();

        var client = new ModConfigSpec.Builder();
        client.push("bar");
        ENABLED = client.comment("Show the bar.", "Default: true").define("enabled", true);
        SCALE = client.comment("The bar's size.", "Default: 1.0").defineInRange("scale", 1.0, 0.5, 3.0);
        VIEW_ANGLE = client.comment("How many degrees of the horizon the bar spans; what lies outside sits at its ends.", "Default: 90").defineInRange("view_angle", 90.0, 30.0, 360.0);
        OFFSET_X = client.comment("Moves the bar sideways from the centre, in pixels.", "Default: 0").defineInRange("offset_x", 0, -2000, 2000);
        OFFSET_Y = client.comment("Moves the bar down from the top, in pixels.", "Default: 0").defineInRange("offset_y", 0, 0, 2000);
        SHOW_DIRECTIONS = client.comment("Mark north, east, south and west on the bar.", "Default: true").define("show_directions", true);
        SHOW_COORDINATES = client.comment("Write your coordinates under the bar.", "Default: true").define("show_coordinates", true);
        HEADS = client.comment("Draw other players as their heads; off draws dots.", "Default: true").define("heads", true);
        BELOW_BOSS_BARS = client.comment("Move the bar down below any boss bar showing, such as a pregeneration's progress.", "Default: true").define("below_boss_bars", true);
        client.pop();
        CLIENT = client.build();
    }
}
