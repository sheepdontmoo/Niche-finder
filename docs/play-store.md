# Google Play submission kit — Litmas

Everything needed to go from this repo to a live Play Store listing. The Android project is already generated, icon/splash assets are in place, and the release build pipeline is verified (`bundleRelease` produces a signed `.aab`).

## 1. One-time local setup

```bash
npm install
# Android SDK + Java 17+ required (Android Studio installs both)
```

Generate your **upload keystore** (do this once, back it up — losing it is painful):

```bash
keytool -genkeypair -v -keystore android/upload-keystore.jks \
  -alias upload -keyalg RSA -keysize 2048 -validity 10000
```

Create `android/key.properties` (gitignored):

```
storeFile=upload-keystore.jks
storePassword=YOUR_PASSWORD
keyAlias=upload
keyPassword=YOUR_PASSWORD
```

## 2. Deploy the backend first

The mobile app is a static shell that calls your hosted API:

1. Deploy this repo to Vercel (or any Node host) with env vars:
   - `ANTHROPIC_API_KEY` (required)
   - `UPSTASH_REDIS_REST_URL` + `UPSTASH_REDIS_REST_TOKEN` (recommended — enables server-side scan limits; free tier at upstash.com)
2. Note the URL, e.g. `https://litmas.vercel.app`

## 3. Build the release bundle

```bash
NEXT_PUBLIC_API_BASE_URL=https://your-deployment.vercel.app npm run build:mobile
npx cap sync android
cd android && ./gradlew bundleRelease
```

Output: `android/app/build/outputs/bundle/release/app-release.aab` — this is what you upload to Play Console.

Bump `versionCode`/`versionName` in `android/app/build.gradle` for every subsequent upload.

## 4. Play Console setup

