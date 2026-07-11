# Portfolio table (Rule 5)

Updated weekly. Verdict ∈ {kill, hold, feed}. Status ∈ {spec, building,
in-review, live, killed}.

**Week of 2026-07-10 (Day 0)**

| Product | Platform | Status | Installs | MRR | Verdict |
|---------|----------|--------|----------|-----|---------|
| C1 · Reseller Analytics & Export | Chrome | spec | 0 | €0 | hold |
| C2 · BYOK AI Rewrite Anywhere | Chrome | proposed | 0 | €0 | hold |
| C3 · Outlook/Yahoo Mail Power Kit | Chrome | proposed | 0 | €0 | hold |
| C4 · Seller SEO / Listing Optimizer | Chrome | proposed | 0 | €0 | hold |
| S1 · Estimated Delivery Date | Shopify | building | 0 | €0 | feed |
| S2 · Purchase Rules | Shopify | proposed | 0 | €0 | hold |
| F1 · Smart Content Populator | Figma | proposed | 0 | €0 | hold |
| F2 · Redline & Handoff Kit | Figma | proposed | 0 | €0 | hold |

**Combined MRR:** €0 · **Live:** 0/8 · **Next gate:** Day 30 — 8 live, first paid sale.

## Log

- **2026-07-10** — Wave 1 researched and proposed (see
  [`wave-1-plan.md`](wave-1-plan.md)). C1 + S1 fully specced. Awaiting
  Darren greenlight + Shopify Partner account.
- **2026-07-11** — Shopify Partner app created (app `328515354625`).
  **S1 built**: full embedded app + theme app extension + billing, in
  [`products/s1-estimated-delivery-date/`](../products/s1-estimated-delivery-date/).
  Tests/typecheck/build/lint all green here; on-store verification + submit
  are the remaining (auth-gated) steps. **C1 handed to Codex** in parallel
  ([`codex-handoff-C1.md`](codex-handoff-C1.md)).
