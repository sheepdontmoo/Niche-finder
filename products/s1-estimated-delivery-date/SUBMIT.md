# Submit runbook — Estimated Delivery Date

This is an approval-gated runbook. Do not deploy, edit the listing, or submit
until the controlled-store checklist in `docs/SALES_READINESS_2026-08-30.md`
passes and Darren gives exact action-time approval.

## 0. (Recommended, 2 min) Confirm the block on your storefront
While `npm run dev` is running: Shopify admin → **Online Store → Themes →
Customize** → open a **Product** template → **Add block** → **Estimated
Delivery Date** → place under Add to cart → **Save**. Confirm the
"Order today to get it by…" line appears. This is the last core check.

## 1. Deploy the app version
PowerShell (press `q` first to stop dev):
```
npm run deploy
```
Confirm the prompts. This uploads the app config + the theme extension version.

## 2. Fill the App Store listing (Dev Dashboard → your app → Distribution / App listing)
All copy is in **`store-listing.md`** — paste field-for-field:

| Field | Source |
|-------|--------|
| App name | "SupaDatewise: Delivery Date" |
| Subtitle / tagline | store-listing.md → subtitle |
| Introduction | store-listing.md → introduction |
| Description | store-listing.md → details |
| Key benefits | store-listing.md → benefits |
| Search terms | store-listing.md → search terms |
| Category | Current: Store design → Product content; request Delivery and pickup only if Shopify confirms eligibility |
| Pricing | $6.99/month, 7-day free trial |

**Media to upload:**
- App icon → `store-assets/app-icon.png`
- Feature image → `store-assets/feature-1.png`
- Screenshot → your settings-page screenshot (+ optional `store-assets/screenshot-storefront.png`)

**Privacy policy URL:** use the branded production route
`https://edd-supadesign.fly.dev/privacy` only after it returns 200 and its copy
matches the released app. The current listing's Telegraph URL must not be
treated as final proof.

**Support email:** your venture support address.

## 3. Submit for review
Click **Submit for review** only after Darren gives exact action-time approval.
Do not promise a review timeline or approval outcome.

---

### After a verified release
- Re-run a fresh install and confirm the root route denies access until Shopify
  reports an active App Pricing subscription.
- Confirm the reviewed `write_app_proxy` permission and `/apps/supadatewise`
  proxy are live, and that the storefront block remains hidden when the proxy
  cannot confirm an active subscription.
- Treat trial, active subscription, processed payment, non-refunded status, and
  cleared payout as separate provider-backed states.
- Start acquisition only after the entire activation path and public trust pages
  are verified live.
