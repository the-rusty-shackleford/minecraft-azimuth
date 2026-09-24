/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth;

import com.chunkworks.azimuth.api.AzimuthLocation;
import com.chunkworks.azimuth.api.AzimuthProviders;
import com.chunkworks.azimuth.api.AzimuthViewer;
import com.chunkworks.azimuth.domain.Roster;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.Items;
import net.neoforged.neoforge.event.tick.ServerTickEvent;
import net.neoforged.neoforge.network.PacketDistributor;
import java.util.*;

/** The server's clock: every few ticks each player is told where the others in their dimension
 * are, and every few more which places the providers want on their bar. The rules for players
 * are the ones players know from the bar this replaces: nobody crouching, nobody in spectator,
 * nobody invisible, nobody whose head is under a pumpkin or a skull; nearest first, capped. */
public final class Cycle {
    private static final Set<Item> HEAD_HIDERS = Set.of(Items.CARVED_PUMPKIN, Items.SKELETON_SKULL, Items.WITHER_SKELETON_SKULL, Items.ZOMBIE_HEAD,
            Items.CREEPER_HEAD, Items.DRAGON_HEAD, Items.PIGLIN_HEAD, Items.PLAYER_HEAD);
    /** Providers that threw this session, so each failure is logged once until it recovers. */
    private static final Set<String> FAILED = new HashSet<>();
    /** The server whose clock this is; null between servers. */
    private static volatile MinecraftServer server;
    private Cycle() {}

    /** effects: the running server, or null when none is; how the built-in providers reach the
     * viewer's player from the id the protocol hands them. */
    public static MinecraftServer server() { return server; }

    static void tick(ServerTickEvent.Post event) {
        var server = event.getServer();
        Cycle.server = server;
        long tick = server.getTickCount();
        boolean players = tick % AzimuthConfig.PLAYER_CADENCE.get() == 0, locations = tick % AzimuthConfig.LOCATION_CADENCE.get() == 0;
        if (!players && !locations) return;
        var all = server.getPlayerList().getPlayers();
        for (var viewer : all) {
            if (players && viewer.connection.hasChannel(Payloads.Players.TYPE)) PacketDistributor.sendToPlayer(viewer, playersFor(viewer, all));
            if (locations && viewer.connection.hasChannel(Payloads.Locations.TYPE)) PacketDistributor.sendToPlayer(viewer, locationsFor(viewer));
        }
    }
    /** effects: the players payload for the viewer under the server's settings. */
    public static Payloads.Players playersFor(ServerPlayer viewer, List<ServerPlayer> all) {
        var entries = new ArrayList<Payloads.Players.Entry>();
        for (var other : all) {
            if (other == viewer || other.level() != viewer.level() || hidden(other)) continue;
            entries.add(new Payloads.Players.Entry(other.getUUID(), other.getX(), other.getZ()));
        }
        var chosen = Roster.select(entries, Payloads.Players.Entry::id, e -> viewer.distanceToSqr(e.x(), viewer.getY(), e.z()), AzimuthConfig.PLAYER_MAX.get());
        return new Payloads.Players(AzimuthConfig.PLAYER_FADE_START.get().floatValue(), AzimuthConfig.PLAYER_FADE_TO_MIN.get().floatValue(),
                AzimuthConfig.PLAYER_MIN_ALPHA.get().floatValue(), chosen);
    }
    /** effects: whether the player is kept off everyone's bar right now. */
    static boolean hidden(ServerPlayer player) {
        return player.isCrouching() || player.isSpectator() || player.isInvisible() || HEAD_HIDERS.contains(player.getItemBySlot(EquipmentSlot.HEAD).getItem());
    }
    /** effects: the places payload for the viewer: every provider asked, the failing ones logged
     * once and skipped, the answers culled to the viewer's dimension and range, nearest first,
     * capped. */
    public static Payloads.Locations locationsFor(ServerPlayer viewer) {
        double range = AzimuthConfig.LOCATION_RANGE.get();
        var who = new AzimuthViewer(viewer.getUUID(), viewer.level().dimension().location().toString(), viewer.getX(), viewer.getY(), viewer.getZ());
        var found = new ArrayList<AzimuthLocation>();
        for (var provider : AzimuthProviders.all()) {
            try {
                for (var place : provider.bearings(who, range + 32)) {
                    if (!place.dimension().equals(who.dimension()) || place.distanceTo(who.x(), who.z()) > range) continue;
                    found.add(place);
                }
                if (FAILED.remove(provider.id())) Azimuth.LOGGER.info("Azimuth provider {} answers again", provider.id());
            } catch (RuntimeException failure) {
                if (FAILED.add(provider.id())) Azimuth.LOGGER.error("Azimuth provider {} failed; skipped until it answers again", provider.id(), failure);
            }
        }
        var chosen = Roster.select(found, AzimuthLocation::key, p -> p.distanceTo(who.x(), who.z()), AzimuthConfig.LOCATION_MAX.get());
        var entries = new ArrayList<Payloads.Locations.Entry>(chosen.size());
        for (var p : chosen) entries.add(new Payloads.Locations.Entry(p.provider(), p.id(), p.name(), p.x(), p.y(), p.z(), p.icon(), p.color()));
        return new Payloads.Locations((float) range, AzimuthConfig.LOCATION_FADE.get().floatValue(), entries);
    }
    /** effects: the ids of the providers currently skipped for throwing. */
    public static Set<String> failedProviders() { return Set.copyOf(FAILED); }
    /** effects: forgets the server and which providers failed; on server stop. */
    static void reset(MinecraftServer stopped) { FAILED.clear(); if (server == stopped) server = null; }
}
