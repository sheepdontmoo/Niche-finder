"""
audit_site.py — score each prospect's existing web presence (the "pain score").

The higher the pain score, the better the prospect: businesses with no website,
no mobile support, no SSL, or a slow/broken site are the easiest sales because
the gap is obvious and the demo we pre-build is a dramatic upgrade.

Reads data/prospects.json, fetches each site (politely), and writes the pain
score + reasons back to the same file.

Usage:
    python niche-finder/audit_site.py
"""
from __future__ import annotations

import argparse
import time

import requests

from common import PROSPECTS_FILE, load_config, read_json, write_json

# Pain points and the weight each contributes (0-100 scale, capped at 100).
WEIGHTS = {
    "no_website": 60,
    "unreachable": 45,
    "no_ssl": 20,
    "not_mobile": 25,
    "slow": 15,
    "tiny_page": 15,   # near-empty / placeholder page
}


def audit_one(prospect: dict, timeout: int) -> dict:
    url = (prospect.get("website") or "").strip()
    reasons, score = [], 0

    if not url:
        return {"pain_score": WEIGHTS["no_website"], "pain_reasons": ["no website at all"],
                "audited_url": "", "http_ok": False}

    if not url.startswith(("http://", "https://")):
        url = "https://" + url

    try:
        t0 = time.time()
        resp = requests.get(
            url, timeout=timeout, allow_redirects=True,
            headers={"User-Agent": "Mozilla/5.0 (compatible; niche-finder audit)"},
        )
        elapsed = time.time() - t0
        final = resp.url
        html = resp.text or ""

        if resp.status_code >= 400:
            score += WEIGHTS["unreachable"]
            reasons.append(f"site returns HTTP {resp.status_code}")
        if not final.startswith("https://"):
            score += WEIGHTS["no_ssl"]
            reasons.append("no HTTPS / insecure (browsers warn visitors)")
        if "viewport" not in html.lower():
            score += WEIGHTS["not_mobile"]
            reasons.append("not mobile-friendly (no responsive viewport)")
        if elapsed > 4:
            score += WEIGHTS["slow"]
            reasons.append(f"slow to load ({elapsed:.1f}s)")
        if len(html) < 800:
            score += WEIGHTS["tiny_page"]
            reasons.append("near-empty / placeholder page")
        if not reasons:
            reasons.append("has a working site — lower priority (offer redesign/SEO)")

        return {"pain_score": min(score, 100), "pain_reasons": reasons,
                "audited_url": final, "http_ok": resp.status_code < 400}

    except requests.RequestException:
        return {"pain_score": WEIGHTS["unreachable"],
                "pain_reasons": ["website tag exists but site is unreachable/broken"],
                "audited_url": url, "http_ok": False}


def main(argv=None) -> int:
    ap = argparse.ArgumentParser(description="Audit prospect websites and score pain.")
    ap.add_argument("--config", default=None)
    args = ap.parse_args(argv)

    cfg = load_config(args.config)
    timeout = cfg["pipeline"]["audit_timeout"]
    prospects = read_json(PROSPECTS_FILE, [])
    if not prospects:
        print("No prospects found. Run source_prospects.py first.")
        return 1

    print(f"Auditing {len(prospects)} prospects ...")
    for i, p in enumerate(prospects, 1):
        result = audit_one(p, timeout)
        p.update(result)
        flag = "•" if result["pain_score"] >= cfg["pipeline"]["min_pain_score"] else " "
        print(f"  [{i:>3}/{len(prospects)}] {flag} {result['pain_score']:>3}  {p['name'][:40]}")
        if p.get("website"):
            time.sleep(0.5)  # only sleep when we actually hit the network

    prospects.sort(key=lambda x: x["pain_score"], reverse=True)
    write_json(PROSPECTS_FILE, prospects)

    hot = [p for p in prospects if p["pain_score"] >= cfg["pipeline"]["min_pain_score"]]
    print(f"\n{len(hot)} hot prospects (pain >= {cfg['pipeline']['min_pain_score']}) "
          f"out of {len(prospects)}. Sorted hottest-first.")
    return 0


if __name__ == "__main__":
    raise SystemExit(main())
