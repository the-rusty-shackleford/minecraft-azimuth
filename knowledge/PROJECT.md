# Azimuth

Version 1.0.2, built 2026-09-24. Minecraft 1.21.1, NeoForge 21.1.248, Java 21, both sides.
Public at github.com/the-rusty-shackleford/minecraft-azimuth.

1.0.2 (the same evening): Rusty, in play: "The bar should still show players at any distance,
and fade in as you get closer like the old bar (villages and other points of interest should
not follow this, they should stay the same)." The far dot of D-0003 read as the player being
gone. Players are framed heads at any distance again, fading by D-0001's curve to the floor
and back; `Fade.far` and the far sprite are gone; places unchanged (D-0005).

1.0.1 (the evening 1.0.0 went live): Rusty, in play: "The bar is way too low. I only want it
lowered if a chunky is running, and one is not running right now." No boss bar was visible.
Cause, pinned on the box: a warden 117 blocks below them was within tracking range, Block
Factory's Bosses gives every tracked warden a boss event and cancels vanilla's drawing of it at
the lowest priority, and Azimuth's listener at normal priority had already counted it. The bar
now sits below only the bars vanilla actually draws: the listener runs last, receives cancelled
events and ignores them (D-0004). The booth's stand-in for such a mod is registered at mod
construction at LOWEST like the real one, and the new check failed on 1.0.0's registration
before passing on the fix. Released as tag v1.0.1 and deployed as pack 1.62.1 the same evening
on Rusty's "Yes do it, no warning this time"; not yet seen by Rusty next to that warden.

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

## The look (restyled before the first release, D-0003)

Rusty judged the first build's bar "plain and shitty, a visual downgrade" from the Locator Bar
it replaces, and approved a side-by-side mockup: an outlined rounded dark bar with a top
highlight and a notch at the heading, coloured N/E/S/W badges with white letters (red, yellow,
blue, green, as players know them), heads in a 1 px black frame, an outlined coloured dot under
each item sprite, chevrons at the ends for what lies outside the view, small outlined dots for
far players. Every sprite is ours, drawn by `devtools/art/sprites.py` into
`assets/azimuth/textures/gui/sprites/` and stitched into the game's GUI atlas
(`GuiGraphics.blitSprite`); the tinted shapes are white with a black outline so one sprite
serves every colour. Markers are drawn farthest first, so where they overlap the nearest is on
top (Magical Map's booth showed a village underfoot hidden under a boat 92 blocks off). Still
1.0.0: nothing had shipped. `devtools/verification/1.0.0/` holds the booth photos judged at 4x
against the mockup's proposed row.

## Gotchas met

- HUD layers must bracket themselves in `RenderSystem.enableBlend()` / `disableBlend()` as every
  vanilla layer does: the boss overlay before us turns blending off at its end, and
  `PlayerFaceRenderer.draw` turns it off again after the hat layer. Without both, every sprite
  after a head drew at full strength whatever its alpha. Found by measuring the booth photo's
  pixels against the computed fade (the far dot read pure cream), not by eye.
- A 5 px outlined disc with its fill corners cut is a plus sign; the fill's corners are cut only
  from 7 px up.

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
[release verification](../devtools/verification/release-1.0.0.md)). **Released 2026-09-24**
(tag v1.0.0) on Rusty's "Release and deploy", together with Magical Map 0.3.0 (which
implements the protocol), as pack 1.62.0, where the Locator Bar's override jar left the pack;
deployed the same hour with nobody on, 20 TPS. Not yet seen by Rusty in play.
