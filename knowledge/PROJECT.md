# Azimuth

Version 1.0.0, built 2026-09-24, unreleased. Minecraft 1.21.1, NeoForge 21.1.248, Java 21, both
sides. Public at github.com/the-rusty-shackleford/minecraft-azimuth.

Rusty on 2026-09-24, after asking for atlas icons on the pack's Locator Bar and hearing what that
mod is inside: "create our own version of the locator bar (call it Azimuth) that clones the same
functionality for players and adds an integration with the atlas. Better yet, I bet we could
make some kind of AzimuthLocation protocol and then have the atlas implement it." Their calls:
players, directions, coordinates, compass and death points, no day counter; atlas places only
while an atlas is carried (the atlas's rule, D-0002); bought villages, the viewer's C.A.M.P. and
own landmarks; the bar at the top, shifted below boss bars ("Chunky").

## Shape

- `src/domain` (JDK-only, 21 JUnit tests): the protocol `api` (`AzimuthLocation`,
  `AzimuthViewer`, `AzimuthProvider`, `AzimuthProviders`, `Identifiers`) and the bar's arithmetic
  (`Bearing`, `BarLayout`, `Fade`, `Roster`, `BarPosition`).
- `src/main`: `Azimuth` (entry), `AzimuthConfig` (SERVER cadence, caps, fades, range; CLIENT
  look), `Payloads` (`Players`, `Locations`, optional channel "1"; the fade rules ride along),
  `Cycle` (the server clock: players by the Locator Bar's rules, places from every provider,
  failures isolated and logged once), `Bearings` (the client's last payloads with their age),
  `provider/CompassBearings` and `provider/DeathBearing` (the built-in providers),
  `client/AzimuthHud` (the layer above the boss overlay; `BossEventProgress` tells it how low to
  sit) and `client/ClientSetup`.
- `src/gametest`: `Mocks` (survival mock players joined as the framework joins its own; test
  providers), three GameTests, the booth (`AzimuthBooth`: a Surveyor peer, a booth provider's
  bell, camp, chest and out-of-range pickaxe, a real lodestone and its compass, a boss bar, dots).

## Gotchas met

- Gradle's `new File("run/...")` inside a task action resolves against the daemon's working
  directory, which is whichever repo started the daemon; resolve with `file()` at configuration
  time (Warehouse Manager's copy of the pattern works by luck).
- The gametest server's default game mode is creative and a joined mock inherits it; set
  survival after `placeNewPlayer`.
- A lodestone compass forgets a lodestone that is not there on its first inventory tick, and
  the game registers a placed block's point of interest on the *next* server tick (`ServerLevel.
  onBlockStateChange` defers it), so a compass handed over in the same tick as its lodestone
  forgets it. Place the lodestone, then point the compass a tick later. Found by a diagnostic
  step in the booth logging the tracker, the block and the POI, not by guessing.
- NeoForge's "current server" hook is not how the built-in providers reach a player; the cycle
  records the server it ticks for and the providers ask it.

## Status

Built 2026-09-24; JUnit, GameTests and the booth green (see
[release verification](../devtools/verification/release-1.0.0.md)). **Not released**: Rusty's go
is the release, together with Magical Map 0.3.0 (which implements the protocol) as pack 1.62.0,
where the Locator Bar's override jar leaves the pack.
