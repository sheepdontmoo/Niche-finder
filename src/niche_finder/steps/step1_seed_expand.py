"""Step 1 — seed expansion + clustering.

Expand the seed term into long-tail phrases via Google autocomplete, mark
buyer-intent phrases, cluster near-duplicate phrasings, and persist keywords +
clusters.
"""

from __future__ import annotations

from ..clients.autocomplete import AutocompleteClient
from ..clustering import phrases as ph
from ..config import Config
from ..storage.repository import Repository
from ..text.signals import is_buyer_keyword


def run(repo: Repository, client: AutocompleteClient, config: Config,
        niche_id: int, seed_term: str) -> int:
    repo.clear_keywords(niche_id)  # idempotent re-runs
    seed = seed_term.split("/")[0].split("(")[0].strip()
    found = client.expand(
        seed,
        depth=config.expansion.autocomplete_depth,
        max_phrases=config.expansion.max_phrases_per_niche,
    )
    # Always include the seed itself.
    found = list(dict.fromkeys([seed, *found]))

    keyword_ids: dict[str, int] = {}
    for phrase in found:
        kid = repo.add_keyword(niche_id, phrase, depth=1, source="autocomplete",
                               is_buyer_modifier=is_buyer_keyword(phrase))
        keyword_ids[phrase] = kid

    clusters = ph.cluster_phrases(found, threshold=config.expansion.cluster_similarity)
    for members in clusters:
        cid = repo.create_cluster(niche_id, ph.cluster_label(members), len(members))
        for phrase in members:
            if phrase in keyword_ids:
                repo.assign_cluster(keyword_ids[phrase], cid)
    repo.commit()
    return len(found)
