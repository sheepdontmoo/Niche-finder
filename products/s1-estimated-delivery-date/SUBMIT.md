# Submit runbook — Estimated Delivery Date

Everything is built and pushed. These are the only steps that need your Shopify
login. ~10–15 minutes. Do them in order.

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
| App name | "Estimated Delivery Date" |
| Subtitle / tagline | store-listing.md → subtitle |
| Introduction | store-listing.md → introduction |
| Description | store-listing.md → details |
| Key benefits | store-listing.md → benefits |
| Search terms | store-listing.md → search terms |
| Category | Store design (Merchandising / product page) |
| Pricing | $6.99/month, 7-day free trial |

**Media to upload:**
- App icon → `store-assets/app-icon.png`
- Feature image → `store-assets/feature-1.png`
- Screenshot → your settings-page screenshot (+ optional `store-assets/screenshot-storefront.png`)

**Privacy policy URL:** host `docs/privacy.html` somewhere public and paste the
link. Fastest options: a page on your own domain, or ask Claude to set up
GitHub Pages.

**Support email:** your venture support address.

## 3. Submit for review
Click **Submit for review**. Then it's Shopify's queue (typically days to ~2
weeks; expect possibly one round of reviewer feedback).

---

### After approval
- App is installable → subscriptions charge (test mode off in production).
- Then it's about traffic: App Store SEO + the product's own ad budget.
