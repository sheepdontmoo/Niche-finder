"""Step 6 — monetisation fit.

Compute commercial signals (avg CPC, buyer-keyword share) from stored metrics,
then have the LLM (or heuristic) classify the niche against affiliate /
own-product / lead-gen / display and surface concrete affiliate programs.
"""

from __future__ import annotations

from ..clients.anthropic_client import AnthropicClient
from ..config import Config
from ..storage.repository import Repository
from ..text.prompts import PROMPT_VERSION


def run(repo: Repository, claude: AnthropicClient, config: Config,
        niche_id: int, seed_term: str) -> str:
    metrics = repo.metrics_for(niche_id)
    cpcs = [m["cpc"] for m in metrics if m["cpc"]]
    avg_cpc = sum(cpcs) / len(cpcs) if cpcs else 0.0
    buyer_ratio = (
        sum(1 for m in metrics if m["is_buyer_modifier"]) / len(metrics)
        if metrics else 0.0
    )

    monet = claude.classify_monetisation(seed_term, avg_cpc, buyer_ratio)
    repo.save_monetisation(niche_id, monet, model=config.models.monetisation,
                           prompt_version=PROMPT_VERSION)
    repo.commit()
    return monet.primary_path
