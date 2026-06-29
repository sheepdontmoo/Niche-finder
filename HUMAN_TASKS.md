# Your checklist (the human-only tasks)

These are the things the engine can't do for you. Everything else is automated.
Do them roughly in order. Total setup time: a few hours.

## Phase 0 — Setup (Day 1, ~2–3 hours, ~$20–60)
- [ ] **Pick a business name** (e.g. "BrightLocal Sites"). Put it in `config.yaml`.
- [ ] **Business email** — a Gmail or Google Workspace address with your brand.
- [ ] **Phone number** — free Google Voice number is fine. Put it in `config.yaml`.
- [ ] **Payment** — open **Stripe** (or PayPal Business). This is how you collect
      deposits/payments. Stripe needs your real identity + a bank account.
- [ ] **Domain** (optional, ~$12/yr) — a `.com` for your brand looks legit.
- [ ] **Free hosting account** — Netlify or Cloudflare Pages, to deploy demos.

## Phase 1 — Let the engine build (Day 1–2, mostly automated)
- [ ] Set your `area.bbox` and `area.label` in `config.yaml` (your city).
- [ ] Run `python niche-finder/run_all.py`.
- [ ] **Deploy** the `demos/` folder to Netlify/Cloudflare; copy the root URL into
      `offer.demo_base_url`; re-run `generate_demo.py` + `generate_outreach.py`.
- [ ] Skim 3–4 generated demos and outreach packs — confirm they look right.

## Phase 2 — Outreach (Day 3 onward, ~10 hrs/week — THIS is the money step)
- [ ] Work the list **hottest-first** (`python niche-finder/crm.py list`).
- [ ] For each prospect: **call** (best), or text/email the demo link. Scripts are
      in `outreach/<slug>.md`. Lead with: *"I built you a free preview — here's the link."*
- [ ] Log every touch: `crm.py set <slug> contacted` / `replied` / `call`.
- [ ] Aim for volume: ~15–25 personalized touches/day. Reply rate climbs with follow-ups.
- [ ] Send me ("Claude") any replies — I'll draft the response and handle objections.

## Phase 3 — Close & deliver (per sale)
- [ ] On a yes: send a simple 1-page agreement + **Stripe invoice for a deposit**
      (e.g. 50% upfront). Collect the client's logo, photos, hours, services.
- [ ] Hand those to me — I build the real site (swap in their real photos) within
      ~3 days, you review, then deploy to their domain.
- [ ] Record it: `python niche-finder/crm.py won <slug> --amount 899 --mrr 59`.
- [ ] Offer the **$59/mo care plan** — recurring revenue is what compounds past $25k.

## Phase 4 — Iterate weekly
- [ ] `python niche-finder/crm.py report` — check reply/close rates and $ vs goal.
- [ ] Tell me which niche/message converts best; I'll refocus sourcing + rewrite copy.

---

### Rules of the road (keep it legit)
- Follow CAN-SPAM (the emails already include identity + opt-out) and local
  Do-Not-Call rules. Honor every "stop"/"no thanks" immediately.
- Never claim to be the business. Demos are clearly free, unsolicited mockups.
- Deliver real value — a good site, on time. Reputation is the whole game.
