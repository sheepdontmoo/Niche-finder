"""Typed configuration: merges config/settings.yaml + config/weights.yaml + env.

Environment variables prefixed with ``NF_`` override scalar settings (e.g.
``NF_MODE=live``). API keys are read from the standard names documented in
``.env.example`` and exposed via :meth:`Config.key_for`.
"""

from __future__ import annotations

import os
from pathlib import Path
from typing import Any

import yaml
from pydantic import BaseModel, Field

# Repo root = three levels up from this file (src/niche_finder/config.py).
ROOT = Path(__file__).resolve().parents[2]
DEFAULT_SETTINGS = ROOT / "config" / "settings.yaml"
DEFAULT_WEIGHTS = ROOT / "config" / "weights.yaml"

# Sources and the env var that activates their live mode.
SOURCE_KEYS: dict[str, str] = {
    "ahrefs": "AHREFS_API_KEY",
    "anthropic": "ANTHROPIC_API_KEY",
    "perplexity": "PERPLEXITY_API_KEY",
    # Reddit defaults to the no-key public JSON path, so it is "available"
    # even without credentials. PRAW keys only raise its rate limit.
    "reddit": "",
    "autocomplete": "",
    "trends": "",
}

METRICS = (
    "commercial_intent",
    "monetisation_fit",
    "seo_winnability",
    "aieo_gap",
    "pain_density",
    "trend_direction",
)


class Paths(BaseModel):
    db: str = "data/niche_finder.db"
    fixtures: str = "data/fixtures"
    output: str = "out"


class Models(BaseModel):
    pain_scoring: str = "claude-haiku-4-5-20251001"
    aieo_grading: str = "claude-sonnet-4-6"
    monetisation: str = "claude-haiku-4-5-20251001"
    temperature: float = 0.0


class Retry(BaseModel):
    attempts: int = 4
    base_delay_seconds: float = 2.0


class Expansion(BaseModel):
    max_phrases_per_niche: int = 200
    autocomplete_depth: int = 2
    cluster_similarity: int = 85


class Pain(BaseModel):
    subreddits_per_niche: int = 4
    posts_per_subreddit: int = 25


class Aieo(BaseModel):
    questions_per_niche: int = 8


class Normalize(BaseModel):
    cpc_cap: float = 20.0
    pain_saturation_k: float = 8.0


class Config(BaseModel):
    mode: str = "auto"
    paths: Paths = Field(default_factory=Paths)
    models: Models = Field(default_factory=Models)
    rate_limits: dict[str, float] = Field(default_factory=dict)
    retry: Retry = Field(default_factory=Retry)
    expansion: Expansion = Field(default_factory=Expansion)
    pain: Pain = Field(default_factory=Pain)
    aieo: Aieo = Field(default_factory=Aieo)
    max_llm_calls: int = 0
    normalize: Normalize = Field(default_factory=Normalize)
    weights: dict[str, float] = Field(default_factory=dict)

    # ---- helpers -----------------------------------------------------------
    def key_for(self, source: str) -> str | None:
        """Return the API key for a source, or None if unset / not key-based."""
        env_name = SOURCE_KEYS.get(source, "")
        if not env_name:
            return None
        val = os.environ.get(env_name, "").strip()
        return val or None

    def source_available(self, source: str) -> bool:
        """True if the source can run live (no-key sources are always True)."""
        env_name = SOURCE_KEYS.get(source, "")
        if not env_name:
            return True
        return bool(self.key_for(source))

    def effective_mode(self, source: str) -> str:
        """Resolve the per-source mode given the global mode and key presence."""
        if self.mode == "auto":
            return "live" if self.source_available(source) else "fixture"
        if self.mode in ("live", "record") and not self.source_available(source):
            # Asked for live but no key — degrade this source only.
            return "fixture"
        return self.mode

    def rate_limit(self, source: str) -> float:
        return self.rate_limits.get(source, self.rate_limits.get("default", 2.0))

    def abs_path(self, p: str) -> Path:
        path = Path(p)
        return path if path.is_absolute() else ROOT / path


def _read_yaml(path: Path) -> dict[str, Any]:
    if not path.exists():
        return {}
    return yaml.safe_load(path.read_text(encoding="utf-8")) or {}


def load_config(
    settings_path: Path | str | None = None,
    weights_path: Path | str | None = None,
    overrides: dict[str, Any] | None = None,
) -> Config:
    """Load settings + weights, apply NF_-prefixed env overrides, then explicit overrides."""
    settings = _read_yaml(Path(settings_path) if settings_path else DEFAULT_SETTINGS)
    weights = _read_yaml(Path(weights_path) if weights_path else DEFAULT_WEIGHTS)
    settings["weights"] = weights

    # NF_ env overrides for top-level scalar keys (e.g. NF_MODE, NF_MAX_LLM_CALLS).
    for env_key, env_val in os.environ.items():
        if not env_key.startswith("NF_"):
            continue
        field = env_key[3:].lower()
        if field in settings and not isinstance(settings[field], dict):
            settings[field] = _coerce(settings[field], env_val)

    if overrides:
        settings.update({k: v for k, v in overrides.items() if v is not None})

    return Config(**settings)


def _coerce(existing: Any, raw: str) -> Any:
    if isinstance(existing, bool):
        return raw.lower() in ("1", "true", "yes", "on")
    if isinstance(existing, int):
        return int(raw)
    if isinstance(existing, float):
        return float(raw)
    return raw
