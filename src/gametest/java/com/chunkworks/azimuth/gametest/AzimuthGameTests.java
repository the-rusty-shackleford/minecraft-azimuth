/* Copyright (C) 2026 Rusty Shackleford and nfx. SPDX-License-Identifier: AGPL-3.0-or-later */
package com.chunkworks.azimuth.gametest;

import com.chunkworks.azimuth.AzimuthConfig;
import com.chunkworks.azimuth.Cycle;
import com.chunkworks.azimuth.api.AzimuthLocation;
import com.chunkworks.azimuth.api.AzimuthProviders;
import com.chunkworks.azimuth.api.AzimuthViewer;
import com.chunkworks.azimuth.provider.CompassBearings;
import com.chunkworks.azimuth.provider.DeathBearing;
import net.minecraft.core.BlockPos;
import net.minecraft.core.GlobalPos;
import net.minecraft.core.component.DataComponents;
import net.minecraft.gametest.framework.GameTest;
import net.minecraft.gametest.framework.GameTestHelper;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.entity.EquipmentSlot;
import net.minecraft.world.entity.Pose;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.Items;
import net.minecraft.world.item.component.LodestoneTracker;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.block.Blocks;
import net.neoforged.neoforge.gametest.GameTestHolder;
import net.neoforged.neoforge.gametest.PrefixGameTestTemplate;
import java.util.List;
import java.util.Optional;

/** Real-server partitions: the players payload lists the others in the viewer's dimension
 * nearest first and leaves out the viewer, a crouching player, one under a pumpkin and one in
 * another dimension, and honours the cap; the places payload asks every provider, keeps only
 * what lies in the viewer's dimension within range, caps nearest first, and skips a throwing
 * provider after logging it once while the others keep answering; the compass provider reads
 * lodestone compasses in the inventory for this dimension only, and the death provider needs a
 * recovery compass and a death in this dimension. */
@GameTestHolder("azimuth") @PrefixGameTestTemplate(false)
public final class AzimuthGameTests {
    private static AzimuthViewer viewer(ServerPlayer p) {
        return new AzimuthViewer(p.getUUID(), p.level().dimension().location().toString(), p.getX(), p.getY(), p.getZ());
    }
    private static ServerPlayer at(GameTestHelper h, String name, int dx, int dz) {
        var pos = h.absolutePos(new BlockPos(32 + dx, 1, 32 + dz));
        return Mocks.player(h.getLevel().getServer(), h.getLevel(), name, pos.getX() + 0.5, pos.getY(), pos.getZ() + 0.5, 0);
    }
    private static void leave(ServerPlayer... players) { for (var p : players) p.server.getPlayerList().remove(p); }

