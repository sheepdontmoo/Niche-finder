"""Shared fixtures: a temp DB + a fixture-mode config (no network, no keys)."""

from __future__ import annotations

import pytest

from niche_finder.config import load_config
from niche_finder.storage.db import connect, init_db
from niche_finder.storage.repository import Repository


@pytest.fixture(autouse=True)
def _no_keys(monkeypatch):
    """Ensure tests never accidentally hit a live API."""
    for var in ("AHREFS_API_KEY", "ANTHROPIC_API_KEY", "PERPLEXITY_API_KEY"):
        monkeypatch.delenv(var, raising=False)


@pytest.fixture
def config(tmp_path):
    cfg = load_config(overrides={"mode": "fixture"})
    cfg.paths.db = str(tmp_path / "test.db")
    cfg.paths.fixtures = str(tmp_path / "fixtures")
    # Keep the test run small and fast.
    cfg.expansion.max_phrases_per_niche = 40
    cfg.expansion.autocomplete_depth = 1
    cfg.pain.subreddits_per_niche = 2
    cfg.pain.posts_per_subreddit = 8
    cfg.aieo.questions_per_niche = 4
    return cfg


@pytest.fixture
def repo(config):
    db = config.abs_path(config.paths.db)
    init_db(db)
    conn = connect(db)
    yield Repository(conn)
    conn.close()
