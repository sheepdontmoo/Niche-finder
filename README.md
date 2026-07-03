# Niche-finder

Find paid Play Store apps with big install bases and bad ratings — proven paid demand where the
incumbent is failing — then build the winner.

| Stage | Artifact |
|---|---|
| 1. Find | [`research/play-store-clone-targets.md`](research/play-store-clone-targets.md) — 20-app scored board across 4 verticals ([raw data](research/data/)) |
| 2. Pick | **OBD2 car scanner**: abandoned Torque Pro (1M+ paid, 4.0★) + subscription-trap Carly (1.8M, 3.58★) |
| 3. Validate | [`research/obd2-validation.md`](research/obd2-validation.md) — feature demand ranking, switching triggers, adapter matrix, pricing, the Android Auto wedge |
|  4. Build | [`obd2-core/`](obd2-core/) — tested Kotlin protocol core · [`android-app/`](android-app/) — the app (Compose UI, BT/BLE/WiFi transports, demo mode) · [`AGENTS.md`](AGENTS.md) — briefing for agent IDEs |
