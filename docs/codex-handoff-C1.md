# Codex handoff — build C1 (Reseller Analytics Chrome extension)

**Why this is the right parallel task:** C1 is 100% client-side (Chrome MV3),
needs no auth, no accounts, and no backend to build — a self-contained unit
Codex can build start-to-finish while Claude Code owns S1 (Shopify). No overlap.

**Read first:** [`specs/C1-reseller-analytics.md`](specs/C1-reseller-analytics.md)
— the full spec (scope, monetization, success criteria, listing copy).

## Build target

MV3 Chrome extension in `products/c1-reseller-analytics/`.

- **v1 platform: eBay only** (seller "active"/"sold" pages). One platform,
  done well. Others are fast-follow.
- **Content script** on the eBay seller pages: parse the listings the user
  already has open (no auth scraping, no data the user can't see) and compute:
  listing count, avg/median price, total inventory value, item age (days
  listed), and on the sold view: sell-through rate + avg days-to-sell.
- **Popup** dashboard summarizing the current page.
- **Pro tier** (multi-page aggregate, CSV export, local history) gated with
  **ExtPay** (`ExtPay('reseller-analytics')`) — Stripe, no backend.
  Docs: https://extensionpay.com/ · https://github.com/glench/ExtPay
- All computation local; nothing leaves the browser (state it in the listing).

## Guardrails (non-negotiable, from the mission)

1. **v1 ships in ≤5 days** — if a piece risks that, descope it, don't gold-plate.
2. Only read data the user can already see on pages they've opened. No
   scraping behind auth, no ToS violations.
3. No deception — the listing must describe exactly what the buyer gets.
4. Isolate all eBay DOM selectors in one module (they break; make fixes cheap)
   and fail soft ("couldn't read this page") rather than crash.

## Definition of done

- Loads unpacked in Chrome; dashboard shows correct stats on a real eBay
  seller page (spot-checked against the page).
- CSV export opens cleanly in Sheets/Excel.
- Free vs Pro gating works via ExtPay `getUser()`.
- Listing assets drafted (icon, 3 screenshots, description — copy in the spec).
- Commit to the same repo on this feature branch under `products/c1-reseller-analytics/`.

## Coordination

- Claude Code is building S1 in `products/s1-estimated-delivery-date/` — no
  shared files, safe to work in parallel.
- Darren: kill/keep on the other 6 Wave-1 picks is still open
  (see [`wave-1-plan.md`](wave-1-plan.md)); C1 was already greenlit as a lead.
