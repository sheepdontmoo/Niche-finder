# Round 2 — 10 More Paid-App Clone Candidates

**Date:** 2026-07-04. Same rubric as round 1 ([`play-store-clone-targets.md`](play-store-clone-targets.md)):
paid or paid-gated, ≥100k installs, rating ≤ ~4.1, low rating driven by abandonment, subscription-trap
resentment, or breakage on modern devices — not just mediocrity. Golf and OBD2 (already built) excluded.
Four parallel research agents swept fresh verticals: photo/video/music, education/kids/language,
finance/PDF tools, home/pet/habit/VPN. Raw sourced findings in [`data/round2_*.json`](data/).

## Vertical results

**Photo/video/music — 3 candidates cleared the bar.** Several strong "abandoned paid app" stories
(ProCam X, Caustic 3, VivaVideo) were found and *excluded* because their current ratings sit above 4.1
despite real complaints — discipline matters more than a long list.

**Education/kids/language — thin, only 1 candidate.** Every major brand (Duolingo, Busuu, Photomath,
Quizlet, Memrise, Reading Eggs) actively holds ratings at 4.3–4.7 despite loud subscription-complaint
volume on Trustpilot/PissedConsumer. Large edtech brands appear to manage their Play Store rating
aggressively. Small-studio abandoned apps (the Toca Boca pattern) didn't surface high-install
matches this round.

**Finance/PDF — 3 candidates, one standout.** CamScanner's paid unlock key is the strongest single
find of this entire round (see below).

**Home/pet/habit/VPN — 4 candidates; recipe and VPN verticals were a bust.** Every major recipe app
(Paprika, Yummly, Mealime) and VPN app (Hotspot Shield, VPN Unlimited) sits at 4.3+ despite
subscription complaints. Plant-ID and habit-tracker candidates carry a new risk flagged below.

## Full candidate board

| App | Price | Installs | Rating | Core failure | Buildable? |
|---|---|---|---|---|---|
| **CamScanner (License)** | $1.99 one-time unlock | 1M+ | **2.4★** | Subscription pivot made the paid unlock feel obsolete; charged but shows "not purchased"; nagware persists after paying | ✅ **High** — on-device OCR is now commoditized (Google ML Kit Document Scanner API) |
| Rosetta Stone: Fluency Builder | Subscription | 500k+ | 2.8★ | Lifetime subs converted to 24-month subscriptions; voice recognition broken; support refuses activation codes | ❌ Content-heavy, needs real course + speech-recognition assets |
| 11pets: Pet Care | Was lifetime, migrated to subscription | installs unconfirmed (likely 100k+) | 3.4★ | Lifetime purchasers forced onto new subscription tier; update scrambled/lost pet records; broken password reset | ✅ High — CRUD app (records, reminders), no exotic tech |
| edjing PRO DJ mixer | $5.99 | 100k+ | 3.53★ | Forced to repay after subscription pivot; broken purchase restore; crashes during mixing | ⚠️ Medium — real-time audio engine nontrivial but well-documented |
| Way of Life Habit Tracker | ~$4.99 one-time unlock | 687–730k | 3.57★ | Free tier very limited; feature requests ignored for years; stagnant development | ⚠️ Buildable, but a beloved free/open-source competitor (Loop Habit Tracker) already exists |
| PlantSnap Plant Identifier | Free + $19.99–29.99/yr or $30 lifetime | 22M+ | 3.6–3.73★ | Undisclosed paywall; heavy ads/upsells; frequent misidentification | ⚠️ Good free alternatives already exist (PlantNet, Seek by iNaturalist) |
| Money Lover Expense Tracker | Free + Premium unlock | 10M+ | 3.79★ | Previously-free features paywalled after update; data loss on update; sync bugs | ⚠️ Buildable but crowded market |
| PlantIn Plant Identifier | Free + ~$19.99/yr | 10M+ | ~4.0 (contested) | Unexpected charge-plan switch ($7.99/wk → $45/mo); free tier blocks after 1–3 IDs | ⚠️ Same free-alternative risk as PlantSnap |
| Camera FV-5 | $4.99 | 500k+ | 4.10★ | Crashes; manual settings reset on restart; no full native resolution | ⚠️ Right at the cutoff, weaker signal |
| FL Studio Mobile | $14.99 | 1M+ | 4.01★ | Crashes causing total project loss, no autosave; audio glitches | ❌ A full mobile DAW is a multi-year engineering effort — same "AAA game port" problem as round 1 |
| Fortune City (gamified finance) | Free + subscription + diamonds IAP | 1M+ | 4.0★ | Progression paywalled behind premium currency; perverse "spend to save money" design | ❌ Wrong shape of opportunity — the complaint is predatory design, not abandonment; doesn't fit our positioning |

## 🏆 The pick: document scanner / PDF tool ("CamScanner successor")

**CamScanner License** is the standout of this round, and it rhymes with the OBD2 pick almost exactly:

- **1M+ people already paid** a one-time $1.99 unlock, expecting it to mean something
- **Rating collapsed to 2.4★** after the parent app pivoted hard to subscriptions — paid users report being charged but the app still shows "not purchased," and nag screens for the subscription persist even after paying the unlock
- **Uniquely buildable in 2026:** document scanning + OCR used to require serious computer-vision work. It doesn't anymore — **Google's own ML Kit ships a free, on-device Document Scanner API** with edge detection, perspective correction, and OCR built in. A small team can ship a genuinely good scanner app without building any of the hard part from scratch.
- **The wedge:** one-time price (~$5–10), no forced cloud account, no upload limits, no watermarks, works fully offline. Positioned directly against "CamScanner made you pay, then made you pay again."

## Runners-up

2. **11pets Pet Care successor** — the *exact same* "you bought lifetime, we switched you to a subscription" betrayal that's now proven twice (Torque/Carly, CamScanner). Genuinely simple to build (pet medical records, vet visit log, reminders — no ML, no real-time engine). Weakest point: install numbers weren't confirmed this round, worth a validation pass before committing.
3. **edjing-style DJ mixer** — same betrayal pattern, moderately harder build (real-time audio mixing), smaller but real install base.

## What NOT to chase (new lesson from this round)

**"High installs + bad rating" isn't sufficient if a good free alternative already exists.** PlantSnap
and PlantIn both look tempting (22M and 10M installs) but Google's own iNaturalist-backed **Seek app**
and **PlantNet** are free, well-regarded plant-ID tools — meaning any clone competes against a strong
free incumbent, not just a resented paid one. Same logic knocked Way of Life down (Loop Habit Tracker
is free and open-source). The golf/OBD2 picks worked because **no good free alternative existed** —
that's now an explicit filter for round 3.

## Suggested next step

Validate CamScanner's successor the same way OBD2 was validated: mine recent CamScanner reviews for
the top 5 specific complaints, confirm Google ML Kit's Document Scanner API covers the core feature
set, and spec an MVP (scan → OCR → export as searchable PDF, one-time $7.99 unlock, no account required).
