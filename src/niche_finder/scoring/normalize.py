"""Pure raw-signal → 0-10 normalisers, one per metric.

No weights, no DB access — each function maps raw aggregates to a [0, 10]
score and is independently unit-testable at its boundaries. The scorer assembles
the raw signals from SQLite and calls these.
"""

from __future__ import annotations

import math

PATH_TIERS = {
    "own-product": 9.0,
    "affiliate": 8.0,
    "lead-gen": 6.0,
    "display": 3.0,
}


def clamp(x: float, lo: float = 0.0, hi: float = 10.0) -> float:
    return max(lo, min(hi, x))


def commercial_intent(avg_cpc: float, buyer_ratio: float, cpc_cap: float = 20.0) -> float:
    """Log-scaled CPC value blended with buyer-keyword share.

    A niche with high CPC (advertisers paying) and many buyer-intent keywords
    scores high. ``buyer_ratio`` is 0-1.
    """
    cpc_cap = max(cpc_cap, 1e-6)
    value = 10.0 * math.log1p(max(0.0, avg_cpc)) / math.log1p(cpc_cap)
    value = clamp(value)
    return clamp(0.7 * value + 0.3 * clamp(buyer_ratio * 10.0))


def monetisation_fit(primary_path: str, confidence: float,
                     has_affiliate_programs: bool) -> float:
    """LLM-classified revenue path → tier, scaled by confidence, +bump if
    real affiliate programs exist for an affiliate/product niche."""
    base = PATH_TIERS.get((primary_path or "").lower(), 3.0)
    conf = clamp(confidence, 0.0, 1.0)
    score = base * (0.6 + 0.4 * conf)
    if has_affiliate_programs and (primary_path or "").lower() in ("affiliate", "own-product"):
        score += 1.0
    return clamp(score)


def seo_winnability(avg_kd: float, weak_fraction: float) -> float:
    """Inverse keyword difficulty blended with the share of page-1 results that
    are weak (forum / thin / low-DR). ``weak_fraction`` is 0-1."""
    inverse_kd = 10.0 * (1.0 - clamp(avg_kd, 0.0, 100.0) / 100.0)
    return clamp(0.5 * inverse_kd + 0.5 * clamp(weak_fraction * 10.0))


def aieo_gap(mean_weakness: float) -> float:
    """Mean weakness of current AI answers (already graded 0-10). Higher =
    bigger gap = better opportunity to become the cited source."""
    return clamp(mean_weakness)


def pain_density(distinct_pain_count: int, saturation_k: float = 8.0) -> float:
    """Saturating count of distinct recurring pains: 0 pains → 0, with
    diminishing returns past ~k."""
    k = max(saturation_k, 1e-6)
    return clamp(10.0 * (1.0 - math.exp(-max(0, distinct_pain_count) / k)))


def trend_direction(slope: float, scale: float = 100.0) -> float:
    """Sigmoid of the normalised Trends slope, centred at 0:
    declining → low, stable → ~5, growing → high."""
    return clamp(10.0 / (1.0 + math.exp(-slope * scale)))
