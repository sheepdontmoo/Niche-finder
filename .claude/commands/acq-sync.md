---
description: Pull live Meta Ads numbers into business-context so the diagnosis stops running on typed data.
argument-hint: [business, or blank for all]
---

# /acq-sync — refresh the numbers

Target:

> $ARGUMENTS

Blank means every business with a file in `business-context/`.

The stated weakness of this whole system is that the diagnosis is automatic but
every number reaching it is typed by hand. This command closes the half of that
gap we can actually close today.

## What is automatable right now

| Source | Status | Gives |
|---|---|---|
| **Meta Ads** (Pipeboard MCP) | Connected, verified | Spend, impressions, clicks, leads, CPL, CPM, CTR per campaign |
| **Page speed** | Fetchable | Landing page load time |
| **Accounting** | **Not connected** — no software in use | Revenue, gross profit, FECC |

Revenue, margin and payroll stay manual by the operator's decision: set by hand,
quarterly. Do not invent them and do not nag about them on every run.

## Process

### 1. Resolve accounts to businesses
Call `mcp__Pipeboard_Meta_Ads__get_ad_accounts`. **Account names lie.** The
account named "Darren Buckley" is 281SPORT (check `default_dsa_payor`), and it
carries campaigns for three unrelated businesses.

Attribute at the **campaign** level, by campaign name, never by account. Any
campaign you cannot confidently attribute goes in an `UNATTRIBUTED` bucket and is
reported as such — a misattributed campaign is worse than an unattributed one,
because it silently credits one business with another's leads.

### 2. Pull insights
`mcp__Pipeboard_Meta_Ads__get_insights` at `level: "campaign"`. Pull both
`maximum` (lifetime) and `last_30d`. Lifetime tells you whether a channel was
ever really run; 30-day tells you what is true now. Both matter — a channel with
healthy lifetime numbers and zero recent spend is a paused channel, not a failed
one.

Read `spend`, `impressions`, `clicks`, `ctr`, `cpm`, and `actions[]` where
`action_type` is `lead` or `onsite_conversion.lead_grouped`, plus
`cost_per_action_type` for CPL.

### 3. Sanity-check before writing
State these plainly — they are usually the finding:

- **Spend far below any real test.** A few hundred over several years is not a
  channel; it's a handful of micro-tests. Say so rather than analysing CPL to two
  decimal places.
- **CPM far above normal** (roughly 3× the account's other campaigns) means the
  audience is too narrow to deliver. Name it.
- **Zero spend attributable to the business being diagnosed.** This is the single
  most important thing this command can surface, and it is easy to miss when
  looking at an account total.
- **Leads recorded but no downstream data.** Meta's lead count is not a customer
  count. Never let a lead number stand in for a sale.

### 4. Write back
Update the **Funnel** section of each `business-context/<slug>.md`:
spend by platform, leads/month, CPL, and channels genuinely running. Preserve
every `NOT TRACKED` you cannot actually fill. Stamp the file with the sync date.

Never overwrite the operator's own entries — offer, price, goals, constraints —
with anything derived. Only the ad-derived fields.

### 5. Report the delta
What changed since last sync, and whether any change flips a verdict. A channel
that just crossed into real spend, or a CPL that doubled, is worth a sentence.
If nothing moved, say "no material change" and stop.

## Rules
- Attribute by campaign name, never by account name.
- Lifetime *and* 30-day. One without the other misleads.
- Leads are not customers.
- An unattributable campaign gets reported, not guessed at.
- If the money side comes up, note once that no accounting software is connected
  and move on.
