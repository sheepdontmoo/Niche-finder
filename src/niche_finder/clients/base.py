"""Client base class implementing the live / record / fixture machinery.

Every client routes its external calls through :meth:`BaseClient.request`, which:

* hashes the request (source + endpoint + normalised params),
* serves a cached / fixture response when one exists,
* in ``fixture`` mode with no recording, returns a **deterministic synthetic
  stub** seeded from the request hash (so offline runs are reproducible),
* in ``live`` / ``record`` mode, rate-limits + calls the network function,
  persists the raw response, and (in ``record``) also writes a fixture file.

This is what makes a missing API key degrade only one source instead of
breaking the run.
"""

from __future__ import annotations

import hashlib
import json
from collections.abc import Callable
from pathlib import Path
from typing import Any

from ..config import Config
from ..ratelimit import RateLimiter, with_retry
from ..storage.repository import Repository


def request_hash(source: str, endpoint: str, params: dict[str, Any]) -> str:
    payload = json.dumps(
        {"source": source, "endpoint": endpoint, "params": params},
        sort_keys=True, separators=(",", ":"),
    )
    return hashlib.sha256(payload.encode("utf-8")).hexdigest()


def seed_from_hash(h: str) -> int:
    """Stable integer seed derived from a request hash (for synthetic stubs)."""
    return int(h[:12], 16)


class BaseClient:
    source: str = "base"

    def __init__(self, config: Config, repo: Repository, limiter: RateLimiter,
                 run_id: int | None = None):
        self.config = config
        self.repo = repo
        self.limiter = limiter
        self.run_id = run_id
        self.mode = config.effective_mode(self.source)
        self.fixtures_dir = config.abs_path(config.paths.fixtures) / self.source

    # -- fixture file helpers ------------------------------------------------
    def _fixture_path(self, h: str) -> Path:
        return self.fixtures_dir / f"{h}.json"

    def _read_fixture_file(self, h: str) -> Any | None:
        path = self._fixture_path(h)
        if path.exists():
            try:
                return json.loads(path.read_text(encoding="utf-8"))
            except json.JSONDecodeError:
                return None
        return None

    def _write_fixture_file(self, h: str, response: Any) -> None:
        self.fixtures_dir.mkdir(parents=True, exist_ok=True)
        self._fixture_path(h).write_text(
            json.dumps(response, indent=2, sort_keys=True), encoding="utf-8"
        )

    # -- the one entry point all clients use ---------------------------------
    def request(
        self,
        endpoint: str,
        params: dict[str, Any],
        live_fn: Callable[[], Any],
        synth_fn: Callable[[int], Any],
    ) -> Any:
        """Return a response, sourced per the client's mode.

        ``live_fn`` performs the real network call (no args).
        ``synth_fn(seed)`` returns a deterministic synthetic response.
        """
        h = request_hash(self.source, endpoint, params)

        # 1. Always honour a cached raw response (cheap + avoids re-billing).
        cached = self.repo.get_raw(h)
        if cached is not None:
            return cached

        # 2. Fixture mode: replay file, else synthesise deterministically.
        if self.mode == "fixture":
            from_file = self._read_fixture_file(h)
            if from_file is not None:
                self.repo.save_raw(self.source, h, {"endpoint": endpoint, "params": params},
                                   from_file, status="ok", run_id=self.run_id)
                return from_file
            synthetic = synth_fn(seed_from_hash(h))
            self.repo.save_raw(self.source, h, {"endpoint": endpoint, "params": params},
                               synthetic, status="synthetic", run_id=self.run_id)
            return synthetic

        # 3. Live / record mode: rate-limit, call, persist.
        self.limiter.wait(self.source, self.config.rate_limit(self.source))
        retried = with_retry(self.config.retry.attempts, self.config.retry.base_delay_seconds)
        try:
            response = retried(live_fn)()
            status = "ok"
        except Exception:  # network failure → fall back to synthetic, keep the run alive
            response = synth_fn(seed_from_hash(h))
            status = "synthetic"
        self.repo.save_raw(self.source, h, {"endpoint": endpoint, "params": params},
                           response, status=status, run_id=self.run_id)
        if self.mode == "record" and status == "ok":
            self._write_fixture_file(h, response)
        return response
