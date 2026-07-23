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

## Deploy

Any Node host works; Vercel is one-click. Set `ANTHROPIC_API_KEY` in the environment.

## Roadmap ideas

- Real payments (Stripe web / RevenueCat if wrapped as a mobile app via Capacitor)
- Server-side scan metering (auth + database) instead of `localStorage`
- Scan history & shareable result cards (built-in TikTok content loop)
- Multi-timeframe analysis: upload 2–3 screenshots of the same asset

## Disclaimer

AI-generated educational analysis only — **not financial advice**. Trading involves substantial risk of loss.
