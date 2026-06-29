"""Optional Perplexity (search-grounded LLM) AIEO grader.

A second opinion on AI-answer quality from a search-grounded model. Same grading
contract as the Anthropic AIEO grader. Without a key it degrades to fixture mode
and returns a deterministic synthetic grade, so the niche still gets a second
data point without hard-failing.
"""

from __future__ import annotations

import json
import random

import httpx

from ..models import AieoGrade
from ..text import prompts, signals
from .base import BaseClient

API_URL = "https://api.perplexity.ai/chat/completions"


class PerplexityClient(BaseClient):
    source = "perplexity"

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.api_key = self.config.key_for("perplexity")

    def grade_aieo(self, question: str, model: str = "sonar") -> AieoGrade:
        params = {"question": question, "v": prompts.PROMPT_VERSION}

        def live() -> dict:
            resp = httpx.post(
                API_URL,
                headers={"Authorization": f"Bearer {self.api_key}"},
                json={
                    "model": model,
                    "messages": [
                        {"role": "system", "content": prompts.AIEO_SYSTEM},
                        {"role": "user", "content": prompts.AIEO_USER.format(question=question)},
                    ],
                },
                timeout=30.0,
            )
            resp.raise_for_status()
            content = resp.json()["choices"][0]["message"]["content"]
            return json.loads(_strip_fences(content))

        def synth(seed: int) -> dict:
            rng = random.Random(seed + 99)
            # Search-grounded answers tend to be more recent / better cited than
            # a vanilla assistant, so a slightly lower gap on average.
            is_buyer = signals.is_buyer_keyword(question)
            depth = round(rng.uniform(4, 7), 1)
            specificity = round(rng.uniform(3, 6), 1)
            recency = round(rng.uniform(4, 8), 1)
            citation = round(rng.uniform(3, 7), 1)
            weakness = round(10 - (depth + specificity + recency + citation) / 4, 1)
            if is_buyer:
                weakness = min(10.0, weakness + 1.0)
            return {"depth": depth, "specificity": specificity, "recency": recency,
                    "citation": citation, "weakness": max(0.0, weakness),
                    "notes": "heuristic perplexity grade"}

        d = self.request("grade_aieo", params, live, synth)
        return AieoGrade(
            provider="perplexity",
            depth=float(d.get("depth", 0)), specificity=float(d.get("specificity", 0)),
            recency=float(d.get("recency", 0)), citation=float(d.get("citation", 0)),
            weakness=float(d.get("weakness", 0)), notes=d.get("notes", ""),
        )


def _strip_fences(s: str) -> str:
    s = s.strip()
    if s.startswith("```"):
        s = s.split("\n", 1)[-1]
        if s.endswith("```"):
            s = s.rsplit("```", 1)[0]
    return s.strip()
