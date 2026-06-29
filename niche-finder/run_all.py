"""
run_all.py — run the whole pipeline end to end:
  source -> audit -> rank -> demos -> outreach -> CRM import -> report

Usage:
    python niche-finder/run_all.py
    python niche-finder/run_all.py --niche plumber --limit 10
"""
from __future__ import annotations

import argparse

import audit_site
import crm
import find_niches
import generate_demo
import generate_outreach
import source_prospects


def main() -> int:
    ap = argparse.ArgumentParser(description="Run the full Niche-finder pipeline.")
    ap.add_argument("--niche", nargs="*")
    ap.add_argument("--limit", type=int, default=None)
    args = ap.parse_args()
    niche_args = (["--niche", *args.niche] if args.niche else [])

    print("\n[1/6] Sourcing prospects from OpenStreetMap ...")
    source_prospects.main(niche_args)
    print("\n[2/6] Auditing web presence ...")
    audit_site.main([])
    print("\n[3/6] Ranking niches ...")
    find_niches.main([])
    print("\n[4/6] Generating demo sites ...")
    demo_args = niche_args + (["--limit", str(args.limit)] if args.limit else [])
    generate_demo.main(demo_args)
    print("\n[5/6] Generating outreach ...")
    generate_outreach.main(niche_args)
    print("\n[6/6] Importing into CRM ...")
    crm.main(["import"])
    crm.main(["report"])
    print("\n✅ Pipeline complete. Next: deploy demos/ to a static host, set "
          "demo_base_url in config.yaml, re-run steps 4-5, then start outreach.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
