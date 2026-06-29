"""Google Trends client via pytrends.

Returns a normalised slope over the lookback window and a direction label.
pytrends is brittle and rate-limited, so any failure degrades to a neutral
'stable' reading rather than stopping the run. Without pytrends installed (or
in fixture mode) a deterministic synthetic series is generated.
"""

from __future__ import annotations

import random

from .base import BaseClient


def _slope_and_direction(series: list[float]) -> tuple[float, str]:
    """Least-squares slope over the index, normalised to [-1, 1]-ish, + label."""
    n = len(series)
    if n < 2:
        return 0.0, "stable"
    xs = list(range(n))
    mean_x = sum(xs) / n
    mean_y = sum(series) / n
    num = sum((x - mean_x) * (y - mean_y) for x, y in zip(xs, series, strict=False))
    den = sum((x - mean_x) ** 2 for x in xs) or 1.0
    raw_slope = num / den
    # Normalise by mean level so it's a relative growth signal.
    norm = raw_slope / (mean_y or 1.0)
    if norm > 0.01:
        direction = "growing"
    elif norm < -0.01:
        direction = "declining"
    else:
        direction = "stable"
    return round(norm, 4), direction


class TrendsClient(BaseClient):
    source = "trends"

    def trend(self, niche: str, timeframe: str = "today 24-m") -> dict:
        """Return {slope, direction, series}."""
        params = {"niche": niche, "timeframe": timeframe}

        def live() -> dict:
            from pytrends.request import TrendReq  # imported lazily

            pytrends = TrendReq(hl="en-US", tz=0)
            pytrends.build_payload([niche], timeframe=timeframe)
            df = pytrends.interest_over_time()
            if df is None or df.empty or niche not in df:
                return {"series": []}
            return {"series": [float(v) for v in df[niche].tolist()]}

        def synth(seed: int) -> dict:
            rng = random.Random(seed)
            trend_kind = rng.choice(["growing", "growing", "stable", "declining"])
            base = rng.uniform(30, 60)
            series = []
            for i in range(24):
                drift = {"growing": 1.2, "stable": 0.0, "declining": -0.9}[trend_kind]
                series.append(max(0.0, base + drift * i + rng.uniform(-6, 6)))
            return {"series": [round(v, 1) for v in series]}

        data = self.request("interest_over_time", params, live, synth)
        series = data.get("series", []) or []
        slope, direction = _slope_and_direction(series)
        return {"slope": slope, "direction": direction, "series": series}
