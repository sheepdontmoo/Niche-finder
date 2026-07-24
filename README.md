# Litmas

**Check every trade.** — AI candlestick & chart detector.

Snap a photo or screenshot of any trading chart and Litmas gives you a second
opinion on it: pattern detection, support and resistance, trend direction, and
a plain-language **BUY / HOLD / SELL** call with a confidence score. Every scan
is saved to your history, and every screen that shows a call carries the
educational-only notice.

Brand is locked — see [`LITMAS-BRAND.md`](LITMAS-BRAND.md). One colour, orange
`#FF6A00`. The logo is a single test strip with a solid top third.

Built with **Next.js (App Router)** + the **Anthropic API** (Claude Opus 4.8
vision with structured outputs), wrapped for iOS/Android with **Capacitor**.

## How it works

1. Pick or shoot a chart. The image is downscaled client-side (2000px JPEG for
   analysis, 180px thumbnail for history).
2. `POST /api/analyze` sends it to Claude Opus 4.8 with a second-opinion system
   prompt and a strict Zod schema (`lib/analysis.ts`).
3. The result renders as the call, confidence, summary, patterns, key levels,
   invalidation, indicator read and risk notes.
4. The scan is saved to on-device history (`lib/history.ts` — localStorage,
   capped at 30 entries, never uploaded).
5. Free scans are metered per anonymous device ID server-side; beyond that the
   paywall sells a 3-day trial with monthly or yearly auto-renewing plans
   through Play Billing / StoreKit (RevenueCat — `lib/billing.ts`), with a
   webhook at `/api/revenuecat` mirroring entitlements into Redis.

Non-chart images are detected (`is_chart: false`) and don't consume a scan.

## Run it locally

```bash
npm install
cp .env.example .env.local   # add your ANTHROPIC_API_KEY
npm run dev
```

http://localhost:3000 — works in a phone browser too (the file input opens the
camera on mobile).

## Deploy (web backend)

Any Node host; Vercel is one-click. Environment variables:

- `ANTHROPIC_API_KEY` — required
- `UPSTASH_REDIS_REST_URL` + `UPSTASH_REDIS_REST_TOKEN` — optional but
  recommended; enables **server-side** free-scan metering per device
  (`lib/metering.ts`). Without them only the client-side gate applies.

## Mobile apps

Native projects live in `android/` and `ios/` (bundle id `com.litmas.app`),
with icons and splash screens generated from the brand mark.

```bash
NEXT_PUBLIC_API_BASE_URL=https://your-deployment.vercel.app npm run build:mobile
npx cap sync
# Android: cd android && ./gradlew bundleRelease   (see docs/play-store.md)
# iOS:     npx cap open ios                        (requires a Mac + Xcode)
```

**Full Play Store submission guide: [`docs/play-store.md`](docs/play-store.md).**

## Regenerating brand assets

```bash
python3 scripts/make-assets.py                 # icon + splash source art
npx @capacitor/assets generate --android --ios # platform icon sets
python3 scripts/make-store-assets.py --shots-dir <dir-of-app-screenshots>
```

## Billing

Configure RevenueCat and the Play subscription products per
[`docs/play-store.md`](docs/play-store.md) §5, then set
`NEXT_PUBLIC_REVENUECAT_ANDROID_KEY` and `REVENUECAT_WEBHOOK_SECRET`. Leave the
key unset and the paywall still renders but cannot transact — useful for a
free-only v1.

> **Note:** `@revenuecat/purchases-capacitor` ships a `prepare` script that can
> wipe its own `dist/` during an install. If imports from it suddenly fail, run
> `rm -rf node_modules/@revenuecat && npm install --ignore-scripts`.

## Roadmap

- Shareable result cards
- Multi-timeframe: several screenshots of the same asset in one check

## Disclaimer

AI-generated educational analysis only — **not financial advice**. Trading
involves substantial risk of loss.
