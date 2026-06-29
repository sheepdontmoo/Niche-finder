"""Golden boundary tests for the six 0-10 normalisers."""

from __future__ import annotations

from niche_finder.scoring import normalize as nz


def test_clamp_bounds():
    assert nz.clamp(-5) == 0.0
    assert nz.clamp(15) == 10.0
    assert nz.clamp(5) == 5.0


def test_commercial_intent_monotonic_and_bounded():
    low = nz.commercial_intent(avg_cpc=0.0, buyer_ratio=0.0)
    high = nz.commercial_intent(avg_cpc=20.0, buyer_ratio=1.0)
    assert 0.0 <= low < high <= 10.0
    assert low == 0.0
    # More buyer keywords always helps.
    assert nz.commercial_intent(5, 0.5) > nz.commercial_intent(5, 0.0)


def test_monetisation_fit_tiers():
    own = nz.monetisation_fit("own-product", 1.0, True)
    display = nz.monetisation_fit("display", 1.0, False)
    assert own > display
    assert 0.0 <= display <= 10.0 and 0.0 <= own <= 10.0
    # Higher confidence raises the score for the same path.
    assert nz.monetisation_fit("affiliate", 1.0, False) > nz.monetisation_fit("affiliate", 0.0, False)


def test_seo_winnability_easy_vs_hard():
    easy = nz.seo_winnability(avg_kd=5, weak_fraction=1.0)
    hard = nz.seo_winnability(avg_kd=95, weak_fraction=0.0)
    assert easy > hard
    assert hard >= 0.0 and easy <= 10.0


def test_aieo_gap_passthrough_clamped():
    assert nz.aieo_gap(7.5) == 7.5
    assert nz.aieo_gap(99) == 10.0


def test_pain_density_saturates():
    assert nz.pain_density(0) == 0.0
    assert nz.pain_density(3) < nz.pain_density(20)
    assert nz.pain_density(1000) <= 10.0


def test_trend_direction_sigmoid():
    assert nz.trend_direction(0.0) == 5.0
    assert nz.trend_direction(0.05) > 7.0     # growing
    assert nz.trend_direction(-0.05) < 3.0    # declining
