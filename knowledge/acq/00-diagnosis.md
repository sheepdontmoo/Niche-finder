# The Diagnosis — constraint identification

> This is the core of the whole system. ACQ AI is described by Acquisition.com as
> "the diagnosis we run on portfolio companies, turned into software." Everything
> else in this knowledge base is a *solution set*; this file is the *triage*.

**Rule: never prescribe before diagnosing.** The most common failure is fixing a
part of the business that isn't the constraint. Work the ladder in order.

---

## The constraint ladder

A business has exactly one binding constraint at a time. Find it in this order —
each rung assumes the one above it is healthy:

| # | Constraint | Symptom | You have this if... |
|---|---|---|---|
| 1 | **Leads** | Nobody is booking / nothing in pipeline | Volume in is too low to hit target even at good conversion |
| 2 | **Sales** | Leads come in, don't convert | Traffic is fine; close rate or show rate is the leak |
| 3 | **LTV** | Customers buy once, don't stay, don't expand | Churn high, no repeat, no upsell, underpriced |
| 4 | **Margin/Cash** | Growing but cash-poor | Payback too slow to reinvest; GP too thin to fund CAC |
| 5 | **People/Ops** | Owner is the bottleneck, quality varies | Growth stalls because delivery/management can't absorb it |

**Bottleneck ping-pong:** fixing one constraint immediately exposes the next.
This is expected and is the actual mechanism of scaling — plan for the *next*
constraint while fixing the current one. (E.g. fix conversion → sales team
becomes the constraint → hire → marketing becomes the constraint again.)

---

## The numbers that reveal the constraint

Ask for these before advising anything. If the operator can't answer, that
inability is itself the first finding.

### Core metrics

- **CAC** — Customer Acquisition Cost. Total sales + marketing spend ÷ new
  customers acquired in the same period. Must include labour, not just ad spend.
- **Gross Profit (GP)** — revenue minus cost of delivering it. *Always reason in
  gross profit, not revenue.* Revenue is vanity; GP is what funds CAC.
- **LTGP** — Lifetime Gross Profit per customer. Not lifetime revenue.
- **FECC** — Front-End Cash Collected. Cash actually in the bank from a customer
  in the first ~30 days.
- **Payback period** — how long until a customer has returned their own CAC in
  gross profit.

### Threshold rules

| Ratio | Threshold | Meaning if you fail it |
|---|---|---|
| **LTGP : CAC** | **> 3** | Below 3, growth doesn't compound — you're buying customers at near-cost. Below 1, growth actively destroys the business. |
| **FECC : CAC** | **> 2** | Below 2, you can't collect enough cash up front to re-fund acquisition — growth is capped by your balance sheet, not by demand. |
| **Payback period** | **≤ 30 days** | The goal is recouping CAC within 30 days so you can recycle the same dollar every month. This is the engine of "unlimited" ad spend. |

**The key insight:** if payback ≤ 30 days and LTGP:CAC > 3, growth becomes a
financing question, not a marketing question — you can spend as fast as you can
collect. Most businesses that "can't afford ads" have a money-model problem, not
an ad problem.

---

## The four levels of growth ($0 → $100M)

Advice must match the level. Giving $10M advice to a $500k business is the most
common way to break one.

| Stage | Focus | Do NOT |
|---|---|---|
| **$0–$1M** | Validate the core product/service. One offer, one channel, one avatar. | Diversify, hire, or build systems |
| **$1–$3M** | Avoid distraction. Double down on the one thing that already works. | Add new offers or channels |
| **$3–$10M** | Build and empower a team. Move from doing to managing. | Stay the sole operator |
| **$10–$100M** | Hire subject-matter experts per department. | Generalists in senior seats |

---

## Diagnostic sequence to run

1. **Intake.** Pull the numbers above plus: offer(s) and price, channels in use,
   headcount by function, current revenue and GP, stated goal and timeline.
2. **Compute the ratios.** LTGP:CAC, FECC:CAC, payback period. State them plainly.
3. **Locate the level** ($0–1M, $1–3M, $3–10M, $10–100M).
4. **Walk the constraint ladder** top-down and name the ONE binding constraint.
   Say why the rungs above it are not the constraint.
5. **Quantify the prize.** Model what fixing it is worth. (E.g. "landing page
   converts at 35%; at 50% that's +42% to the business, with zero extra traffic.")
6. **Prescribe from the matching playbook** — and only that playbook.
7. **Name the next constraint** that this fix will expose.

---

## Hidden constraint to always check: conversion rate

Operators reflexively chase more traffic while ignoring funnel efficiency. Before
recommending any lead-gen work, check page-level and stage-level conversion:

- Weekly CRO testing on the weakest page can produce 30–40% revenue growth with
  no additional traffic.
- Target ~50% *relative* improvement per test.
- 80% of optimization effort belongs above the fold, desktop and mobile.
- Page speed: roughly 7% revenue lost per 1 second of load delay. Audit it.

Buying more traffic to feed a leaky funnel is the single most expensive mistake
available to a business with money.
