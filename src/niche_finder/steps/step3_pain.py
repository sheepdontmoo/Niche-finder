"""Step 3 — pain mining via Reddit + Claude scoring.

Scrape relevant subreddits, keep posts containing pain signals, persist them,
then run them through the LLM (or heuristic) scorer to extract distinct
recurring pain points.
"""

from __future__ import annotations

from ..clients.anthropic_client import AnthropicClient
from ..clients.reddit import RedditClient
from ..config import Config
from ..models import RedditPost
from ..storage.repository import Repository
from ..text.prompts import PROMPT_VERSION
from ..text.signals import count_pain_signals


def run(repo: Repository, reddit: RedditClient, claude: AnthropicClient,
        config: Config, niche_id: int, seed_term: str) -> int:
    repo.clear_pain(niche_id)  # idempotent re-runs
    subs = reddit.default_subreddits(seed_term, config.pain.subreddits_per_niche)
    raw_posts = reddit.search(seed_term, subs, config.pain.posts_per_subreddit)

    kept: list[dict] = []
    for p in raw_posts:
        signals_hit = count_pain_signals(f"{p.get('title','')} {p.get('body','')}")
        if not signals_hit:
            continue
        post = RedditPost(
            subreddit=p.get("subreddit", ""), permalink=p.get("permalink", ""),
            title=p.get("title", ""), body=p.get("body", ""),
            score=int(p.get("score", 0)), created_utc=float(p.get("created_utc", 0)),
            matched_signals=signals_hit,
        )
        repo.save_reddit_post(niche_id, post)
        kept.append(p)
    repo.commit()

    if not kept:
        return 0

    pains = claude.score_pain(seed_term, kept)
    model = config.models.pain_scoring
    for pp in pains:
        repo.save_pain_point(niche_id, pp, post_id=None, model=model,
                             prompt_version=PROMPT_VERSION)
    repo.commit()
    return len(pains)
