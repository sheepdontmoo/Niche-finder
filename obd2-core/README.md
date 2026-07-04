# obd2-core

Pure-JVM Kotlin library implementing the protocol heart of the OBD2 scanner app picked in
[`research/play-store-clone-targets.md`](../research/play-store-clone-targets.md) and validated in
[`research/obd2-validation.md`](../research/obd2-validation.md). No Android dependencies — the
Android app wraps this with Bluetooth/BLE/WiFi transports, Compose UI, and Android Auto surfaces.

## What's implemented

- **`transport.ObdTransport`** — byte-stream abstraction the Android layer implements per medium
  (BT Classic RFCOMM, BLE GATT, WiFi TCP). Tests use `SimulatedElm327`, which emulates a cheap
  v1.5 clone including its quirks (`SEARCHING...` noise, `?` for unknown AT commands, headered
  CAN frames).
- **`elm.Elm327`** — adapter driver: reset/init sequence (ATZ→ATE0→ATL0→ATS0→ATH1→ATSP0),
  protocol detection via ATDPN, response parsing tolerant of the ELM error vocabulary
  (`NO DATA`, `CAN ERROR`, `UNABLE TO CONNECT`, …) and multi-ECU headered replies.
- **`pid.Pid`** — SAE J1979 Mode 01 PIDs (RPM, speed, coolant, MAF, fuel trims, voltage, oil temp,
  …) with decode formulas, units, and display formatting; PID-support bitmap walking (0100/0120/…).
- **`dtc`** — SAE J2012 trouble-code decoding (P/C/B/U from the top two bits), Mode 03/07 parsing
  with CAN count-byte and zero-padding handling.
- **`ObdSession`** — the app-facing façade: connect → discover → read gauges / read & clear codes.

## Run tests

```
cd obd2-core && ./gradlew test
```

16 tests cover J1979 decode formulas, DTC encoding edge cases (all four system letters), ELM error
vocabulary, and a full simulated session (init order, protocol detection, discovery stopping at the
right bitmap, live reads, stored/pending codes).

## Deliberate design points

- Clone tolerance is a first-class requirement (see validation doc §3): unknown AT commands are
  non-fatal, responses are parsed defensively, spaces/linefeeds are normalized.
- `ATH1` (headers on) from the start: multi-ECU parsing needs it, and it's required later for
  manufacturer-specific ECUs and EV packs.
- Next in this module: ISO-TP multi-frame reassembly (VIN via Mode 09, freeze frames), custom-PID
  definitions (Torque CSV import), Mode 06, and an STN fast-path.
