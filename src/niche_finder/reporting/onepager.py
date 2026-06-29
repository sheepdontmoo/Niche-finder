"""Top-N deep-dive one-pagers (Markdown), rendered with Jinja2.

For each of the highest-scoring niches: the recurring pain in the audience's own
words, the money keyword cluster, the SEO angle (what ranks + why it's weak),
the AIEO angle (which questions AI answers badly = priority pages), and the
monetisation path.
"""

from __future__ import annotations

from pathlib import Path

from jinja2 import Template

from ..scoring.explain import deep_dive
from ..storage.repository import Repository

_TEMPLATE = Template(
    """# {{ d.seed_term }}

**Rank #{{ d.rank }}** · Composite **{{ "%.2f"|format(d.composite) }}/10**\
{% if d.confidence is not none %} · Data confidence {{ "%.0f"|format(d.confidence * 100) }}%{% endif %}
{% if d.notes %}*{{ d.notes }}*{% endif %}

## Scorecard
| Metric | Score /10 |
|---|---|
{% for k, v in d.metric_scores.items() -%}
| {{ k.replace('_', ' ') }} | {{ "%.1f"|format(v) }} |
{% endfor %}

## Pain & recurring questions (audience's words)
{% if d.pains %}{% for p in d.pains -%}
- **{{ p.question }}** — *"{{ p.quote }}"* (intensity {{ "%.0f"|format(p.intensity) }}, recurs ×{{ p.frequency }})
{% endfor %}{% else %}_No pain points captured for this niche._{% endif %}

## Money keyword cluster
| Keyword | Volume | CPC | KD | Buyer |
|---|--:|--:|--:|:--:|
{% for k in d.money_keywords -%}
| {{ k.phrase }} | {{ k.volume or 0 }} | ${{ "%.2f"|format(k.cpc or 0) }} | {{ "%.0f"|format(k.kd or 0) }} | {{ "✓" if k.buyer else "" }} |
{% endfor %}

## SEO angle
- Page-1 sample across money keywords: **{{ d.seo.total_serp }}** results, **{{ d.seo.weak_count }}** weak (forum / thin / DR<30).
{% if d.seo.big_players %}- Established incumbents (DR 70+): {{ d.seo.big_players | join(", ") }}.{% endif %}
{% if d.seo.weak_examples %}- Weak / beatable results: {{ d.seo.weak_examples | join(", ") }}.{% endif %}
- **Where to break in:** target the clusters where forums/thin pages rank — a dedicated, structured page beats them.

## AIEO angle — questions the AI engines answer badly (priority pages)
{% if d.aieo.weak_questions %}{% for q in d.aieo.weak_questions -%}
- **{{ q.question }}** (current-answer weakness {{ "%.1f"|format(q.weakness) }}/10)
{% endfor %}
*Build the page that becomes the cited source for these.*
{% else %}_No AIEO grades captured._{% endif %}

## Monetisation path
{% if d.monetisation %}- **Primary:** {{ d.monetisation.primary_path }}{% if d.monetisation.paths %} (also: {{ d.monetisation.paths | join(", ") }}){% endif %}
{% if d.monetisation.affiliate_programs %}- **Programs to promote:** {{ d.monetisation.affiliate_programs | join(", ") }}{% endif %}
{% if d.monetisation.rationale %}- {{ d.monetisation.rationale }}{% endif %}
{% else %}_No monetisation classification captured._{% endif %}

## Trend
{% if d.trend %}**{{ d.trend.direction }}** (slope {{ "%.4f"|format(d.trend.slope) }}){% else %}_Unknown._{% endif %}

## Suggested angle
A solo content site targeting the buyer-intent clusters above, leading with the
{% if d.aieo.weak_questions %}"{{ d.aieo.weak_questions[0].question }}"{% else %}top money question{% endif %} page as the
flagship citation target, monetised via {{ d.monetisation.primary_path if d.monetisation else "the strongest available path" }}.
"""
)


def generate_onepagers(repo: Repository, run_id: int, top: int,
                       out_dir: str | Path) -> list[Path]:
    out = Path(out_dir)
    out.mkdir(parents=True, exist_ok=True)
    scores = repo.scores_for_run(run_id)[:top]
    written = []
    for s in scores:
        d = deep_dive(repo, s["niche_id"], run_id)
        md = _TEMPLATE.render(d=_AttrDict(d))
        slug = _slugify(s["seed_term"])
        path = out / f"{s['rank']:02d}_{slug}.md"
        path.write_text(md, encoding="utf-8")
        written.append(path)
    return written


class _AttrDict(dict):
    """Dict with attribute access, recursively, for clean template syntax."""

    def __getattr__(self, key):
        try:
            val = self[key]
        except KeyError as e:
            raise AttributeError(key) from e
        return _wrap(val)


def _wrap(val):
    if isinstance(val, dict):
        return _AttrDict(val)
    if isinstance(val, list):
        return [_wrap(v) for v in val]
    return val


def _slugify(text: str) -> str:
    keep = [c.lower() if c.isalnum() else "-" for c in text]
    slug = "".join(keep)
    while "--" in slug:
        slug = slug.replace("--", "-")
    return slug.strip("-")[:50]
