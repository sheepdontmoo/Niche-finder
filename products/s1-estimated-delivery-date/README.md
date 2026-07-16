# S1 · Estimated Delivery Date (Shopify app)

Shows a dynamic "Order today to get it by &lt;date&gt;" estimate on product/cart
pages via a theme app extension, configured from an embedded admin app.
Monetized with the Shopify Billing API (single Pro plan, $6.99/mo, 7-day trial).

Spec: [`../../docs/specs/S1-estimated-delivery-date.md`](../../docs/specs/S1-estimated-delivery-date.md)

## Architecture

| Piece | Path | Notes |
|-------|------|-------|
| Delivery-date logic (pure, tested) | `app/lib/delivery-date.ts` | 11 unit tests. Cutoff, processing, transit, working-day math. |
| Storefront port of that logic | `extensions/delivery-date/assets/delivery-date.js` | **Verbatim port** — keep in sync with the TS. |
| Settings persistence | `app/lib/settings.server.ts` | Writes a shop metafield `delivery_date.settings` (+ a definition with storefront read access). |
| Admin settings UI | `app/routes/app._index.tsx` | Polaris web components, native-form submit, server-rendered preview, billing CTA. |
| Setup guide | `app/routes/app.setup.tsx` | How to add the block. |
| Billing | `app/shopify.server.ts` (`PRO_PLAN`) | `Every30Days`, $6.99, 7 trial days. Test mode off only in production. |
| Compliance webhooks | `app/routes/webhooks.compliance.tsx` | GDPR topics; app stores no customer PII. |
| Storefront block | `extensions/delivery-date/blocks/delivery_date.liquid` | Reads the metafield, falls back to block settings. |

**Config resolution:** the storefront block prefers the shop metafield (set by
the admin app); if it's missing it uses the block's own theme-editor settings,
so the block is never broken while the metafield path is being wired.

## Verified here (in this environment)

- ✅ `npm test` — 11/11 unit tests on the date logic pass.
- ✅ `npm run typecheck` — clean.
- ✅ `npm run build` — production build succeeds.
- ✅ `npm run lint` — clean.
- ✅ **Storefront render** — the real `assets/delivery-date.js` was loaded in
  headless Chromium against the block markup and rendered correct live dates:
  a range (`en-IE`, "17 Jul 2026 – 21 Jul 2026") and a single day (`en-US`,
  "Wednesday, July 15, 2026"), with cutoff + weekend-skipping applied.

## NOT yet verified (needs a real dev store — Darren)

These require Shopify auth + a store and can't run in this environment:

- The metafield → Liquid read path (`shop.metafields.delivery_date.settings`)
  feeding config into the block. (The client render itself is verified above;
  what's unverified is that path *sourcing* the config on a live store.) If it
  doesn't surface, the block falls back to its own settings; the fix is
  confirming the metafield definition's storefront access on the store.
- The billing subscribe/return flow end-to-end.
- The admin settings page inside the Shopify admin iframe.

## Handoff — steps that need your login

App already exists in the Partner org: **app `328515354625`, org `208004935`**.

```bash
cd products/s1-estimated-delivery-date
npm install

# 1. Link this code to the existing app (populates client_id + URLs in the toml)
npm run config:link      # choose org 208004935 -> app 328515354625

# 2. Run locally against a dev store (opens a tunnel + installs the app)
npm run dev

# 3. In the dev store: Settings page -> set rules -> Save.
#    Then Online Store -> Themes -> Customize -> Product template ->
#    Add block -> "Estimated Delivery Date". Confirm the date renders.

# 4. Push the extension + config, then submit for review
npm run deploy
```

The App Store **review queue is the real critical path** — deploy and submit
as early as the above checks pass on the store, then iterate on listing assets
(icon, screenshots, description — draft copy is in the spec) while it's in the
queue.

## Local dev commands

```bash
npm test         # unit tests (node:test)
npm run typecheck
npm run build
npm run lint
npm run dev      # Shopify CLI dev (needs auth)
```
