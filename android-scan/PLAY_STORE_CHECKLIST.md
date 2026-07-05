# DocStash — Play Store Upload Checklist

Everything code-side is done and verified (see "What's already done" below). What's left is entirely
human steps in Play Console — account setup, an upload keystore only you should hold, and clicking
through the listing form. This doc is the ordered path from here to a published (or testing-track)
listing.

## What's already done

- [x] App builds, a signed release AAB comes out the other end (`./gradlew bundleRelease`), verified
      in-sandbox with a throwaway test keystore — see "Generate your upload keystore" below for the
      real one only you should generate.
- [x] Release build is minified (R8) and verified clean — no crashes, no missing-class issues across
      kotlinx-serialization + ML Kit + Play Billing + Compose.
- [x] A Bugbot review pass found and fixed 2 high-severity + 4 medium-severity issues (file-overwrite
      data loss after deleting a page, Pro unlock getting revoked on a transient billing error,
      manifest corruption/concurrency handling, an OCR crash on edge-case text, a hidden-error-dialog
      bug). See the git log for details.
- [x] Live-tested on an emulator: app installs, launches, library/search/branding all render
      correctly. Caught and fixed 3 real branding bugs this way (FAB/nav colors and card backgrounds
      silently defaulting to Material's baseline purple instead of the app's own palette; a
      main-thread file I/O call at startup) — none of which showed up from reading the code alone.
- [x] Store listing copy: [`store-listing-docstash.md`](../store-listing-docstash.md)
- [x] Privacy policy page: [`docs/privacy-docstash.html`](../docs/privacy-docstash.html)
- [x] Hi-res icon, feature graphic, and a real app screenshot: [`store-assets/`](store-assets/)

## 1. Google Play Developer account (human, ~15 min + review time)

- [ ] Sign up at [play.google.com/console](https://play.google.com/console) — $25 one-time fee,
      government ID verification required for new accounts.
- [ ] **Note the closed-testing requirement**: personal developer accounts created after
      November 13, 2023 must run a closed test with 12–20 testers for 14 consecutive days before
      Play Console allows a production release. Budget for that timeline — recruit testers early.

## 2. Generate your upload keystore (human — do this yourself, don't reuse any test keystore)

```bash
keytool -genkeypair -v -keystore docstash-upload-key.jks \
  -alias upload -keyalg RSA -keysize 2048 -validity 10000
```

- [ ] Store `docstash-upload-key.jks` and its password somewhere durable (password manager +
      offline backup). **If you lose this file, you lose the ability to ever publish an update to
      this app under the same listing** — Play Console has an account-recovery process for this but
      it's slow and best avoided entirely.
- [ ] Never commit this file or its password to git. The build already reads them from environment
      variables (`ANDROID_KEYSTORE`, `ANDROID_KEYSTORE_PASS`) — see
      [`app/build.gradle.kts`](app/build.gradle.kts) — so no code change is needed to use it.
- [ ] Build the real release bundle:
  ```bash
  export ANDROID_KEYSTORE=/path/to/docstash-upload-key.jks
  export ANDROID_KEYSTORE_PASS=<your password>
  cd android-scan && ./gradlew bundleRelease
  # -> app/build/outputs/bundle/release/app-release.aab
  ```
- [ ] When Play Console asks about Play App Signing (it will, and you should accept it — it's the
      default and recommended path): Google re-signs the app for distribution with its own key,
      using your upload key only to verify it's really you uploading. This is unrelated to and
      doesn't reduce the importance of keeping your upload key safe.

## 3. Create the app in Play Console

- [ ] Play Console → Create app → name it, select **Free** app type with **in-app products**
      (the one-time Pro unlock is an in-app purchase, not a paid app download — the app itself is
      free to install).
- [ ] Upload `app-release.aab` to an **Internal testing** track first to unlock the rest of the
      Console (Play Console requires one upload before most other setup screens become usable).

## 4. Main store listing

Copy directly from [`store-listing-docstash.md`](../store-listing-docstash.md):

- [ ] App name: `DocStash: PDF Scanner & OCR`
- [ ] Short description (80 char)
- [ ] Full description (4000 char)
- [ ] Category: Productivity
- [ ] Contact email: `basdesignco@gmail.com`
- [ ] Privacy policy URL: `https://sheepdontmoo.github.io/Niche-finder/privacy-docstash.html`
      — **enable GitHub Pages on this repo first** if it isn't already (Settings → Pages → serve
      from `/docs`), then confirm the URL actually loads before submitting.

