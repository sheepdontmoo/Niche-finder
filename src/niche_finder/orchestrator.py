"""Pipeline orchestrator: drives the 7 research steps per niche, then scores.

Resumable by construction — every step persists to SQLite and every external
call is cached by request hash, so re-running skips already-fetched data. A
budget guard caps total LLM calls per run.
"""

from __future__ import annotations

import csv
from dataclasses import dataclass, field
from pathlib import Path

from .clients.ahrefs import AhrefsClient
from .clients.anthropic_client import AnthropicClient
from .clients.autocomplete import AutocompleteClient
from .clients.perplexity import PerplexityClient
from .clients.reddit import RedditClient
from .clients.trends import TrendsClient
from .config import SOURCE_KEYS, Config
from .ratelimit import RateLimiter
from .scoring.scorer import score_run
from .steps import (
    step1_seed_expand,
    step2_demand,
    step3_pain,
    step4_supply,
    step5_aieo,
    step6_monetisation,
    step7_trend,
)
from .storage.repository import Repository


@dataclass
class Clients:
    autocomplete: AutocompleteClient
    ahrefs: AhrefsClient
    reddit: RedditClient
    anthropic: AnthropicClient
    perplexity: PerplexityClient
    trends: TrendsClient


@dataclass
class RunReport:
    run_id: int
    niches: list[str] = field(default_factory=list)
    per_step: dict[str, dict[str, int]] = field(default_factory=dict)
    scored: list[dict] = field(default_factory=list)


def build_clients(config: Config, repo: Repository, run_id: int) -> Clients:
    limiter = RateLimiter()
    kw = dict(config=config, repo=repo, limiter=limiter, run_id=run_id)
    return Clients(
        autocomplete=AutocompleteClient(**kw),
        ahrefs=AhrefsClient(**kw),
        reddit=RedditClient(**kw),
        anthropic=AnthropicClient(**kw),
        perplexity=PerplexityClient(**kw),
        trends=TrendsClient(**kw),
    )


def load_niches(input_path: str | Path) -> list[tuple[str, str]]:
    """Read seed terms from a CSV with a ``seed_term`` column (+ optional notes)."""
    rows = []
    with open(input_path, newline="", encoding="utf-8") as f:
        reader = csv.DictReader(f)
        for r in reader:
            seed = (r.get("seed_term") or "").strip()
            if seed:
                rows.append((seed, (r.get("notes") or "").strip()))
    return rows


def effective_modes(config: Config) -> dict[str, str]:
    return {src: config.effective_mode(src) for src in SOURCE_KEYS}


def run_pipeline(repo: Repository, config: Config, input_path: str | Path,
                 steps: set[int] | None = None,
                 log=lambda msg: None) -> RunReport:
    """Run the pipeline over all seed niches and score them.

    ``steps`` selects which of steps 1-7 to run (default all). Scoring (step 8)
    always runs at the end.
    """
    steps = steps or set(range(1, 8))
    modes = effective_modes(config)
    run_id = repo.create_run(
        mode=config.mode,
        config_json={"effective_modes": modes, "settings_mode": config.mode},
        weights=config.weights,
    )
    log(f"Run #{run_id} — source modes: {modes}")

    clients = build_clients(config, repo, run_id)
    report = RunReport(run_id=run_id)

    rows = load_niches(input_path)
    for seed, notes in rows:
        nid = repo.upsert_niche(seed, notes)
        report.niches.append(seed)
        log(f"→ {seed}")

        if 1 in steps:
            n = step1_seed_expand.run(repo, clients.autocomplete, config, nid, seed)
            _record(report, "expand", seed, n)
        if 2 in steps:
            n = step2_demand.run(repo, clients.ahrefs, config, nid)
            _record(report, "demand", seed, n)
        if 3 in steps:
            n = step3_pain.run(repo, clients.reddit, clients.anthropic, config, nid, seed)
            _record(report, "pain", seed, n)
        if 4 in steps:
            n = step4_supply.run(repo, clients.ahrefs, config, nid)
            _record(report, "supply", seed, n)
        if 5 in steps:
            n = step5_aieo.run(repo, clients.anthropic, clients.perplexity, config, nid, seed)
            _record(report, "aieo", seed, n)
        if 6 in steps:
            path = step6_monetisation.run(repo, clients.anthropic, config, nid, seed)
            _record(report, "monetisation", seed, path)
        if 7 in steps:
            d = step7_trend.run(repo, clients.trends, config, nid, seed)
            _record(report, "trend", seed, d)

    report.scored = score_run(repo, config, run_id)
    repo.finish_run(run_id)
    log(f"Scored {len(report.scored)} niches.")
    return report


def _record(report: RunReport, step: str, seed: str, value) -> None:
    report.per_step.setdefault(step, {})[seed] = value
