# Funnel benchmarks by business model

Salvaged from the retired `web/src/lib/engine.ts`. The generic funnel numbers in
`04-marketing-ops.md` are the Services column of this table — applying them to an
ecommerce or info business produces a confidently wrong verdict, which is the
most expensive kind.

**Pick the row before judging any conversion rate.**

| Model | Visit→opt-in | Opt-in→booked | Booked→showed | Showed→closed | End-to-end |
|---|---|---|---|---|---|
| **Services** | 30% | 40% | 70% | 25% | 2.1% |
| **Local** | 30% | 45% | 75% | 30% | 3.0% |
| **Info** | 35% | 30% | 60% | 15% | 0.95% |
| **SaaS** | 25% | 30% | 70% | 20% | 1.05% |
| **Ecommerce** | 20% | 15% | 90% | 40% | 1.08% |
| **Product** | 25% | 30% | 80% | 30% | 1.8% |
| **Other** | 30% | 40% | 70% | 25% | 2.1% |

End-to-end is the product of the four steps — the share of visitors who become
customers. Compare a business to its own row's end-to-end before concluding the
funnel is broken.

## How to use these

1. **Classify the model first.** Ecommerce "booked/showed" maps to cart→checkout,
   not calls. If the stages don't map cleanly, say so rather than forcing the fit.
2. **Find the weakest step relative to its own benchmark**, not the lowest raw
   percentage. A 40% showed-rate against a 90% ecommerce benchmark is a worse
   failure than a 20% close rate against 25%.
3. **Compute the prize before prescribing.** Lifting the weakest step to benchmark
   multiplies the whole funnel by `benchmark ÷ actual` — that multiple is the
   number that justifies the work.
4. **Fix one step at a time.** Parallel changes destroy attribution.

## Health of these numbers

Rules of thumb from published material, not measured across a portfolio. Say so
when one is load-bearing in a diagnosis. Any model not listed is provisional —
and when we measure our own funnels, `knowledge/experience/` overrides this file.
That substitution is the whole point: their edge is 1,000+ measured companies,
and ours can only be our own measured results accumulating over time.
