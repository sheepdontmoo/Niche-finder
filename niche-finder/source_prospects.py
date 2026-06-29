"""
source_prospects.py — pull local businesses from OpenStreetMap (free, no API key).

For each niche in config.yaml we query the Overpass API inside the configured
bounding box and collect businesses with their name, address, phone, and any
website tag. The result is written to data/prospects.json for the next stage
(audit_site.py).

Usage:
    python niche-finder/source_prospects.py
    python niche-finder/source_prospects.py --niche plumber roofer
"""
from __future__ import annotations

import argparse
import sys
import time

import requests

from common import PROSPECTS_FILE, ensure_dirs, load_config, read_json, slugify, write_json


def build_query(bbox, osm_filters) -> str:
    south, west, north, east = bbox
    bb = f"{south},{west},{north},{east}"
    parts = []
    for flt in osm_filters:
        key, _, val = flt.partition("=")
        sel = f'["{key}"="{val}"]' if val else f'["{key}"]'
        for kind in ("node", "way"):
            parts.append(f"  {kind}{sel}({bb});")
    body = "\n".join(parts)
    return f"[out:json][timeout:60];\n(\n{body}\n);\nout center tags;"


def parse_address(tags: dict) -> str:
    num = tags.get("addr:housenumber", "")
    street = tags.get("addr:street", "")
    city = tags.get("addr:city", "")
    line = " ".join(p for p in (num, street) if p).strip()
    return ", ".join(p for p in (line, city) if p).strip()


def fetch_niche(cfg, niche) -> list[dict]:
    query = build_query(cfg["area"]["bbox"], niche["osm_filters"])
    resp = requests.post(
        cfg["pipeline"]["overpass_url"],
        data={"data": query},
        headers={"User-Agent": cfg["pipeline"]["user_agent"]},
        timeout=cfg["pipeline"]["request_timeout"],
    )
    resp.raise_for_status()
    elements = resp.json().get("elements", [])

    seen, prospects = set(), []
    for el in elements:
        tags = el.get("tags", {})
        name = tags.get("name")
        if not name:
            continue  # unnamed POIs are not sellable prospects
        slug = slugify(name)
        if slug in seen:
            continue
        seen.add(slug)
        lat = el.get("lat") or el.get("center", {}).get("lat")
        lon = el.get("lon") or el.get("center", {}).get("lon")
        prospects.append({
            "slug": slug,
            "name": name,
            "niche": niche["key"],
            "niche_label": niche["label"],
            "address": parse_address(tags),
            "phone": tags.get("phone") or tags.get("contact:phone", ""),
            "email": tags.get("email") or tags.get("contact:email", ""),
            "website": tags.get("website") or tags.get("contact:website", ""),
            "lat": lat,
            "lon": lon,
            "source": f"osm/{el.get('type')}/{el.get('id')}",
        })
    return prospects


def main(argv=None) -> int:
    ap = argparse.ArgumentParser(description="Source local-business prospects from OSM.")
    ap.add_argument("--config", default=None)
    ap.add_argument("--niche", nargs="*", help="limit to these niche keys")
    ap.add_argument("--sample", action="store_true",
                    help="load data/sample_prospects.json instead of hitting OSM "
                         "(for offline testing / restricted networks)")
    args = ap.parse_args(argv)

    ensure_dirs()
    cfg = load_config(args.config)

    if args.sample:
        from common import DATA
        sample = read_json(DATA / "sample_prospects.json", [])
        if args.niche:
            sample = [p for p in sample if p["niche"] in set(args.niche)]
        write_json(PROSPECTS_FILE, sample)
        print(f"Loaded {len(sample)} SAMPLE prospects → {PROSPECTS_FILE}")
        return 0
    niches = cfg["niches"]
    if args.niche:
        wanted = set(args.niche)
        niches = [n for n in niches if n["key"] in wanted]
        if not niches:
            print(f"No niches matched {args.niche}", file=sys.stderr)
            return 1

    all_prospects, by_slug = [], {}
    for niche in niches:
        print(f"→ {niche['label']:<28} querying OSM in {cfg['area']['label']} ...", flush=True)
        try:
            found = fetch_niche(cfg, niche)
        except requests.RequestException as exc:
            print(f"  ! request failed: {exc}", file=sys.stderr)
            continue
        # de-dup across niches by slug, keep first
        new = 0
        for p in found:
            if p["slug"] not in by_slug:
                by_slug[p["slug"]] = p
                all_prospects.append(p)
                new += 1
        print(f"  found {len(found)} ({new} new)")
        time.sleep(2)  # be polite to the free Overpass endpoint

    write_json(PROSPECTS_FILE, all_prospects)
    with_site = sum(1 for p in all_prospects if p["website"])
    print(
        f"\nWrote {len(all_prospects)} prospects → {PROSPECTS_FILE.relative_to(PROSPECTS_FILE.parents[1])}"
        f"  ({len(all_prospects) - with_site} with NO website tag)"
    )
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
