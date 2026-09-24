# Azimuth

A bar across the top of the screen for **Minecraft 1.21.1 / NeoForge 21.1.248** that shows which
way the other players are and how far, and any place a mod points it at: a lodestone compass in
your pockets, where you last died, what your atlas knows. Other mods contribute places through
the **AzimuthLocation protocol**. Both sides: the server gathers, the client draws.

## What the bar shows

- **Other players in your dimension**, at any distance, as their heads in a black frame (plain
  dots if you prefer), placed on the bar by the direction they lie in from where you face. They
  fade from full at 50 blocks to 40 % at 125 and stay there, brightening again as you close in;
  they never vanish. Nobody crouching, in spectator, invisible, or wearing a carved pumpkin or a
  skull is shown to anyone. At most 16, nearest first.
- **North, east, south and west**, as coloured badges where they lie: red N, yellow E, blue S,
  green W.
- **Your coordinates**, written under the bar.
- **Places**: the item that stands for the place over an outlined dot in the place's colour, shown
  within 256 blocks and fading in over the last 64. Built in: every lodestone compass you carry
  points at its lodestone (one colour per slot), and with a recovery compass in your pockets your
  last death in this dimension. With Magical Map, while you carry an atlas: bought villages, your
  C.A.M.P. and your own landmarks. At most 16, nearest first.
- Whatever lies beyond the bar's view (90° across by default) shows as a chevron at that end of
  the bar, in the colour of the nearest thing hidden that way, so nothing is lost behind you.
- A notch at the centre marks your own heading.
- When a boss bar is showing (a raid, the dragon, Chunky's pregeneration progress), the bar moves
  down below it and comes back up when it is gone. Only bars the game itself draws count: a bar
  another mod hides or restyles (Block Factory's Bosses does this for every warden you are near,
  Mowzie's Mobs for its bosses) leaves the bar at the top.

The bar is a HUD layer drawn after the boss overlay; nothing is drawn while the HUD is hidden (F1).
Its pixel art is the mod's own, under `assets/azimuth/textures/gui/sprites/`, drawn by
`devtools/art/sprites.py` (`uv run --no-project --with pillow python devtools/art/sprites.py`,
`--preview <png>` for a 6x mock of every sprite in place); the dots and chevrons are white with a
black outline and take a marker's colour when drawn.

## Config

Server, `serverconfig/azimuth-server.toml` in the world:

| Key | Default | |
|---|---|---|
| `players.cadence_ticks` | 10 | How often each player is told where the others are |
| `players.max` | 16 | Most players on a bar |
| `players.fade_start`, `players.fade_to_min`, `players.min_alpha` | 50, 125, 0.4 | Full up to the first, easing to the floor at the second |
| `locations.cadence_ticks` | 20 | How often places are gathered |
| `locations.max` | 16 | Most places on a bar |
| `locations.range`, `locations.fade` | 256, 64 | Shown within the range, fading in over the last band |

The fade rules travel with every payload, so a change on the server reaches every client on
the next cycle without a config of their own. Client, `config/azimuth-client.toml`:

| Key | Default | |
|---|---|---|
| `bar.enabled` | true | |
| `bar.scale` | 1.0 | The bar is 102 by 10 pixels at 1 |
| `bar.view_angle` | 90 | Degrees of horizon across the bar |
| `bar.offset_x`, `bar.offset_y` | 0, 0 | Sideways from the centre, down from the top |
| `bar.show_directions`, `bar.show_coordinates` | true | |
| `bar.heads` | true | Off draws a dot for every player instead of a head |
| `bar.below_boss_bars` | true | |

## The AzimuthLocation protocol

`com.chunkworks.azimuth.api`, JDK-only, in the jar: compile against
`com.chunkworks.azimuth:azimuth` and declare `azimuth` an optional (or required) dependency.

- `AzimuthLocation(provider, id, name, dimension, x, y, z, icon, color)`: immutable; `provider`,
  `dimension` and `icon` (an item id) are namespaced identifiers, `id` 1..160 characters, `name`
  1..64 plain characters, coordinates finite and in the world, `color` 24-bit RGB. `(provider, id)`
  is the place's identity.
- `AzimuthViewer(player, dimension, x, y, z)`: who is asking and where they stand. That is all
  Azimuth tells a provider; whether the viewer carries something or may see a place is the
  provider's own business to check on the server by the player's id.
- `AzimuthProvider`: `id()` and `bearings(viewer, range)`, called on the server thread once per
  cycle per viewer. Answer with an immutable, bounded list, in the viewer's dimension and within
  range where that is cheap to know; never load chunks or touch the world; leave out what the
  viewer may not see. Azimuth culls to the dimension and range anyway, caps nearest first, sends,
  and fades on the client. A provider that throws is logged once and skipped until it answers
  again; the others keep answering.
- `AzimuthProviders.register(provider)` from your mod constructor. Duplicate ids are refused.
- Players are not a provider's business: the bar shows them itself.

Registering from a mod that should also run without Azimuth: keep the class that references
Azimuth apart and load it only when `ModList.get().isLoaded("azimuth")`.

## Diagnosing it

- No bar: `bar.enabled`, F1, or the server has no Azimuth (the channel is optional, so a client
  joins either way and simply sees nothing).
- The bar sits low under empty sky: a boss bar the game draws pushes it down; since 1.0.1 a bar
  another mod cancels does not. A mod that cancels after Azimuth has counted (one listening at
  LOWEST priority and registered later) would still push it down.
- A player missing: they are crouching, in spectator, invisible, wearing a pumpkin or a skull, in
  another dimension, or beyond the 16 nearest. Heads that show as Steve or Alex have no skin
  loaded yet.
- A place missing: beyond 256 blocks, in another dimension, beyond the 16 nearest, or its provider
  threw: the server log has "Azimuth provider <id> failed; skipped until it answers again" once,
  and "answers again" when it recovers.
- A lodestone compass that stops pointing: its lodestone is gone; the game itself forgets it.

## Building

Java 21. `./gradlew test` runs the JDK-only domain tests (bearings, the bar's layout, both fade
curves, the roster, the bar's position, the protocol's invariants). `./gradlew runGameTestServer`
runs the real-server GameTests (the players payload's rules and cap; providers asked, culled,
capped and isolated; the compass and death providers over a real inventory).
`./gradlew runPhotoBooth` opens a client on the booth world for a visual check of the bar with a
second player near (a framed head), one far (a faded head) and one behind (a chevron), a provider's
places, a lodestone compass, a boss bar, the same boss bar hidden by another mod, and the dots
style.
`./gradlew publishToMavenLocal` shares the jar with mods that compile against the protocol.
`./gradlew build` produces `build/libs/azimuth-<version>.jar`.

## Status

**1.0.2**: players are heads at any distance, fading with distance as the old bar did; the far
dot of 1.0.0 read as the player being gone. Places unchanged. **1.0.1**: the bar sits below only
the boss bars the game itself draws; 1.0.0 also moved down
for bars other mods hide, such as the one Block Factory's Bosses attaches to every tracked
warden, and sat low under empty sky. **1.0.0**: first release: players, directions, coordinates,
compass and death points, the protocol, boss-bar avoidance, in the mod's own pixel art. Download
from [GitHub Releases](https://github.com/the-rusty-shackleford/minecraft-azimuth/releases).
Verified: 21 JUnit tests, 3 real-server GameTests and the photo booth; see
[release verification](devtools/verification/).
