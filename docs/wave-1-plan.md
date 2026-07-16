# Wave 1 Plan — evidence & picks

**Date:** 2026-07-10 · **Status:** proposed, awaiting Darren kill/keep
· **Owner:** engine (Claude Code)

Wave 1 is 8 products: **4 Chrome, 2 Shopify, 2 Figma**. Every pick below
is constrained by **Rule 1 (v1 in ≤5 days)** and **each product funds its
own ads** — so the bar is: thin or no backend, a clear paid tier, and a
buyer with obvious willingness-to-pay.

## Monetization mechanics (verified)

These decide what's buildable in 5 days, so they're settled up front:

- **Chrome → ExtPay (ExtensionPay).** One-time and recurring payments via
  Stripe, **no backend/server/database needed**. Library is open source.
  This is the post-Chrome-Web-Store-payments standard.
  [extensionpay.com](https://extensionpay.com/) ·
  [github.com/glench/ExtPay](https://github.com/glench/ExtPay)
- **Figma → LemonSqueezy license keys, freemium.** Free plugin on the
  Community for reach; Pro features unlocked by a license key the user
  buys on a LemonSqueezy checkout and pastes into the plugin. LS handles
  VAT/tax, subscriptions, ~3.5% + $0.30 vs Gumroad's flat 10%.
  [Figma: publish plugins](https://help.figma.com/hc/en-us/articles/360042293394-Publish-plugins-to-the-Figma-Community)
  · [dodopayments.com/blogs/sell-figma-plugins](https://dodopayments.com/blogs/sell-figma-plugins)
- **Shopify → embedded app + Billing API.** Inherently heavier (OAuth,
  app review, GraphQL Admin API, theme app extensions). This is *why*
  Shopify starts first — the App Store review queue is the critical path,
  not the code. Picks are deliberately the *smallest* app-shaped tools.

## Evidence base (directional)

Strong signals, consistent across sources. Concrete case studies are the
credible anchor; aggregator/SEO-blog numbers are marked `UNVERIFIED`.

**Credible case studies** ([StarterStory](https://www.starterstory.com/ideas/chrome-extension/success-stories)):
- Real-estate listing→CRM formatter: **$4,200 MRR in 4 months**, $29/mo,
  saves agents ~30 min/day.
- Merch Wizard / KDP Wizard (niche Amazon seller tools): **~$10k MRR**.
- Sync2Sheets (Notion↔Sheets): **$9k MRR, 400+ paying**, built in 2 weeks.
- Bluedot (AI notes for Google Meet): $1.5k MRR, 50%/mo growth, 2 months in.

**Directional (`UNVERIFIED` — SEO/aggregator blogs):**
- Most-cited profitable Chrome niches: **platform-specific seller tools**,
  B2B workflow automation, AI content/tone tools, e-commerce deal finders.
  [chromegoldmine](https://chromegoldmine.com/blog/profitable-chrome-extension-niches/) ·
  [profitable.app/chrome](https://profitable.app/chrome)
- **Underserved email clients:** Outlook / Yahoo / corporate (Gmail is
  saturated — GMass ~$130k MRR owns that lane).
- Shopify install gaps: ~98% of stores lack dedicated analytics/subs, ~94%
  lack reviews. Accounting integrations "mediocre, poorly-reviewed,
  expensive." POD: 1-star complaints that incumbents shipped "almost the
  same features as 5 years ago." [gapquery](https://www.gapquery.com/blog/shopify-high-demand-low-supply) ·
  [Printify reviews](https://apps.shopify.com/printify)
- Figma: Tokens Studio (~264k users) owns tokens; Stark owns accessibility.
  Cited open gap = **design→dev handoff / annotation**.
  [muz.li](https://muz.li/blog/best-figma-plugins-for-designers-in-2026/)

> Aggregator install/revenue figures are **not** independently verified
> against live marketplace listings. Before any build is greenlit past
> spec, the engine confirms competitor count + top-app review counts on
> the actual store page. Treated as `UNVERIFIED` until then.

## Why NOT the highest-demand categories (the non-obvious calls)

- **Shopify subscriptions / accounting sync:** highest demand, but billing
  cycles and QuickBooks OAuth sync engines are multi-week, not 5-day.
  Descoped from Wave 1 by Rule 1.
- **POD apps:** demand is real, but the complaints are about *physical
  print quality and fulfillment* — the print provider's problem, not
  something a thin app fixes. Bad fit.
- **Figma tokens / accessibility:** genuine incumbents (Tokens Studio,
  Stark). Not a Wave-1 wedge.

## The 8 picks

Each is a **lead pick** + one-line backup. Lead picks are proposals for
Darren's kill/keep. `Effort` = engine build days to v1.

### Chrome (4) — all ExtPay, thin/no backend

| # | Product | What it does (v1) | Paid tier | Effort | Risk |
|---|---------|-------------------|-----------|--------|------|
| **C1** | **Reseller Analytics & Export** | On the user's own open marketplace seller pages (eBay/Vinted/Depop/Etsy), reads the page, computes sell-through / price / aging analytics, exports CSV. | Free: view current page. Pro (~$7/mo): multi-page aggregate, CSV export, history. | 4d | DOM fragility; keep to *user's own data on pages they've opened* — no auth scraping. |
| **C2** | **BYOK AI Rewrite Anywhere** | Select text on any page → rewrite / tone-shift / shorten via the user's *own* API key (stored locally). No backend. | Free: 10 rewrites/day. Pro (~$5/mo): unlimited, saved presets. | 4d | Crowded; wedge = BYOK (no per-call cost to us) + niche presets. |
| **C3** | **Outlook/Yahoo Mail Power Kit** | Templates, send-later, and mail-merge for the *underserved* Outlook Web / Yahoo webmail. Client-side. | Free: 3 templates. Pro (~$8/mo): unlimited templates, merge, scheduling. | 5d | Webmail DOM changes; scope to one client first (Outlook Web). |
| **C4** | **Seller SEO / Listing Optimizer** | On a public Etsy/Amazon listing page, scores title/tags/keywords and suggests fixes. Public data only. | Free: score. Pro (~$7/mo): suggestions, keyword expansion, bulk. | 4d | Public-page only (no auth). Backup: YouTube/Udemy timestamped notes exporter. |

### Shopify (2) — embedded app + Billing API. **Start first (review latency).**

| # | Product | What it does (v1) | Paid tier | Effort | Risk |
|---|---------|-------------------|-----------|--------|------|
| **S1** | **Estimated Delivery Date** | Shows a dynamic "Get it by <date>" on product/cart pages via a theme app extension + simple rules (cutoff time, processing days, zones). | Free trial → ~$6.99/mo. | 5d | App review queue is the real gate — submit ASAP. |
| **S2** | **Purchase Rules (min/max & limits)** | Enforce min/max quantities, per-customer limits, and multiples at checkout. | Free trial → ~$5.99/mo. | 5d | Checkout enforcement path; validate on dev store. Backup: back-in-stock alerts. |

### Figma (2) — client-side, LemonSqueezy license, freemium

| # | Product | What it does (v1) | Paid tier | Effort | Risk |
|---|---------|-------------------|-----------|--------|------|
| **F1** | **Smart Content Populator** | Fills selected text/image layers from presets or an imported CSV (names, prices, avatars, i18n length tests). Content Reel is stagnant — modern, maintained take. | Free: built-in presets. Pro (license): CSV import, large datasets, image fill. | 3d | Thin, fast, purely client-side — strongest speed pick. |
| **F2** | **Redline & Handoff Kit** | Numbered callouts, spacing/measurement specs, and a one-click handoff export — the cited design→dev gap. | Free: annotate. Pro (license): measurement specs, export, styles. | 5d | Handoff is a real gap but has adjacent players; keep v1 tight. |

## Build order (critical path)

1. **Now — Darren:** create Shopify Partner account + dev store, and
   greenlight/kill the 8 picks. Shopify review latency makes the Partner
   account the true blocker.
2. **Now — engine:** build **C1** (highest evidence, pure client-side,
   fastest to paid) and **F1** (thinnest, 3-day) in parallel. Specs ready:
   [`specs/C1-reseller-analytics.md`](specs/C1-reseller-analytics.md),
   [`specs/S1-estimated-delivery-date.md`](specs/S1-estimated-delivery-date.md).
3. **On Shopify account:** scaffold **S1** and submit to review
   immediately (code is ready before the queue clears).
4. Remaining specs (C2–C4, F1–F2, S2) written on greenlight — not before,
   to avoid speccing products that get killed at pick stage.

## Open decisions for Darren

- Which 8 of the leads/backups to greenlight (or swap).
- Pricing sanity-check on the ~$5–8/mo Chrome tiers.
- Separate bank pot + Stripe/LemonSqueezy/Shopify Partner accounts under
  the venture entity.
