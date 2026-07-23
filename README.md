# ChartDetector 📈

Snap a photo (or screenshot) of any trading chart and get an instant AI technical analysis — trend, chart patterns, support/resistance levels, indicator reads, and a hypothetical trade setup.

Built with **Next.js (App Router)** + the **Anthropic API** (Claude Opus 4.8 vision with structured outputs).

## How it works

1. User taps the dropzone → takes a photo or picks a screenshot of a chart.
2. The image is downscaled client-side (max 2000px, JPEG) to keep requests fast and cheap.
3. `POST /api/analyze` sends the image to Claude Opus 4.8 with a technical-analyst system prompt and a strict Zod-backed output schema (`lib/analysis.ts`).
4. The structured result renders as cards: verdict + confidence, summary, patterns, key levels, hypothetical setup, indicator read, and risk notes.
5. Freemium gate: 3 free scans tracked in `localStorage`, then a paywall sheet (UI stub — wire up Stripe/RevenueCat to make it real).

Non-chart images are detected (`is_chart: false`) and don't consume a scan.

## Run it locally

```bash
npm install
cp .env.example .env.local   # add your ANTHROPIC_API_KEY
npm run dev
```

Open http://localhost:3000 — works great in a phone browser too (the file input opens the camera on mobile).

## Deploy (web backend)

Any Node host works; Vercel is one-click. Environment variables:

- `ANTHROPIC_API_KEY` — required
- `UPSTASH_REDIS_REST_URL` + `UPSTASH_REDIS_REST_TOKEN` — optional but recommended; enables **server-side** free-scan metering per device (see `lib/metering.ts`). Without them, only the client-side gate applies.

## Mobile apps (Capacitor)

Native Android and iOS projects live in `android/` and `ios/`, with app icons and splash screens already generated (source art in `resources/`, regenerate with `scripts/make-assets.py` + `npx @capacitor/assets generate`). The mobile apps are static shells that call your hosted backend:

```bash
NEXT_PUBLIC_API_BASE_URL=https://your-deployment.vercel.app npm run build:mobile
npx cap sync
# Android: cd android && ./gradlew bundleRelease  (see docs/play-store.md)
# iOS:     npx cap open ios  (requires a Mac + Xcode)
```

**Full Play Store submission guide — listing copy, data safety answers, signing, checklist: [`docs/play-store.md`](docs/play-store.md).**

## Roadmap ideas

- RevenueCat subscription (`pro_weekly`) wired to the `pro:{deviceId}` flag in Redis
- Scan history & shareable result cards (built-in TikTok content loop)
- Multi-timeframe analysis: upload 2–3 screenshots of the same asset

## Disclaimer

AI-generated educational analysis only — **not financial advice**. Trading involves substantial risk of loss.
