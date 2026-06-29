"""Step 7 — trend direction via Google Trends.

Classify the niche as growing / stable / declining over the lookback window.
A declining trend is penalised in scoring; never recommend a dying market.
"""

from __future__ import annotations

from ..clients.trends import TrendsClient
from ..config import Config
from ..models import Trend
from ..storage.repository import Repository


def run(repo: Repository, client: TrendsClient, config: Config,
        niche_id: int, seed_term: str) -> str:
    seed = seed_term.split("/")[0].split("(")[0].strip()
    data = client.trend(seed)
    trend = Trend(
        slope=float(data.get("slope", 0)),
        direction=data.get("direction", "stable"),
        series=data.get("series", []) or [],
    )
    repo.save_trend(niche_id, trend)
    repo.commit()
    return trend.direction
