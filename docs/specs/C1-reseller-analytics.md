# SPEC — C1 · Reseller Analytics & Export

**Platform:** Chrome (MV3) · **Status:** spec · **Effort to v1:** 4 days
· **Monetization:** ExtPay (Stripe, no backend)

## Problem & buyer
Resellers on eBay / Vinted / Depop / Etsy run their business off the
platform's own seller pages, which show listings but almost no analytics —
no sell-through rate, no aging (how long unsold), no price-vs-sold view,
no export. They copy numbers into spreadsheets by hand. A tool that reads
the page they already have open and turns it into analytics + a CSV saves
real time, and platform-specific seller tools are the single most-cited
profitable Chrome niche.

## Evidence
- Niche seller-tool case studies: Merch/KDP Wizard ~$10k MRR; real-estate
  listing→CRM formatter $4.2k MRR at $29/mo.
  [StarterStory](https://www.starterstory.com/ideas/chrome-extension/success-stories)
- "Platform-specific seller tools" cited as top profitable niche
  (`UNVERIFIED`, aggregator). [chromegoldmine](https://chromegoldmine.com/blog/profitable-chrome-extension-niches/)
- **Before build:** confirm competitor count + review counts for
  "Vinted/Depop/eBay analytics" on the Chrome Web Store. `UNVERIFIED`.

## v1 scope (≤5 days)

### In
- Start with **one** platform (eBay seller "active/sold" pages — richest
  DOM, largest base). Reads the page the user has open.
- Compute: listing count, avg/median price, total inventory value, item
  age (days listed), and — on the sold view — sell-through rate and avg
  days-to-sell.
- Popup dashboard summarizing the current page.
- **Pro:** aggregate across paginated pages, CSV export, and local history
  snapshots (stored in `chrome.storage.local`).

### Out (v1)
- Other platforms (Vinted/Depop/Etsy) — fast-follow once eBay validates.
- Any server, login, or cross-device sync.
- Anything requiring auth scraping or accessing data the user can't
  already see on their own screen.

## Success criteria (verify before "done")
- [ ] On a real eBay seller page, the dashboard shows correct counts and
      price stats (spot-checked against the page).
- [ ] CSV export opens cleanly in Sheets/Excel with the right columns.
- [ ] Free tier shows current-page summary; Pro gate blocks export +
      aggregate until ExtPay reports an active subscription.
- [ ] Listing assets ready (icon, 3 screenshots, description).

## Monetization
- **Free:** current-page summary dashboard.
- **Pro ~$6.99/mo (ExtPay):** multi-page aggregate, CSV export, history.
- ExtPay `getUser()` gates Pro features; no backend.

## Build notes
- MV3, content script injected on the target seller URLs only (narrow
  `host_permissions`), popup UI (vanilla or Preact — keep tiny).
- Parse via DOM selectors with defensive fallbacks; fail soft with a
  "couldn't read this page" message rather than crashing.
- `ExtPay('reseller-analytics')` in background + popup.
- **Privacy:** all computation local; nothing leaves the browser. Say so
  in the listing — it's a selling point and keeps the CWS review clean.
- Fragility: platform DOM changes break parsing — keep selectors isolated
  in one module for quick fixes.

## Listing copy
- **Title:** Reseller Analytics & CSV Export for eBay
- **Subtitle:** Sell-through, aging, and pricing insights on your own
  listings — plus one-click CSV export.
- **Description bullets:** See sell-through and days-to-sell · Spot dead
  stock by listing age · Export every listing to CSV · 100% local, your
  data never leaves your browser.
- **Keywords:** ebay seller tools, reseller analytics, sell-through, csv
  export, inventory aging.
