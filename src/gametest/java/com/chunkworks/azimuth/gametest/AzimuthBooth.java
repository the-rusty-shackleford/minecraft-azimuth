/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.gametest;

import com.chunkworks.azimuth.AzimuthConfig;
import com.chunkworks.azimuth.Bearings;
import com.chunkworks.azimuth.Payloads;
import com.chunkworks.azimuth.api.AzimuthLocation;
import com.chunkworks.azimuth.api.AzimuthProviders;
import com.chunkworks.azimuth.client.AzimuthHud;
import net.minecraft.client.Minecraft;
import net.minecraft.client.Screenshot;
import net.minecraft.client.gui.screens.PauseScreen;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.server.level.ServerLevel;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.GameRules;
import net.minecraft.world.level.GameType;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.api.distmarker.Dist;
import net.neoforged.bus.api.SubscribeEvent;
import net.neoforged.fml.common.EventBusSubscriber;
import net.neoforged.neoforge.client.event.ClientTickEvent;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import java.util.List;
import java.util.function.Consumer;

/** Hardware-client gate: the booth player stands on a stone pad facing south with a second player
 * to the south-east (a framed head), a third far to the south-west (a head at the fade's floor)
 * and a fourth behind (a chevron), a booth provider's places around them (a bell ahead within range, a
 * campfire at the edge of range fading in, a chest behind as a chevron at the bar's end, a
 * pickaxe out of range) and a lodestone compass in the inventory; then a boss bar, then the same
 * boss bar hidden by another mod, then dots instead of heads. Photographed each time. Screenshots need a human eye; this fixture never
 * ships. */
