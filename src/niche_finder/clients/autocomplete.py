"""Google autocomplete (no API key) recursive long-tail expander.

Re-implemented from the approach in bhowe/niche-keyword-planning-tool: hit the
public suggestion endpoint, then recurse by appending a–z and common
question/modifier prefixes to surface long-tail phrasings. Synthetic mode
generates plausible long-tail phrases deterministically so the pipeline runs
offline.
"""

from __future__ import annotations

import random
import string

import httpx

from .base import BaseClient

SUGGEST_URL = "https://suggestqueries.google.com/complete/search"

QUESTION_PREFIXES = ["how to", "what is", "best", "why", "can you", "when to"]
BUYER_PREFIXES = ["best", "top", "cheap", "review", "vs"]


class AutocompleteClient(BaseClient):
    source = "autocomplete"

    def expand(self, seed: str, depth: int, max_phrases: int) -> list[str]:
        """Return a deduped list of long-tail phrases for ``seed``."""
        found: set[str] = set()
        frontier = [seed] + [f"{seed} {c}" for c in string.ascii_lowercase[:10]]
        frontier += [f"{p} {seed}" for p in QUESTION_PREFIXES]

        for _ in range(max(1, depth)):
            next_frontier: list[str] = []
            for term in frontier:
                if len(found) >= max_phrases:
                    break
                suggestions = self._suggest(term)
                for s in suggestions:
                    if s and s not in found:
                        found.add(s)
                        next_frontier.append(s)
            frontier = next_frontier[: max_phrases]
            if not frontier or len(found) >= max_phrases:
                break

        return sorted(found)[:max_phrases]

    def _suggest(self, term: str) -> list[str]:
        params = {"client": "firefox", "q": term}

        def live() -> list[str]:
            resp = httpx.get(SUGGEST_URL, params=params, timeout=10.0)
            resp.raise_for_status()
            data = resp.json()
            return list(data[1]) if len(data) > 1 else []

        def synth(seed: int) -> list[str]:
            rng = random.Random(seed)
            suffixes = [
                "for beginners", "at home", "without equipment", "reddit", "tips",
                "vs", "review", "guide", "checklist", "mistakes", "app", "free",
                "cost", "schedule", "ideas", "plan", "alternatives", "near me",
            ]
            n = rng.randint(6, 10)
            picks = rng.sample(suffixes, k=min(n, len(suffixes)))
            out = [f"{term} {sfx}".strip() for sfx in picks]
            # occasionally prepend a buyer modifier to seed commercial-intent signal
            if rng.random() < 0.4:
                out.append(f"{rng.choice(BUYER_PREFIXES)} {term}")
            return out

        return self.request("suggest", params, live, synth)
