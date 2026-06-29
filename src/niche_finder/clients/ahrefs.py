"""Ahrefs API client — the one paid source.

Pulls per-keyword volume / KD / CPC / traffic value / SERP features and a SERP
overview (positions, domains, domain rating) used for the supply check.

The live calls target the Ahrefs API v3 endpoints; exact response parsing is
kept defensive because Ahrefs payload shapes evolve. Without a key the client
runs in fixture mode and returns deterministic synthetic metrics so the rest of
the pipeline is exercised end-to-end.
"""

from __future__ import annotations

import random

import httpx

from .base import BaseClient

API_BASE = "https://api.ahrefs.com/v3"
FORUM_DOMAINS = ("reddit.com", "quora.com", "stackexchange.com", "stackoverflow.com",
                 "forum", "community", "discuss")


class AhrefsClient(BaseClient):
    source = "ahrefs"

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.api_key = self.config.key_for("ahrefs")

    # -- keyword metrics -----------------------------------------------------
    def keyword_metrics(self, phrase: str, country: str = "us") -> dict:
        """Return {volume, kd, cpc, traffic_value, serp_features, has_forum_on_p1}."""
        params = {"keyword": phrase, "country": country}

        def live() -> dict:
            resp = httpx.get(
                f"{API_BASE}/keywords-explorer/overview",
                params={"select": "volume,difficulty,cpc,traffic_potential",
                        "keyword": phrase, "country": country},
                headers={"Authorization": f"Bearer {self.api_key}"},
                timeout=20.0,
            )
            resp.raise_for_status()
            data = resp.json()
            row = (data.get("keywords") or [{}])[0] if isinstance(data, dict) else {}
            return {
                "volume": int(row.get("volume", 0) or 0),
                "kd": float(row.get("difficulty", 0) or 0),
                "cpc": float(row.get("cpc", 0) or 0),
                "traffic_value": float(row.get("traffic_potential", 0) or 0),
                "serp_features": row.get("serp_features", []) or [],
            }

        def synth(seed: int) -> dict:
            rng = random.Random(seed)
            volume = int(rng.choice([10, 20, 40, 90, 170, 320, 590, 1000, 2400]))
            cpc = round(rng.uniform(0.1, 12.0), 2)
            return {
                "volume": volume,
                "kd": round(rng.uniform(2, 80), 1),
                "cpc": cpc,
                "traffic_value": round(cpc * volume * rng.uniform(0.2, 0.6), 2),
                "serp_features": rng.sample(
                    ["featured_snippet", "people_also_ask", "video", "shopping"],
                    k=rng.randint(0, 2),
                ),
            }

        result = self.request("kw_overview", params, live, synth)
        result["has_forum_on_p1"] = self._forum_present(phrase)
        return result

    # -- SERP overview (supply check) ---------------------------------------
    def serp_overview(self, phrase: str, country: str = "us") -> list[dict]:
        """Return up to 10 page-1 results: {position, url, domain, dr}."""
        params = {"keyword": phrase, "country": country}

        def live() -> list[dict]:
            resp = httpx.get(
                f"{API_BASE}/serp-overview/serp-overview",
                params={"keyword": phrase, "country": country, "top_positions": 10},
                headers={"Authorization": f"Bearer {self.api_key}"},
                timeout=20.0,
            )
            resp.raise_for_status()
            data = resp.json()
            rows = data.get("positions", []) if isinstance(data, dict) else []
            out = []
            for i, r in enumerate(rows[:10], start=1):
                url = r.get("url", "")
                out.append({
                    "position": r.get("position", i),
                    "url": url,
                    "domain": _domain(url),
                    "dr": float(r.get("domain_rating", 0) or 0),
                })
            return out

        def synth(seed: int) -> list[dict]:
            rng = random.Random(seed + 7)
            sample_domains = [
                "reddit.com", "quora.com", "medium.com", "healthline.com",
                "nytimes.com", "wikipedia.org", "someblog.net", "nichesite.io",
                "forbes.com", "a-forum.com",
            ]
            out = []
            for i in range(1, 11):
                dom = rng.choice(sample_domains)
                out.append({
                    "position": i,
                    "url": f"https://{dom}/{phrase.replace(' ', '-')}",
                    "domain": dom,
                    "dr": round(rng.uniform(10, 92), 1),
                })
            return out

        rows = self.request("serp_overview", params, live, synth)
        for r in rows:
            r["is_forum"] = _is_forum(r.get("domain", ""))
            r["is_thin"] = bool(r.get("dr", 0) < 30 and not r["is_forum"])
        return rows

    def _forum_present(self, phrase: str) -> bool:
        rows = self.serp_overview(phrase)
        return any(r.get("is_forum") for r in rows)


def _domain(url: str) -> str:
    if not url:
        return ""
    netloc = url.split("//")[-1].split("/")[0]
    return netloc.lower().removeprefix("www.")


def _is_forum(domain: str) -> bool:
    return any(token in domain for token in FORUM_DOMAINS)
