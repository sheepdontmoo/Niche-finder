# Experience — our own measured results

Acquisition.com's actual edge is not the frameworks. Those are in three published
books and reconstructed in `knowledge/acq/`. Their edge is **pattern recognition
across 1,000+ portfolio companies** — 50,000 hours of consulting, or "$31M of
consulting across 1,026 businesses" depending on which ad you read.

We cannot buy that and we cannot fake it. The only honest substitute is to
accumulate our own, from our own businesses, one measured outcome at a time.

**This directory is that corpus. It starts empty and is worth nothing until it
isn't.**

## What goes in

One file per entry, named `YYYY-MM-DD-<slug>.md`:

- **Campaign results** — what was run, what it cost, what it returned. Including,
  especially, the ones that failed.
- **Diagnoses and what actually happened next.** A diagnosis nobody followed up
  is a guess with good formatting.
- **Call recordings and notes** — real objections in the prospect's real words.
- **Post-mortems** — what was believed, what turned out true, what it cost to
  find out.
- **Measured funnel rates** from our own businesses. These override the rule-of-thumb
  benchmarks in `07-benchmarks.md` the moment we have enough of them.

## The rule that makes this worth keeping

**Record the prediction before the outcome, and never edit it afterwards.**

An entry that only records what happened teaches nothing — hindsight will
reconstruct a story where the result was obvious. An entry that records what we
expected, and was wrong, is the only thing here with real information in it.

Write the prediction at diagnosis time. Come back and append the outcome. Leave
the wrong prediction standing.

## Entry shape

```markdown
# <what this is> — <business>

**Date:**
**Constraint diagnosed:**
**Prediction:** what we expected, with the number and the date
**Action taken:**

---

## Outcome (filled in later — do not edit the prediction)

**Date checked:**
**What actually happened:** with numbers
**Was the prediction right?** honestly
**What this changes:** which file in knowledge/acq/ is now wrong, if any
```

## How the commands use this

`/acq` and `/acq-ask` should read this directory when a business has entries, and
weigh a measured result from our own business above a published rule of thumb.
When the two conflict, **our own data wins and the conflict gets stated out loud.**
