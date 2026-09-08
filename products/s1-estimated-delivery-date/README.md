# S1 · Estimated Delivery Date (Shopify app)

Shows a dynamic "Order today to get it by &lt;date&gt;" estimate on product/cart
pages via a theme app extension, configured from an embedded admin app.
The public listing currently shows one Shopify App Pricing plan at $6.99/month
with a seven-day trial. Provider evidence remains the source of truth.

Spec: [`../../docs/specs/S1-estimated-delivery-date.md`](../../docs/specs/S1-estimated-delivery-date.md)

## Architecture

| Piece | Path | Notes |
|-------|------|-------|
| Delivery-date logic (pure, tested) | `app/lib/delivery-date.ts` | Cutoff, processing, transit, working-day and timezone math. |
| Storefront port of that logic | `extensions/delivery-date/assets/delivery-date.js` | **Verbatim port** — keep in sync with the TS. |
| Settings persistence | `app/lib/settings.server.ts` | Writes the app-owned shop metafield `$app.settings`; no metafield definition or Admin API scope is required. |
| Admin settings UI | `app/routes/app._index.tsx` | Polaris web components, native-form submit, and server-rendered preview. |
| Setup guide | `app/routes/app.setup.tsx` | How to add the block. |
| Pricing | Shopify App Pricing + Partner API | The app root checks `activeSubscription` and redirects unpaid installs to Shopify's hosted plan selector. |
| Storefront entitlement | `app/routes/apps.supadatewise.entitlement.tsx` | HMAC-authenticated app proxy verifies the active subscription before the hidden block can render. |
| Compliance webhooks | `app/routes/webhooks.compliance.tsx` | Customer topics are acknowledged; `shop/redact` deletes all app-held sessions for the shop. |
| Storefront block | `extensions/delivery-date/blocks/delivery_date.liquid` | Reads the saved metafield; stays disabled when settings are absent. |

**Config resolution:** the storefront block prefers the app-owned shop metafield
set by the admin app (`shop.metafields["$app"].settings`). If it is missing, the
block stays disabled. Saved cutoffs use Shopify's authoritative IANA store
timezone rather than the shopper device timezone. The block starts hidden and
is revealed only after the app proxy confirms an active subscription.

## Verified here (in this environment)

- ✅ `npm test` — delivery math, timezone, persistence, theme contract, and
  storefront/server parity tests.
- ✅ `npm run typecheck` — clean.
- ✅ `npm run build` — production build succeeds.
- ✅ `npm run lint` — clean.
- ✅ `shopify app build` — Shopify Theme Check and extension bundling succeed.
- ✅ **Production-server preview** — `/`, `/support`, `/privacy`, robots,
  sitemap, and LLM guidance routes return 200; unsigned webhook probes fail
  closed.

## NOT yet verified (needs a real dev store — Darren)

These require Shopify auth + a store and can't run in this environment:

- The `$app.settings` metafield → Liquid read path on a published theme. The
  source and storefront/server parity are tested; a real theme render is not.
- The `write_app_proxy` OAuth/config change, HMAC proxy request, active render,
  inactive/frozen hide path, and one-minute maximum entitlement-cache window.
- Existing saved settings from before the timezone field stay hidden until the
  merchant opens the app and saves once; that resave path needs controlled-store
  proof before release.
- Shopify App Pricing plan selection, active-payment gate, subscription events,
  paid conversion, refunds, and cleared revenue.
- The App Home handle, organization ID, app GraphQL ID, and Partner API client
  token required by the active-payment gate. They are intentionally required as
  provider configuration; public listing metadata is not used as a substitute.
- The admin settings page inside the Shopify admin iframe.
- Signed compliance/uninstall webhook delivery and idempotent deletion.
- Linux container startup and Prisma migration. Docker is unavailable here.

## Handoff — steps that need your login

Legacy repository notes name app `328515354625` and organization `208004935`,
but authenticated provider evidence is required before either is used as
current configuration.

```bash
cd products/s1-estimated-delivery-date
npm install

# The checked-in client ID already matches the public listing. Do not run
# config:link unless authenticated provider evidence proves that link is stale.

# 2. Run locally against a dev store (opens a tunnel + installs the app)
npm run dev

# 3. In the dev store: Settings page -> set rules -> Save.
#    Then Online Store -> Themes -> Customize -> Product template ->
#    Add block -> "Estimated Delivery Date". Confirm the date renders.

# 4. Stop. Record the controlled-store results and request exact approval before
#    any deploy, listing edit, pricing change, or submission.
```

The app is already publicly listed. The critical path is now proving the
install → active plan → saved settings → published block funnel before changing
production or sending merchants to it.

## Local dev commands

```bash
npm test         # unit tests (node:test)
npm run typecheck
npm run build
npm run lint
npm run dev      # Shopify CLI dev (needs auth)
```
