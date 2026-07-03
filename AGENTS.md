# Agent Briefing — Niche Scanner (OBD2 car scanner app)

You are working on an Android OBD2 car-scanner app. The market research, product decisions, and
protocol core are done — read this file, then continue from Step 1 below.

## Project map

| Path | What it is | Status |
|---|---|---|
| `research/` | Market research: why this app, feature priorities, pricing | Done — treat as product requirements |
| `obd2-core/` | Pure-JVM Kotlin protocol library (ELM327 driver, J1979 PIDs, DTC parsing) | **Done, 16/16 tests pass.** Run `gradle test` inside it after any change |
| `android-app/` | Android app (Kotlin + Compose) wrapping obd2-core | Code written but **never compiled** (authored in an environment without Google Maven access). Expect minor compile fixes |
| `ROADMAP.md` | Ordered build plan to Play Store launch | Follow it |

## Hard product rules (from the research — do not violate)

1. **No subscriptions.** One-time Pro purchase only. This is the core positioning.
2. **Standard ELM327 adapters only** — Bluetooth Classic, BLE, and WiFi. No proprietary hardware.
3. **Original branding.** Nothing resembling "Torque", "Carly", or "Car Scanner" in name, icon, or UI.
4. Clone tolerance is a feature: cheap "v1.5" clones answer `?` to unknown AT commands, fragment
   responses, and lie about voltage. Never assume a well-behaved adapter.

## Step 1 — Make it build and run (do this first)

1. Open `android-app/` as the Gradle project (it pulls in `../obd2-core` via `includeBuild`).
2. Sync + compile. Fix any compile errors (imports, API signatures, Compose versions). Keep fixes
   minimal; don't restructure.
3. Run on an emulator. **Demo mode must work end-to-end with no hardware**: Connect screen →
   "Try demo mode" → Dashboard shows live gauges (RPM 1726, speed 75 km/h, coolant 83°C…) →
   Codes screen reads P0301 + P0420 with descriptions → Clear works.
4. If you change anything in `obd2-core/`, run its test suite (`gradle test` in that directory)
   and keep all 16 green.

## Step 2 — Real-hardware hardening (needs the human + adapters + car)

- Test Bluetooth Classic with a cheap ELM327 clone, BLE with a Veepeak OBDCheck BLE.
- Log every raw exchange in debug builds; save transcripts of misbehaving adapters as test cases.

## Step 3 — Beta polish

- Play Billing: one one-time product (`pro_unlock`, $9.99). Gate: >4 gauges, CSV logging.
- App icon + name decision with the human, privacy policy page, closed-test track upload.

## Backlog (post-beta, in priority order from `research/obd2-validation.md`)

1. Android Auto graphical gauges — the #1 differentiator in the niche
2. Torque CSV custom-PID import — migration path for 1M orphaned Torque Pro users
3. EV/hybrid packs (community PID lists exist for Lightning, Mach-E, MG4, VW ID)
4. ISO-TP multi-frame in obd2-core (VIN via Mode 09, freeze frames), Mode 06, STN fast-path

## Conventions

- Kotlin, Compose Material3, coroutines. `obd2-core` stays pure JVM (no Android imports) so its
  tests run anywhere and the logic stays portable.
- Keep `ObdTransport` the only seam between app and adapter — new adapter types are new
  implementations, nothing else changes.
