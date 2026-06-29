"""Reddit pain-mining client.

Default path uses Reddit's no-key public JSON endpoints (the yars approach):
``https://www.reddit.com/r/<sub>/search.json``. If PRAW credentials are present
they could be wired in for higher limits (left as an optional enhancement). In
fixture mode the client synthesises plausible pain-laden posts deterministically.
"""

from __future__ import annotations

import random

import httpx

from .base import BaseClient

SEARCH_URL = "https://www.reddit.com/r/{sub}/search.json"
USER_AGENT = "niche-finder/0.1 (no-key public JSON)"

# Subreddit guesses keyed by tokens in the seed term — best-effort; the search
# endpoint itself does the heavy lifting.
GENERIC_SUBS = ["AskReddit", "NoStupidQuestions", "findapath"]

PAIN_TEMPLATES = [
    "I struggle with {topic} and don't know where to start",
    "Frustrated — every {topic} guide says the same generic thing",
    "Wish there was a simple tool for {topic}",
    "How do I actually stick with {topic}?",
    "What's the best way to approach {topic} on a budget?",
    "Anyone know a good alternative for {topic}? Current options are bad",
    "Why is {topic} so confusing? I keep failing",
    "Recommend something for {topic} that actually works",
]


class RedditClient(BaseClient):
    source = "reddit"

    def search(self, niche: str, subreddits: list[str], limit: int) -> list[dict]:
        """Search the given subreddits for posts about ``niche``."""
        posts: list[dict] = []
        for sub in subreddits:
            posts.extend(self._search_sub(sub, niche, limit))
        return posts

    def default_subreddits(self, niche: str, n: int) -> list[str]:
        # A real run would map niches → curated subs; the public search works
        # across subs anyway, so we use a small generic set plus a niche guess.
        guess = niche.split("/")[0].split("(")[0].strip().replace(" ", "")
        subs = [guess] + GENERIC_SUBS
        return subs[:n]

    def _search_sub(self, sub: str, niche: str, limit: int) -> list[dict]:
        params = {"sub": sub, "q": niche, "limit": limit, "sort": "relevance"}

        def live() -> list[dict]:
            resp = httpx.get(
                SEARCH_URL.format(sub=sub),
                params={"q": niche, "limit": limit, "restrict_sr": 1,
                        "sort": "relevance", "t": "year"},
                headers={"User-Agent": USER_AGENT},
                timeout=20.0,
            )
            resp.raise_for_status()
            data = resp.json()
            children = data.get("data", {}).get("children", [])
            out = []
            for c in children:
                d = c.get("data", {})
                out.append({
                    "subreddit": d.get("subreddit", sub),
                    "permalink": "https://reddit.com" + d.get("permalink", ""),
                    "title": d.get("title", ""),
                    "body": d.get("selftext", ""),
                    "score": int(d.get("score", 0)),
                    "created_utc": float(d.get("created_utc", 0)),
                })
            return out

        def synth(seed: int) -> list[dict]:
            rng = random.Random(seed)
            topic = niche.split("/")[0].strip()
            n = rng.randint(5, min(limit, 12))
            out = []
            for i in range(n):
                title = rng.choice(PAIN_TEMPLATES).format(topic=topic)
                out.append({
                    "subreddit": sub,
                    "permalink": f"https://reddit.com/r/{sub}/comments/synth{seed}{i}",
                    "title": title,
                    "body": f"{title}. I've tried a few things but nothing clicks. "
                            f"Looking for something that fits {topic}.",
                    "score": rng.randint(1, 800),
                    "created_utc": 1_700_000_000.0 + i * 3600,
                })
            return out

        return self.request("search", params, live, synth)
