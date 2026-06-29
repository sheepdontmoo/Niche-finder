"""Lexical phrase clustering.

Groups near-duplicate / same-intent phrasings together using token-set
similarity (rapidfuzz) and union-find. This is deliberately cheap and
deterministic — no embedding model, no network — so it runs offline and the
same input always yields the same clusters. Swap in sentence-transformers here
later if quality demands it; the public function signature stays the same.
"""

from __future__ import annotations

from rapidfuzz import fuzz

# Words too generic to anchor a cluster label.
_STOP = {"the", "a", "an", "to", "for", "of", "and", "or", "in", "on", "best",
         "how", "what", "is", "are", "do", "you", "i", "my", "with"}


class _UnionFind:
    def __init__(self, n: int):
        self.parent = list(range(n))

    def find(self, x: int) -> int:
        while self.parent[x] != x:
            self.parent[x] = self.parent[self.parent[x]]
            x = self.parent[x]
        return x

    def union(self, a: int, b: int) -> None:
        ra, rb = self.find(a), self.find(b)
        if ra != rb:
            self.parent[max(ra, rb)] = min(ra, rb)


def cluster_phrases(phrases: list[str], threshold: int = 85) -> list[list[str]]:
    """Cluster phrases by token-set similarity.

    ``threshold`` is a 0-100 rapidfuzz ratio; higher = stricter (fewer merges).
    Returns a list of clusters, each a list of the original phrases.
    """
    items = [p for p in dict.fromkeys(p.strip() for p in phrases) if p]
    n = len(items)
    if n == 0:
        return []
    uf = _UnionFind(n)
    for i in range(n):
        for j in range(i + 1, n):
            if fuzz.token_set_ratio(items[i], items[j]) >= threshold:
                uf.union(i, j)
    groups: dict[int, list[str]] = {}
    for idx, phrase in enumerate(items):
        groups.setdefault(uf.find(idx), []).append(phrase)
    # Stable order: largest clusters first, then alphabetical.
    return sorted(groups.values(), key=lambda g: (-len(g), g[0]))


def cluster_label(members: list[str]) -> str:
    """Pick a human-readable label for a cluster (shortest non-trivial phrase)."""
    if not members:
        return ""
    informative = [m for m in members if any(w not in _STOP for w in m.split())]
    pool = informative or members
    return min(pool, key=len)
