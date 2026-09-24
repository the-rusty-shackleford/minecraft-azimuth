# Release verification — 1.0.0

2026-09-24. Azimuth's first build, on Rusty's approval of the plan (D-0001, D-0002). **Not yet
released: Rusty's go is the release**, together with Magical Map 0.3.0 as pack 1.62.0.

- `./gradlew test`: 21 JUnit tests over the domain and the protocol (bearings around a viewer
  facing each way, wrapping, compass directions; the bar's layout inside, at and beyond its
  edges, mirrored pixel for pixel; both fade curves at their breakpoints; the roster's cap,
  order and duplicates; the bar's position with and without boss bars; every invariant of the
  protocol's records and the registry's refusals).
- `./gradlew runGameTestServer`: 3 real-server GameTests with survival mock players joined as the
  framework joins its own:
  - the players payload lists the two visible players nearest first and leaves out the viewer, a
    crouching player, one under a carved pumpkin and one in the Nether; the fade rule rides along;
    the cap keeps the nearest;
  - a test provider's four places (in range, beyond range, in the Nether, close) become the two in
    this dimension within range, nearest first, while a throwing provider is logged once, skipped,
    and the others keep answering; the cap keeps the nearest;
  - a lodestone compass pointing at a real lodestone in this dimension is one bearing from its
    slot, one pointing at the Nether and a plain compass are not; the death point appears only
    with a recovery compass and a death in this dimension.
- `./gradlew runPhotoBooth` on an iconified Xephyr, muted, no shaders: COMPLETE, 8 checks. The
  Surveyor peer's head sits left of centre for a viewer facing south with the peer to the
  south-east; a booth provider's bell (100 blocks south-east) at the bar's left edge, its campfire
  240 blocks ahead faint at the centre, its chest 100 blocks behind pinned to the right end at
  half strength, its pickaxe 300 blocks out absent; the lodestone compass's point at the right
  end; "S" at the centre; the coordinates under the bar
  ([photo](1.0.0/00-bar-heads-and-places.png)). A boss bar ("Pregeneration 42%") pushes the bar
  down below it, top 34 against the bar's bottom 31 ([photo](1.0.0/01-below-boss-bar.png)), and
  it returns to the top when the boss bar goes. Dots instead of heads
  ([photo](1.0.0/02-dots.png)). Judged by eye: the face, the bell sprite, the fade step and the
  edge pin are all there; the bar is small at scale 1 (102 by 10 at GUI scale 2), which the
  client's `bar.scale` answers.
- Two things the booth caught: a lodestone compass forgets a lodestone that is not there on its
  first inventory tick, and the game registers a placed block's point of interest one tick after
  the block (a compass handed over in the same tick as its lodestone forgot it). Found by a
  diagnostic step logging the tracker, the block and the point of interest, not by guessing.
- `./gradlew clean build publishToMavenLocal`: green; jar `azimuth-1.0.0.jar` sha1
  `a70053e1293612e4d4ab1c477b96dc709c2c2afb` (44781 bytes), domain, api and main inside;
  published as `com.chunkworks.azimuth:azimuth:1.0.0`.
- Not verified: a real second client's skin (the peer shows the default skin); a dedicated server
  with players in several dimensions at once; the bar under shaders (a HUD is unaffected, but the
  pack's players will be the first to see it under Complementary); team colours are not drawn.
