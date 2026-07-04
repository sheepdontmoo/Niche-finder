# Play Store Niche Finder — Round 2: New Verticals

**Date:** 2026-07-04
**Goal:** Round 1 produced two builds (OBD2 scanner, golf GPS). Round 2 sweeps the verticals round 1 didn't touch — photography/video, education/kids, personal finance, music, PDF/document tools, fitness beyond golf, home/DIY, pet care, language learning, VPN/security, recipes, habit tracking, and barcode/inventory — for 10 new candidates. Same rubric: **paid or paid-gated, ≥100k installs (ideally 1M+), rating ≤ ~4.1**, where the low rating comes from abandonment, subscription-trap resentment, or breakage on modern devices — not mere mediocrity.

## Methodology

Same as round 1: direct Play Store scraping is blocked (network policy denies `play.google.com`), so **six parallel research agents** swept web sources — AppBrain mirrors, Trustpilot, BBB/FTC records, Reddit and product forums, review-mining sites (JustUseApp, AppGrooves, PissedConsumer, Kimola) — across: photo/video + music, kids/education + language, finance + habit tracking, PDF/docs + inventory, fitness + pet care, and home/DIY + recipes + security. Raw structured findings with source URLs are in [`data/round2_*.json`](data/); each file also records the leads investigated and **rejected** (mostly for healthy Play ratings — e.g. Rosetta Stone 4.6+, HabitBull 4.39, Paprika 4.87) so they don't get re-chased in round 3.

Scoring (1–5 each, total /20):

- **Demand** — installs × price actually flowing (subscription $ counts more than a one-off $0.99)
- **Pain** — rating gap below 4.5 + complaint severity (data loss, billing traps, abandonment)
- **Buildability** — can a small team ship a credible competitor (already killed the AAA-port and VPN-fleet categories)
- **Moat safety** — 5 = incumbent owns nothing defensible; 1 = map database / content library / network effect blocks a clone

## Full candidate board (everything that survived the rubric)

