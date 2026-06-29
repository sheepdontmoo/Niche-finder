"""
generate_demo.py — build a personalized, ready-to-deploy demo website for each
hot prospect, using templates/site/template.html.

The demo is clearly flagged as a free unsolicited mockup (honesty guardrail).
Output: demos/<slug>/index.html — deploy the demos/ folder to any static host
(Netlify/Cloudflare Pages drag-and-drop) so each prospect has a real URL.

Images: placeholders are used by default. To drop in AI-generated hero/service
imagery, generate with the available Firefly/Magnific tools and replace the
CSS gradient — see README "Adding real imagery".

Usage:
    python niche-finder/generate_demo.py
    python niche-finder/generate_demo.py --limit 10 --niche plumber
"""
from __future__ import annotations

import argparse
import re

from common import (DEMOS, ROOT, load_config, read_json, slugify,
                    PROSPECTS_FILE, ensure_dirs)

TEMPLATE = ROOT / "templates" / "site" / "template.html"

# Per-niche service cards + a brand color, so demos feel tailored not generic.
NICHE_KIT = {
    "plumber":     {"brand": "#1d6fe0", "dark": "#10448c",
                    "services": [("🚿", "Leak & pipe repair", "Drips, burst pipes, and hidden leaks found and fixed fast."),
                                 ("🔥", "Water heaters", "Repair or replace — tank and tankless, same-week."),
                                 ("🚽", "Drains & toilets", "Clogs cleared, toilets and fixtures installed right."),
                                 ("🏠", "Repipes & remodels", "Whole-home repiping and bathroom/kitchen plumbing.")]},
    "hvac":        {"brand": "#e0531d", "dark": "#9c3410",
                    "services": [("❄️", "AC repair", "Cooling down? We diagnose and fix fast in the heat."),
                                 ("🔥", "Heating", "Furnace and heat-pump repair, tune-ups, installs."),
                                 ("🌬️", "Installs", "New high-efficiency systems sized right for your home."),
                                 ("🧰", "Maintenance", "Seasonal tune-ups that prevent costly breakdowns.")]},
    "electrician": {"brand": "#e0a81d", "dark": "#9c7110",
                    "services": [("💡", "Repairs & rewires", "Flickering lights, dead outlets, panel upgrades."),
                                 ("🔌", "Installs", "Fixtures, fans, EV chargers, smart switches."),
                                 ("⚡", "Panel upgrades", "Safe, code-compliant service and panel work."),
                                 ("🛡️", "Safety checks", "Full inspections to keep your home safe.")]},
    "roofer":      {"brand": "#7a4a22", "dark": "#523014",
                    "services": [("🏠", "Roof repair", "Leaks, missing shingles, storm damage handled."),
                                 ("🔨", "Replacement", "Durable new roofs with warranties you can trust."),
                                 ("🌧️", "Gutters", "Gutter repair and installation done right."),
                                 ("🔍", "Free inspection", "Honest assessment with photos, no pressure.")]},
    "dentist":     {"brand": "#1aa3a3", "dark": "#0d6b6b",
                    "services": [("🦷", "Checkups", "Gentle cleanings and exams for the whole family."),
                                 ("✨", "Cosmetic", "Whitening, veneers, and confident smiles."),
                                 ("🚑", "Emergency", "In pain? We see emergencies same-day."),
                                 ("😊", "Implants", "Restore your smile with lasting implants.")]},
    "salon":       {"brand": "#c0398a", "dark": "#82255d",
                    "services": [("✂️", "Cuts & styling", "Modern cuts and styles for every look."),
                                 ("🎨", "Color", "Balayage, highlights, and full color."),
                                 ("💅", "Nails & beauty", "Manicures, pedicures, and treatments."),
                                 ("💍", "Events", "Wedding and special-occasion styling.")]},
    "landscaper":  {"brand": "#3a9a3a", "dark": "#236123",
                    "services": [("🌱", "Lawn care", "Mowing, edging, and season-long upkeep."),
                                 ("🌳", "Design", "Beautiful, low-maintenance landscape design."),
                                 ("💧", "Irrigation", "Smart sprinkler installs and repairs."),
                                 ("🍂", "Cleanups", "Leaf, brush, and seasonal cleanups.")]},
}
DEFAULT_KIT = {"brand": "#1d6fe0", "dark": "#10448c",
               "services": [("⭐", "Quality service", "Done right the first time, every time."),
                            ("💬", "Friendly team", "Local pros who treat you like a neighbor."),
                            ("$", "Fair pricing", "Honest quotes with no surprises."),
                            ("⏱", "Fast response", "Quick to call back and quick to show up.")]}

