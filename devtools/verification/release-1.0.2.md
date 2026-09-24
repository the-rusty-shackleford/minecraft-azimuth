# Release verification — 1.0.2

2026-09-24, after 1.0.1 went live in pack 1.62.1. Rusty, in play: "The bar should still show
players at any distance, and fade in as you get closer like the old bar (villages and other
points of interest should not follow this, they should stay the same)." The far dot of D-0003
read as the player being gone; players are framed heads at any distance again, by D-0001's fade
curve, and places are unchanged (D-0005).

Full `./gradlew clean build publishToMavenLocal` on the release tree, Xephyr `:7`, llvmpipe,
muted, no shaders:

- `test`: 21 JUnit tests (the far rule and its test are gone with the far dot).
- `runGameTestServer`: 3 real-server GameTests, unchanged.
- `runPhotoBooth`: COMPLETE, 7 checks, unchanged in what they check. In
  [00-bar-heads-and-places](1.0.2/00-bar-heads-and-places.png) the Rover, 200 blocks off, is
  now a framed head right of centre at the fade's floor beside the near Surveyor at near-full;
  judged by eye at 8x, and by measurement: the Rover's frame reads (25, 28, 31), exactly 40 %
  black over the bar's body, the Surveyor's face (124, 82, 55) as before. The other photos
  ([boss bar](1.0.2/01-below-boss-bar.png), [hidden boss bar](1.0.2/02-boss-bar-hidden-by-another-mod.png),
  [dots](1.0.2/03-dots.png)) are as in 1.0.1.
- Jar `azimuth-1.0.2.jar` sha1 `13bd383089f3db98cd7d2628813af80183fdd2f0` (51184 bytes), no
  `far.png` inside; published as `com.chunkworks.azimuth:azimuth:1.0.2` in mavenLocal. The api
  package is unchanged.
- Not verified: a 40 % head on Rusty's screen under Complementary next to a real far player;
  the floor is the server's `players.min_alpha` if it wants raising.
