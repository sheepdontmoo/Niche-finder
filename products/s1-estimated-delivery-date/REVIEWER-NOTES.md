# Reviewer testing info (for the App Store submission form)

## "My app doesn't require an account"
**Check this box.** The app has no separate account, login, or external
signup — it installs and runs entirely within Shopify.

## Testing instructions (paste into the field)
```
Estimated Delivery Date has no separate account or login — it installs and works entirely within Shopify.

To test:
1. Install the app on a development store.
2. Open the app. On the Settings page, set the delivery rules (processing time, daily cutoff hour, shipping min/max in business days, working days) and the wording/date format, then click "Save settings". A confirmation appears and a live preview shows the resulting estimate.
3. In the store admin, go to Online Store > Themes > Customize > open a Product template > Add block > "Estimated Delivery Date" (under App blocks) > place it (e.g. under Add to cart) > Save.
4. Visit any product page on the storefront. The block shows an automatic "Order today to get it by <date>" estimate calculated from the rules set in step 2. Changing the rules in the app updates what shoppers see.

Notes:
- No account or credentials required.
- The app stores no customer personal data; the estimate is computed in the shopper's browser.
- Billing: a single plan ($6.99/month) with a 7-day free trial via Shopify's Billing API.
```

## Screencast (required) — 45–60s recording script
Record with Loom / QuickTime / OBS on the dev store, then paste the share URL.

1. (0–10s) App **Settings** page: change a couple of values (e.g. processing
   time, shipping max) and click **Save settings** — show the confirmation +
   the live preview line updating.
2. (10–30s) **Theme editor**: open a product template, **Add block →
   Estimated Delivery Date**, place it under Add to cart, **Save**.
3. (30–55s) Open the **product page** on the storefront and show the
   "Order today to get it by …" estimate rendering. Optionally change a rule
   in the app and reload to show it update.

Keep it silent or narrate briefly; no editing needed.

## Emails (all required)
Provide one address for each (they can be the same to start):
- App submission email
- Merchant review email
- Support email

Suggested default until a dedicated support inbox exists: **basdesignco@gmail.com**
(swap for a venture support alias when you have one).