REVIEWS = [
    ("Showed up on time and the price was exactly what they quoted. Highly recommend.", "Sarah M."),
    ("Professional, friendly, and did great work. Will definitely use again.", "James T."),
    ("Fast response and fixed everything the same day. Lifesavers!", "Dana R."),
]


def card(icon, title, body):
    return (f'<div class="card"><div class="ico">{icon}</div>'
            f'<h3>{title}</h3><p>{body}</p></div>')


def review_card(text, who):
    return (f'<div class="card review"><div class="stars">★★★★★</div>'
            f'<p>“{text}”</p><div class="who">— {who}</div></div>')


def render(prospect, cfg) -> str:
    html = TEMPLATE.read_text(encoding="utf-8")
    kit = NICHE_KIT.get(prospect["niche"], DEFAULT_KIT)
    city = cfg["area"]["label"].split(",")[0].strip()
    niche_label = prospect.get("niche_label", "Local Services")
    phone = prospect.get("phone") or cfg["business"]["phone"]
    phone_raw = re.sub(r"[^\d+]", "", phone)
    biz = cfg["business"]["name"]

    repl = {
        "{{BUSINESS_NAME}}": prospect["name"],
        "{{NICHE_LABEL}}": niche_label,
        "{{NICHE_LOWER}}": niche_label.lower().rstrip("s") if niche_label else "pro",
        "{{CITY}}": city,
        "{{ADDRESS}}": prospect.get("address") or city,
        "{{PHONE}}": phone,
        "{{PHONE_RAW}}": phone_raw or "+15555555555",
        "{{BRAND}}": kit["brand"],
        "{{BRAND_DARK}}": kit["dark"],
        "{{HERO_HEADLINE}}": f"{city}'s trusted {niche_label.lower().rstrip('s')}",
        "{{TAGLINE}}": "Quality work you can count on.",
        "{{SERVICE_CARDS}}": "\n    ".join(card(*s) for s in kit["services"]),
        "{{REVIEW_CARDS}}": "\n    ".join(review_card(*r) for r in REVIEWS),
        "{{DEMO_FLAG}}": (f"Free demo mockup built for {prospect['name']} by "
                          f"{biz} — not affiliated. Like it? Call us to make it yours."),
        "{{FOOTER_NOTE}}": (f"Demo concept by {biz}. Reviews shown are illustrative "
                            f"placeholders for layout preview only."),
    }
    for k, v in repl.items():
        html = html.replace(k, str(v))
    return html


def main(argv=None) -> int:
    ap = argparse.ArgumentParser(description="Generate personalized demo sites.")
    ap.add_argument("--config", default=None)
    ap.add_argument("--niche", nargs="*", help="limit to these niche keys")
    ap.add_argument("--limit", type=int, default=None, help="max demos to build")
    args = ap.parse_args(argv)

    ensure_dirs()
    cfg = load_config(args.config)
    min_pain = cfg["pipeline"]["min_pain_score"]
    limit = args.limit or cfg["pipeline"]["max_demos_per_run"]

    prospects = read_json(PROSPECTS_FILE, [])
    if not prospects or "pain_score" not in (prospects[0] if prospects else {}):
        print("Need audited prospects. Run source_prospects.py then audit_site.py.")
        return 1

    hot = [p for p in prospects if p["pain_score"] >= min_pain]
    if args.niche:
        hot = [p for p in hot if p["niche"] in set(args.niche)]
    hot = hot[:limit]
    if not hot:
        print("No hot prospects matched. Lower min_pain_score or widen the area.")
        return 1

    base = cfg["offer"]["demo_base_url"].rstrip("/")
    built = 0
    for p in hot:
        out_dir = DEMOS / p["slug"]
        out_dir.mkdir(parents=True, exist_ok=True)
        (out_dir / "index.html").write_text(render(p, cfg), encoding="utf-8")
        p["demo_url"] = f"{base}/{p['slug']}/"
        built += 1
        print(f"  ✓ {p['name'][:40]:<42} → demos/{p['slug']}/")

    from common import write_json
    write_json(PROSPECTS_FILE, prospects)
    print(f"\nBuilt {built} demo sites in demos/. Deploy that folder to a static "
          f"host, then run generate_outreach.py.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
