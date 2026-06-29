"""Composite-math and re-score determinism tests."""

from __future__ import annotations

import pytest

from niche_finder.scoring.scorer import score_run, validate_weights


def test_validate_weights_must_sum_to_one():
    with pytest.raises(ValueError):
        validate_weights({"commercial_intent": 0.5})  # missing + wrong sum
    good = {
        "commercial_intent": 0.25, "monetisation_fit": 0.20, "seo_winnability": 0.20,
        "aieo_gap": 0.15, "pain_density": 0.10, "trend_direction": 0.10,
    }
    validate_weights(good)  # should not raise


def _seed_one_niche(repo):
    from niche_finder.models import (
        AieoGrade,
        KeywordMetric,
        Monetisation,
        PainPoint,
        SerpResult,
        Trend,
    )
    nid = repo.upsert_niche("meal prep")
    kid = repo.add_keyword(nid, "best meal prep containers", 1, "autocomplete", True)
    repo.save_keyword_metric(kid, KeywordMetric(
        phrase="best meal prep containers", volume=1000, kd=20, cpc=4.0,
        traffic_value=2000, has_forum_on_p1=True))
    repo.save_serp_result(nid, kid, SerpResult(position=1, url="https://reddit.com/x",
                          domain="reddit.com", dr=10, is_forum=True, is_thin=False))
    repo.save_pain_point(nid, PainPoint(question_norm="storage", intensity=7,
                         frequency=3, buildability=6), None, "m", "v1")
    mq = repo.add_money_question(nid, "best meal prep containers?")
    repo.save_aieo_grade(mq, AieoGrade(weakness=8.0), "m", "v1")
    repo.save_monetisation(nid, Monetisation(primary_path="affiliate", confidence=0.8,
                           affiliate_programs=["Amazon Associates"]), "m", "v1")
    repo.save_trend(nid, Trend(slope=0.04, direction="growing"))
    repo.commit()
    return nid


def test_score_run_persists_and_ranks(repo, config):
    _seed_one_niche(repo)
    run_id = repo.create_run("fixture", {}, config.weights)
    results = score_run(repo, config, run_id)
    assert len(results) == 1
    r = results[0]
    assert r["rank"] == 1
    assert 0.0 <= r["composite"] <= 10.0
    # Composite equals the weighted sum of the persisted metric scores.
    expected = sum(config.weights[k] * v for k, v in r["metrics"].items())
    assert r["composite"] == pytest.approx(expected, abs=0.01)


def test_rescore_is_deterministic(repo, config):
    _seed_one_niche(repo)
    run1 = repo.create_run("fixture", {}, config.weights)
    res1 = score_run(repo, config, run1)
    run2 = repo.create_run("fixture", {}, config.weights)
    res2 = score_run(repo, config, run2)
    assert res1[0]["composite"] == res2[0]["composite"]
    assert res1[0]["metrics"] == res2[0]["metrics"]
