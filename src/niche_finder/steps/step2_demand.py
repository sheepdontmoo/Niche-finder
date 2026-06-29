"""Step 2 — demand quantification via Ahrefs.

Pull volume / KD / CPC / traffic value / SERP features for each keyword.
Low-volume clusters are aggregated downstream in scoring (sum the cluster, don't
judge keywords individually), so here we just persist per-keyword metrics.
"""

from __future__ import annotations

from ..clients.ahrefs import AhrefsClient
from ..config import Config
from ..models import KeywordMetric
from ..storage.repository import Repository

# Cap how many keywords we query Ahrefs for per niche (cost control). We keep
# buyer-intent keywords first, then fill with the rest.
MAX_KEYWORDS = 60


def run(repo: Repository, client: AhrefsClient, config: Config, niche_id: int) -> int:
    rows = repo.keywords_for(niche_id)
    rows = sorted(rows, key=lambda r: (0 if r["is_buyer_modifier"] else 1, r["keyword_id"]))
    rows = rows[:MAX_KEYWORDS]

    for row in rows:
        m = client.keyword_metrics(row["phrase"])
        repo.save_keyword_metric(row["keyword_id"], KeywordMetric(
            phrase=row["phrase"],
            volume=int(m.get("volume", 0)),
            kd=float(m.get("kd", 0)),
            cpc=float(m.get("cpc", 0)),
            traffic_value=float(m.get("traffic_value", 0)),
            serp_features=m.get("serp_features", []) or [],
            has_forum_on_p1=bool(m.get("has_forum_on_p1", False)),
        ))
    repo.commit()
    return len(rows)
