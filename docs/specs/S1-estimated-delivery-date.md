# SPEC — S1 · Estimated Delivery Date

**Platform:** Shopify (embedded app + theme app extension) · **Status:** spec
· **Effort to v1:** 5 days · **Monetization:** Shopify Billing API

## Problem & buyer
Shoppers convert better when they see *when* an order will arrive.
Merchants constantly ask for a "Get it by <date>" line on product and cart
pages, but it's fiddly to hardcode (cutoff times, processing days, weekends,
zones). A small app that computes and displays a dynamic delivery estimate
is a classic, well-understood conversion utility with clear WTP — and it's
**app-shaped and small enough to ship in 5 days**, unlike the higher-demand
Shopify categories (subscriptions, accounting) that can't.

## Evidence
- ~98% of Shopify stores lack dedicated analytics/subscriptions; broad
  under-app-ification of the base (`UNVERIFIED`, aggregator).
  [gapquery](https://www.gapquery.com/blog/shopify-high-demand-low-supply)
- Delivery-date apps are an established, monetized category — **before
  build, confirm** competitor count, price band, and top-app review counts
  on the live Shopify App Store. `UNVERIFIED`.
- Rationale for picking this over higher-demand categories: Rule 1 (5-day
  ship). See [`../wave-1-plan.md`](../wave-1-plan.md) "Why NOT…".

## v1 scope (≤5 days)

### In
- Embedded admin app (Shopify App Bridge + Polaris) with a settings page:
  processing days, daily cutoff time, working days, and a simple
  copy template ("Get it by {date}").
- **Theme app extension** block the merchant drops onto the product page
  (and/or cart) that renders the computed date client-side.
- Estimate = today + processing days, respecting cutoff + working days.
- Shopify **Billing API**: free trial → single recurring plan.

### Out (v1)
- Per-zone/carrier-accurate transit times (start with a flat "delivery
  days" number; zones are fast-follow).
- Multi-language, A/B tests, per-product overrides.

## Success criteria (verify before "done")
- [ ] Installs on a dev store via OAuth; passes the automated review
      checks (App Bridge, correct scopes, GDPR webhooks).
- [ ] Block renders a correct date on the product page in the dev store,
      respecting cutoff/working-days settings.
- [ ] Billing flow: trial starts, charge approves, app gates on active
      subscription.
- [ ] Listing assets ready (icon, screenshots, description).

## Monetization
- Free trial → **~$6.99/mo** single plan via Shopify Billing API.

## Build notes
- **Critical path is App Store review, not code** — this is why S1 is
  scheduled first. Scaffold with Shopify CLI (`shopify app`), Remix
  template, deploy the theme app extension, submit for review the moment
  Darren's Partner account + dev store exist.
- Required for review: mandatory GDPR/compliance webhooks, correct
  `access_scopes`, App Bridge session tokens, Polaris UI.
- Keep the date logic in one shared module (used by settings preview +
  storefront block).

## Listing copy
- **Title:** Estimated Delivery Date — "Get it by" timer
- **Subtitle:** Show shoppers a dynamic delivery estimate on product and
  cart pages to lift conversion.
- **Description bullets:** Dynamic "Get it by <date>" · Cutoff-time &
  working-day aware · No theme code — drag-and-drop block · Fully
  customizable copy.
