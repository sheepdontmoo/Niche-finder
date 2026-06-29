"""Pain-signal lexicon and buyer-intent keyword modifiers.

These power both the Reddit pre-filter (which posts are worth scoring) and the
deterministic heuristic graders used when no LLM key is present.
"""

from __future__ import annotations

# Phrases that signal a pain point / unanswered question in forum text.
PAIN_SIGNALS: list[str] = [
    "struggle", "struggling", "frustrated", "frustrating", "wish there was",
    "wish there were", "is there a", "how do i", "how do you", "best way to",
    "anyone know", "anyone else", "recommend", "recommendation", "help",
    "can't figure out", "cant figure out", "no idea", "overwhelmed", "confused",
    "hate that", "tired of", "looking for", "alternative to", "better than",
    "what's the best", "whats the best", "fed up", "give up", "gave up",
]

# Buyer-intent modifiers: their presence in a keyword signals commercial intent.
BUYER_MODIFIERS: list[str] = [
    "best", "top", "review", "reviews", "vs", "versus", "alternative",
    "alternatives", "cheap", "cheapest", "price", "pricing", "cost", "buy",
    "deal", "deals", "discount", "compare", "comparison", "worth it", "for",
]


def count_pain_signals(text: str) -> list[str]:
    """Return the distinct pain-signal phrases present in ``text``."""
    low = text.lower()
    return sorted({sig for sig in PAIN_SIGNALS if sig in low})


def is_buyer_keyword(phrase: str) -> bool:
    """True if a keyword phrase contains a buyer-intent modifier."""
    tokens = set(phrase.lower().split())
    # Match single-word modifiers as whole tokens, plus a few substrings.
    if tokens & {m for m in BUYER_MODIFIERS if " " not in m}:
        return True
    low = phrase.lower()
    return any(m in low for m in ("worth it", "best ", " vs ", "alternative"))
