# Release verification — 1.0.1

2026-09-24, the evening 1.0.0 went live in pack 1.62.0. Rusty, in play, with no boss bar on
screen: "The bar is way too low. I only want it lowered if a chunky is running." Cause pinned on
the box (D-0004): Jdrum12, the only player on, stood 117 blocks above a warden inside tracking
range; Block Factory's Bosses gives every tracked warden a boss event and cancels vanilla's
drawing of it at the lowest priority; Azimuth's listener at normal priority had counted it
first. The bar now sits below only the boss bars vanilla actually draws.

Full `./gradlew clean build publishToMavenLocal` on the release tree, Xephyr `:7`, llvmpipe,
muted, no shaders:

- `test`: 22 JUnit tests, unchanged.
- `runGameTestServer`: 3 real-server GameTests, unchanged.
- `runPhotoBooth`: COMPLETE, 7 checks. The booth's `BossBarHider` stands in for such a mod
  exactly: registered from the test mod's constructor, at LOWEST priority, cancelling the draw
  while a flag is set. With the command boss bar showing the bar moves down (top 34 under a
  bottom of 31, [photo](1.0.1/01-below-boss-bar.png)); with the hider on and the boss event
  still held, nothing is drawn at the top and the bar is back at 4 with a counted bottom of 0
  ([photo](1.0.1/02-boss-bar-hidden-by-another-mod.png)); with the boss bar removed it stays
  there ([photo](1.0.1/03-dots.png)). The first photo is the 1.0.0 bar unchanged
  ([photo](1.0.1/00-bar-heads-and-places.png)).
- The new check bites: run once with 1.0.0's registration restored (normal priority, cancelled
  events not received), the booth failed it with "top 34 boss bottom 31" under the hidden bar;
  with the fix it passes.
- A GLFW "failed to detect any supported platform" on the first attempt was the Xephyr display
  having gone away, not the code; a fresh `:7` and the same tree passed.
- Jar `azimuth-1.0.1.jar` sha1 `d2e937bd5bb2c059b682b9b1a982185b19f57953` (51564 bytes),
  reproducible across two builds; published as `com.chunkworks.azimuth:azimuth:1.0.1` in
  mavenLocal, sha1 identical. The api package is unchanged; Magical Map 0.3.0 keeps compiling
  against 1.0.0 and runs against either.
- Not verified: the fix on Rusty's own client next to that warden (the pack release carries it);
  a mod that listens at LOWEST and registers after Azimuth's client setup would still be counted
  (none in the pack does).
