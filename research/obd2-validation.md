# OBD2 Niche Validation — Review & Forum Mining

**Date:** 2026-07-02. Deep-dive validation of the clone pick from
[`play-store-clone-targets.md`](play-store-clone-targets.md). Findings from car-model forums,
XDA, Reddit, Trustpilot, and review-mining sites.

## 1. Feature demand ranking (strongest first)

1. **Android Auto / CarPlay gauge display** — the single loudest demand in the niche. A 180+ page
   XDA thread exists just to hack Torque onto Android Auto ([XDA OBD2 Plugin](https://xdaforums.com/t/app-5-0-obd2-plugin-for-android-auto-use-torque-with-androidauto.3657805/));
   the open-source [AA-Torque](https://github.com/agronick/aa-torque) project exists for the same
   reason; how-to threads span at least 8 model forums (Mazda3, Subaru, Silverado, Giulia,
   Ridgeline, Mach-E, MG4, VW ID). Users compare phone-based AA gauges to "$$$$ external gauge
   sets like Banks iDash and Edge CTS."
2. **EV/hybrid battery data** — SOC, SOH, cell/module voltages, HV battery temps. Fast-growing;
   Lightning/Mach-E/MG4/VW ID forums maintain community PID profiles ([example](https://www.macheforum.com/site/threads/car-scanner-or-torque-pro-any-way-to-see-individual-cell-or-module-voltages.28889/)).
3. **Manufacturer-specific PIDs / vehicle profiles** — transmission temp, HVAC, battery health per
   model; Torque's custom-PID CSV ecosystem is a legacy asset users don't want to lose.
4. **DPF regen monitoring/forcing** (diesel) — Carly markets this as a premium hook.
5. **Light coding/adaptations** — Car Scanner only covers some VAG/Toyota platforms; the gap drives
   users to Carly/OBDeleven despite hating their pricing.
6. **Data logging + CSV export at high polling rate** — Torque criticized for ~0.5 Hz polling.
7. **BLE adapter support** — requested in Torque for 8+ years, never shipped; Torque cannot talk to
   BLE-only adapters (OBDLink CX, Veepeak BLE+).
8. **Emissions/I&M readiness indicators**.

## 2. Switching triggers

**Torque Pro users leave because:** abandonment (no updates >1 yr, called "abandoned" on forums),
no BLE (app can't connect to their new adapter at all), no native Android Auto (hacks broke on
AA 6.4+), no EV support, slow polling. Notably, buyers cite *active development itself* as the
purchase reason for Car Scanner Pro.

**Car Scanner's open wound:** its native Android Auto view is **text-only — ~3.5 parameters, no
graphical gauges** ([Mach-E thread](https://www.macheforum.com/site/threads/car-scanner-now-showing-some-info-on-android-auto-without-needing-work-arounds.38541/)).
Every forum thread about it complains the same way. Free tier has ads, daily limits, gauge-count caps.

**Carly refugees:** adapter goes functionally dead without subscription; Trustpilot reports of 7–8×
repeated charges after cancellation; reviewers call it "the worst-value Bluetooth tool." These
users actively seek one-time-payment alternatives (BimmerCode ~€30, OBDeleven credits).

## 3. Adapter landscape (must-support matrix)

| Adapter | Price | Link | Notes |
|---|---|---|---|
| ELM327 "v1.5" clones | $10–25 | BT Classic (SPP) | The overwhelming majority. "v1.5" never officially existed; "v2.1" clones are worse (hacked v1.0 firmware, missing AT commands) |
| Veepeak OBDCheck BLE/BLE+ | $20–25 | BLE | "Safest choice for most buyers" |
| Vgate iCar Pro / vLinker MS/FS | $25–40 | BT/BLE/WiFi | Value tier; auto-sleep favorite |
| OBDLink MX+ / CX | $100–140 | BT / BLE | STN22xx chips, ~3× faster multi-PID polling; enthusiast gold standard |

Requirements this implies: Bluetooth Classic **and** BLE **and** WiFi transports; a clone-tolerance
layer (missing AT commands answered '?', bogus voltage, fragmented responses); an adapter
authenticity check (a standalone "ELM327 Identifier" app exists — the pain is real); an
STN fast-path (ST commands, multi-PID batching) for the enthusiast tier.

## 4. Pricing signal

- Validated one-time price points: Torque Pro **$4.95** (1M+ installs), Car Scanner Pro **$7.99**
  (markets itself "no subscriptions"), BimmerCode **~€30**, OBDeleven credits.
- **Subscription is the villain of this niche**: Carly's €80–95 adapter + €50–90/yr is universally
  framed as a subscription trap; BlueDriver's #1 Amazon selling point is literally "No Subscription
  Fee" in the product title.
- Model: freemium + one-time Pro unlock in the **$8–30 band**, with a higher one-time tier
  (~$20–30) for coding/EV packs, mirroring BimmerCode.

## 5. Android Auto: the wedge

- Google only approves certain AA app categories; generic gauge dashboards aren't one, so Torque
  never shipped natively and the hack ecosystem (AAAD, Screen2Auto) broke on AA 6.4+.
- Car Scanner shipped native AA but renders **text lists only**.
- **Product takeaway:** rich graphical gauges on Android Auto within Google's rules is the unsolved
  problem of this niche. Whoever solves it (cluster/instrument APIs, navigation-template rendering,
  approved-category canvas surfaces) captures the loudest unmet demand.

## MVP definition (from the evidence)

**P0 (launch):** BT Classic + BLE + WiFi transports with clone-tolerance · realistic gauges +
dashboards on phone · DTC read/clear with plain-language descriptions · PID discovery + standard
Mode 01 live data · emissions readiness · CSV logging/export · free tier generous, one-time Pro
$9.99.

**P1 (differentiators):** Android Auto graphical gauges (the wedge) · community vehicle-profile
import (accept Torque CSV custom PIDs — zero-friction migration for the 1M orphaned users) ·
EV/hybrid packs (Lightning, Mach-E, MG4, ID.x first — forums already maintain the PID lists) ·
STN fast-path.

**P2 (expansion):** DPF regen monitoring · light coding on 1–2 platforms · fleet/trip features.

**Explicit non-goals:** subscriptions of any kind; proprietary adapter hardware.