| App | Price | Installs | Rating | Core failure | Clonable? |
|---|---|---|---|---|---|
| 11pets: Pet care | free→forced sub | 530k | 2.12★ | Rewrite locked users out of their own pet records; data lost | ✅ pure CRUD |
| Home Design 3D Outdoor/Garden | $4.99 + stacked IAP | 3.6M | 2.29★ | Paid users double-charged for save; abandoned | ✅ asset-library build |
| ezPDF Reader | $3.99 | 500k | 2.8★ | Repeatedly abandoned; crashes; creepy new permissions | ✅ |
| Quicken Classic companion | gated on $60–110/yr | 680k | 3.01★ | Desktop sync chronically broken for paying subscribers | ⚠️ proprietary QDF format |
| Sports Tracker | free→paywalled stats | 16M | 3.12★ | Retroactive paywall + stop-button/tracking failures | ⚠️ Strava/free tier crowd |
| Adobe Premiere Rush | CC sub | 5M | 3.2★ | Officially killed Sept 2026; Android users stranded | ❌ CapCut is free |
| Gaia GPS | $40–90/yr sub | 4.1M | 3.36★ | Offline maps demand online login; Outside-acquisition price hikes | ✅ open map data |
| Guitar Pro (mobile) | $5.49–6.99 | 100k+ | ~3.4★ | No update since ~2022; crashes; desktop users captive | ✅ open .gp renderers exist |
| FluentU | $240/yr | ~1M | 3.49★ | Auto-billing anger; core subtitle feature broken on Android | ⚠️ video-content licensing |
| Expense IQ Money Manager | freemium + lifetime IAP | 1.3M | 3.49★ | Abandoned 2023; lifetime buyers locked out; data loss | ✅ manual-entry, no bank feeds |
| Mobills Budget Planner | sub-gated | 10M | 3.5★ | Sync silently loses all transactions; sub resentment | ⚠️ bank-feed (Open Finance) moat |
| Photo Mate R3 | paid-gated IAP | 190k | 3.53★ | Solo-dev dormancy; stale RAW profiles | ⚠️ Lightroom free tier |
| BackCountry Navigator PRO | $11.99 | 250k | 3.54★ | Dev moved to sub-based XE; PRO gets compliance patches only | ✅ open map data |
| VSCO | $30–60/yr sub | 100M+ | 3.56★ | One-time preset purchases converted into rentals | ⚠️ Snapseed free; brand aesthetic |
| Alight Motion | ~$30–60/yr sub | 100M+ | 3.59★ | Trial/paywall traps; Bending Spoons monetization | ⚠️ engine build + CapCut |
| ABCmouse Classic | $12.99/mo | 23M | 3.67★ | FTC-sanctioned cancellation dark patterns; app decaying | ⚠️ content volume; Khan Kids free |
| F-Secure Freedome VPN | $35–50/yr | 8M | 3.7★ | EOL'd; backend change deliberately broke paying users | ❌ server-fleet moat |
| Home Budget with Sync | $5.99 | ~175k (full+lite) | 3.75★ | Abandoned 3 yrs; family sync decaying | ✅ |
| Sortly | $29–59+/mo | 310k | 3.77★ | Renewal hikes ($468→$1,467/yr); data held hostage | ✅ CRUD + barcode |
| Sago Mini World | ~$8–10/mo | 18M | 3.8★ | Paid standalone apps deleted; owners pushed to sub | ⚠️ art/content build |
| My PlayHome Plus | IAP areas | 49M | 3.85★ | Prior paid ownership not recognized; purchase pressure on kids | ✅ mechanically simple |
| SwiftScan (ex-Scanbot Pro) | $22.49/yr (was $7 once) | 6.8M | 3.86★ | Paid→sub rug-pull; owned features re-paywalled | ⚠️ free scanners crowd |
| Teach Your Monster to Read | $4.99–8.99 | 1.2M | 3.96★ | Android build crashes (iOS 4.51 vs Android 3.96) | ⚠️ pedagogy content |
| FL Studio Mobile | $14.99 | 1M+ | 4.01★ | Constant crashes; mobile neglected for desktop | ⚠️ DAW = big build |
| Zombies, Run! | $39.99/yr (was $3.99) | 1M+ | 4.04★ | Paid→sub migration; tracking broken by battery optimization | ⚠️ story-content moat |
| eMeals | $59.99/yr | 500k | 4.07★ | Phone-only cancellation; app abandoned while billing continues | ⚠️ menu-content pipeline |
| eFax | $16.95+/mo | 3M | 4.08★ | Cancellation maze; billing after cancellation | ✅ fax APIs exist |
| Penzu | $19.99/yr | 790k | 4.11★ | Pro features not delivered; journals lost; double charges | ✅ |
| Xodo (Apryse) | ~$10–12/mo | 29M | 4.14★ | Decade-free features aggressively re-paywalled | ⚠️ rating just over bar |

