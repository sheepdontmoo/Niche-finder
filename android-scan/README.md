# android-scan — DocStash

Android app (Kotlin + Compose) for **DocStash**, the document-scanner app picked in
[`research/round-2-candidates.md`](../research/round-2-candidates.md) as CamScanner's successor:
scan → OCR → searchable PDF, one $7.99 one-time unlock, no subscription ever. Wraps
[`scan-core`](../scan-core) (PDF assembly, naming, search, entitlement logic) with Google ML Kit's
Document Scanner + Text Recognition APIs, Compose UI, and Play Billing.

## Status: compiles clean, release-signed, live-tested, reviewed

Ready for the Play Console steps in [`PLAY_STORE_CHECKLIST.md`](PLAY_STORE_CHECKLIST.md). Concretely,
in this sandbox:

- `./gradlew assembleDebug`, `./gradlew lint`, `./gradlew assembleRelease`, and
  `./gradlew bundleRelease` all pass clean — real dependency resolution against ML Kit and Play
  Billing's Maven artifacts, a real signed + minified (R8) release APK and AAB, not just a
  read-through of the code.
- A Bugbot review pass (see git log: "Fix 6 issues from a Bugbot review pass") found and fixed 2
  high-severity issues (a delete-then-append file collision that could silently overwrite a page's
  image; a failed billing query that could revoke a paying user's Pro status) and 4 medium ones.
- Installed and ran the actual signed release APK on an emulator, seeded it with sample documents,
  and live-tested the Library screen, search, and branding. This caught 3 real bugs a code review
  never would have: two Material3 color roles (`primaryContainer`/`secondaryContainer` and, less
  obviously, `surfaceContainer*`) silently falling back to Material's baseline purple instead of the
  app's own palette, and a blocking file read on the main thread at startup. All three fixed.
- **Not verified here** (needs a device/emulator with a signed-in Google account and Play Store,
  which this sandbox's emulator doesn't have): the actual scan → OCR capture flow through ML Kit's
  dynamically-delivered document scanner module, and a real Play Billing sandbox purchase. Both
  showed up as expected in logs (ML Kit's `mlkit.docscan.crop`/`enhance` modules and Play Billing's
  API check both report unavailable without a signed-in account) rather than as app bugs.

## Project map

| Path | What it is |
|---|---|
| `app/src/main/kotlin/.../scanner/ScannerLauncher.kt` | Wraps `GmsDocumentScanner` — camera, edge detection, perspective correction, multi-page capture are entirely Google's UI flow |
| `app/src/main/kotlin/.../ocr/OcrProcessor.kt` | Wraps ML Kit Text Recognition (bundled Latin model) to build the OCR text layer |
| `app/src/main/kotlin/.../data/DocumentStore.kt` | On-disk library: JSON manifest + per-page JPEGs, all under app-private storage |
| `app/src/main/kotlin/.../billing/BillingManager.kt` | Play Billing wiring for the one-time `pro_unlock` product |
| `app/src/main/kotlin/.../ui/` | Library (search), Document (viewer/export), Settings (Pro unlock) screens |
| `../scan-core/` | PDF assembly, file naming, OCR search, entitlement — the genuinely testable, non-ML-Kit logic |
| `store-assets/` | Play Store hi-res icon, feature graphic, and a real app screenshot |
| `PLAY_STORE_CHECKLIST.md` | The ordered, human-side path from here to a published listing |

## Play Console setup

See [`PLAY_STORE_CHECKLIST.md`](PLAY_STORE_CHECKLIST.md) for the full ordered checklist (account
setup, upload keystore, store listing, the `pro_unlock` in-app product, closed testing requirement,
submission). Short version: create **one** in-app product (not a subscription) with ID `pro_unlock`
at **$7.99**, and add license testers before doing real purchase QA.

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
