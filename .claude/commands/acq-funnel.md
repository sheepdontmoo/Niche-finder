---
description: Design or repair the funnel — stages, conversion targets, and where the money is leaking.
argument-hint: <business, or the funnel to fix>
---

# /acq-funnel — funnel design

Working on:

> $ARGUMENTS

The remaining artefact from ACQ AI's internal prompt set. Read
`knowledge/acq/04-marketing-ops.md` and `knowledge/acq/02-leads.md`. Read
`knowledge/acq/03-money-models.md` if the funnel has to pay for itself quickly.
Read the matching `business-context/` file if one exists.

## Diagnose before designing

If a funnel already exists, **do not redesign it.** Find the leak and fix that.
Rebuilding a funnel that converts at 2% end-to-end usually produces a different
funnel that converts at 2%, having cost a month.

Map the current stages and get the conversion rate at each:

```
Traffic → Landing → Opt-in → Booked → Showed → Closed → Delivered → Repeat
```

Then compute the **prize per stage**: what lifting that one step to benchmark
does to the whole. Fix the biggest prize first. One step at a time — parallel
changes make attribution impossible and you learn nothing.

## Benchmarks (defaults; tune per model)

| Step | Benchmark |
|---|---|
| Visit → opt-in | 30% |
| Opt-in → booked | 40% |
| Booked → showed | 70% |
| Showed → closed | 25% |

End-to-end is the product of these — roughly 2.1%. A funnel far below that has
one bad step, not four. Find it.

## Designing a new funnel

### 1. Start from the money, not the pages
What must be true for this to be profitable? Work backwards:
- Target CAC — from LTGP, keeping LTGP:CAC above 3
- Required close rate and traffic cost to hit it
- Whether payback lands inside 30 days (`/acq-money`)

A funnel that can't clear those numbers is a bad funnel regardless of how it
looks. Establish this **before** designing pages.

### 2. Choose the shortest path that qualifies
Every stage costs conversion. Add a stage only when it earns its place:
- **High ticket** → opt-in → booked call → showed → closed
- **Mid ticket** → VSL → application → close
- **Low ticket** → direct-to-offer, no call
- **Ecommerce** → product page → cart → checkout, with the upsell at point of sale

More stages means more qualification and less volume. Match the stage count to
price point, not to what looks sophisticated.

### 3. Build each stage against the Value Equation
Every page raises Dream Outcome or Perceived Likelihood, or lowers Time Delay or
Effort. A page doing none of those should be deleted.

Opt-in page structure: **headline → video/image → testimonials**, above the fold
where possible. Incomplete-information bias (blurred assets, locked video) lifts
opt-ins.

### 4. Attach the money model
The funnel isn't done at "closed":
- **Upsell at point of sale** — highest-intent moment there is
- **Downsell on every no** — that lead is already paid for
- **Continuity** — what recurring need does the outcome create?

A funnel without these is leaving the money that funds the traffic.

### 5. Instrument it
Name the metric at every stage and where it gets recorded. **A funnel you can't
measure per stage cannot be diagnosed**, and you'll be back to guessing which
step is broken.

## Output

Stage-by-stage table: stage → purpose → current rate → benchmark → gap → prize if
fixed. Then the ordered fix list, one change at a time, each with the metric and
the review date. For a new build: the full stage map with target rates and the
unit economics that must hold.

## Rules
- Fix the leak before adding traffic. Always.
- One change at a time or you learn nothing.
- Fewer stages beats more unless a stage qualifies or raises value.
- Load speed first — ~7% of revenue per second of delay, and it's the cheapest fix.
- If the numbers can't clear LTGP:CAC > 3, say the funnel isn't the problem.
