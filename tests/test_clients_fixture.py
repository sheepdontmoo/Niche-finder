"""Each client returns deterministic data in fixture mode (no network, no keys)."""

from __future__ import annotations

from niche_finder.clients.ahrefs import AhrefsClient
from niche_finder.clients.anthropic_client import AnthropicClient
from niche_finder.clients.autocomplete import AutocompleteClient
from niche_finder.clients.reddit import RedditClient
from niche_finder.clients.trends import TrendsClient
from niche_finder.ratelimit import RateLimiter


def _kw(config, repo):
    return dict(config=config, repo=repo, limiter=RateLimiter(), run_id=None)


def test_autocomplete_deterministic(config, repo):
    c1 = AutocompleteClient(**_kw(config, repo))
    out1 = c1.expand("meal prep", depth=1, max_phrases=20)
    out2 = c1.expand("meal prep", depth=1, max_phrases=20)
    assert out1 == out2
    assert len(out1) > 0


def test_ahrefs_metrics_shape(config, repo):
    c = AhrefsClient(**_kw(config, repo))
    m = c.keyword_metrics("best meal prep containers")
    assert set(["volume", "kd", "cpc", "traffic_value", "has_forum_on_p1"]) <= set(m)
    assert m["volume"] >= 0 and 0 <= m["kd"] <= 100


def test_ahrefs_serp_flags(config, repo):
    c = AhrefsClient(**_kw(config, repo))
    rows = c.serp_overview("best meal prep containers")
    assert len(rows) == 10
    assert all("is_forum" in r and "is_thin" in r for r in rows)


def test_reddit_search_deterministic(config, repo):
    c = RedditClient(**_kw(config, repo))
    posts = c.search("meal prep", ["mealprep"], 8)
    assert posts
    assert all("title" in p for p in posts)


def test_anthropic_heuristic_pain(config, repo):
    c = AnthropicClient(**_kw(config, repo))
    posts = [{"subreddit": "x", "title": "I struggle with meal prep", "body": "frustrated"}]
    pains = c.score_pain("meal prep", posts)
    assert pains
    assert all(0 <= p.intensity <= 10 for p in pains)


def test_anthropic_heuristic_monetisation(config, repo):
    c = AnthropicClient(**_kw(config, repo))
    m = c.classify_monetisation("meal prep", avg_cpc=5.0, buyer_ratio=0.3)
    assert m.primary_path in ("affiliate", "own-product", "lead-gen", "display")
    assert 0.0 <= m.confidence <= 1.0


def test_trends_classification(config, repo):
    c = TrendsClient(**_kw(config, repo))
    t = c.trend("meal prep")
    assert t["direction"] in ("growing", "stable", "declining")
    assert len(t["series"]) > 0
