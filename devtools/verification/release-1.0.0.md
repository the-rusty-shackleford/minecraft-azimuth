# Release verification — 1.0.0

2026-09-24. Azimuth's first build, on Rusty's approval of the plan (D-0001, D-0002), then the
same day the restyle of the bar (D-0003) after Rusty judged the first build's look a downgrade
from the Locator Bar. Still 1.0.0: nothing had shipped. **Not yet released: Rusty's go is the
release**, together with Magical Map 0.3.0 as pack 1.62.0.

Full `./gradlew clean build publishToMavenLocal` on the release tree, Xephyr `:7`, llvmpipe,
muted, no shaders:

- `test`: 22 JUnit tests over the domain and the protocol (bearings around a viewer facing each
  way, wrapping, compass directions; the bar's layout inside, at and beyond its edges, mirrored
  pixel for pixel; both fade curves at their breakpoints and, new, the far rule at the floor's
  distance; the roster's cap, order and duplicates; the bar's position with and without boss
  bars; every invariant of the protocol's records and the registry's refusals).
- `runGameTestServer`: 3 real-server GameTests with survival mock players joined as the
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
- `runPhotoBooth`: COMPLETE, 6 checks. Three peers: the Surveyor 60 blocks south-east, the Rover
  200 blocks south-west (past the fade's floor), the Tracker 70 blocks behind; the booth
  provider's bell 100 blocks south-east at the view's edge, its campfire 239 blocks ahead and
  40 to the left fading in, its chest 100 blocks behind, its pickaxe 300 out; a lodestone compass
  in slot 3 pointing 63° right, outside the view. The payloads list the three peers nearest first
  and the bell, camp, chest and compass, not the pickaxe; the bar sits at the top (4), moves
  below a boss bar ("Pregeneration 42%", top 34 against its bottom 31) and comes back.
  - [00-bar-heads-and-places](1.0.0/00-bar-heads-and-places.png): the outlined rounded bar with
    its highlight and shadow rows; the bell's sprite over its gold dot at the left end; the
    Surveyor's head in its black frame; the campfire over its green dot, faint; the blue S badge
    dead ahead with the notch showing above it; the Rover as a small outlined dot at 40 %; one
    green chevron at the right end for the compass point, the nearest of the three things hidden
    that way (the chest and the Tracker are the others); the coordinates under the bar.
  - [01-below-boss-bar](1.0.0/01-below-boss-bar.png): the same bar under the boss bar.
  - [02-dots](1.0.0/02-dots.png): heads off, the Surveyor and the Rover as outlined dots.
  - [03-judged-against-mockup](1.0.0/03-judged-against-mockup.png): the three photos cropped at
    4x under the mockup's proposed row at the same pixel scale
    ([the mockup's first-build and proposed rows](1.0.0/mockup-first-build-and-proposed.png)).
    Judged by eye, element by element: bar, badge, framed head, item over dot, far dot and
    chevron all match the proposed row. Chosen differences: the head's frame is flush with the
    bar rather than hanging a row below it, the chevron sits inside the bar's outline rather
    than poking out of it, and a badge has a 1 px ring in its darker shade rather than a plain
    margin.
  - Judged by measurement, because a fade cannot be read by eye: the campfire's dot measures
    (66, 81, 75) against a computed (66, 81, 74) at 26 %, the Rover's dot (122, 117, 95) against
    (122, 117, 96) at 40 %, the bell's dot and the chevron their full colours, the bar body and
    highlight rows exactly the sprite's colours.
- Two defects the booth caught, both by measurement, both fixed: with only the sprites drawn,
  every marker measured at full strength whatever its alpha, because the boss overlay before us
  turns blending off at its end (every vanilla layer brackets itself in `enableBlend` /
  `disableBlend`; ours now does too); then everything after a head still did, because
  `PlayerFaceRenderer.draw` turns blending off after the hat layer (re-enabled after each head).
  And by eye: the 5 px far dot with its fill corners cut read as a plus sign (whole fill now),
  and a stack of one chevron per hidden marker hid each other and crowded the end next to the
  far dot (one chevron per end, the nearest marker's colour).
- Also from the first build: a lodestone compass forgets a lodestone that is not there on its
  first inventory tick, and the game registers a placed block's point of interest one tick after
  the block (a compass handed over in the same tick as its lodestone forgot it).
- Markers are drawn farthest first, so where they overlap the nearest is on top; Magical Map's
  booth showed the need, with a village underfoot (dead ahead, at the centre) under the ford's
  boat 92 blocks off.
- Jar `azimuth-1.0.0.jar` sha1 `3fe4b3c300fc3c380d4eebc58b4fde0402ee659c` (51488 bytes),
  domain, api, main and the twelve sprites inside; published as
  `com.chunkworks.azimuth:azimuth:1.0.0` in mavenLocal, sha1 identical.
- Not verified: a real second client's skin (the peers show the default skin); a dedicated server
  with players in several dimensions at once; the bar under shaders (a HUD is unaffected, but
  the pack's players will be the first to see it under Complementary; Magical Map's booth runs
  under Iris and photographs it); team colours are not drawn; the E, N and W badges only in the
  sprite preview, not in a booth photo (the booth faces south).
