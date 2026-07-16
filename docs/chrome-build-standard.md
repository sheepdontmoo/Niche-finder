# Chrome extension build standard (Wave 1)

The shared spec every Chrome extension in this venture follows. Applies to
C1–C4 and any future Chrome build. Keep each build inside these rails.

## Scope
- **Manifest V3**, client-side only. No backend, no server, no database beyond
  `chrome.storage`.
- **v1 ships in ≤5 days** or the product is descoped (mission Rule 1).
- One clear job done well. Start with one platform/site; fast-follow the rest.

## Monetization — ExtPay (the paywall)
Chrome Web Store **removed native payments**, so we monetize with **ExtPay
(ExtensionPay)**: Stripe-backed, **no backend required**.
- Library: https://github.com/glench/ExtPay · Dashboard: https://extensionpay.com
- **Freemium model:** a genuinely useful free tier + a **Pro** tier gated by
  `extpay.getUser().paid` (and `.subscriptionStatus`). Support a **free trial**.
- Integration: register the extension on extensionpay.com, add the ExtPay lib,
  init `ExtPay('<extension-id>')` in the background service worker AND popup,
  gate Pro features on the user object. Payment/subscription UI is ExtPay's
  hosted page — no card handling in our code.
- **Darren (rails):** create the ExtPay account + connect Stripe, set the price
  and trial on the ExtPay dashboard. The engine wires the code to it.
- Pricing default band: ~$5–8/mo Pro (per product spec).

## Guardrails (non-negotiable — mission Rules 3 & 4)
- Only read data the user can **already see** on pages they've opened. **No
  scraping behind auth, no ToS violations, no bulk/automated data harvesting.**
- **No deception:** the listing describes exactly what the buyer gets.
- **Privacy:** all computation local; nothing leaves the browser. State this in
  the listing — it's a selling point and keeps CWS review clean.
- Request the **narrowest** `host_permissions` and permissions that work.

## Distribution baked in (from the user-acquisition plan)
- **Review-request prompt:** after a clear value moment (e.g. Nth successful
  use / first export), show a non-nagging "enjoying this? leave a review" with
  a direct CWS review link. Snooze/dismiss respected, shown at most once.
- **ASO:** keyword-rich title/subtitle/description; ≥3 clean screenshots; a
  short demo. Draft all listing copy in the product dir.

## Quality & verification (verify before "done" — Rule 3)
- **Load unpacked in Chrome and drive the real flow** on a real page; spot-check
  computed values against the page.
- **Fail soft:** if the page can't be parsed, show a friendly "couldn't read
  this page" — never crash.
- Isolate all site-specific DOM selectors in **one module** (they break; make
  fixes cheap).
- Free vs Pro gating verified via ExtPay's test mode.

## Per-extension deliverables
- `products/<id>/` — the MV3 extension code.
- Listing copy + icon + ≥3 screenshots (engine generates assets).
- A short README: what's verified, and the human-only steps (ExtPay/Stripe
  setup, CWS developer account + submission — Darren's rails).

## Build order
C1 (Reseller Analytics) → C2 (BYOK AI Rewrite) → C3 (Outlook/Yahoo Mail Kit)
→ C4 (Seller SEO). Each greenlit before its spec is fully written, to avoid
speccing products that get killed at the pick stage.
