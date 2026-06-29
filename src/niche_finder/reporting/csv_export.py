"""Ranked shortlist CSV export.

One row per niche: all six metric scores (0-10), the weighted composite, the
rank, and the data-coverage confidence flag, sorted highest composite first.
"""

from __future__ import annotations

from pathlib import Path

import pandas as pd

from ..config import METRICS
from ..storage.repository import Repository


def build_dataframe(repo: Repository, run_id: int) -> pd.DataFrame:
    scores = repo.scores_for_run(run_id)
    metric_rows = repo.metric_scores_for_run(run_id)
    by_niche: dict[int, dict[str, float]] = {}
    for r in metric_rows:
        by_niche.setdefault(r["niche_id"], {})[r["metric"]] = round(r["normalized_0_10"], 2)

    records = []
    for s in scores:
        rec = {
            "rank": s["rank"],
            "niche": s["seed_term"],
            "composite": round(s["composite"], 3),
        }
        metrics = by_niche.get(s["niche_id"], {})
        for m in METRICS:
            rec[m] = metrics.get(m)
        rec["confidence"] = s["confidence"]
        records.append(rec)

    df = pd.DataFrame.from_records(records)
    if not df.empty:
        df = df.sort_values("rank").reset_index(drop=True)
    return df


def export_csv(repo: Repository, run_id: int, out_path: str | Path) -> Path:
    out = Path(out_path)
    out.parent.mkdir(parents=True, exist_ok=True)
    build_dataframe(repo, run_id).to_csv(out, index=False)
    return out
