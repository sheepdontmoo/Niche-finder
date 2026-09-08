# Reviewer testing info (for the App Store submission form)

## "My app doesn't require an account"
**Check this box.** The app has no separate account, login, or external
signup — it installs and runs entirely within Shopify.

## Testing instructions (paste into the field)
```
Estimated Delivery Date has no separate account or login — it installs and works entirely within Shopify.

To test:
1. Install the app on a development store. Confirm Shopify redirects to its hosted plan selector, choose the no-charge development-store plan, and then confirm the app opens.
2. On the Settings page, set the delivery rules (processing time, daily cutoff hour in the displayed Shopify store timezone, shipping min/max in business days, working days) and the wording/date format, then click "Save settings". A confirmation appears and a live preview shows the resulting estimate.
3. Open the Setup guide and choose "Open theme editor", or manually go to Online Store > Themes > Customize. Preview the "Estimated Delivery Date" app block on a Product template, place it (for example under Add to cart), then Save.
4. Visit any product page on the storefront. The block shows an automatic "Order today to get it by <date>" estimate calculated from the rules set in step 2. Changing the rules in the app updates what shoppers see.

Notes:
- No account or credentials required.
- The app requests only `write_app_proxy`, used for an HMAC-authenticated subscription check. It requests no order or customer-record scopes. It stores the Shopify app session and merchant delivery settings; the estimate runs in the shopper's browser using the Shopify store timezone for the cutoff.
- Pricing: the current listing shows $6.99/month with a seven-day trial through Shopify App Pricing. Test the no-charge development-store plan before submission.
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

Support inbox: **MISSING — Darren must choose the venture support address before
any listing edit or submission. Do not publish a personal fallback address.**