Graphics — all in [`store-assets/`](store-assets/), all pre-verified against Play's exact format
requirements (dimensions, color depth, no-alpha-where-required):

- [ ] App icon: `store-assets/icon-512.png` (512×512, 32-bit PNG with alpha)
- [ ] Feature graphic: `store-assets/feature-graphic-1024x500.png` (1024×500, 24-bit PNG, no alpha)
- [ ] Phone screenshot: `store-assets/screenshot-1-library.png` (1080×2160, real capture of the
      running app's Library screen with sample documents)
- [ ] **Add 1–3 more phone screenshots before submitting** — Play requires a minimum of 2, and
      recommends 4+. This one screenshot is genuinely captured from the running app (not a mockup);
      grab 2–3 more the same way, in about a minute, using Android Studio's emulator (which has
      real hardware acceleration and won't fight you the way this sandbox's did):
      1. Run the app, tap into a document → screenshot the page grid / export buttons
      2. Tap Settings → screenshot the Pro unlock card
      3. Type a word into search → screenshot a matched result
      - Alt text suggestions (140 char max each, per Play's guidelines): "The document library with
        saved scans" / "A scanned document's pages, with export options" / "The Pro unlock screen"

## 5. Content rating questionnaire

- [ ] Fill out honestly per [`store-listing-docstash.md`](../store-listing-docstash.md#content-rating-questionnaire-answers):
      no violence, no user-generated content shared with others, no location data, no account or
      personal data collected. Expect **Everyone / PEGI 3**.

## 6. Data safety form

- [ ] Per [`store-listing-docstash.md`](../store-listing-docstash.md#data-safety-form): no data
      collected or shared by the app (documents/OCR text/settings are local-only, no analytics SDK,
      no ad SDK). Purchases are processed entirely by Google Play. No account exists, so there's
      nothing to "delete" beyond uninstalling the app.
- [ ] Declare the app **does not share data with third parties** and **does not collect any of the
      listed data categories** — this should be a short form given the app's actual architecture.

## 7. App content declarations

- [ ] Ads: declare **no ads** (true — there's no ad SDK anywhere in the app).
- [ ] Target audience: general audience, not primarily directed at children.
- [ ] Government apps / COVID-19 apps / financial features questionnaires: not applicable, answer no.

## 8. Set up the one-time Pro unlock product

- [ ] Play Console → Monetize → Products → **In-app products** (not Subscriptions — this is the one
      hard rule that must never be violated for this app).
- [ ] Create product ID exactly `pro_unlock` (must match `Entitlement.PRO_PRODUCT_ID` in
      [`scan-core`](../scan-core/src/main/kotlin/com/nichefinder/scan/billing/Entitlement.kt) — if
      you ever rename it, update both places).
- [ ] Set price: **$7.99, one-time** (per the research positioning — CamScanner charged $1.99 once,
      then subscriptions on top; $7.99 once, never again, is the entire pitch).
- [ ] Activate the product. It won't be purchasable by testers until the app build containing the
      Billing wiring is live on at least a testing track.
- [ ] Add license testers (Play Console → Setup → License testing) before doing any real purchase
      QA — this lets you complete test purchases without being charged real money.

## 9. Closed testing (required before production, see step 1)

- [ ] Create a closed testing track, add 12–20 tester emails, share the opt-in link.
- [ ] Let it run 14 consecutive days minimum with active testers (Play Console tracks this
      automatically once testers have actually installed and opted in — recruiting testers who
      never install doesn't count).
- [ ] Use this window to verify the checklist in
      [`android-scan/README.md`](README.md#manual-verification-checklist-deviceemulator-with-play-store)
      on a real device: full scan → OCR → export flow, and a real (test-account) Pro purchase.

## 10. Submit for review

- [ ] Once closed testing satisfies the requirement and everything above is filled in, promote the
      release to Production from Play Console.
- [ ] First-time app reviews typically take a few days. Subsequent updates are usually faster.

## Ongoing: version bumps

Each new release needs `versionCode` incremented and `versionName` updated in
[`app/build.gradle.kts`](app/build.gradle.kts) before running `bundleRelease` again — Play Console
rejects an upload with a `versionCode` it's already seen.
