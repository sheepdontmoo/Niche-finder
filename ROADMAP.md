# Build Roadmap — OBD2 Scanner App

Simple, ordered plan from here to Play Store launch. Workflow: develop with an agentic IDE
(e.g. Google Antigravity) on a machine with normal internet access, finish/package in Android
Studio, publish via Play Console.

## Step 0 — Hardware (~$40, this week)
- [ ] Buy a cheap ELM327 Bluetooth clone (~$15) — what most customers own
- [ ] Buy a Veepeak OBDCheck BLE (~$25) — the modern adapter type Torque never supported
- [ ] Google Play developer account ready

## Step 1 — Android app around `obd2-core` (2–3 weeks)
The protocol engine is already written and tested (16/16) in [`obd2-core/`](obd2-core/).
Build the app shell around it — 4 screens for v1:
- [ ] **Connect**: Bluetooth Classic + BLE + WiFi transports implementing `ObdTransport`; adapter scan/pair; clone-tolerance
- [ ] **Dashboard**: live gauges (RPM, speed, coolant, voltage, throttle) driven by `ObdSession`
- [ ] **Check Engine**: read stored/pending codes with plain-language descriptions; clear with confirmation
- [ ] **Settings**: units (°C/°F, km/mi), demo mode (uses `SimulatedElm327`), Pro unlock
- Tech: Kotlin + Jetpack Compose, min SDK ~26, Play Billing for the one-time Pro purchase

## Step 2 — Real-car test (a weekend)
- [ ] Both adapters, real car: connect, gauges live while driving, read codes
- [ ] Note any adapter quirks → feed fixes back into `obd2-core`

## Step 3 — Closed beta (2 weeks)
- [ ] Play Console closed track. (New personal accounts need ~12 testers for 14 days before production — the beta does double duty.)
- [ ] Recruit testers from the forums where Torque/Carly users already complain (list in [`research/obd2-validation.md`](research/obd2-validation.md))
- [ ] Collect car-model + adapter compatibility reports

## Step 4 — Launch (1 week)
- [ ] Free: gauges + read codes. **Pro $9.99 one-time, no subscription** — the headline
- [ ] Original name & branding — nothing resembling "Torque"/"Carly" (trademark safety)
- [ ] Store listing, screenshots, privacy policy (app stores no personal data — easy)

## Step 5 — The wedge (post-launch)
- [ ] **Android Auto graphical gauges** — the niche's loudest unmet demand (validation doc §5)
- [ ] **Torque CSV custom-PID import** — one-tap migration for 1M orphaned paid users
- [ ] EV/hybrid packs (Lightning, Mach-E, MG4, ID.x — forums already maintain the PID lists)
- [ ] ISO-TP multi-frame in `obd2-core` (VIN, freeze frames) + STN fast-path

## Division of labor
- **Agent/IDE:** all code, store listing copy, beta recruiting posts
- **Human:** buy adapters, drive the car, Play Console clicks, screenshots

Target: paid launch in 6–8 weeks.
