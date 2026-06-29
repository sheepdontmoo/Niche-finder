"""Per-host rate limiting and retry/backoff helpers.

A simple monotonic token-bucket gate (requests/second) plus a tenacity-based
retry decorator. Both are no-ops in fixture mode because no network is touched.
"""

from __future__ import annotations

import threading
import time
from collections.abc import Callable
from typing import TypeVar

try:  # tenacity is a core dep, but keep import resilient for minimal installs
    from tenacity import retry, stop_after_attempt, wait_exponential_jitter
    _HAS_TENACITY = True
except ImportError:  # pragma: no cover
    _HAS_TENACITY = False

T = TypeVar("T")


class RateLimiter:
    """Token-bucket limiter keyed by host/source name."""

    def __init__(self) -> None:
        self._next_allowed: dict[str, float] = {}
        self._lock = threading.Lock()

    def wait(self, key: str, rate_per_sec: float) -> None:
        if rate_per_sec <= 0:
            return
        interval = 1.0 / rate_per_sec
        with self._lock:
            now = time.monotonic()
            earliest = self._next_allowed.get(key, 0.0)
            sleep_for = max(0.0, earliest - now)
            self._next_allowed[key] = max(now, earliest) + interval
        if sleep_for > 0:
            time.sleep(sleep_for)


def with_retry(attempts: int, base_delay: float) -> Callable[[Callable[..., T]], Callable[..., T]]:
    """Return a decorator that retries on exception with exponential backoff + jitter."""
    if not _HAS_TENACITY:  # pragma: no cover
        def passthrough(fn: Callable[..., T]) -> Callable[..., T]:
            return fn
        return passthrough

    return retry(
        stop=stop_after_attempt(attempts),
        wait=wait_exponential_jitter(initial=base_delay, max=base_delay * 8),
        reraise=True,
    )
