# SupaDatewise competitive scorecard — 2026-08-30

Evidence date: **2026-08-30**. Competitor facts use only public Shopify App
Store listing pages and first-party reviews on those pages. Ratings and review
counts are point-in-time. Private installs, activation, paid subscriptions,
churn, refunds and revenue are **UNAVAILABLE** for every competitor and are not
inferred from ratings, reviews or a Built for Shopify badge.

SupaDatewise evidence is grounded in
[`SALES_READINESS_2026-08-30.md`](./SALES_READINESS_2026-08-30.md). Scores are
public-signal judgments, not hands-on competitor correctness tests. Five is
strongest.

## Executive finding

A generic product-page ETA widget is commoditized: Essent is free, Built for
Shopify and has 914 reviews. SupaDatewise should not position itself as simply
another estimated-delivery-date app.

The smallest currently defensible wedge is:

> Store-timezone delivery windows for small DTC stores with one clear dispatch
> schedule, safe theme preview and no order or customer-record access.

The next wedge should only be claimed after it exists and passes live tests:
made-to-order and preorder catalogs with variant, inventory, location or
metafield lead times, bulk rule management, and promised-versus-actual accuracy.

## Current public competitor signals

All prices are USD.

| App | Public positioning and visible capabilities | Exact visible pricing | Rating / reviews | Trust and activity signals |
|---|---|---|---:|---|
| [Essent Estimated Delivery Date](https://apps.shopify.com/essential-estimated-delivery) | Simple ETA, processing-time and timer widgets; cutoffs, holidays, geolocation and multilingual customization; product-page emphasis | Free; no trial required | 5.0 / 914 | Built for Shopify; launched 2025-06-06; demo and privacy policy; visible developer reply 2026-08-28 |
| [S Estimated Delivery Date Plus](https://apps.shopify.com/omega-estimated-shipping-date) | Product, cart, checkout, order-status and email surfaces; country, ZIP, shipping-method, variant and metafield rules; analytics, translations and shipping protection | Free; Standard $4.99/month or $53.88/year; Pro $9.99/month or $107.88/year; Plus $29.99/month or $287.90/year; 3-day trials on paid plans | 4.9 / 404 | Built for Shopify; launched 2018-06-22; demo, privacy, docs and changelog; visible developer reply 2026-08-21 |
| [Estimated Delivery Date - ETA](https://apps.shopify.com/estimated-delivery-days) | Inventory, warehouse, vendor, tag, location, ZIP and shipping rules; product through checkout/email; pickup, analytics and multiple templates | Free, capped at 300 ETA views; $6.99/month or $75.50/year; $14.99/month or $149.99/year; $19.99/month or $199.99/year; 7-day trials on paid plans | 4.9 / 466 | Built for Shopify badge **not visible on the direct page**; launched 2020-08-04; demo, privacy, FAQ, docs and changelog; latest visible review 2026-08-11 |
| [C-EDD: Estimated Delivery Date](https://apps.shopify.com/estimated-delivery-date-plus) | Product, collection, vendor and country rules; cutoffs, holidays, local timezone, product/cart/checkout claims | Free; Basic $4.98/month; Pro $8.98/month; no trial displayed | 4.9 / 337 | Built for Shopify badge **not visible**; launched 2022-07-22; demo and privacy policy; no FAQ, docs or changelog displayed; latest visible review 2026-01-08 |
| [Estimated Delivery Date & ETA](https://apps.shopify.com/order-delivery-estimated) | Product, variant, collection, vendor, tag, shipping-method, country/state and ZIP rules; multi-page display and analytics | Free with unlimited views and five rules; Professional $4.99/month or $56.89/year; Growth $8.99/month or $97.09/year; 7-day trials on paid plans | 5.0 / 78 | Built for Shopify; launched 2022-04-20; demo and privacy policy; featured in a Shopify guide; latest visible review 2026-07-24 |
| [ArrivesBy - Delivery Date ETA](https://apps.shopify.com/arrives-by) | Variant, location and inventory-aware ETAs; preorder/backorder support; comparison of promised versus actual delivery dates; product through checkout/order status | Free; Grow $4.99/month or $49/year; Scale $19.99/month or $199/year; 7-day trials on paid plans | 4.5 / 45 | Built for Shopify; launched 2021-09-15; demo, privacy, FAQ, docs and changelog; latest visible review 2026-07-15 |

## Permissions and privacy signals

These are the data-access summaries visible on the listings, not independent
scope audits.

| App | Publicly disclosed access |
|---|---|
| Essent | Store-owner details; edit products and discounts; view theme, locales, Markets and inventory. No customer-data access is listed. |
| S Estimated Delivery Date Plus | Customer address/geolocation/device data; customers; editable products, all order history and fulfillment; Online Store pages, script tags and theme; locales, locations, Markets and product publications. This is the broadest disclosed access in this set. |
| Estimated Delivery Date - ETA | Customer name, email, address, geolocation and device data; store-owner details; view inventory/products/collections, all order history and shipping, theme and locations. |
| C-EDD | Privacy policy is linked, but no Data access section was displayed. Permissions are **UNAVAILABLE**, not “none.” |
| Estimated Delivery Date & ETA | Privacy policy is linked, but no Data access section was displayed. Permissions are **UNAVAILABLE**. |
| ArrivesBy | Customer address/geolocation/device data; store-owner details; editable products, last 60 days of order history, theme/app-controlled pages, locations and Markets. |
| SupaDatewise live | Listing discloses store-owner contact data associated with the app session. The authenticated active app version has no app proxy and the reviewed live source declares no order/customer-record scope. |
| SupaDatewise branch | Adds only `write_app_proxy` for an HMAC-authenticated entitlement check; no order/customer-record scope. Scope/config deployment and reauthorization impact remain **UNAVAILABLE** until controlled install. |

## First-party review themes to design against

| App | Supported theme from Shopify-hosted reviews |
|---|---|
| Essent | No recurring failure theme established. Recent requests cluster around managing several widgets/rule profiles more efficiently and inventory-dependent lead times. |
| S Estimated Delivery Date Plus | Its six one-star reviews include live-store rendering/persistence failures, automatic filtering problems for larger catalogs, and free-plan/pricing expectation problems. Positive reviews repeatedly praise responsive support. [One-star reviews](https://apps.shopify.com/omega-estimated-shipping-date/reviews?ratings%5B%5D=1) |
| Estimated Delivery Date - ETA | Recurring low-rating themes include theme compatibility, uncontrolled placement/styling, setup becoming live before it felt safe, and unresolved support. One reviewer **alleges** support activated the app despite contrary instructions; this is an unverified merchant allegation. [One-star reviews](https://apps.shopify.com/estimated-delivery-days/reviews?ratings%5B%5D=1) |
| C-EDD | Multiple reviews report unclear or unavailable support; others cite one-by-one catalog setup, vendor-rule complexity, incorrect business-day calculations and a widget disappearing after variant changes. Two reviewers **allege** features were offered for five-star reviews; these are allegations, not established facts. [One-star reviews](https://apps.shopify.com/estimated-delivery-date-plus/reviews?ratings%5B%5D=1) |
| Estimated Delivery Date & ETA | No review below four stars. The sole four-star review criticized manual per-product setup; the developer replied that bulk options had been added. No recurring negative theme is established. [Four-star review](https://apps.shopify.com/order-delivery-estimated/reviews?ratings%5B%5D=4) |
| ArrivesBy | Price is the only repeated complaint across one- and three-star reviews; another review reports a plan-selection loop. Higher-rated reviews request metafield and fixed-restock-date support. [One-star reviews](https://apps.shopify.com/arrives-by/reviews?ratings%5B%5D=1) · [three-star reviews](https://apps.shopify.com/arrives-by/reviews?ratings%5B%5D=3) |

## Public-signal scorecard

“Config” is claimed breadth tempered by review evidence; it is not a verified
competitor product test. “Theme” combines Built for Shopify, stated theme
compatibility and review evidence. Private funnel and cleared revenue are
**UNAVAILABLE** for every row.

| App | Problem clarity | Config | Setup | Theme / reliability | Price / value | Trust / privacy | Social proof | Listing conversion |
|---|---:|---:|---:|---:|---:|---:|---:|---:|
| Essent | 3 | 3 | 5 | 5 | 5 | 4 | 5 | 4 |
| S Estimated Delivery Date Plus | 3 | 5 | 4 | 4 | 4 | 3 | 4 | 4 |
| Estimated Delivery Date - ETA | 4 | 5 | 3 | 2 | 4 | 3 | 4 | 5 |
| C-EDD | 3 | 2 | 2 | 2 | 4 | 1 | 4* | 3 |
| Estimated Delivery Date & ETA | 4 | 5 | 4 | 5 | 5 | 3 | 3 | 5 |
| ArrivesBy | 5 | 5 | 4 | 4 | 3 | 4 | 2 | 5 |
| SupaDatewise live | 2 | 2 | 2 | 2 | 2 | 2 | 1 | 3 |
| SupaDatewise branch, local evidence only | 4 | 4 | 4 | 4 | 2 | 3 | 1 | 3 |

`*` C-EDD review volume is visible, but the review-incentive allegations above
weaken its trust signal. They do not prove misconduct.

## SupaDatewise evidence state

| Field | State on 2026-08-30 |
|---|---|
| Public listing | **VERIFIED:** [SupaDatewise: Delivery Date](https://apps.shopify.com/estimated-delivery-date-6), public with active Install control |
| Visible price/trial | **VERIFIED:** $6.99/month, 7-day trial |
| Visible rating/reviews | **VERIFIED:** 0 reviews; no rating |
| Discoverability | **VERIFIED LOW:** page 18, intra-position 8 in a 454-app product-content category; absent from page one of Delivery and pickup |
| Public support | **MISSING:** live `/support` returns 404 and listing exposes no support URL/mail link |
| Public privacy | **MISSING:** live `/privacy` returns 404; listing points to an older Telegraph policy |
| Live install start | **VERIFIED:** public Install control reaches Shopify login/store selection |
| Install completion, OAuth and reauthorization | **PARTIAL:** current controlled development-store install is verified; fresh uninstall/reinstall and branch reauthorization remain approval-gated |
| Active theme block / first widget rendered | **PARTIAL:** Savor editor found the block and an unsaved preview rendered fallback dates; no saved published render or trusted event exists |
| Billing enforcement in reviewed live source | **BROKEN:** app use was not gated by an active Shopify App Pricing subscription |
| Branch billing and storefront entitlement | **VERIFIED LOCALLY:** app/settings/storefront paths fail closed without provider-confirmed active subscription; live/provider proof **UNAVAILABLE** |
| Delivery correctness | **VERIFIED LOCALLY:** `$app.settings`, store-IANA-timezone cutoff math, duplicate-load removal and preview deep link pass local regression/build checks; live proof **UNAVAILABLE** |
| Current installs/trials/paid subscriptions/churn/refunds | **VERIFIED BASELINE:** one current controlled dev-store install and one $0 Standard test contract; no real paid recurring subscription; paid churn/refunds not established |
| Verified processed, non-refunded paid installs | **0** |
| Cleared subscription revenue | **USD $0.00** in Shopify Partner evidence |

Traffic, listing reach, code, drafts, test charges, installs without payment and
pipeline are not sales or cleared revenue.

## Strategic implications

1. **Do not sell breadth before proof.** Incumbents already claim extensive
   country, ZIP, warehouse, inventory, variant and checkout rule engines.
2. **Lead with correctness and safety.** Store-timezone math, saved-rule
   delivery, one-load rendering and preview-before-activation directly counter
   visible competitor complaints and are locally testable.
3. **Make the narrow scope a trust benefit.** A single-schedule merchant does
   not need the broad order/customer access disclosed by several alternatives.
4. **Do not compete on “free basic ETA.”** Essent owns that value anchor. The
   $6.99 offer needs verified setup speed, accuracy and support, not feature-list
   parity claims.
5. **Suppress acquisition until readiness is live.** Controlled install,
   hosted plan selection, settings save, theme activation, widget render,
   privacy/support, uninstall and rollback must pass before outreach.

## Official Shopify constraints

- Listings must use a unique brand-led name, factual claims, complete
  pricing/trial disclosure and real product UI. Statistics, guarantees and
  incentivized reviews are prohibited. [App Store requirements](https://shopify.dev/docs/apps/launch/shopify-app-store/app-store-requirements)
- Paid apps must use Shopify App Pricing or the Billing API, remain free of
  billing errors, and allow plan changes without reinstalling or contacting
  support. [Shopify App Pricing](https://shopify.dev/docs/apps/launch/billing/shopify-app-pricing)
- Theme app extensions support editor placement, Shopify-hosted versioning and
  cleaner uninstall without direct theme-code edits. [Theme app extensions](https://shopify.dev/docs/apps/build/online-store/theme-app-extensions)
- Built for Shopify currently targets admin LCP at or below 2.5 seconds, CLS at
  or below 0.1 and INP at or below 200 ms at the 75th percentile; storefront
  Lighthouse impact must be no more than ten points. [Built for Shopify requirements](https://shopify.dev/docs/apps/launch/built-for-shopify/requirements)
- Public apps must verify and handle `customers/data_request`,
  `customers/redact` and `shop/redact`. [Privacy-law compliance](https://shopify.dev/docs/apps/build/compliance/privacy-law-compliance)
- Apps should request only necessary scopes; write permission includes read,
  and protected customer data requires approval. [Access scopes](https://shopify.dev/docs/api/usage/access-scopes)
- A privacy policy linked from the listing is mandatory. [Privacy requirements](https://shopify.dev/docs/apps/launch/privacy-requirements)

## Decision gate

Before any listing edit or merchant validation, obtain controlled-store and
provider evidence that the branch can complete install, hosted plan selection,
settings save, Dawn plus one non-native theme activation, first render,
uninstall, privacy/support access and rollback. Until then, the competitive
wedge is a tested local claim, not a live commercial promise.
