"""Versioned Claude prompt templates and their JSON output schemas.

Bumping PROMPT_VERSION when a template changes lets stored grades record which
prompt produced them (see the prompt_version columns).
"""

from __future__ import annotations

PROMPT_VERSION = "v1"

# --- Step 3: pain scoring ---------------------------------------------------
PAIN_SYSTEM = (
    "You analyse Reddit posts to extract recurring pain points for a content "
    "and product opportunity researcher. Be terse and concrete. Score honestly; "
    "most posts are mild, not acute."
)

PAIN_USER = """Niche: {niche}

Below are Reddit posts. Extract the distinct, recurring pain points / unmet needs.
For each, return:
- question_norm: the underlying question or complaint, normalised to a canonical phrasing
- quote: a short representative phrase in the user's own words (paraphrased, <120 chars)
- intensity: 0-10, how acute the frustration is
- frequency: integer, how many of the posts express this same pain
- alternatives: list of tools/methods people currently use (and that fall short)
- buildability: 0-10, how clearly this maps to a buildable product or content angle

POSTS:
{posts}
"""

PAIN_SCHEMA = {
    "type": "object",
    "properties": {
        "pains": {
            "type": "array",
            "items": {
                "type": "object",
                "properties": {
                    "question_norm": {"type": "string"},
                    "quote": {"type": "string"},
                    "intensity": {"type": "number"},
                    "frequency": {"type": "integer"},
                    "alternatives": {"type": "array", "items": {"type": "string"}},
                    "buildability": {"type": "number"},
                },
                "required": ["question_norm", "intensity", "frequency", "buildability"],
                "additionalProperties": False,
            },
        }
    },
    "required": ["pains"],
    "additionalProperties": False,
}

# --- Step 5: money-question generation + AIEO grading -----------------------
MQ_SYSTEM = "You generate high commercial-intent questions buyers ask in a niche."

MQ_USER = """Niche: {niche}
Top keywords (with buyer intent where present):
{keywords}

Generate {n} specific, high commercial-intent questions a buyer in this niche
would ask an AI assistant (think "best X for Y", "is X worth it", "X vs Y").
Return a JSON object with a "questions" array of strings."""

MQ_SCHEMA = {
    "type": "object",
    "properties": {"questions": {"type": "array", "items": {"type": "string"}}},
    "required": ["questions"],
    "additionalProperties": False,
}

AIEO_SYSTEM = (
    "You grade how good a CURRENT AI-assistant answer to a question is, to find "
    "gaps a dedicated content page could exploit. Higher weakness = bigger gap."
)

AIEO_USER = """Question: {question}

First answer the question as a current general AI assistant would (briefly).
Then grade that answer on each axis 0-10:
- depth: thoroughness and coverage
- specificity: concrete, non-generic detail
- recency: how current the information is
- citation: whether it cites real, checkable sources
Then compute:
- weakness: 0-10, how open this is for a dedicated page to beat the AI answer
  (low depth/specificity/recency/citation => high weakness)
Return JSON with depth, specificity, recency, citation, weakness, notes."""

AIEO_SCHEMA = {
    "type": "object",
    "properties": {
        "depth": {"type": "number"},
        "specificity": {"type": "number"},
        "recency": {"type": "number"},
        "citation": {"type": "number"},
        "weakness": {"type": "number"},
        "notes": {"type": "string"},
    },
    "required": ["depth", "specificity", "recency", "citation", "weakness"],
    "additionalProperties": False,
}

# --- Step 6: monetisation classification ------------------------------------
MONET_SYSTEM = (
    "You classify how a content niche can be monetised by a solo operator via "
    "organic search/AI traffic. Be realistic: many niches have weak revenue paths."
)

MONET_USER = """Niche: {niche}
Signals: avg CPC ${avg_cpc}, buyer-keyword share {buyer_ratio:.0%}.

Classify the niche against four models and pick the strongest primary path:
- affiliate (comparison/review/best-X content)
- own-product (digital product funnel)
- lead-gen (sell leads to businesses)
- display (ads at volume only)

Return JSON:
- primary_path: one of affiliate|own-product|lead-gen|display
- paths: array of applicable paths
- confidence: 0-1
- affiliate_programs: array of concrete affiliate programs/networks that exist for this niche
- rationale: one sentence"""

MONET_SCHEMA = {
    "type": "object",
    "properties": {
        "primary_path": {"type": "string"},
        "paths": {"type": "array", "items": {"type": "string"}},
        "confidence": {"type": "number"},
        "affiliate_programs": {"type": "array", "items": {"type": "string"}},
        "rationale": {"type": "string"},
    },
    "required": ["primary_path", "confidence"],
    "additionalProperties": False,
}
