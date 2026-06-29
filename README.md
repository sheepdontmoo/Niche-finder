# Niche-finder — a $25k local web-presence engine

A pipeline that finds local businesses with weak or missing websites,
**pre-builds a personalized demo site for each**, and writes the personalized
outreach to land them as paying clients. The goal: **$25,000** from selling
website builds (~$899 each) + recurring care plans ($59/mo).

> **The honest version:** this engine does the *building, research, and writing*.
> It does **not** replace the human work that actually closes money — sending the
> messages, making the calls, and collecting payment. The engine makes those
> human hours far more productive (you walk in with a finished demo, not a cold
> pitch), but consistent outreach is still the bottleneck. Realistic ramp: first
> client in ~1–2 weeks, $25k in ~6–14 weeks with steady effort.

---

## Who does what

| Claude / this engine (automated) | You, the human (only what software can't) |
|---|---|
| Source prospects from public data | Set up business email, phone, **Stripe/PayPal** |
| Audit each site → "pain score" | Buy a domain + deploy demos (free hosts) |
| Build personalized demo sites | **Send** the outreach, **make the calls** |
| Write email/SMS/call scripts | Run sales calls, **close**, collect deposits |
| Track funnel + revenue in CRM | Pass phone/ID checks (Google Business etc.) |
| Build the real site once sold | Collect client's real logo/photos; final QA |

See **[HUMAN_TASKS.md](HUMAN_TASKS.md)** for your exact step-by-step checklist.

---

## Quick start

```bash
pip install -r requirements.txt
cd niche-finder

# 1. Edit config.yaml — set your area, business name/phone/email, demo_base_url.

# 2. Run the whole pipeline (live OSM data):
python run_all.py
#    ...or target one niche:  python run_all.py --niche roofer --limit 15

# No network / want to see it work first? Use the bundled sample:
python source_prospects.py --sample
python audit_site.py && python find_niches.py
python generate_demo.py && python generate_outreach.py
python crm.py import && python crm.py report
```

Outputs:
- `data/prospects.json` — sourced + audited + scored prospects
- `data/niches_ranked.json` — which niche to attack first
- `demos/<slug>/index.html` — a deployable demo site per prospect
- `outreach/<slug>.md` + `outreach/_ALL.md` — ready-to-send message packs
- `data/crm.json` — your pipeline + revenue ledger

---

## The pipeline (each stage is its own script)

1. **`source_prospects.py`** — query OpenStreetMap (free, no key) for local
   businesses in your area for each niche in `config.yaml`.
2. **`audit_site.py`** — fetch each prospect's site and score "pain" (no site,
   no HTTPS, not mobile, slow, empty). Higher pain = easier sale.
3. **`find_niches.py`** — rank niches by `hot prospects × customer value ×
   weak-presence rate` so you focus where the money is.
4. **`generate_demo.py`** — render a personalized, niche-tailored demo from
   `templates/site/template.html`. Clearly flagged as a free mockup.
5. **`generate_outreach.py`** — personalized email + SMS + phone script per
   prospect, each referencing their demo URL and specific gap. CAN-SPAM safe.
6. **`crm.py`** — move prospects through `new → contacted → replied → call →
   won/lost`, log revenue, and track progress to $25k.

`run_all.py` chains 1–6.

---

## Deploying demos (so prospects get a real link)

The `demos/` folder is plain static HTML. Fastest free options:
- **Netlify Drop** — drag the `demos/` folder onto https://app.netlify.com/drop
- **Cloudflare Pages** — connect this repo or upload `demos/`
- **GitHub Pages** — push and enable Pages on `demos/`

Then set `offer.demo_base_url` in `config.yaml` to your deployed root and re-run
steps 4–5 so the outreach links are correct.

---

## Adding real imagery (optional, raises close rate)

Demos ship with a clean gradient hero by default. To add AI-generated,
niche-specific photography, generate images with the available Firefly/Magnific
tools and drop them into the demo's hero/services, replacing the CSS gradient in
`templates/site/template.html` (`.hero { background: ... }`). Swap in the
**client's real photos** on delivery.

---

## Guardrails (don't skip)

- Only **public, ToS-compliant** data; the sourcer rate-limits itself.
- Outreach includes sender identity + opt-out (**CAN-SPAM**). You send it and
  follow local anti-spam / Do-Not-Call rules.
- Demos are labeled **free unsolicited mockups** — no impersonation, honest that
  the business is new. Review placeholders are marked illustrative.
- No fake reviews, no spam blasting, no deception. Build something real.

---

## Tracking the goal

`python crm.py report` shows your funnel (reply/close rates) and a progress bar
toward $25,000, counting one-time builds plus 3 months of booked recurring. Use
it weekly to double down on the niche and message that convert best.
