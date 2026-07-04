# scan-core

Pure-JVM Kotlin library for the parts of [`android-scan`](../android-scan)'s document-scanner app
(picked in [`research/round-2-candidates.md`](../research/round-2-candidates.md)) that are
genuinely ours to build and genuinely testable without Android.

## Why this module looks different from `obd2-core` / `golf-core`

Those two modules exist because the *hard* part of each app — ELM327 protocol parsing, J1979 PID
decoding, geo/rangefinder math — was real, novel logic worth writing and testing in a portable
JVM module. A document scanner doesn't have an equivalent: the actual hard part (camera capture,
edge detection, perspective correction, OCR) *is* Google ML Kit's `GmsDocumentScanner` and
`TextRecognition` APIs — both Android-only, both closed-source, and neither has any logic to
extract into a JVM module. There's no fake "scanner-core" here pretending otherwise.

What's left, and what this module actually holds, is everything genuinely ours:

- **`pdf/`** — assembles a multi-page **searchable PDF** by hand (no external PDF library): each
  page embeds its scanned JPEG verbatim as a `/DCTDecode` image XObject plus an invisible OCR text
  layer positioned from word bounding boxes. See [`PdfAssembler`](src/main/kotlin/com/nichefinder/scan/pdf/PdfAssembler.kt).
- **`naming/`** — default document titles, filesystem-safe sanitization, collision-safe uniquing,
  and per-page filenames. See [`DocumentNaming`](src/main/kotlin/com/nichefinder/scan/naming/DocumentNaming.kt).
- **`search/`** — search-by-OCR-text across the whole local document library, with ranking and
  preview snippets. See [`DocumentSearch`](src/main/kotlin/com/nichefinder/scan/search/DocumentSearch.kt).
- **`billing/`** — one-time-purchase entitlement state, deliberately modeled to prevent the exact
  bug that helped sink CamScanner's rating (users charged but shown "not purchased"). See
  [`Entitlement`](src/main/kotlin/com/nichefinder/scan/billing/Entitlement.kt).

The scanning and OCR calls themselves are exercised manually on a device/emulator, the same way
any other platform-API integration (camera, Bluetooth, GPS) is — there's nothing about them that
unit tests in a JVM module could meaningfully verify.

## Run tests

```
cd scan-core && ./gradlew test
```

No Android/ML Kit/Google Play Services dependency here — this runs anywhere, offline, same as
`obd2-core` and `golf-core`.