Create the app in [Play Console](https://play.google.com/console) → **Create app** → App name "Litmas", App/Free (with in-app purchases later).

### Store listing copy (ready to paste)

**App name:** Litmas — AI Chart Detector

**Short description (80 chars max):**
> Check every trade. Scan any chart for patterns, levels and a buy/hold/sell read.

**Full description:**
> Litmas is the second opinion on any position — before you enter, and while you're holding.
>
> Point your camera at any trading chart — stocks, crypto, forex — or upload a screenshot, and Litmas reads it in seconds.
>
> WHAT YOU GET PER SCAN
> • A plain-language BUY, HOLD or SELL call, with the reasoning in one line
> • A confidence score, so you know how much the read is worth
> • Chart patterns detected, and what each one typically signals
> • Support and resistance levels read off the chart
> • The level that would prove the read wrong
> • Indicator observations (RSI, MACD, volume, moving averages)
> • Honest risk notes on anything that weakens the read
>
> SAVED HISTORY
> Every scan is kept on your device so you can look back at what you were seeing at the time.
>
> WORKS EVERYWHERE
> Any platform, any broker, any market — if you can screenshot it, Litmas can read it.
>
> SUBSCRIPTION
> Litmas starts with free scans. Unlimited checks are a subscription, offered monthly or yearly with a 3-day free trial. Subscriptions renew automatically unless cancelled at least 24 hours before the period ends; manage or cancel any time in your Google Play account settings.
>
> IMPORTANT
> Litmas provides AI-generated educational analysis only. It is not financial advice, and past patterns do not predict future results. Trading involves substantial risk of loss.

**Category:** Finance
**Tags:** technical analysis, trading, charts, candlestick

### Privacy policy URL

Point to your deployment: `https://your-deployment.vercel.app/privacy` (page ships in this repo).

### Data safety form answers

| Question | Answer |
|---|---|
| Does your app collect or share user data? | Yes |
| Photos — collected? | Yes — chart image, sent for processing (analysis), not shared for ads, ephemeral processing, optional (user-initiated) |
| Device or other IDs — collected? | Yes — anonymous app-generated ID for scan limits; not linked to identity; not shared |
| Data encrypted in transit? | Yes (HTTPS) |
| Can users request deletion? | Yes (contact email; also clearing app data removes the device ID) |
| Anything shared with third parties? | Chart images processed by AI provider (Anthropic) as a service provider |

### Content rating questionnaire

Category: Utility/Productivity/Finance. No user-generated content, no violence, no gambling (the app does not facilitate real-money wagering — say **no** to gambling questions; it's an analysis tool). Expected rating: Everyone / PEGI 3. The "simulated gambling" question is also **no**.

### Finance app declarations

Play may ask finance-specific questions. Litmas is **not** a trading app, does not execute trades, does not hold funds, and is not a personal financial advisory service — it provides general educational analysis of user-supplied images. Keep the disclaimer visible in screenshots you upload.

## 5. Monetization — built, needs configuring

Play Billing is wired through RevenueCat (`lib/billing.ts`, plugin
`@revenuecat/purchases-capacitor`). The AAB already contains the billing
client and the `com.android.vending.BILLING` permission. What's left is
account configuration:

1. **Play Console → Monetize → Products → Subscriptions.** Create
   `litmas_monthly` and `litmas_yearly`, each with a **3-day free trial**.
2. **RevenueCat → Project.** Connect the Play account, import both products,
   put them in an Offering as the *Monthly* and *Annual* packages, and create
   an entitlement with the identifier **`pro`** (this exact string is in
   `lib/billing.ts`).
3. **API keys.** Copy the public Google Play SDK key into
   `NEXT_PUBLIC_REVENUECAT_ANDROID_KEY` and rebuild the mobile bundle. Without
   it the paywall renders but cannot sell.
4. **Webhook.** RevenueCat → Integrations → Webhooks → URL
   `https://<your-deployment>/api/revenuecat`, Authorization header set to the
   same value as `REVENUECAT_WEBHOOK_SECRET`. This mirrors entitlements into
   Redis so the API stops gating scans for subscribers.

Prices shown in the paywall come from the store at runtime; the constants in
`app/page.tsx` are only a fallback for the web build.

**Store requirements already handled:** 3-day trial, monthly + yearly
auto-renewing plans, the auto-renew/cancellation disclosure on the paywall, a
**Restore purchase** button, and links to Terms and Privacy from the paywall.

Tip: you can still ship v1 free-only to clear first review faster — just leave
the RevenueCat key unset and the paywall stays non-transacting.

## 6. Store graphics — already made

`store-assets/` contains ready-to-upload marketing assets, generated by
`scripts/make-store-assets.py`:

- `feature-graphic.png` — the required 1024×500 feature graphic
- `screenshot-1..4.png` — four branded 1080×1920 listing screenshots with
  bold headline captions (Check every trade → Buy, hold or sell → Patterns &
  levels → Every scan, saved)

The app icon (512×512 for Play Console) can be exported from
`resources/icon.png` (it's 1024×1024 — Play accepts it downscaled, or upload
as-is where 1024 is allowed).

## 7. Testing tracks — the part people get blocked on

- **Internal testing** (up to 100 testers, instant): upload the `.aab`, add
  your own Gmail as a tester, install via the opt-in link. Use this first —
  it's live within minutes and needs no review.
- **⚠️ Personal developer accounts created after Nov 13, 2023** must run a
  **closed test with at least 12 testers opted in for 14 consecutive days**
  before Google grants production access. If your account is affected, start
  the closed test immediately — it's the long pole in the schedule. Friends +
  family + a "testers wanted" post in a trading Discord/subreddit gets to 12
  fast. Older or organization accounts skip this entirely.
- **Production** after that — first production review typically takes a few
  days for a new app.

## 8. Submission checklist

- [ ] Backend deployed with `ANTHROPIC_API_KEY` + Upstash metering
- [ ] `NEXT_PUBLIC_API_BASE_URL` baked into the mobile build
- [ ] Upload keystore generated and backed up
- [ ] `bundleRelease` `.aab` uploaded to the **internal testing** track
- [ ] Installed on a real device via the opt-in link; full scan flow works
- [ ] Upload `store-assets/screenshot-1..4.png` + `store-assets/feature-graphic.png`
- [ ] Privacy policy URL live
- [ ] Data safety + content rating forms completed
- [ ] Closed test with 12 testers running (if your account requires it)
- [ ] Promote to production

## iOS (when the Apple account is ready)

The `ios/` project is already generated with icons/splash. On a Mac: `npm run build:mobile && npx cap sync ios && npx cap open ios`, set the signing team in Xcode, archive, and upload via Xcode Organizer. Apple will require: App Privacy labels (same answers as data safety above), Sign in with Apple only if you add third-party login (we have none), and StoreKit/RevenueCat for the subscription.
