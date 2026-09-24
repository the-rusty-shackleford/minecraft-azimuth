/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.gametest;

import com.chunkworks.azimuth.api.AzimuthLocation;
import com.chunkworks.azimuth.api.AzimuthProvider;
import com.chunkworks.azimuth.api.AzimuthViewer;
import com.mojang.authlib.GameProfile;
import io.netty.channel.embedded.EmbeddedChannel;
import net.minecraft.network.Connection;
import net.minecraft.network.protocol.PacketFlow;
import net.minecraft.server.MinecraftServer;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.server.network.CommonListenerCookie;
import net.minecraft.world.level.GameType;
import java.util.List;
import java.util.UUID;
import java.util.function.Function;

/** Players and providers for the tests and the booth. */
public final class Mocks {
    private Mocks() {}
    /** effects: a survival player named {@code name} joined to the server over an embedded
     * connection, the way the framework's own mock joins, standing at the position. The gametest
     * server's default game mode is creative, hence the explicit survival. */
    public static ServerPlayer player(MinecraftServer server, ServerLevel level, String name, double x, double y, double z, float yaw) {
        var cookie = CommonListenerCookie.createInitial(new GameProfile(UUID.nameUUIDFromBytes(name.getBytes()), name), false);
        var player = new ServerPlayer(server, level, cookie.gameProfile(), cookie.clientInformation()) {
            @Override public boolean isSpectator() { return false; }
        };
        var connection = new Connection(PacketFlow.SERVERBOUND);
        new EmbeddedChannel(connection);
        server.getPlayerList().placeNewPlayer(connection, player, cookie);
        player.setGameMode(GameType.SURVIVAL);
        player.getInventory().clearContent();
        player.moveTo(x, y, z, yaw, 0);
        return player;
    }
    /** effects: a provider with the id that answers with what {@code places} makes of the viewer. */
    public static AzimuthProvider provider(String id, Function<AzimuthViewer, List<AzimuthLocation>> places) {
        return new AzimuthProvider() {
            @Override public String id() { return id; }
            @Override public List<AzimuthLocation> bearings(AzimuthViewer viewer, double range) { return places.apply(viewer); }
        };
    }
    /** effects: a place in the viewer's dimension at the offset from them. */
    public static AzimuthLocation near(AzimuthViewer viewer, String provider, String id, String name, double dx, double dz, String icon, int color) {
        return new AzimuthLocation(provider, id, name, viewer.dimension(), viewer.x() + dx, viewer.y(), viewer.z() + dz, icon, color);
    }
}