    @GameTest(template = "arena", timeoutTicks = 200) public void playersPayloadFollowsTheLocatorBarRules(GameTestHelper h) {
        var viewer = at(h, "viewer", 0, 0);
        var near = at(h, "near", 0, 10);
        var far = at(h, "far", 0, 30);
        var crouching = at(h, "crouching", 5, 0);
        crouching.setShiftKeyDown(true);
        crouching.setPose(Pose.CROUCHING);
        var masked = at(h, "masked", -5, 0);
        masked.setItemSlot(EquipmentSlot.HEAD, new ItemStack(Items.CARVED_PUMPKIN));
        var elsewhere = at(h, "elsewhere", 3, 3);
        var nether = h.getLevel().getServer().getLevel(Level.NETHER);
        h.assertTrue(nether != null, "the server has a Nether");
        elsewhere.teleportTo(nether, 0.5, 64, 0.5, 0, 0);
        h.runAfterDelay(2, () -> {
            var all = h.getLevel().getServer().getPlayerList().getPlayers();
            var payload = Cycle.playersFor(viewer, all);
            var ids = payload.entries().stream().map(e -> e.id()).toList();
            h.assertTrue(ids.equals(List.of(near.getUUID(), far.getUUID())), "the two visible players nearest first, nobody else: " + ids);
            h.assertTrue(payload.fadeStart() == 50f && payload.fadeToMin() == 125f && payload.minAlpha() == 0.4f, "the fade rule travels with the payload");
            int max = AzimuthConfig.PLAYER_MAX.get();
            AzimuthConfig.PLAYER_MAX.set(1);
            try {
                h.assertTrue(Cycle.playersFor(viewer, all).entries().size() == 1 && Cycle.playersFor(viewer, all).entries().get(0).id().equals(near.getUUID()), "the cap keeps the nearest");
            } finally { AzimuthConfig.PLAYER_MAX.set(max); }
            leave(viewer, near, far, crouching, masked, elsewhere);
            h.succeed();
        });
    }
    @GameTest(template = "arena", timeoutTicks = 200) public void providersAreAskedCulledCappedAndIsolated(GameTestHelper h) {
        var viewer = at(h, "viewer2", 0, 0);
        AzimuthProviders.register(Mocks.provider("azimuth_gametest:places", v -> List.of(
                Mocks.near(v, "azimuth_gametest:places", "far", "Far", 0, 100, "minecraft:bell", 0x112233),
                Mocks.near(v, "azimuth_gametest:places", "beyond", "Beyond", 0, 400, "minecraft:bell", 0x112233),
                new AzimuthLocation("azimuth_gametest:places", "nether", "Nether", "minecraft:the_nether", v.x(), v.y(), v.z(), "minecraft:bell", 0x112233),
                Mocks.near(v, "azimuth_gametest:places", "close", "Close", 30, 0, "minecraft:campfire", 0x445566))));
        AzimuthProviders.register(Mocks.provider("azimuth_gametest:broken", v -> { throw new IllegalStateException("broken on purpose"); }));
        try {
            var payload = Cycle.locationsFor(viewer);
            var ids = payload.entries().stream().map(e -> e.id()).toList();
            h.assertTrue(ids.equals(List.of("close", "far")), "in this dimension, within range, nearest first: " + ids);
            h.assertTrue(payload.range() == 256f && payload.fade() == 64f, "the fade rule travels with the payload");
            h.assertTrue(Cycle.failedProviders().equals(java.util.Set.of("azimuth_gametest:broken")), "the throwing provider is skipped: " + Cycle.failedProviders());
            h.assertTrue(Cycle.locationsFor(viewer).entries().size() == 2, "the others keep answering after the failure");
            int max = AzimuthConfig.LOCATION_MAX.get();
            AzimuthConfig.LOCATION_MAX.set(1);
            try {
                h.assertTrue(Cycle.locationsFor(viewer).entries().get(0).id().equals("close"), "the cap keeps the nearest");
            } finally { AzimuthConfig.LOCATION_MAX.set(max); }
        } finally {
            AzimuthProviders.unregister("azimuth_gametest:places");
            AzimuthProviders.unregister("azimuth_gametest:broken");
            leave(viewer);
        }
        h.succeed();
    }
    @GameTest(template = "arena", timeoutTicks = 200) public void compassAndDeathBearingsReadTheInventory(GameTestHelper h) {
        var player = at(h, "navigator", 0, 0);
        // A lodestone compass forgets a lodestone that is not there once the inventory ticks, and
        // the game registers a block's point of interest a tick after the block: place now, point later.
        h.setBlock(new BlockPos(10, 1, 20), Blocks.LODESTONE);
        var here = h.absolutePos(new BlockPos(10, 1, 20));
        h.runAfterDelay(2, () -> {
            var compass = new ItemStack(Items.COMPASS);
            compass.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(GlobalPos.of(Level.OVERWORLD, here)), true));
            var netherCompass = new ItemStack(Items.COMPASS);
            netherCompass.set(DataComponents.LODESTONE_TRACKER, new LodestoneTracker(Optional.of(GlobalPos.of(Level.NETHER, here)), true));
            player.getInventory().setItem(3, compass);
            player.getInventory().setItem(4, netherCompass);
            player.getInventory().setItem(5, new ItemStack(Items.COMPASS));
        });
        h.runAfterDelay(6, () -> {
            h.assertTrue(player.getInventory().getItem(3).get(DataComponents.LODESTONE_TRACKER).target().isPresent(), "the compass kept its lodestone through the inventory ticks");
            var bearings = new CompassBearings().bearings(viewer(player), 256);
            h.assertTrue(bearings.size() == 1 && bearings.get(0).id().equals("slot/3") && bearings.get(0).x() == here.getX() + 0.5 && bearings.get(0).z() == here.getZ() + 0.5,
                    "one lodestone in this dimension, from slot 3: " + bearings);
            var grave = h.absolutePos(new BlockPos(40, 1, 40));
            player.setLastDeathLocation(Optional.of(GlobalPos.of(Level.OVERWORLD, grave)));
            h.assertTrue(new DeathBearing().bearings(viewer(player), 256).isEmpty(), "no recovery compass, no death point");
            player.getInventory().setItem(6, new ItemStack(Items.RECOVERY_COMPASS));
            var death = new DeathBearing().bearings(viewer(player), 256);
            h.assertTrue(death.size() == 1 && death.get(0).x() == grave.getX() + 0.5 && death.get(0).icon().equals("minecraft:recovery_compass"), "the death point with a recovery compass: " + death);
            player.setLastDeathLocation(Optional.of(GlobalPos.of(Level.NETHER, grave)));
            h.assertTrue(new DeathBearing().bearings(viewer(player), 256).isEmpty(), "a death in another dimension is not shown");
            leave(player);
            h.succeed();
        });
    }
}
