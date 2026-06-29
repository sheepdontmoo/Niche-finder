# Niche & Product Opportunity Finder

A repeatable research pipeline that finds niches **winnable through organic
content (SEO + AIEO)** and **monetisable** (affiliate / own product / lead gen /
display), then outputs a **scored, ranked shortlist**.

It's a demand-discovery engine for a solo content operator: it surfaces where
the gap between **demand** (people searching and asking) and **supply** (weak
existing content + thin AI answers) is widest, filtered by whether that gap can
be turned into money. It removes guesswork and shows where the gap is — it does
**not** make the final call. You apply the "does this fit me" filter.

## How it scores

Each candidate niche is scored on six metrics, each normalised 0–10, combined
into a weighted composite:

| Metric | What it measures | Weight |
|---|---|---|
| **commercial_intent** | Avg CPC / traffic value + presence of buyer-intent modifiers (best, vs, review…) | 25% |
| **monetisation_fit** | Does it map cleanly to affiliate / product / lead-gen? Do real programs exist? | 20% |
| **seo_winnability** | Inverse keyword difficulty + weak/forum/thin results on page 1 | 20% |
| **aieo_gap** | How thin/generic/outdated current AI-engine answers are (LLM-graded) | 15% |
| **pain_density** | Count of distinct recurring questions/complaints (Reddit + autocomplete) | 10% |
| **trend_direction** | Google Trends slope over 24 months | 10% |

```
opportunity_score = ci*0.25 + mf*0.20 + sw*0.20 + ag*0.15 + pd*0.10 + td*0.10
```

The target profile: **high commercial intent + clear monetisation + winnable SEO
+ weak current AI answers + growing trend.**

## The pipeline (8 steps)

1. **Seed expansion** — Google autocomplete (no key) expands each seed into
   long-tail phrases, clustered by similarity.
2. **Demand** — Ahrefs API: volume, KD, CPC, traffic value, SERP features.
3. **Pain mining** — Reddit (no-key public JSON) filtered for pain signals, then
   scored by Claude for intensity / frequency / alternatives / buildability.
4. **Supply** — Ahrefs SERP overview: are incumbents DR70+ or is page 1 weak?
5. **AIEO gap** — generate money questions, grade how weak current AI answers
   are (Claude, + Perplexity if available).
6. **Monetisation** — Claude classifies the revenue path and finds real programs.
7. **Trend** — Google Trends slope (growing / stable / declining).
8. **Score & rank** — composite, persisted, ranked.

## Architecture

- **Steps talk only through SQLite.** Raw / intermediate / scored data live in
  separate layers, so re-scoring with new weights is a fast SQLite-to-SQLite
  pass that never re-scrapes.
- **Every external client runs in `live | record | fixture` mode.** A missing
  API key degrades only that source to fixture/synthetic data — the pipeline
  always reaches step 8 and emits a CSV. LLM steps fall back to deterministic
  heuristic graders.
- All raw responses are cached by request hash, so re-runs are resumable and
  don't re-bill the Ahrefs/Claude APIs.

```
src/niche_finder/
├── cli.py             # init-db / run / score / report / dashboard
├── orchestrator.py    # drives the 7 steps per niche, then scores
├── config.py          # settings.yaml + weights.yaml + env
├── models.py          # pydantic domain models
├── storage/           # schema.sql, db, repository
├── clients/           # autocomplete, ahrefs, reddit, anthropic, perplexity, trends
├── steps/             # step1..step7
├── clustering/        # lexical phrase clustering
├── scoring/           # normalize (6 metrics), scorer (step 8), explain
├── reporting/         # csv_export, onepager, dashboard (Streamlit)
└── text/              # signals lexicon, prompt templates
```

## Install

```bash
python -m venv .venv && source .venv/bin/activate
pip install -e ".[dev]"          # core + test tooling
pip install -e ".[live]"          # add anthropic + pytrends for live runs
pip install -e ".[dashboard]"     # add streamlit
```

## Configure API keys (all optional)

Copy `.env.example` to `.env` and fill in what you have. **Every key is
optional** — any source whose key is missing falls back to fixture/synthetic
data automatically, so the pipeline always runs.

| Var | Used for | Without it |
|---|---|---|
| `AHREFS_API_KEY` | volume / KD / CPC / SERP (the one paid source) | synthetic demand + SERP data |
| `ANTHROPIC_API_KEY` | pain scoring, AIEO grading, monetisation | deterministic heuristic graders |
| `REDDIT_*` | optional PRAW path (higher limits) | no-key public JSON (default) |
| `PERPLEXITY_API_KEY` | second AIEO grader | synthetic second opinion |

## Run

```bash
niche-finder init-db
niche-finder run --input config/niches.example.csv      # mode auto: live where keys exist
niche-finder run --input config/niches.example.csv --mode fixture   # fully offline
niche-finder report --top 5 --out out                   # ranked CSV + top-5 deep dives
niche-finder dashboard                                   # Streamlit (needs the dashboard extra)
```

Outputs land in `out/`:
- `ranked_niches.csv` — one row per niche, all six metric scores + composite, sorted.
- `deep_dives/NN_<niche>.md` — for the top N: pain quotes, money keyword cluster,
  SEO angle, AIEO angle (priority pages), monetisation path, suggested angle.

### Re-score without re-scraping

Edit `config/weights.yaml` (must sum to 1.0), then:

```bash
niche-finder score --weights config/weights.yaml        # reads stored data only
niche-finder report --top 5                              # regenerate outputs
```

## Add new niches / re-run

The niche list is just a CSV with a `seed_term` column (and optional `notes`):

```csv
seed_term,notes
electric bikes,commuter angle
home espresso,enthusiast gear
```

Point `--input` at it and re-run. No code changes needed. Already-scraped niches
reuse their cached data; new ones are fetched.

## Modes

- `auto` (default) — each source runs live if its key is present, else fixture.
- `live` — force live (sources without keys still fall back to fixture).
- `record` — live, and save every response under `data/fixtures/` for replay.
- `fixture` — never touch the network; replay fixtures, synthesise on miss.

The effective per-source mode is recorded in the `runs` table, and each niche
carries a `confidence` (data-coverage) flag in the CSV — so a dev/offline run is
never mistaken for a real one.

## Honest scoring

The score is a **shortlisting tool, not a verdict.** It surfaces the data and
flags uncertainty (the `confidence` column) where coverage is thin. Tune the
weights to your own thesis and re-score freely.

## Develop / test

```bash
pytest          # unit + end-to-end offline tests
ruff check src tests
```

The test suite runs the whole pipeline end-to-end in fixture mode (no keys, no
network) and asserts the CSV + deep-dives are produced.
