# Golf GPS App UI/UX — Competitor Research (2026)

Apps studied: 18Birdies, Hole19, SwingU, Golfshot, Golf Pad GPS, TheGrint.
Full sources inline. Summary of what we adopt / reject for Fairway Caddie.

## What the leaders do

1. **Play screen**: market norm is a full-bleed aerial hole map, tee-to-green, with F/C/B
   yardages stacked near the green end — **center distance always largest**. Hole/par/index in a
   slim top bar; score entry reachable from the same screen ("post your score in three taps from
   the GPS screen" — 18Birdies, the 4.9★ category leader). SwingU overlays distance arcs; Golfshot
   offers a clean "GPS Target" simplified view toggled against the aerial.
   *Our numbers-first screen with a drawn vector green is a deliberate differentiator, not the
   norm — closest precedents are Hole19's distances list and every app's watch face.*
2. **Colors**: play screens are dark/photographic with white yardages; scorecard/brand areas
   trend white + green (TheGrint, Golf Pad). Scoring convention everywhere: **circles = under
   par, squares = over par; red = under, blue/black = over.** (Hole19 gets criticized for a
   "happy blue" bogey and near-invisible par — semantic weighting matters.)
3. **Scorecard UX**: best pattern = par pre-highlighted, 1–2 taps per hole, putts optional,
   entering a score advances the hole. >3 taps/hole or forced partner emails (TheGrint) generate
   complaints.
4. **Onboarding**: location permission → nearby-course list → straight into a round. GPS +
   scorecard free with no card required (18Birdies/Hole19). Handicap optional, never a blocker.

## What users complain about (do NOT copy)

1. Paywall pop-ups / upsell interruptions (the #1 stated difference between 18Birdies and Hole19)
2. Clutter and feature overload (TheGrint: "excessive number of buttons")
3. Small low-contrast type; sunlight illegibility (category-wide)
4. Battery drain from continuous GPS (category-wide)
5. Slow GPS settle and slow aerial tile loading (vector green sidesteps this)
6. Missing auto-advance (was Golfshot's long-running gripe)
7. Mandatory accounts before any value

## Fairway Caddie decisions taken from this research

| Finding | Action | Status |
|---|---|---|
| ≤3-tap scoring, par pre-highlighted | One-tap score chips on Play screen, par emphasized; tap again clears | ✅ shipped |
| Entering score advances hole | setStrokes auto-selects next hole | ✅ shipped |
| Center distance dominant | Already 128sp Anton vs 44sp F/B | ✅ |
| Red-under/blue-over, circle/square notation | Already implemented | ✅ |
| No popups, no forced account | Product rules | ✅ |
| White high-contrast type for sunlight | Light retheme | ✅ |
| Battery: relax GPS cadence, watch support | Backlog | ⏳ |
| Optional aerial/map view toggle | Consider post-beta (tile source needed) | ⏳ |
| Nearby-course list as first screen | Courses screen is the landing tab; improve to auto-list | ⏳ |

Key sources: [18Birdies Play screen](https://help.18birdies.com/article/585-play-screen) ·
[18Birdies Keeping Score](https://help.18birdies.com/article/28-keeping-score) ·
[Golf Monthly 18Birdies review](https://www.golfmonthly.com/reviews/gps/18birdies-app-review-is-this-the-best-free-golf-gps-app) ·
[Plugged In Golf](https://pluggedingolf.com/18birdies-golf-app-review/) ·
[RangeTheDrive Hole19](https://www.rangethedrive.com/hole-19-distance-app/) ·
[MyGolfSpy SwingU](https://mygolfspy.com/we-tried-it/we-tried-it-swingu-golf-gps-and-shot-tracking-app/) ·
[Critical Golf Golfshot](https://criticalgolf.com/reviews/archived-products/golfshot-review/) ·
[Golf Pad scorecard symbols](https://support.golfpadgps.com/support/solutions/articles/6000193012-golf-pad-scorecard-what-do-the-symbols-mean-) ·
[JustUseApp TheGrint](https://justuseapp.com/en/app/532085262/thegrint-your-golferhood/reviews) ·
[Sand Trap battery thread](https://thesandtrap.com/forums/topic/68377-golf-apps-killing-battery/)