*(Pattern check vs round 1: the same three failure modes dominate — abandonment, paid→subscription rug-pulls, and Android builds rotting while iOS stays healthy. New this round: "data hostage" as the single angriest complaint class — Sortly, 11pets, Expense IQ, Penzu all trap users' own records behind a decaying or extortionate app.)*

## The 10 scored candidates

| # | Candidate niche | Failing incumbent(s) | Demand | Pain | Build | Moat | Total |
|---|---|---|---|---|---|---|---|
| 1 | **Photo-first inventory tracker** | Sortly (310k, 3.77★, $29–59+/mo) | 4 | 4 | 5 | 5 | **18** |
| 2 | **Offline backcountry topo GPS** | Gaia GPS (4.1M, 3.36★) + BackCountry Navigator (250k paid, 3.54★) | 5 | 4 | 4 | 4 | **17** |
| 3 | **Pet health records** | 11pets (530k, 2.12★) | 3 | 5 | 5 | 4 | **17** |
| 4 | **.gp tab player / guitar practice** | Guitar Pro mobile (100k+ paid, ~3.4★) | 3 | 4 | 4 | 4 | **15** |
| 5 | **Pay-per-use mobile fax** | eFax (3M, 4.08★, $17+/mo) + iFax et al. | 4 | 4 | 4 | 3 | **15** |
| 6 | **Manual family budget ledger** | Expense IQ (1.3M, 3.49★) + Home Budget w/ Sync (175k, 3.75★) | 3 | 4 | 5 | 3 | **15** |
| 7 | **One-time-price document scanner** | SwiftScan/Scanbot (6.8M, 3.86★) + CamScanner resentment | 4 | 4 | 4 | 2 | **14** |
| 8 | **One-time-price PDF annotator** | ezPDF (500k paid, 2.8★) + Xodo paywall exodus (29M) | 4 | 4 | 3 | 3 | **14** |
| 9 | **Buy-once film-preset photo editor** | VSCO (100M+, 3.56★) | 5 | 4 | 3 | 2 | **14** |
| 10 | **Own-forever kids dollhouse sandbox** | My PlayHome Plus (49M, 3.85★) + Sago Mini World (18M, 3.8★) | 4 | 3 | 3 | 3 | **13** |

**Scoring notes:**

- **#1 Sortly**: the money is extreme for the install base — users report renewals jumping from $468 to $1,467/yr, and they stay because their inventory data is hostage. The product is photos + folders + barcode + CSV export: a solved-problem build. "Flat price, your data exports freely, forever" is the entire pitch. No free incumbent owns this (spreadsheets are the competition).
- **#2 Gaia + BackCountry Navigator** replays round 1's winning Torque/Carly shape exactly: two incumbents failing in *opposite* directions — one subscription-trapped (Gaia post-Outside: login walls in front of paid offline maps, surprise billing), one abandoned (BackCountry PRO frozen while the dev's sub-based successor also rates 3.44). Map data is open (OSM, USGS, USFS); onX at $30–100/yr proves the price ceiling. Bonus: heavy code reuse from the golf build (GPS, offline tiles, OSM/Overpass parsing already written in `golf-core`).
- **#3 11pets** is the most severe pain on the board (2.12★): an update locked families out of years of vaccination and medical records. A local-first, export-everything pet record app is a weekend-scale CRUD build with a trust-shaped marketing wedge. Capped demand (was free for years; ARPU unproven) keeps it at #3.
- **#4 Guitar Pro**: captive audience (desktop GP users need a mobile companion for existing .gp libraries), open-source rendering engines (alphaTab) exist, and the incumbent hasn't shipped since ~2022 — but Arobas demoed a new app at NAMM 2025, so the window is real but time-limited.
- **#5 eFax**: faxing is inherently paid (telco costs) so no free-incumbent risk; the entire 1-star driver is billing abuse of occasional users. Fax-API providers (Telnyx, Documo) make pay-per-page buildable; margin per fax is thin, which caps the score.
- **#7–9** all fight well-funded free or freemium incumbents (Google/Adobe scanners, Snapseed/Lightroom) — the paying wedge is real ("buy once, offline, no account") but narrower than the raw install counts suggest.
- **#10** is the round-1 Toca gap again, now double-confirmed by Sago and PlayHome pulling the same paid→sub rug — but it stays last because art/content production is the actual product.

## 🏆 Ranked top-3 recommendation

### 1. Photo-first inventory tracker — the "Sortly hostage rescue" (18/20)

Sortly built a genuinely loved product shape (photo folders + barcode + search for small businesses, insurance documentation, and collectors), then weaponized it: legacy plans discontinued, renewals tripling with 30 days' notice, free tier capped at 100 items, per-item pricing — while the app itself sheds data in sync bugs. Trustpilot and Capterra reviews use the words "held hostage." The build is the smallest on the top-3 (local-first CRUD + camera + ML Kit barcode + CSV/PDF export + optional sync), the willingness to pay is the highest per user, and the positioning writes itself: **flat $29–49 one-time or cheap capped tier, unlimited items, one-tap full export — your inventory is yours.** Main risk: B2B distribution differs from consumer Play Store search; mitigate by aiming at the prosumer/home-inventory half of Sortly's base first (insurance documentation is the killer use case).

### 2. Offline backcountry topo GPS — the "Gaia refugee camp" (17/20)

The strongest *pattern* match to the OBD2 pick: Gaia GPS (4.1M installs, 3.36★, Trustpilot 2.3) charges $40–90/yr and still demands an internet login before opening the offline maps people bought — in the backcountry, where that failure is a safety issue — while BackCountry Navigator PRO's 250k one-time buyers watch their app rot. ViewRanger's shutdown and onX's $30–100/yr pricing prove both the refugee flow and the ceiling. Map data is open (OSM + USGS/USFS tiles). **One-time price, truly-offline-first (no login gate ever), import GPX from Gaia/AllTrails in one tap.** This is the biggest prize of the three and the best strategic fit — `golf-core` already contains the GPS, Overpass/OSM parsing, and offline-course machinery — but the offline tile engine and layer catalog make it a bigger build than #1, and Organic Maps/OsmAnd serve the free casual tier.

### 3. Pet health records — the "11pets betrayal" (17/20)

A 2.12★ rating on 530k installs is the angriest user base found in either round: a rewrite that force-migrated users to a subscription *and* lost their pets' vaccination histories, with support literally unable to receive messages. No dominant free incumbent exists (competitors are vet-clinic-tethered). The build is trivial — multi-pet profiles, vaccination/medication schedules with reminders, weight charts, document photos, **all local-first with one-tap PDF/CSV export** — and "we can never lose your records because they live on your phone" is the counter-position to the exact betrayal. Ranked third only because monetization is less proven: 11pets was free for years, so the paying intent (maybe $9.99 one-time, family sync as the paid add-on) needs a smoke test before code.

**Suggested validation (same playbook as round 1):** land pages for #1 and #3 in a weekend waitlist A/B; for #2, mine the newest 500 Gaia reviews for feature-level demand ranking before committing to the tile-engine build.

## 🔄 Follow-up: the everyday-tools lens (same day)

Direction change after the first pass: prefer **simple tools a normal person uses daily or weekly** — small feature surface, no enthusiast gear, no content pipelines. That drops the backcountry GPS (enthusiast), Sortly (leans small-business), Guitar Pro (hobbyist), VSCO and the kids sandbox (content-heavy) down the priority list, and it triggered a supplemental two-agent sweep of the everyday-utility categories neither round had covered: notes, calendars, alarm clocks, keyboards, weather, calculators, QR/price scanners, timers, and shopping lists. Raw data: [`data/round2_everyday_notes_calendar_alarm_keyboard.json`](data/round2_everyday_notes_calendar_alarm_keyboard.json) and [`data/round2_everyday_weather_scanner_lists.json`](data/round2_everyday_weather_scanner_lists.json).

New rubric-passing finds from that sweep:

| App | Price | Installs | Rating | Core failure | Clonable? |
|---|---|---|---|---|---|
| Storm Shield | $1.49→$5.99/mo | 460k | 3.0★ | Alerts failed during actual tornadoes; support email bounces | ✅ NWS data is free |
| Alarm Clock Plus | ~$2 NoAds app | 5M | ~3.1★ | Abandoned; dismiss screen never appears on modern Android | ✅ but weak paid gate |
| Chrooma Keyboard | $2.49 lifetime IAP | 5.6M | 3.39★ | Abandoned 6+ yrs while still selling lifetime unlocks | ⚠️ Gboard is free |
| Evernote | ~$130+/yr | 100M+ | ~3.4★ | v10 rewrite + Bending Spoons price hikes; sync failures | ⚠️ Keep/Obsidian free |
| Jorte Calendar | $2.99/mo + packs | 29M | 3.4★ | Daily sync failures to Google Calendar; support silent | ⚠️ Google Calendar free |
| WeatherPro | was $2.99, now sub | 2M | 3.49★ | Paid buyers re-charged via Premium sub (DTN) | ⚠️ forecast-data licensing |
| ai.type keyboard Plus | $2.99 | 500k | 3.87★ | Abandoned + 31M-user data breach + ad fraud | ⚠️ Gboard is free |
| ShopSavvy | $4.99/mo Pro | 17M | 4.09★ | Paying users still see ads; price data wrong | ⚠️ price-data ops |

*(Also notable: the alarm/calendar/keyboard categories are full of dead paid apps that were **unpublished** — Timely, Swype, Minuum, Touch Calendar, Grocery IQ — demand signals with no live listing to position against. Shopping lists and calculators are healthy: every incumbent rates 4.4+.)*

### Everyday re-rank (same 1–5 rubric)

| # | Candidate niche | Failing incumbent(s) | Demand | Pain | Build | Moat | Total |
|---|---|---|---|---|---|---|---|
| 1 | **Pet health records** | 11pets (530k, 2.12★) | 3 | 5 | 5 | 4 | **17** |
| 2 | **Manual family budget ledger** | Expense IQ (1.3M, 3.49★) + Home Budget w/ Sync (175k, 3.75★) | 3 | 4 | 5 | 3 | **15** |
| 3 | **Pay-per-use mobile fax** | eFax (3M, 4.08★) | 4 | 4 | 4 | 3 | **15** |
| 4 | **One-time-price document scanner** | SwiftScan (6.8M, 3.86★) | 4 | 4 | 4 | 2 | **14** |
| 5 | **One-time-price PDF annotator** | ezPDF (500k, 2.8★) + Xodo exodus | 4 | 4 | 3 | 3 | **14** |
| 6 | **Simple notes with painless Evernote import** | Evernote (100M+, ~3.4★) | 5 | 4 | 3 | 2 | **14** |
| 7 | **Planner-style calendar with trustworthy Google sync** | Jorte (29M, 3.4★) | 4 | 4 | 4 | 2 | **14** |
| 8 | **Buy-once weather app** | WeatherPro (2M, 3.49★) + round-1 dead-widgets gap | 3 | 4 | 4 | 3 | **14** |
| 9 | **Severe-weather alerts that actually fire** | Storm Shield (460k, 3.0★) | 2 | 5 | 3 | 3 | **13** |
| 10 | **Ad-free price-comparison scanner** | ShopSavvy (17M, 4.09★) | 4 | 3 | 2 | 2 | **11** |

### Revised top-3 (everyday lens)

**1. Pet health records — the "11pets betrayal" (17/20).** Unchanged from the main board and now the leader: it's the simplest build of anything found in two rounds (multi-pet profiles, vaccine/medication reminders, weight charts, document photos — all local-first with one-tap PDF/CSV export), the pain is the most severe (2.12★ after users' records were lost/held hostage), and pet care is a routine weekly-use category in most households. Validation need: price-point smoke test, since 11pets was free for years.

