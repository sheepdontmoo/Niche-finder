"""Claude (Anthropic) client for pain scoring, AIEO grading, monetisation.

Each method routes through ``BaseClient.request`` so results are cached by
request hash and replayable offline. When ``ANTHROPIC_API_KEY`` is absent the
client runs in fixture mode and the synthetic functions fall back to
deterministic heuristic graders (lexicon-based), keeping the pipeline runnable
and free without an LLM.

Model ids and SDK usage follow the Claude API reference: the official
``anthropic`` SDK, ``messages.create`` with ``output_config.format`` for
schema-constrained JSON. Bulk pain scoring uses a Haiku-class model; AIEO
grading uses a stronger model.
"""

from __future__ import annotations

import json
import random

from ..models import AieoGrade, Monetisation, PainPoint
from ..text import prompts, signals
from .base import BaseClient


class AnthropicClient(BaseClient):
    source = "anthropic"

    def __init__(self, *args, **kwargs):
        super().__init__(*args, **kwargs)
        self.api_key = self.config.key_for("anthropic")
        self._sdk = None

    def _client(self):
        if self._sdk is None:
            import anthropic  # imported lazily so the dep is optional

            self._sdk = anthropic.Anthropic(api_key=self.api_key)
        return self._sdk

    # -- generic JSON call ---------------------------------------------------
    def _complete_json(self, model: str, system: str, user: str, schema: dict,
                       max_tokens: int = 2048) -> dict:
        resp = self._client().messages.create(
            model=model,
            max_tokens=max_tokens,
            system=system,
            messages=[{"role": "user", "content": user}],
            output_config={"format": {"type": "json_schema", "schema": schema}},
        )
        if getattr(resp, "stop_reason", None) == "refusal":
            raise RuntimeError("Claude refused the request")
        text_out = next((b.text for b in resp.content if b.type == "text"), "{}")
        return json.loads(text_out)

    # -- step 3: pain scoring ------------------------------------------------
    def score_pain(self, niche: str, posts: list[dict]) -> list[PainPoint]:
        joined = "\n".join(
            f"- [{p.get('subreddit', '')}] {p.get('title', '')} :: {p.get('body', '')[:240]}"
            for p in posts
        )
        params = {"niche": niche, "n_posts": len(posts), "v": prompts.PROMPT_VERSION}

        def live() -> dict:
            user = prompts.PAIN_USER.format(niche=niche, posts=joined)
            return self._complete_json(
                self.config.models.pain_scoring, prompts.PAIN_SYSTEM, user,
                prompts.PAIN_SCHEMA,
            )

        def synth(seed: int) -> dict:
            return _heuristic_pain(niche, posts, seed)

        data = self.request("score_pain", params, live, synth)
        out = []
        for p in data.get("pains", []):
            out.append(PainPoint(
                question_norm=p.get("question_norm", ""),
                quote=p.get("quote", ""),
                intensity=float(p.get("intensity", 0)),
                frequency=int(p.get("frequency", 1)),
                alternatives=p.get("alternatives", []) or [],
                buildability=float(p.get("buildability", 0)),
            ))
        return out

    # -- step 5: money questions + AIEO grading ------------------------------
    def generate_money_questions(self, niche: str, keywords: list[str], n: int) -> list[str]:
        params = {"niche": niche, "n": n, "v": prompts.PROMPT_VERSION}

        def live() -> dict:
            user = prompts.MQ_USER.format(niche=niche, keywords="\n".join(keywords[:40]), n=n)
            return self._complete_json(
                self.config.models.aieo_grading, prompts.MQ_SYSTEM, user, prompts.MQ_SCHEMA,
                max_tokens=1024,
            )

        def synth(seed: int) -> dict:
            return {"questions": _heuristic_questions(niche, keywords, n, seed)}

        return self.request("money_questions", params, live, synth).get("questions", [])[:n]

    def grade_aieo(self, question: str) -> AieoGrade:
        params = {"question": question, "v": prompts.PROMPT_VERSION}

        def live() -> dict:
            user = prompts.AIEO_USER.format(question=question)
            return self._complete_json(
                self.config.models.aieo_grading, prompts.AIEO_SYSTEM, user,
                prompts.AIEO_SCHEMA, max_tokens=1024,
            )

        def synth(seed: int) -> dict:
            return _heuristic_aieo(question, seed)

        d = self.request("grade_aieo", params, live, synth)
        return AieoGrade(
            provider="anthropic",
            depth=float(d.get("depth", 0)), specificity=float(d.get("specificity", 0)),
            recency=float(d.get("recency", 0)), citation=float(d.get("citation", 0)),
            weakness=float(d.get("weakness", 0)), notes=d.get("notes", ""),
        )

    # -- step 6: monetisation -----------------------------------------------
    def classify_monetisation(self, niche: str, avg_cpc: float, buyer_ratio: float) -> Monetisation:
        params = {"niche": niche, "avg_cpc": round(avg_cpc, 2),
                  "buyer_ratio": round(buyer_ratio, 3), "v": prompts.PROMPT_VERSION}

        def live() -> dict:
            user = prompts.MONET_USER.format(niche=niche, avg_cpc=round(avg_cpc, 2),
                                             buyer_ratio=buyer_ratio)
            return self._complete_json(
                self.config.models.monetisation, prompts.MONET_SYSTEM, user,
                prompts.MONET_SCHEMA, max_tokens=1024,
            )

        def synth(seed: int) -> dict:
            return _heuristic_monetisation(niche, avg_cpc, buyer_ratio, seed)

        d = self.request("monetisation", params, live, synth)
        return Monetisation(
            primary_path=d.get("primary_path", "display"),
            paths=d.get("paths", []) or [],
            confidence=float(d.get("confidence", 0)),
            affiliate_programs=d.get("affiliate_programs", []) or [],
            rationale=d.get("rationale", ""),
        )


