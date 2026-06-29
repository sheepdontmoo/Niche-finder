"""Step 4 — supply / competition check (SEO winnability).

For the top money keywords, pull the page-1 SERP from Ahrefs and flag whether
results are big established players (DR 70+) or weak/forum/thin content (= gap).
"""

from __future__ import annotations

from ..clients.ahrefs import AhrefsClient
from ..config import Config
from ..models import SerpResult
from ..storage.repository import Repository

# How many money keywords to inspect SERPs for (cost control).
TOP_KEYWORDS = 10


def run(repo: Repository, client: AhrefsClient, config: Config, niche_id: int) -> int:
    repo.clear_serp(niche_id)  # idempotent re-runs
    metrics = repo.metrics_for(niche_id)
    # Prefer buyer-intent, higher-volume keywords as "money keywords".
    ranked = sorted(
        metrics,
        key=lambda m: (
            0 if m["is_buyer_modifier"] else 1,
            -(m["volume"] or 0),
        ),
    )[:TOP_KEYWORDS]

    n = 0
    for m in ranked:
        rows = client.serp_overview(m["phrase"])
        for r in rows:
            repo.save_serp_result(niche_id, m["keyword_id"], SerpResult(
                position=int(r.get("position", 0)),
                url=r.get("url", ""), domain=r.get("domain", ""),
                dr=float(r.get("dr", 0)),
                is_forum=bool(r.get("is_forum", False)),
                is_thin=bool(r.get("is_thin", False)),
            ))
            n += 1
    repo.commit()
    return n