**2. Manual family budget ledger (15/20).** The most "everyday" job on the board — people open a spending tracker daily. Two incumbents failed in the classic round-1 double pattern: Expense IQ (1.3M installs) abandoned in 2023 with lifetime buyers locked out and data lost, Home Budget with Sync decaying with broken family sync. No bank feeds needed — the paying base is *manual-entry* users, which kills the Plaid-cost moat problem that disqualifies most finance apps. Wedge: import their old backups (Expense IQ CSV, HomeBudget exports), reliable local backup, one-time price, family sync as the paid tier.

**3. One-time-price document scanner (14/20).** Scan-to-PDF is a weekly chore for most adults, and the incumbent story is pure rug-pull: Scanbot sold for $7 one-time, then re-paywalled owned features behind a $22.49/yr sub as SwiftScan (6.8M installs, 3.86★), on top of CamScanner's license drama. Build is commodity (CameraX + ML Kit on-device OCR). The honest risk — and why it's #3 despite the biggest audience: Google Drive, Microsoft Lens, and Adobe Scan are free and good, so the wedge is strictly "buy once, offline OCR, no account, no watermark, no cloud" privacy positioning.

*(The fax candidate scores 15 but drops out of the top-3 on the everyday test — it's an occasional-need tool. Evernote refugees are the biggest prize numerically, but a notes app faces the harshest free competition (Keep, Obsidian, OneNote) and sync infrastructure makes it a bigger build than anything above it.)*

## Legal note

Same as round 1: "clone" = build a competing app for the same job-to-be-done with original code, assets, name, and branding. Do **not** copy app names, icons, UI assets, or proprietary databases (Sortly's templates, Gaia's curated layers, Guitar Pro's soundbanks); trademark/copyright applies even when the functionality is fair game.