# --------------------------------------------------------------------------
# Deterministic heuristic graders (used in fixture mode / no API key).
# --------------------------------------------------------------------------
def _heuristic_pain(niche: str, posts: list[dict], seed: int) -> dict:
    rng = random.Random(seed)
    # Group posts by the pain signals they contain.
    buckets: dict[str, list[dict]] = {}
    for p in posts:
        sigs = signals.count_pain_signals(f"{p.get('title','')} {p.get('body','')}")
        key = sigs[0] if sigs else "general"
        buckets.setdefault(key, []).append(p)
    pains = []
    for key, group in buckets.items():
        sample = group[0]
        title = sample.get("title", "")
        pains.append({
            "question_norm": f"{key.capitalize()} with {niche.split('/')[0].strip()}",
            "quote": title[:120],
            "intensity": round(min(10.0, 4 + len(group) + rng.uniform(0, 2)), 1),
            "frequency": len(group),
            "alternatives": [],
            "buildability": round(rng.uniform(4, 9), 1),
        })
    return {"pains": pains}


def _heuristic_questions(niche: str, keywords: list[str], n: int, seed: int) -> dict:
    rng = random.Random(seed)
    base = niche.split("/")[0].strip()
    buyer_kws = [k for k in keywords if signals.is_buyer_keyword(k)] or keywords
    templates = [
        "What is the best {kw}?",
        "Is {kw} worth it?",
        "{kw} vs alternatives — which should I choose?",
        "How much does {kw} cost?",
        "What's the cheapest way to {base}?",
        "Best {base} for beginners?",
    ]
    out: list[str] = []
    pool = (buyer_kws or [base]) * 3
    rng.shuffle(pool)
    for i in range(n):
        t = templates[i % len(templates)]
        kw = pool[i % len(pool)] if pool else base
        out.append(t.format(kw=kw, base=base))
    return out


def _heuristic_aieo(question: str, seed: int) -> dict:
    rng = random.Random(seed)
    # Buyer questions ("best", "vs", "worth it") tend to get weaker generic AI
    # answers => higher gap. Informational ones get stronger answers.
    is_buyer = signals.is_buyer_keyword(question)
    depth = round(rng.uniform(3, 6), 1)
    specificity = round(rng.uniform(2, 5) if is_buyer else rng.uniform(4, 7), 1)
    recency = round(rng.uniform(1, 4), 1)          # AI answers tend to be stale
    citation = round(rng.uniform(0, 3), 1)         # rarely cite real sources
    weakness = round(10 - (depth + specificity + recency + citation) / 4, 1)
    return {"depth": depth, "specificity": specificity, "recency": recency,
            "citation": citation, "weakness": max(0.0, weakness),
            "notes": "heuristic grade (no LLM key)"}


def _heuristic_monetisation(niche: str, avg_cpc: float, buyer_ratio: float, seed: int) -> dict:
    if avg_cpc >= 3 and buyer_ratio >= 0.2:
        primary, paths = "affiliate", ["affiliate", "own-product"]
    elif buyer_ratio >= 0.15:
        primary, paths = "own-product", ["own-product", "affiliate"]
    elif avg_cpc >= 5:
        primary, paths = "lead-gen", ["lead-gen", "affiliate"]
    else:
        primary, paths = "display", ["display"]
    return {
        "primary_path": primary,
        "paths": paths,
        "confidence": round(min(1.0, 0.4 + buyer_ratio + avg_cpc / 20), 2),
        "affiliate_programs": ["Amazon Associates", "Impact", "Awin"]
        if "affiliate" in paths else [],
        "rationale": f"heuristic: cpc ${avg_cpc:.2f}, buyer share {buyer_ratio:.0%}",
    }