@EventBusSubscriber(modid = "azimuth_gametest", value = Dist.CLIENT)
public final class AzimuthBooth {
    private static final Logger LOG = LoggerFactory.getLogger("Azimuth booth");
    private static final double X = 0.5, Z = 0.5;
    private static int tick;
    private static ServerPlayer peer, rover, tracker;
    @SubscribeEvent public static void tick(ClientTickEvent.Post event) {
        if (!Boolean.getBoolean("azimuth.booth")) return;
        var mc = Minecraft.getInstance();
        if (mc.player == null || mc.level == null) return;
        if (mc.screen instanceof PauseScreen) mc.setScreen(null);
        mc.getToasts().clear(); mc.gui.getChat().clearMessages(true);
        try {
            switch (++tick) {
                case 20 -> server(mc, p -> {
                    var l = p.serverLevel();
                    l.getGameRules().getRule(GameRules.RULE_DOMOBSPAWNING).set(false, l.getServer());
                    l.setDayTime(6000); l.setWeatherParameters(6000, 0, false, false);
                    int y = l.getHeight(net.minecraft.world.level.levelgen.Heightmap.Types.MOTION_BLOCKING, 0, 0);
                    pad(l, y - 1);
                    p.setGameMode(GameType.SURVIVAL); p.getInventory().clearContent();
                    p.teleportTo(l, X, y, Z, 0, 0);
                    peer = Mocks.player(p.server, l, "Surveyor", X + 30, y, Z + 52, 180);
                    peer.setNoGravity(true);
                    // 200 blocks off, past the fade's floor at 125: a head at the floor, right of centre.
                    rover = Mocks.player(p.server, l, "Rover", X - 120, y, Z + 160, 180);
                    rover.setNoGravity(true);
                    // 70 blocks behind (the Surveyor is 60 off): outside the view, a chevron at the bar's end.
                    tracker = Mocks.player(p.server, l, "Tracker", X, y, Z - 70, 0);
                    tracker.setNoGravity(true);
                    AzimuthProviders.register(Mocks.provider("azimuth_gametest:booth", v -> List.of(
                            place(v, "bell", "Plains Village", 70, 70, "minecraft:bell", 0xe5b85b),
                            // Off centre by 40, so the S badge and the notch dead ahead show.
                            place(v, "camp", "Your C.A.M.P.", 40, 236, "minecraft:campfire", 0x86b68a),
                            place(v, "chest", "Iron mine", 0, -100, "minecraft:chest", 0xc0a060),
                            place(v, "pick", "Too far", -300, 0, "minecraft:iron_pickaxe", 0xffffff))));
                    // A lodestone compass forgets a lodestone that is not there once the inventory
                    // ticks, so the target is a real lodestone block, placed now and pointed at a
                    // tick later: the game registers a block's point of interest on the next tick.
                    l.setBlock(new BlockPos(-40, y - 1, 20), Blocks.LODESTONE.defaultBlockState(), 3);
                });
                case 25 -> server(mc, p -> {
                    var compass = new ItemStack(Items.COMPASS);
                    compass.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(java.util.Optional.of(GlobalPos.of(Level.OVERWORLD, new BlockPos(-40, p.getBlockY() - 1, 20))), true));
                    p.getInventory().setItem(3, compass);
                });
                case 90 -> {
                    var players = Bearings.players().orElseThrow(() -> new IllegalStateException("no players payload arrived"));
                    var heads = players.entries().stream().map(Payloads.Players.Entry::id).toList();
                    check(heads.equals(List.of(peer.getUUID(), tracker.getUUID(), rover.getUUID())), "the Surveyor, the Tracker and the Rover are on the bar, nearest first: " + players);
                    var places = Bearings.locations().orElseThrow(() -> new IllegalStateException("no places payload arrived"));
                    var ids = places.entries().stream().map(e -> e.provider() + "/" + e.id()).toList();
                    check(ids.contains("azimuth_gametest:booth/bell") && ids.contains("azimuth_gametest:booth/camp") && ids.contains("azimuth_gametest:booth/chest") && ids.contains("azimuth:compass/slot/3"),
                            "the bell, the camp, the chest and the compass are on the bar: " + ids);
                    check(!ids.contains("azimuth_gametest:booth/pick"), "the pickaxe 300 blocks out is not: " + ids);
                    check(AzimuthHud.lastBossBarsBottom() == 0 && AzimuthHud.lastTop() == 4, "no boss bar, the bar sits at the top: " + AzimuthHud.lastTop());
                    photo(mc, "00-bar-heads-and-places");
                }
                case 100 -> server(mc, p -> {
                    var server = p.server;
                    for (var command : new String[] { "bossbar add azimuth:booth \"Pregeneration 42%\"", "bossbar set azimuth:booth players @a", "bossbar set azimuth:booth max 100", "bossbar set azimuth:booth value 42", "bossbar set azimuth:booth color yellow" })
                        server.getCommands().performPrefixedCommand(server.createCommandSourceStack().withSuppressedOutput(), command);
                });
                case 140 -> {
                    check(AzimuthHud.lastBossBarsBottom() > 0 && AzimuthHud.lastTop() >= AzimuthHud.lastBossBarsBottom() + 3, "the bar moved below the boss bar: top " + AzimuthHud.lastTop() + " boss bottom " + AzimuthHud.lastBossBarsBottom());
                    photo(mc, "01-below-boss-bar");
                    // Another mod hides the boss bar (Block Factory's Bosses does this for every
                    // tracked warden): the bar must come back up while the boss event still exists.
                    BossBarHider.hiding = true;
                }
                case 160 -> {
                    check(AzimuthHud.lastBossBarsBottom() == 0 && AzimuthHud.lastTop() == 4, "a boss bar another mod hides does not push the bar down: top " + AzimuthHud.lastTop() + " boss bottom " + AzimuthHud.lastBossBarsBottom());
                    photo(mc, "02-boss-bar-hidden-by-another-mod");
                    BossBarHider.hiding = false;
                    server(mc, p -> p.server.getCommands().performPrefixedCommand(p.server.createCommandSourceStack().withSuppressedOutput(), "bossbar remove azimuth:booth"));
                }
                case 175 -> { AzimuthConfig.HEADS.set(false); }
                case 185 -> { check(AzimuthHud.lastTop() == 4, "the bar is at the top with the boss bar gone"); photo(mc, "03-dots"); AzimuthConfig.HEADS.set(true); }
                case 200 -> { LOG.info("azimuth booth: COMPLETE"); mc.stop(); }
            }
        } catch (Throwable failure) { LOG.error("azimuth booth: FAIL", failure); mc.stop(); }
    }
    private static AzimuthLocation place(com.chunkworks.azimuth.api.AzimuthViewer v, String id, String name, double dx, double dz, String icon, int color) {
        return new AzimuthLocation("azimuth_gametest:booth", id, name, v.dimension(), X + dx, v.y(), Z + dz, icon, color);
    }
    /** effects: a 7x7 stone pad under the spot, so the photos have a floor. */
    private static void pad(ServerLevel l, int y) {
        for (int x = -3; x <= 3; x++) for (int z = -3; z <= 3; z++) l.setBlock(new BlockPos(x, y, z), Blocks.STONE.defaultBlockState(), 3);
    }
    private static void server(Minecraft mc, Consumer<ServerPlayer> action) {
        var server = mc.getSingleplayerServer(); var id = mc.player.getUUID();
        server.execute(() -> { try { action.accept(server.getPlayerList().getPlayer(id)); } catch (Throwable failure) { LOG.error("azimuth booth: FAIL", failure); mc.execute(mc::stop); } });
    }
    private static void photo(Minecraft mc, String name) {
        mc.getToasts().clear();
        Screenshot.grab(mc.gameDirectory, "azimuth-" + name + ".png", mc.getMainRenderTarget(), m -> LOG.info("azimuth booth: {}", m.getString()));
    }
    private static void check(boolean ok, String message) { if (!ok) throw new IllegalStateException(message); LOG.info("azimuth booth: PASS {}", message); }
}
