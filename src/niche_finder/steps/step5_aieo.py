"""Step 5 — AIEO gap check.

Generate the niche's top money questions, then grade how weak current AI-engine
answers are (Claude, and Perplexity if available). Weak answers = high AIEO gap
= open lane to become the cited source.
"""

from __future__ import annotations

from ..clients.anthropic_client import AnthropicClient
from ..clients.perplexity import PerplexityClient
from ..config import Config
from ..storage.repository import Repository
from ..text.prompts import PROMPT_VERSION


def run(repo: Repository, claude: AnthropicClient, perplexity: PerplexityClient,
        config: Config, niche_id: int, seed_term: str) -> int:
    repo.clear_aieo(niche_id)  # idempotent re-runs
    # Use buyer keywords as raw material for the money-question generator.
    metrics = repo.metrics_for(niche_id)
    keywords = [m["phrase"] for m in sorted(
        metrics, key=lambda m: (0 if m["is_buyer_modifier"] else 1, -(m["volume"] or 0))
    )]
    if not keywords:
        keywords = [r["phrase"] for r in repo.keywords_for(niche_id)]

    questions = claude.generate_money_questions(
        seed_term, keywords, config.aieo.questions_per_niche
    )

    n = 0
    for q in questions:
        mq_id = repo.add_money_question(niche_id, q)
        grade = claude.grade_aieo(q)
        repo.save_aieo_grade(mq_id, grade, model=config.models.aieo_grading,
                             prompt_version=PROMPT_VERSION)
        n += 1
        # Second opinion from a search-grounded model when available.
        pgrade = perplexity.grade_aieo(q)
        repo.save_aieo_grade(mq_id, pgrade, model="perplexity",
                             prompt_version=PROMPT_VERSION)
    repo.commit()
    return n
