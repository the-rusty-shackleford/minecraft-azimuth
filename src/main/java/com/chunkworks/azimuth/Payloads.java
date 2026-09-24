/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth;

import net.minecraft.network.RegistryFriendlyByteBuf;
import net.minecraft.network.codec.StreamCodec;
import net.minecraft.network.protocol.common.custom.CustomPacketPayload;
import net.neoforged.neoforge.network.event.RegisterPayloadHandlersEvent;
import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

/** What the server tells a client: where the other players are, and which places its providers
 * want on the bar, each with the fade rule to draw them by. The channel is optional, so a client
 * without Azimuth still joins and simply has no bar. */
public final class Payloads {
    private Payloads() {}
    /** The other players in the viewer's dimension, nearest first, and how to fade them. */
    public record Players(float fadeStart, float fadeToMin, float minAlpha, List<Entry> entries) implements CustomPacketPayload {
        public record Entry(UUID id, double x, double z) {}
        public static final Type<Players> TYPE = new Type<>(Azimuth.id("players"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Players> CODEC = StreamCodec.of((buf, p) -> {
            buf.writeFloat(p.fadeStart()); buf.writeFloat(p.fadeToMin()); buf.writeFloat(p.minAlpha());
            buf.writeVarInt(p.entries().size());
            for (var e : p.entries()) { buf.writeUUID(e.id()); buf.writeDouble(e.x()); buf.writeDouble(e.z()); }
        }, buf -> {
            float start = buf.readFloat(), toMin = buf.readFloat(), min = buf.readFloat();
            int n = buf.readVarInt();
            var entries = new ArrayList<Entry>(n);
            for (int i = 0; i < n; i++) entries.add(new Entry(buf.readUUID(), buf.readDouble(), buf.readDouble()));
            return new Players(start, toMin, min, entries);
        });
        public Players { entries = List.copyOf(entries); }
        @Override public Type<Players> type() { return TYPE; }
    }
    /** The places within range in the viewer's dimension, nearest first, and how to fade them. */
    public record Locations(float range, float fade, List<Entry> entries) implements CustomPacketPayload {
        public record Entry(String provider, String id, String name, double x, double y, double z, String icon, int color) {}
        public static final Type<Locations> TYPE = new Type<>(Azimuth.id("locations"));
        public static final StreamCodec<RegistryFriendlyByteBuf, Locations> CODEC = StreamCodec.of((buf, p) -> {
            buf.writeFloat(p.range()); buf.writeFloat(p.fade());
            buf.writeVarInt(p.entries().size());
            for (var e : p.entries()) {
                buf.writeUtf(e.provider()); buf.writeUtf(e.id()); buf.writeUtf(e.name());
                buf.writeDouble(e.x()); buf.writeDouble(e.y()); buf.writeDouble(e.z());
                buf.writeUtf(e.icon()); buf.writeInt(e.color());
            }
        }, buf -> {
            float range = buf.readFloat(), fade = buf.readFloat();
            int n = buf.readVarInt();
            var entries = new ArrayList<Entry>(n);
            for (int i = 0; i < n; i++) entries.add(new Entry(buf.readUtf(), buf.readUtf(), buf.readUtf(), buf.readDouble(), buf.readDouble(), buf.readDouble(), buf.readUtf(), buf.readInt()));
            return new Locations(range, fade, entries);
        });
        public Locations { entries = List.copyOf(entries); }
        @Override public Type<Locations> type() { return TYPE; }
    }
    /** effects: registers both payloads on an optional channel; the client keeps each as it arrives. */
    static void register(RegisterPayloadHandlersEvent event) {
        var registrar = event.registrar("1").optional();
        registrar.playToClient(Players.TYPE, Players.CODEC, (p, ctx) -> Bearings.players(p));
        registrar.playToClient(Locations.TYPE, Locations.CODEC, (p, ctx) -> Bearings.locations(p));
    }
}
