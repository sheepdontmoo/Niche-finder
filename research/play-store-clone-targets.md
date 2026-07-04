# Play Store Niche Finder — Paid Apps, Big Downloads, Bad Ratings

**Date:** 2026-07-02
**Goal:** Find paid Google Play apps with large install bases but bad ratings — proof that people *pay* for the job-to-be-done while the incumbent fails them — then pick the best one to out-build.

## Methodology

Direct Play Store scraping is blocked from this environment (network policy denies `play.google.com`), so four parallel research agents swept web sources (AppBrain mirrors, Trustpilot, support forums, review-mining sites) across: navigation/travel/auto, premium games, productivity/tools, and health/family/niche verticals. Filter: **paid or paid-gated, ≥100k installs, rating ≤ ~4.1**. Raw structured findings with source URLs are in [`data/`](data/).

Each candidate was then scored on:

- **Demand** — installs × price (how much money is already on the table)
- **Pain** — rating gap below 4.5 and complaint severity (abandonment, crashes, subscription bait)
- **Buildability** — can a small team actually ship a competitive clone (kills AAA game ports)
- **Moat risk** — does the incumbent's real asset (map data, IP, AIS network) block a clone

## Full candidate board

| App | Price | Installs | Rating | Core failure | Clonable? |
|---|---|---|---|---|---|
| GTA: San Andreas | $6.99 | ~7.6M | 2.5★ | Crashes on modern Android, abandoned | ❌ Rockstar IP |
| Max Payne Mobile | $2.99 | ~760k | 2.07★ | Broken for years while still sold | ❌ Rockstar IP |
| GTA: Vice City | $4.99 | ~3.8M | 3.19★ | 2012 port, full price, unmaintained | ❌ Rockstar IP |
| MarineTraffic | ~$4.99 + subs | 1M+ | 2.89★ | Paywalls stacked on paid app, redesign fury | ⚠️ AIS network moat |
| CoPilot GPS | sub (was paid) | 6.4M | 3.18★ | Buggy rewrite, paid users stripped of features | ⚠️ Map-data moat |
| SkySafari 8 Pro | $49.99 | 2.2k (franchise 3.7M) | 3.33★ | Crashes constantly at a $50 price | ⚠️ Stellarium owns this |
| HD Widgets | $0.99 | 1M+ | 3.43★ | Abandoned, weather source died | ✅ |
| Carly OBD2 | $60–114/yr + adapter | 1.8M | 3.58★ | Subscription trap, Trustpilot 2.1/5 | ✅ |
| TomTom GO | sub (was ~$20) | 22M | 3.59★ | Forced paid→subscription migration | ⚠️ Map-data moat |
| Cleartune Tuner | $3.99 | 220k | 3.69★ | Abandoned since 2014, delisted | ✅ small market |
| My Backup Pro | $6.99 | 580k | 3.74★ | Silent backup failures, no support | ⚠️ platform-limited |
| AndroVid Pro | $19.99 | 100k+ | 3.81★ | Removed paid features, crashes | ❌ CapCut is free |
| Beautiful Widgets Pro | $2.49 | 1.8M | 1.91★ | Abandoned 2016, unpublished | ✅ |
| iGO Navigation | ~$20–50 maps | 3.2M | 2.9★ | Stale maps, purchases lost on reset | ⚠️ Map-data moat |
| Golfshot Plus | $79.99 + IAP | 50k+ | 3.94★ | Handicap broken 2+ yrs, stale | ✅ |
| Auto Call Recorder Pro | $8.49 | 100–500k | 3.95★ | Killed by Android policy | ❌ platform-blocked |
| Toca Life paid titles (×3) | $3.99 ea | ~1.7M combined | ~4.0★ | Abandoned; buyers pushed to IAP app | ✅ content-heavy |
| Torque Pro | $4.95 | 1M+ | 4.0★ | Abandoned; fails on modern cars/Android Auto | ✅ |
| SkyDroid Golf GPS | $1.99 | 100k+ | 4.06★ | Dev "evaporated" in 2017, GPS 13–34 yds off | ✅ |
| Camera FV-5 | $4.99 | 510k (Lite 27M) | 4.1★ | Absent dev, broken manual controls | ✅ |

*(Insight from the games column: people demonstrably pay $3–7 for premium mobile experiences and rage when they break — but AAA ports aren't clonable. The signal transfers to the utility niches below.)*

## 🏆 The pick: OBD2 car scanner ("Torque Pro successor")

**One niche, two furious paid user bases, both incumbents failing in opposite directions:**

- **Torque Pro** ($4.95 one-time, 1M+ paid installs, ~80k ratings, 4.0★ and sinking): effectively **abandoned**. Can't read most modules on 2024+ vehicles, Bluetooth reconnect failures, no real Android Auto support, UI from 2012. A million people paid for this and are actively shopping for a successor on forums.
- **Carly** ($60–114/yr **plus** a $60–90 proprietary adapter, 1.8M installs, 3.58★, Trustpilot 2.1/5): proves the **high willingness to pay**, but users despise the subscription trap, unauthorized renewals, and features that don't work on their actual cars.

**The wedge:** a modern OBD2 dashboard/diagnostics app that is everything both fail to be —

1. **One-time price** (~$9.99) — directly weaponizes Carly's subscription hatred; undercuts nothing since Torque buyers already proved the price point.
2. **Works with cheap $10 ELM327 adapters** — no proprietary hardware.
3. **First-class Android Auto dashboards** — Torque's #1 unmet request.
4. **Modern vehicle coverage** (CAN/UDS, manufacturer-extended PIDs, EV/hybrid data) — Torque's #1 breakage.
5. Reliable Bluetooth LE + WiFi adapter handling, dark modern UI, cloud backup of logs.

**Why it's buildable:** OBD-II PIDs and ELM327 AT command sets are publicly documented; the core product is a protocol client + real-time gauge UI — no map licenses, no AIS network, no AAA IP. The moat you build over time is the vehicle-specific PID database (community-sourced, like Torque's was).

**Main risk:** Car Scanner ELM OBD2 is a competent freemium incumbent. Differentiate on Android Auto, modern-vehicle coverage, and the anti-subscription positioning.

## Runners-up

2. **Golf GPS** — SkyDroid is dead (abandoned 2017) and Golfshot Plus charges **$79.99** yet sits at 3.94★ with a handicap feature broken for two years. Golfers are the highest-paying consumer niche found. Moat to solve: course database (OpenStreetMap golf mapping + community corrections is a viable bootstrap).
3. **Premium kids sandbox (the Toca gap)** — Toca Boca abandoned ~1.7M paid installs across its $3.99 Toca Life titles to funnel families into an IAP app. Parents explicitly pay to *avoid* IAP/ads. "Pay once, no ads, no IAP, offline" is the whole marketing page. Content-production-heavy, but zero platform risk.
4. **Premium weather widgets** — Beautiful Widgets Pro (1.8M paid, now 1.91★) and HD Widgets (1M+, 3.43★) both died and were never replaced. Small build, real gap, modest ceiling.

## Legal note

"Clone" = build a competing app for the same job-to-be-done with original code, assets, name, and branding. Do **not** copy app names, icons, UI assets, or proprietary databases; trademark/copyright applies even when the functionality is fair game.

## Suggested next step

Validate the pick in a weekend: mine the newest 500 Torque Pro / Carly reviews for feature-level demand ranking, then spec an MVP (gauges + fault codes + Android Auto) against a $10 ELM327 on one test vehicle.
