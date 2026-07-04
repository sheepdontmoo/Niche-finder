# android-scan — DocStash

Android app (Kotlin + Compose) for **DocStash**, the document-scanner app picked in
[`research/round-2-candidates.md`](../research/round-2-candidates.md) as CamScanner's successor:
scan → OCR → searchable PDF, one $7.99 one-time unlock, no subscription ever. Wraps
[`scan-core`](../scan-core) (PDF assembly, naming, search, entitlement logic) with Google ML Kit's
Document Scanner + Text Recognition APIs, Compose UI, and Play Billing.

## Status: compiles clean, runtime-untested

`./gradlew assembleDebug` and `./gradlew lint` both pass with zero errors (verified in this
sandbox, including real dependency resolution against ML Kit and Play Billing's Maven artifacts —
not just a manual code review). What's **not** verified here: actually running the scan → OCR →
export flow on a device/emulator with live Google Play Services, and a real Play Billing sandbox
purchase. Both need a real device/emulator with Play Services, which this sandbox doesn't have
running (even though, contrary to expectations, its network policy did allow fetching the ML
Kit/Play Services *build-time* Maven dependencies — see the PR description for details). Verify
end-to-end on a device/emulator with Play Store before shipping.

## Project map

| Path | What it is |
|---|---|
| `app/src/main/kotlin/.../scanner/ScannerLauncher.kt` | Wraps `GmsDocumentScanner` — camera, edge detection, perspective correction, multi-page capture are entirely Google's UI flow |
| `app/src/main/kotlin/.../ocr/OcrProcessor.kt` | Wraps ML Kit Text Recognition (bundled Latin model) to build the OCR text layer |
| `app/src/main/kotlin/.../data/DocumentStore.kt` | On-disk library: JSON manifest + per-page JPEGs, all under app-private storage |
| `app/src/main/kotlin/.../billing/BillingManager.kt` | Play Billing wiring for the one-time `pro_unlock` product |
| `app/src/main/kotlin/.../ui/` | Library (search), Document (viewer/export), Settings (Pro unlock) screens |
| `../scan-core/` | PDF assembly, file naming, OCR search, entitlement — the genuinely testable, non-ML-Kit logic |

## Play Console setup (human — not doable from this sandbox)

1. Create the app in Play Console, upload a signed build to a closed test track.
2. Create **one** managed product (one-time product): ID `pro_unlock`, suggested price **$7.99**.
   Do not create a subscription product — the entire pitch is "we don't do that."
3. Add test accounts under Play Console → Setup → License testing before doing real purchase QA.

## Manual verification checklist (device/emulator with Play Store)

- [ ] Scan flow: Library → Scan → camera → auto edge detect → capture 2–3 pages → confirm
- [ ] OCR text shows up in search (Library search bar, type a word visible on a scanned page)
- [ ] "Add more pages" appends to an existing document instead of creating a new one
- [ ] Export images → share sheet appears with one JPEG per page, no watermark
- [ ] Export PDF while locked → routes to Settings' Pro card instead of exporting
- [ ] Play Billing sandbox purchase of `pro_unlock` → Settings flips to "Pro unlocked" immediately
      (this is the specific behavior [`Entitlement`](../scan-core/src/main/kotlin/com/nichefinder/scan/billing/Entitlement.kt)
      is designed to guarantee — no "charged but shows not purchased")
- [ ] Export PDF while unlocked → searchable PDF opens in another app with selectable text
- [ ] Force-close and reopen the app → library, Pro status, and search all survive restart

## Toolchain notes vs. the other two apps in this repo

Same AGP (8.7.3), Kotlin (2.0.21), Compose BOM (2024.12.01), `compileSdk`/`minSdk` (35/26) as
`android-app`/`android-golf`. One deliberate exception: `com.android.billingclient:billing-ktx` is
pinned to **8.0.0** (the first release with the modern one-time-product `PendingPurchasesParams`
API) rather than the current 9.1.0 — 9.x's AAR ships Kotlin metadata newer than this repo's Kotlin
2.0.21 can read (`compileDebugKotlin` fails with "Module was compiled with an incompatible version
of Kotlin"). Bump billing-ktx and the repo's Kotlin version together if upgrading later.
