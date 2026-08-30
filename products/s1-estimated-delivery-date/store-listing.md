# Shopify App Store listing packet — SupaDatewise

Status: **draft only — do not publish without Darren's exact action-time approval**
Evidence checked: 2026-08-30

This packet describes the current product after the paid-readiness branch is
released and verified on a controlled development store. Until that live check
passes, claims about store-timezone cutoffs and the settings-to-storefront path
are locally tested, not production proof.

## Positioning decision

**Segment:** small direct-to-consumer Shopify stores with one primary dispatch
schedule and Online Store 2.0 themes. These merchants need a dependable,
store-wide delivery window without order-data access or complex carrier rules.

**Promise:** set processing time, dispatch days, shipping range, and a daily
cutoff once; SupaDatewise shows the resulting delivery window in the store's
own timezone through a lightweight theme block.

**Honest boundary:** SupaDatewise does not currently offer product-, variant-,
inventory-, carrier-, ZIP-, or location-specific rules. It should not claim to
replace the broader rule engines sold by established competitors.

## App name (30 characters maximum)

```text
SupaDatewise: Delivery Date
```

Rationale: keep the distinctive brand first. The current listing already uses
this name; changing it is not recommended.

## Subtitle / card tagline (62 characters maximum)

```text
Store-timezone delivery dates with simple dispatch rules
```

## Introduction (100 characters maximum)

```text
Show delivery windows using your store timezone, cutoff, processing time, and dispatch days.
```

## Benefit-led description

```text
Give shoppers a clear delivery window before they buy—without editing theme
code or giving the app access to orders or customer records.

SupaDatewise is built for stores with one dependable fulfilment schedule. Set
your rules once:

• processing time in business days
• daily order cutoff in your Shopify store timezone
• minimum and maximum shipping time
• the weekdays your team dispatches orders
• message wording, date format, and locale

Add the Estimated Delivery Date block from Shopify's theme editor, preview its
placement, and save only when it looks right. The storefront calculation uses
the rules saved by the merchant and runs in a lightweight theme extension.

SupaDatewise stores the Shopify app session needed to operate the embedded app
and the merchant's delivery settings. It does not read orders or collect
shopper names, email addresses, shipping addresses, or order contents for the
delivery estimate.

Delivery dates are estimates based on the rules you configure. They are not
carrier guarantees.
```

## Feature bullets

```text
• Store-timezone cutoff: avoid shifting the promise to a shopper's device timezone
• Business-day rules: set processing, shipping range, and dispatch weekdays
• Theme-editor activation: preview the app block before saving it live
• Flexible message: choose wording, locale, and date format
• Minimal operational access: no order or customer-record scopes requested
```

## Pricing and trial

Public Shopify listing evidence on 2026-08-30:

- Standard: **USD $6.99/month**
- Trial: **7 days**
- Usage charges: none displayed

Paste-ready text:

```text
7-day free trial, then $6.99/month. One plan with all current features included.
```

The Shopify listing and Shopify App Pricing configuration remain the source of
truth. Any change to the plan, price, or trial requires a separate approval.

## Categories and compatibility

- Current primary category: **Store design → Product content**
- Recommended secondary category, if Shopify permits it and functionality
  qualifies: **Orders and shipping → Delivery and pickup**
- Compatibility: Online Store 2.0 themes whose product or cart sections support
  Shopify app blocks
- Language: English; the displayed date supports merchant-selected BCP-47
  locales

## Search terms

```text
estimated delivery date, delivery window, get it by, dispatch cutoff,
business days, shipping estimate, order cutoff, delivery ETA
```

## Screenshot plan

Use only captures from the verified app and a controlled development store.
Do not use mock competitor screens, fabricated reviews, outcome claims, or a
graphic that looks like functionality the app does not have.

1. **Settings and timezone** — real embedded settings page showing the live
   preview and Shopify store timezone. Caption: `Cutoffs follow your Shopify store timezone.`
2. **Dispatch rules** — real processing, shipping range, cutoff, and working-day
   controls. Caption: `Set the schedule your team can actually fulfil.`
3. **Theme-editor preview** — app block selected before the theme is saved.
   Caption: `Preview placement before shoppers see it.`
4. **Product-page result** — real product page with the delivery window near Add
   to cart. Caption: `A clear delivery window where shoppers decide.`
5. **Cart result**, only after verified in a compatible cart section. Caption:
   `Keep the same delivery message through the cart.`

## FAQ

### Does the cutoff use the shopper's timezone?

No. After the merchant saves settings, SupaDatewise uses the IANA timezone from
the Shopify store for the cutoff calculation. This claim must be rechecked on a
development store before publishing this packet.

### Does SupaDatewise skip weekends?

It skips every day the merchant leaves unselected. A Monday-to-Friday schedule
therefore skips Saturday and Sunday; stores that dispatch on either weekend day
can select it.

### Does it require theme code changes?

No. It uses a Shopify theme app extension. The merchant adds and previews the
block in the theme editor before saving the theme.

### What data does it access?

The branch requests only `write_app_proxy`, used to let Shopify authenticate the
storefront subscription check. Shopify's current listing still discloses
store-owner contact details associated with the app session. The app does not
request order or customer-record scopes for its delivery estimate. Reverify the
permission disclosure after a controlled install before publishing this copy.
Shopify may include a logged-in customer ID in its signed app-proxy request;
SupaDatewise ignores that field and does not persist it.

### Does it guarantee carrier delivery?

No. The date is an estimate generated from merchant-configured rules.

## Trust, privacy, and support proof required before publication

| Item | Current state on 2026-08-30 | Publication gate |
|---|---|---|
| Branded privacy URL | **MISSING live** — listing links to an older Telegraph page | Deploy and verify `/privacy`, then update the listing |
| Public support page | **MISSING live** — Fly `/support` returns 404 | Deploy and verify `/support` |
| Public support contact | **MISSING/UNAVAILABLE** on listing | Darren selects the support inbox; listing edit needs approval |
| OAuth/install | **UNAVAILABLE** without a controlled store | Complete a fresh-install test |
| Settings → storefront rules | Fixed and locally tested on this branch; **UNAVAILABLE live** | Save settings and verify the real theme block |
| Store-timezone cutoff | Locally tested; **UNAVAILABLE live** | Cross-timezone dev-store QA |
| Shopify App Pricing | Public price/trial visible; subscription lifecycle **UNAVAILABLE** | Test no-charge dev-store plan and provider events |
| Uninstall/compliance webhooks | Routes and HMAC authentication code present; delivery **UNAVAILABLE** | Verify signed deliveries in provider logs |

## Install CTA

```text
Start the 7-day trial on Shopify, set your dispatch schedule, and preview the delivery block before saving it to your theme.
```

Do not publish this CTA until the controlled install, plan selection, settings
save, theme activation, and first render all pass.
