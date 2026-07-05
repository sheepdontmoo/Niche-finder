# Niche-finder

Find paid Play Store apps with big install bases and bad ratings — proven paid demand where the
incumbent is failing — then build the winner.

| Research round | Artifact |
|---|---|
| 1 | [`research/play-store-clone-targets.md`](research/play-store-clone-targets.md) — 20-app scored board across 4 verticals ([raw data](research/data/)) |
| 2 | [`research/round-2-candidates.md`](research/round-2-candidates.md) — 10 more clone-target candidates across fresh verticals ([raw data](research/data/)) |

| App | Pick | Validate | Build |
|---|---|---|---|
| **OBD2 car scanner** | Abandoned Torque Pro (1M+ paid, 4.0★) + subscription-trap Carly (1.8M, 3.58★) | [`research/obd2-validation.md`](research/obd2-validation.md) — feature demand ranking, switching triggers, adapter matrix, pricing, the Android Auto wedge | [`obd2-core/`](obd2-core/) — tested Kotlin protocol core · [`android-app/`](android-app/) — the app (Compose UI, BT/BLE/WiFi transports, demo mode) · [`AGENTS.md`](AGENTS.md) — briefing for agent IDEs |
| **Fairway Caddie** (golf GPS) | Abandoned SkyDroid ($1.99, dev gone since 2017, GPS 13–34 yds off) + overpriced Golfshot Plus ($79.99, 3.94★, handicap broken 2+ yrs) | [`research/golf-ui-competitors.md`](research/golf-ui-competitors.md) — UI/UX teardown of 18Birdies, Hole19, SwingU, Golfshot, Golf Pad GPS, TheGrint | [`golf-core/`](golf-core/) — tested Kotlin course/geo/scoring core · [`android-golf/`](android-golf/) — the app · [`store-listing.md`](store-listing.md) — Play Store listing kit |
| **DocStash** (document scanner) | CamScanner's one-time $1.99 unlock (1M+ paid, **2.4★**) — parent app pivoted to subscriptions; paid users charged but shown "not purchased," nag screens persist after paying | Validated inline in [`research/round-2-candidates.md`](research/round-2-candidates.md) — Google ML Kit's free on-device Document Scanner + Text Recognition APIs make this buildable without any custom CV/OCR engineering | [`scan-core/`](scan-core/) — tested Kotlin PDF assembly/naming/search/entitlement core · [`android-scan/`](android-scan/) — the app (ML Kit scanning + OCR, Compose UI, Play Billing) · [`store-listing-docstash.md`](store-listing-docstash.md) — Play Store listing kit |

All three follow the same rule: **one-time price, no subscription, ever** — that's the whole wedge
against each incumbent above.
