"""Per-niche deep-dive assembly for the top-N one-pagers.

Pulls the evidence behind a niche's score into a structured dict the reporting
templates render: real pain quotes, the money keyword cluster, the SEO/AIEO
angles, and the monetisation path.
"""

from __future__ import annotations

import json

from ..storage.repository import Repository


def deep_dive(repo: Repository, niche_id: int, run_id: int) -> dict:
    niche = repo.get_niche(niche_id)
    metrics = repo.metrics_for(niche_id)
    pains = repo.pain_for(niche_id)
    serp = repo.serp_for(niche_id)
    grades = repo.grades_for(niche_id)
    questions = repo.money_questions_for(niche_id)
    monet = repo.monetisation_for(niche_id)
    trend = repo.trend_for(niche_id)
    score_row = next((s for s in repo.scores_for_run(run_id)
                      if s["niche_id"] == niche_id), None)
    metric_scores = {
        r["metric"]: r["normalized_0_10"]
        for r in repo.metric_scores_for_run(run_id) if r["niche_id"] == niche_id
    }

    # Top money keywords by buyer-intent then volume.
    money_kws = sorted(
        metrics,
        key=lambda m: (0 if m["is_buyer_modifier"] else 1, -(m["volume"] or 0)),
    )[:10]

    # Page-1 supply summary.
    big_players = [r for r in serp if (r["dr"] or 0) >= 70]
    weak_results = [r for r in serp if r["is_forum"] or r["is_thin"] or (r["dr"] or 0) < 30]

    # Worst-answered questions (highest mean weakness) → priority content.
    by_q: dict[int, list] = {}
    for g in grades:
        by_q.setdefault(g["mq_id"], []).append(g["weakness"])
    q_text = {q["mq_id"]: q["question"] for q in questions}
    weakest = sorted(
        ((q_text.get(mq, ""), sum(ws) / len(ws)) for mq, ws in by_q.items()),
        key=lambda t: t[1], reverse=True,
    )[:5]

    return {
        "seed_term": niche["seed_term"],
        "notes": niche["notes"],
        "composite": score_row["composite"] if score_row else None,
        "rank": score_row["rank"] if score_row else None,
        "confidence": score_row["confidence"] if score_row else None,
        "metric_scores": metric_scores,
        "pains": [
            {"question": p["question_norm"], "quote": p["quote"],
             "intensity": p["intensity"], "frequency": p["frequency"]}
            for p in pains[:8]
        ],
        "money_keywords": [
            {"phrase": m["phrase"], "volume": m["volume"], "cpc": m["cpc"],
             "kd": m["kd"], "buyer": bool(m["is_buyer_modifier"])}
            for m in money_kws
        ],
        "seo": {
            "big_players": sorted({r["domain"] for r in big_players})[:8],
            "weak_count": len(weak_results),
            "total_serp": len(serp),
            "weak_examples": sorted({r["domain"] for r in weak_results})[:8],
        },
        "aieo": {
            "weak_questions": [{"question": q, "weakness": round(w, 1)} for q, w in weakest],
        },
        "monetisation": {
            "primary_path": monet["primary_path"] if monet else None,
            "paths": json.loads(monet["paths_json"]) if monet and monet["paths_json"] else [],
            "affiliate_programs": json.loads(monet["affiliate_programs_json"])
            if monet and monet["affiliate_programs_json"] else [],
            "rationale": monet["rationale"] if monet else "",
        } if monet else None,
        "trend": {"direction": trend["direction"], "slope": trend["slope"]} if trend else None,
    }
