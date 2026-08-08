# ACQ — a private replica of the ACQ Vantage toolset

A self-hosted version of what Acquisition.com sells as ACQ Vantage, rebuilt for
running on **our own businesses**. No community, no pricing, no funnel — just the
advisory engine.

Built from the teardown in [`research/acq-vantage-teardown.md`](research/acq-vantage-teardown.md).

---

## What this is, and what it isn't

Vantage's AI is described by Acquisition.com as *"the diagnosis we run on
portfolio companies, turned into software"* — a retrieval layer over frontier
models, grounded in a corpus of Hormozi's books, frameworks, playbooks and
50,000+ hours of private consulting calls.

**We can rebuild the diagnosis and the frameworks. We cannot rebuild their
private call transcripts.** So:

| Layer | Theirs | Ours |
|---|---|---|
| Model | "state-of-the-art providers" | Claude, via Claude Code |
| Frameworks | Books + internal playbooks | `knowledge/acq/` — reconstructed from the books and published workshop write-ups |
| Diagnosis | Portfolio director process | `/acq` — the constraint ladder + ratio thresholds |
| Business context | Metrics submitted 2 months pre-workshop | `business-context/*.md` |
| Agents | VSL, Meta ads, sales scripts | `/acq-ads`, `/acq-sales`, `/acq-offer`, `/acq-money` |
| Private corpus | 50k hrs of client calls | **Missing — this is the real gap** |

The frameworks are public — they're in three books Hormozi sells and in write-ups
by people who paid $35–45k for the workshops. What's genuinely proprietary is the
pattern recognition across 1,000+ companies. Our substitute is our *own* history:
see "Closing the corpus gap" below.

---

## Setup

1. Copy `business-context/TEMPLATE.md` to `business-context/<business>.md`
2. Fill it in. Mark anything untracked as `NOT TRACKED` — **don't guess.** An
   untracked number is a finding in its own right, and a guessed one poisons the
   diagnosis.
3. Run `/acq <business>`

---

## Commands

| Command | Use for |
|---|---|
| **`/acq-ask`** | **Start here.** Describe the problem in plain English, get the constraint and the turnaround. No form. |
| `/acq` | Full structured diagnosis when you have the numbers |
| `/acq-offer` | Grand Slam Offer — Value Equation, obstacle stack, enhancers, MAGIC naming |
| `/acq-money` | Money model — the four offer types, CAC payback, cash ratios |
| `/acq-ads` | CRO test plans, 6×5 ad assembly, 70/20/10 scaling, VSL copy |
| `/acq-sales` | Speed to lead, closer rubric, scripts, incentives, team structure |
| `/acq-objections` | Objections guide — every reason they say no, and what dissolves it |
| `/acq-funnel` | Funnel design and repair — stages, targets, where the money leaks |

`/acq-ask` is the front door and mirrors how people actually use ACQ Vantage:
state the problem, get the answer. It asks at most three questions — only the
ones that would change the prescription — and never demands a filled-in intake.

`/acq` is the same engine with the numbers up front. The specialist commands
assume you already know the constraint; using them without a diagnosis is how you
optimise the wrong thing well.

The specialist set mirrors the artefacts ACQ AI's own internal prompts produce —
scripts, offers, funnels, objections guides, VSLs and sales systems.

---

## Knowledge base

| File | Contents |
|---|---|
| `knowledge/acq/00-diagnosis.md` | **The core.** Constraint ladder, the ratio thresholds, four growth levels, diagnostic sequence |
| `knowledge/acq/01-offers.md` | Value Equation, Grand Slam Offer (5 steps), Delivery Cube, five enhancers, MAGIC, pricing |
| `knowledge/acq/02-leads.md` | Core Four, lead magnets, More/Better/New |
| `knowledge/acq/03-money-models.md` | Attraction/upsell/downsell/continuity, CAC, GP, LTGP, FECC, payback |
| `knowledge/acq/04-marketing-ops.md` | CRO, page structure, ad assembly, 70/20/10, media incentives |
| `knowledge/acq/05-sales.md` | Speed to lead, 31-point rubric, operationalising behaviour, player-coach |
| `knowledge/acq/06-people-scaling.md` | Hiring for traits, operational clarity, retention, status ladders |

---

## The numbers that drive everything

Three thresholds decide most recommendations:

| Ratio | Target | If you fail it |
|---|---|---|
| **LTGP : CAC** | **> 3** | Growth doesn't compound. Below 1, growth destroys the business |
| **FECC : CAC** | **> 2** | Can't self-fund acquisition — capped by balance sheet, not demand |
| **Payback period** | **≤ 30 days** | Can't recycle the dollar monthly; spend is capped |

And one ordering rule — the **constraint ladder**:

```
Leads → Sales → LTV → Margin/Cash → People/Ops
```

Exactly one rung binds at a time. Fixing it exposes the next ("bottleneck
ping-pong") — that's the mechanism of scaling, not a sign of failure.

Two standing corrections the system will make:
- **Gross profit, never revenue.** Revenue-based reasoning is how profitable-
  looking businesses run out of money.
- **More before New.** Most "the channel stopped working" is "we never scaled it."

---

## Closing the corpus gap

The frameworks alone make this roughly a good $35k-workshop-in-a-box. What would
make it genuinely better than Vantage *for our businesses specifically* is a
private corpus they don't have — ours:

- Past campaign results: creative, spend, CPL, CAC, what won and what died
- Sales call recordings and transcripts
- Historical P&Ls and cohort retention data
- Post-mortems on things that failed and why

Drop these in `business-context/` (or a `corpus/` directory) and they become
retrievable context. Hormozi's advantage is pattern recognition across 1,000
companies; ours is total recall on *our own*. For our own businesses that is
frequently the more useful of the two.

---

## Provenance

Everything here is reconstructed from public sources: *$100M Offers*, *$100M
Leads*, *$100M Money Models*, Acquisition.com's public training and Vantage pages,
and published attendee write-ups of the $35–45k Scaling/VAM workshops. Sources are
listed in `research/acq-vantage-teardown.md`.

Thresholds like LTGP:CAC > 3 are rules of thumb, not laws — the commands are
instructed to say so rather than present them as physics.
