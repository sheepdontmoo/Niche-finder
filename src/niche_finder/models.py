"""Pydantic domain models shared across clients, steps, and scoring.

These describe the *shape* of data as it flows; persistence is handled by the
repository, which maps these to/from SQLite rows.
"""

from __future__ import annotations

from pydantic import BaseModel, Field


class Keyword(BaseModel):
    phrase: str
    depth: int = 0
    source: str = "autocomplete"
    is_buyer_modifier: bool = False


class KeywordMetric(BaseModel):
    phrase: str
    volume: int = 0
    kd: float = 0.0
    cpc: float = 0.0
    traffic_value: float = 0.0
    serp_features: list[str] = Field(default_factory=list)
    has_forum_on_p1: bool = False


class SerpResult(BaseModel):
    position: int
    url: str
    domain: str
    dr: float = 0.0
    is_forum: bool = False
    is_thin: bool = False


class RedditPost(BaseModel):
    subreddit: str
    permalink: str
    title: str
    body: str = ""
    score: int = 0
    created_utc: float = 0.0
    matched_signals: list[str] = Field(default_factory=list)


class PainPoint(BaseModel):
    question_norm: str
    quote: str = ""
    intensity: float = 0.0          # 0-10
    frequency: int = 1
    alternatives: list[str] = Field(default_factory=list)
    buildability: float = 0.0       # 0-10


class AieoGrade(BaseModel):
    provider: str = "anthropic"
    depth: float = 0.0              # 0-10
    specificity: float = 0.0        # 0-10
    recency: float = 0.0            # 0-10
    citation: float = 0.0           # 0-10
    weakness: float = 0.0           # 0-10 (higher = bigger gap)
    notes: str = ""


class Monetisation(BaseModel):
    primary_path: str = "display"   # affiliate | own-product | lead-gen | display
    paths: list[str] = Field(default_factory=list)
    confidence: float = 0.0         # 0-1
    affiliate_programs: list[str] = Field(default_factory=list)
    rationale: str = ""


class Trend(BaseModel):
    slope: float = 0.0
    direction: str = "stable"       # growing | stable | declining
    series: list[float] = Field(default_factory=list)


class MetricScore(BaseModel):
    metric: str
    raw_value: float
    normalized_0_10: float


class NicheScore(BaseModel):
    niche_id: int
    seed_term: str
    composite: float
    rank: int = 0
    confidence: float = 1.0
    metrics: dict[str, MetricScore] = Field(default_factory=dict)
