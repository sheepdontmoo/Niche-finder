"""
find_niches.py — rank the targeted niches by opportunity so you focus your
limited time on the niche that converts best.

opportunity_score = (#hot prospects) x (avg_customer_value) x (weak-presence rate)

Reads data/prospects.json (after auditing) and writes data/niches_ranked.json,
and prints a leaderboard.

Usage:
    python niche-finder/find_niches.py
"""
from __future__ import annotations

import argparse
from collections import defaultdict

from common import NICHES_FILE, PROSPECTS_FILE, load_config, read_json, write_json


def main(argv=None) -> int:
    ap = argparse.ArgumentParser(description="Rank niches by opportunity.")
    ap.add_argument("--config", default=None)
    args = ap.parse_args(argv)

    cfg = load_config(args.config)
    values = {n["key"]: n["avg_customer_value"] for n in cfg["niches"]}
    labels = {n["key"]: n["label"] for n in cfg["niches"]}
    min_pain = cfg["pipeline"]["min_pain_score"]

    prospects = read_json(PROSPECTS_FILE, [])
    if not prospects:
        print("No prospects found. Run source_prospects.py then audit_site.py first.")
        return 1
    if "pain_score" not in prospects[0]:
        print("Prospects are not audited yet. Run audit_site.py first.")
        return 1

    buckets = defaultdict(list)
    for p in prospects:
        buckets[p["niche"]].append(p)

    ranked = []
    for key, items in buckets.items():
        total = len(items)
        hot = sum(1 for p in items if p["pain_score"] >= min_pain)
        weak_rate = hot / total if total else 0
        value = values.get(key, 100)
        score = round(hot * value * weak_rate, 1)
        ranked.append({
            "niche": key,
            "label": labels.get(key, key),
            "total_prospects": total,
            "hot_prospects": hot,
            "weak_presence_rate": round(weak_rate, 2),
            "avg_customer_value": value,
            "opportunity_score": score,
        })

    ranked.sort(key=lambda x: x["opportunity_score"], reverse=True)
    write_json(NICHES_FILE, ranked)

    print(f"\n{'NICHE':<26}{'TOTAL':>6}{'HOT':>5}{'WEAK%':>7}{'$VAL':>7}{'SCORE':>10}")
    print("-" * 61)
    for r in ranked:
        print(f"{r['label'][:25]:<26}{r['total_prospects']:>6}{r['hot_prospects']:>5}"
              f"{int(r['weak_presence_rate']*100):>6}%{r['avg_customer_value']:>7}"
              f"{r['opportunity_score']:>10}")
    if ranked:
        print(f"\n→ Start with: {ranked[0]['label']} "
              f"({ranked[0]['hot_prospects']} hot prospects ready).")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
