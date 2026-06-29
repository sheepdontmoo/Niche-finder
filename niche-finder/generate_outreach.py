"""
generate_outreach.py — write personalized email, SMS/DM, and phone scripts for
each prospect that has a demo, referencing the specific demo URL and their
specific web-presence gap. This is what makes cold outreach convert: you lead
with a free thing you already built for them.

Output: outreach/<slug>.md (one ready-to-send pack per prospect) and
outreach/_ALL.md (everything in one file for fast copy/paste).

Compliance: every email includes sender identity + an opt-out line (CAN-SPAM).
YOU send these from your own accounts and follow local anti-spam/DNC rules.

Usage:
    python niche-finder/generate_outreach.py
"""
from __future__ import annotations

import argparse

from common import (OUTREACH, PROSPECTS_FILE, ensure_dirs, load_config,
                    read_json)


def primary_gap(reasons: list[str]) -> str:
    if not reasons:
        return "your online presence could bring in more local customers"
    r = reasons[0]
    mapping = {
        "no website": "you don't have a website yet, so customers searching online find your competitors instead",
        "unreachable": "your current site isn't loading, which means lost calls",
        "no HTTPS": "your site shows a 'not secure' warning that scares off visitors",
        "not mobile": "your site isn't mobile-friendly — and most local searches are on phones",
        "slow": "your site loads slowly, and slow sites lose customers",
        "near-empty": "your current page is mostly empty, so visitors bounce",
    }
    for k, v in mapping.items():
        if k in r:
            return v
    return "a sharper website would help you win more local jobs"


def make_pack(p, cfg) -> str:
    biz = cfg["business"]
    offer = cfg["offer"]
    city = cfg["area"]["label"].split(",")[0].strip()
    name = p["name"]
    demo = p.get("demo_url", "(deploy demos first)")
    gap = primary_gap(p.get("pain_reasons", []))
    niche = p.get("niche_label", "business").lower().rstrip("s")
    price = offer["build_price"]

    email = f"""Subject: built a quick website preview for {name}

Hi {name} team,

I'm {biz['owner']} with {biz['name']}, here in {city}. I build websites for
local {niche}s — and I noticed {gap}.

So I went ahead and built you a free preview to show what it could look like:
{demo}

If you like it, I can have your real version live in {offer['turnaround_days']} days
for a one-time ${price} (optional ${offer['care_plan_monthly']}/mo to keep it
updated and hosted). No pressure either way — the preview is yours to look at.

Worth a quick call?  — {biz['owner']}, {biz['name']}, {biz['phone']}

----
You're receiving this one-time note because your business is publicly listed in
{city}. Reply "no thanks" and I won't contact you again. {biz['name']}, {biz['email']}.
"""

    sms = (f"Hi {name} — {biz['owner']} here, local web designer in {city}. "
           f"I built you a free website preview: {demo} — like it? Happy to make it "
           f"yours for a flat ${price}. No pressure! Reply STOP to opt out.")

    call = f"""PHONE SCRIPT — {name}  ({p.get('phone','no number on file')})
Pain: {', '.join(p.get('pain_reasons', [])) or 'n/a'}   Demo: {demo}

"Hi, is this the owner? Great — my name's {biz['owner']}, I'm a local web
designer here in {city}. I'll be quick: I noticed {gap}, so I actually built
you a free sample website to show what it could look like. Can I text or email
you the link right now so you can see it?"

[If yes →] "Awesome, what's the best number/email? ... You'll have it in a
second. If you like it, I can make it your real site, live in
{offer['turnaround_days']} days, flat ${price}. Want me to walk you through it?"

[Objection: "too busy / not interested"] → "Totally fair — I'll text the link
so you can peek whenever. Costs you nothing to look. Sound okay?"

[Objection: "I already have a site"] → "I saw it — that's actually why I called.
{gap.capitalize()}. The preview fixes that. Take 30 seconds to compare?"
"""
    return f"# {name}\n\n## Email\n```\n{email}\n```\n\n## SMS / DM\n```\n{sms}\n```\n\n## {call}\n"


def main(argv=None) -> int:
    ap = argparse.ArgumentParser(description="Generate outreach packs.")
    ap.add_argument("--config", default=None)
    ap.add_argument("--niche", nargs="*")
    args = ap.parse_args(argv)

    ensure_dirs()
    cfg = load_config(args.config)
    prospects = read_json(PROSPECTS_FILE, [])
    targets = [p for p in prospects if p.get("demo_url")]
    if args.niche:
        targets = [p for p in targets if p["niche"] in set(args.niche)]
    if not targets:
        print("No prospects with demos yet. Run generate_demo.py first.")
        return 1

    all_md = [f"# Outreach packs — {cfg['business']['name']} ({len(targets)} prospects)\n"]
    for p in targets:
        pack = make_pack(p, cfg)
        (OUTREACH / f"{p['slug']}.md").write_text(pack, encoding="utf-8")
        all_md.append(pack + "\n---\n")
        print(f"  ✓ outreach/{p['slug']}.md")

    (OUTREACH / "_ALL.md").write_text("\n".join(all_md), encoding="utf-8")
    print(f"\nWrote {len(targets)} outreach packs → outreach/  "
          f"(combined: outreach/_ALL.md). Load into the CRM, then start sending.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
