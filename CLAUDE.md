# Project memory

## The Frank Fellers branding framework

Apply this 5-step framework to every product/brand decision in this repo:

1. **Focus** — create a focus that's a little different than the competition.
2. **Slogan** — create a slogan that reinforces that focus. Simple and memorable.
3. **Name** — create a name that's different from the competition: memorable, **two syllables**, non-generic.
4. **Logo & color** — create a logo that reinforces the name, then use **one color** in all marketing and advertising that's different from the competition.
5. **Deliver** — work your ass off to make sure the company comes through on the promise of that brand.

## Chart-analysis app brand: LOCKED as Litmas

**See [`LITMAS-BRAND.md`](LITMAS-BRAND.md) — that file is the authority.**

The brand is **closed**. Name, subtitle, slogan, logo and colour are settled:
Litmas · "AI candlestick & chart detector" · "Check every trade." · single
test strip logo · orange `#FF6A00` only · fallback name Second. Do not
reopen, re-pitch or re-litigate any of it — build to the brief.

**The brand is applied.** The old "ChartDetector" placeholder is gone from
the codebase. Bundle id is `com.litmas.app` on both platforms — permanent
once the first build is uploaded, so do not change it after that.

Regenerating brand assets:

```bash
python3 scripts/make-assets.py                  # icon + splash source art
npx @capacitor/assets generate --android --ios  # platform icon sets
python3 scripts/make-store-assets.py --shots-dir <app-screenshots>
```

Keep the one-colour rule when touching UI: `--brand` is the only tinted
token in `app/globals.css`. Greys are ground. Nothing else gets a hue.
