"""Step 8 — assemble metrics, apply weights, compute the composite, rank.

This operates purely on stored data: it reads the intermediate tables, computes
the six normalised metrics, and writes the scored layer. Re-scoring with new
weights is therefore a fast SQLite-to-SQLite pass with no scraping.
"""

from __future__ import annotations

import json

from ..config import METRICS, Config
from ..storage.repository import Repository
from . import normalize as nz


def gather_signals(repo: Repository, niche_id: int) -> dict:
    """Collect raw aggregates for one niche from the intermediate tables."""
    metrics = repo.metrics_for(niche_id)
    cpcs = [m["cpc"] for m in metrics if m["cpc"]]
    kds = [m["kd"] for m in metrics if m["kd"] is not None]
    avg_cpc = sum(cpcs) / len(cpcs) if cpcs else 0.0
    avg_kd = sum(kds) / len(kds) if kds else 100.0  # no data => assume hard
    buyer_ratio = (
        sum(1 for m in metrics if m["is_buyer_modifier"]) / len(metrics)
        if metrics else 0.0
    )

    serp = repo.serp_for(niche_id)
    weak = sum(1 for r in serp if r["is_forum"] or r["is_thin"] or (r["dr"] or 0) < 30)
    weak_fraction = weak / len(serp) if serp else 0.0

    pains = repo.pain_for(niche_id)
    grades = repo.grades_for(niche_id)
    mean_weakness = (
        sum(g["weakness"] for g in grades) / len(grades) if grades else 0.0
    )

    monet = repo.monetisation_for(niche_id)
    trend = repo.trend_for(niche_id)

    return {
        "avg_cpc": avg_cpc,
        "avg_kd": avg_kd,
        "buyer_ratio": buyer_ratio,
        "weak_fraction": weak_fraction,
        "pain_count": len(pains),
        "mean_weakness": mean_weakness,
        "monet": monet,
        "trend": trend,
        # coverage flags (each True if the source produced data)
        "_has": {
            "metrics": bool(metrics),
            "serp": bool(serp),
            "pain": bool(pains),
            "aieo": bool(grades),
            "monet": monet is not None,
            "trend": trend is not None,
        },
    }


def compute_metrics(signals: dict, config: Config) -> dict[str, tuple[float, float]]:
    """Return {metric: (raw_value, normalized_0_10)} for one niche."""
    monet = signals["monet"]
    primary_path = monet["primary_path"] if monet else "display"
    confidence = monet["confidence"] if monet else 0.0
    has_programs = bool(json.loads(monet["affiliate_programs_json"])) if monet and \
        monet["affiliate_programs_json"] else False
    slope = signals["trend"]["slope"] if signals["trend"] else 0.0

    n = config.normalize
    return {
        "commercial_intent": (
            signals["avg_cpc"],
            nz.commercial_intent(signals["avg_cpc"], signals["buyer_ratio"], n.cpc_cap),
        ),
        "monetisation_fit": (
            confidence,
            nz.monetisation_fit(primary_path, confidence, has_programs),
        ),
        "seo_winnability": (
            signals["avg_kd"],
            nz.seo_winnability(signals["avg_kd"], signals["weak_fraction"]),
        ),
        "aieo_gap": (
            signals["mean_weakness"],
            nz.aieo_gap(signals["mean_weakness"]),
        ),
        "pain_density": (
            float(signals["pain_count"]),
            nz.pain_density(signals["pain_count"], n.pain_saturation_k),
        ),
        "trend_direction": (
            slope,
            nz.trend_direction(slope),
        ),
    }


def validate_weights(weights: dict[str, float]) -> None:
    missing = [m for m in METRICS if m not in weights]
    if missing:
        raise ValueError(f"weights.yaml missing metrics: {missing}")
    total = sum(weights[m] for m in METRICS)
    if abs(total - 1.0) > 1e-6:
        raise ValueError(f"weights must sum to 1.0, got {total:.4f}")


def score_run(repo: Repository, config: Config, run_id: int) -> list[dict]:
    """Score every niche for ``run_id`` and persist the scored layer.

    Returns a ranked list of {niche_id, seed_term, composite, confidence, metrics}.
    """
    weights = config.weights
    validate_weights(weights)

    results = []
    for niche in repo.list_niches():
        nid = niche["niche_id"]
        signals = gather_signals(repo, nid)
        metrics = compute_metrics(signals, config)

        composite = 0.0
        for name, (raw, norm) in metrics.items():
            repo.save_metric_score(nid, run_id, name, raw, norm)
            composite += weights[name] * norm

        coverage = sum(signals["_has"].values()) / len(signals["_has"])
        results.append({
            "niche_id": nid,
            "seed_term": niche["seed_term"],
            "composite": round(composite, 3),
            "confidence": round(coverage, 2),
            "metrics": {k: round(v[1], 2) for k, v in metrics.items()},
        })

    results.sort(key=lambda r: r["composite"], reverse=True)
    for rank, r in enumerate(results, start=1):
        r["rank"] = rank
        repo.save_score(r["niche_id"], run_id, r["composite"], rank,
                        r["confidence"], weights)
    repo.commit()
    return results
