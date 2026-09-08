# SupaDatewise first paid merchant plan

Prepared: 2026-08-30
Commercial objective: obtain the first real, processed, non-refunded paid
Shopify subscription from a non-development merchant and reconcile it to
first-party Shopify provider evidence.
Planning classification: **Tier 3 controlled validation**, subject to Darren's
confirmation before any external action.
Decision owner: **Darren**.
Launch date: **NOT SCHEDULED**. `D0` below means the time of the first separately
approved merchant invitation, not the date of this document.

This is an approval packet, not authority to deploy, edit the listing, publish,
contact anyone, submit a form, change an account, spend money, or create an
automation. No target names, contact details, messages, or sends are included.

## 1. Commercial truth at baseline

Evidence date: refreshed 2026-09-08. Public facts come from the Shopify App Store and live
Fly URLs. Private commercial facts come from the current first-party Shopify
provider evidence supplied for this plan.

| Fact | Verified baseline | Source and interpretation |
|---|---:|---|
| Public listing | Live | Shopify App Store listing for `SupaDatewise: Delivery Date` |
| Public price | $6.99/month; seven-day trial | Shopify App Store; pricing has not been changed |
| Public reviews | 0 | Shopify App Store; this is not an install count |
| Current installed stores | 1 development store only | Shopify provider evidence; QA state, not a merchant sale |
| Verified paid merchant installs | **0** | Shopify provider evidence; exact first-party zero, not an estimate |
| Provider earnings | **$0** | Shopify provider earnings evidence; exact first-party zero, not an estimate |
| Listing discoverability | Product content page 18, intra-position 8 | Direct Shopify category response; a weak high-intent discovery signal |
| High-intent category visibility | Absent from page one of Delivery and pickup | Direct Shopify category response |
| Listing views and install starts | **UNAVAILABLE** | No authenticated App Store analytics export in the evidence packet |
| Public support | **LIVE branded route** | Fly `/support` returns 200; listing still needs a separately approved support URL/contact edit |
| Public privacy | **LIVE route; operational proof blocked** | Fly `/privacy` returns 200, but v7 access logs contain authenticated query strings and the listing still points to the older Telegraph policy |
| Production release identity | Fly `v7`, image `5fbbef8e248d0215da02e8d7f8765929ffb2149c-preflight`, manifest `sha256:f51f123fda499ce1141d74d9f1e95f7ddcc9a3ad2c1e1c94285a20ef3dfd48af`; active Shopify version `1056076529665` | Exact Fly source/image is live with v6 retained, but fails the new access-log privacy gate. The Shopify proxy/theme version remains unreleased |

Traffic, listing position, permissions, replies, calls, installs, development-
store plans, $0/test subscriptions, trial starts, screenshots, drafts, forecasts,
and pipeline are never sales or revenue. A provider-visible active subscription
without a processed, non-refunded charge is a paid-conversion lead, not terminal
commercial proof. Cash is counted only when the corresponding provider earnings
or payout evidence clears.

## 2. Offer and merchant fit

### Narrow initial segment

Small direct-to-consumer Shopify stores that:

- use an Online Store 2.0 theme;
- have one main dispatch schedule and store timezone;
- currently answer “when will this arrive?” manually or leave it unclear;
- can verify their own processing time, cutoff, working days, and shipping
  window; and
- explicitly opt in to evaluate the app on their own store.

### Not for this validation

- multi-location, carrier-rate, ZIP/postcode, inventory-aware, or variant-level
  delivery promises;
- stores that need checkout delivery slots or a guaranteed carrier date;
- merchants seeking a free permanent plan;
- any merchant who has not explicitly given permission to be contacted; or
- any store whose theme cannot support the reviewed app block.

### Factual message foundation

**Headline:** Give shoppers a delivery window that follows your store's real
dispatch clock.

**Support line:** SupaDatewise applies your processing time, daily cutoff,
working days, shipping window, wording, date format, and store timezone in a
lightweight Shopify theme block.

**Thirty-second description:** For small Shopify stores with one clear dispatch
schedule, SupaDatewise displays a merchant-configured estimated delivery window
on supported product and cart sections. It uses Shopify App Pricing at $6.99 per
month after a seven-day trial. It is an estimate, not a carrier guarantee, and
the current version does not offer complex location, inventory, or variant
rules.

Do not use “increases conversion,” “guaranteed accurate,” “works with every
theme,” “privacy certified,” or any social-proof claim until first-party
evidence supports it.

## 3. Readiness and decision gates

Acquisition remains suppressed until every gate through G5 is evidenced. A gate
passing in local code does not prove it passed in Shopify or production.

| Gate | Evidence required | Authority boundary | Current state |
|---|---|---|---|
| G0 — source | Reviewed commit, clean branch, passing tests/build/security checks, rollback notes | Local work only | **PASS:** commit `5548e96343092082ac460ded003846eb316f2684` passes fresh install, 62 tests, lint, typecheck, production build, Shopify app/theme build, secret scan, zero-runtime-vulnerability audit and independent review; exact build-only image and manifest are recorded in G3 |
| G1 — provider identity | Authenticated App Home handle, Partner organization ID, app GID, pricing/subscription source, and exact current app identity | Approval immediately before any token, secret, permission, or setting change | **PASS FOR FLY:** handle `estimated-delivery-date-34`, Partner organization `4774175`, app `gid://shopify/App/396333842433`; least-privilege provider values are deployed to Fly v7. Shopify scope/proxy release remains gated |
| G2 — controlled store | Fresh development-store install/reauthorization; hosted plan selection; settings save; store-timezone calculation; Dawn plus one other compatible theme; first render; cancellation/fail-closed; signed uninstall/privacy webhooks | Approval immediately before provider configuration or any controlled-store mutation | **BLOCKED ON G3:** existing dev install and $0 Standard contract open Fly v7; saved rules, `America/New_York` timezone, Settings and Setup guide render. Do not reauthorize or continue lifecycle QA until path-only access logging is live and verified |
| G3 — release | Reviewed PR commit, Fly release ID, Shopify app-version ID, live smoke test, previous release/version retained | Exact approval immediately before deployment or app-version release | **AWAITING EXACT FLY GATE:** v7 functions but its stock logger records authenticated query strings; v6 used the same logger. Reviewed image `registry.fly.io/edd-supadesign:5548e96343092082ac460ded003846eb316f2684-preflight`, manifest `sha256:07d8f34b4b0265ce8a131e7eeed0fb3a3515a68b07599397b02d24b541e0aa1c`, is build-only and not running. Shopify version 10 remains active |
| G4 — public trust | Live `/support` and `/privacy` return 200; support owner/inbox selected; policy matches actual data lifecycle; old Telegraph link replacement ready | Exact approval immediately before listing edit/publication | **BLOCKED:** branded support/privacy routes return 200, but provider access logs are not yet clean privacy evidence. Listing URL/contact edits and final support-owner decision remain gated |
| G5 — measurement | Event definitions below implemented only as approved, privacy reviewed, test events excluded, provider sources readable | Approval before adding or enabling new live collection | **PARTLY MISSING** |
| G6 — validation cohort | Maximum-five qualification rules, exact named recipients, permission source, final message, sender, and send time | Exact action-time approval immediately before each merchant message or batch | **NO TARGETS; NO SEND AUTHORITY** |
| G7 — channel expansion | One verified paid merchant, cleared earnings evidence, activation/accuracy/support review, and a written continue/iterate/stop decision | Separate approval for any larger cohort, partner programme, review request, or automation | **NOT ELIGIBLE** |

Any auth, billing, entitlement, timezone, render, privacy, webhook, or rollback
failure returns the plan to G1–G3. Do not compensate with more traffic.

## 4. Privacy-safe measurement contract

### Data minimisation

If approved, store only a UTC timestamp, unique event ID, app/release version,
experiment cohort code, event name, event source, test flag, and a server-side
keyed pseudonymous installation identifier. Keep the raw Shopify shop domain in
the operational session store only where Shopify requires it; do not copy it
into analytics.

Never collect shopper names, customer IDs, email addresses, phone numbers,
shipping/billing addresses, order or cart contents, product titles, raw product
URLs, free-text messages, IP-derived location, or user-agent fingerprints. A
Shopify app-proxy request can contain `logged_in_customer_id`; ignore it and do
not log it. Separate any operational consent record from product analytics.

`first_widget_rendered` is not implemented proof today. If a dedicated signed,
pseudonymous, once-per-installation beacon is not privacy-reviewed and approved,
leave the metric **MISSING** rather than infer it from page traffic or an
entitlement request.

### Metric definitions and windows

All experiment windows use UTC. Enrollment is capped at `D0` through `D+14`.
Activation is assessed within 48 hours of each completed install. Paid
conversion is assessed through 14 days after each trial starts. Retention is
checked 30 days after a processed charge. Cash reconciliation runs from the
charge through the first Shopify earnings/payout statement that covers it, with
a reporting checkpoint at `D+60`; if Shopify has not yet issued that evidence,
the value is **UNAVAILABLE**, not zero.

| Metric/event | Exact definition | Trusted source | Baseline refreshed 2026-09-08 | Evaluation window |
|---|---|---|---|---|
| `permission_granted` | Merchant explicitly opts in to receive the validation invitation | Dated permission record approved for the test; not product analytics | No cohort assembled | `D0`–`D+14` |
| `listing_view` | Shopify counts a SupaDatewise listing detail-page view | Shopify App Store/Partner analytics only | **UNAVAILABLE** | Baseline immediately before `D0`; cumulative through `D+14` |
| `install_start` | Shopify records an installation/OAuth attempt for a selected store | Shopify provider analytics/event | **UNAVAILABLE** | Within 24 hours after each approved invitation |
| `install_completed` | First valid production offline session is stored for a non-development shop | Signed Shopify OAuth plus session database, reconciled to provider install | One development-store install only; merchant count 0 | Within 48 hours of `install_start` |
| `onboarding_completed` | First successful `$app.settings` save with no GraphQL user errors and an active entitlement | Authenticated app action plus Admin GraphQL result | Event **MISSING** | Within 48 hours of `install_completed` |
| `theme_block_active` | Shopify reports the SupaDatewise block active on the merchant-selected published theme | Authenticated Shopify theme/app-extension evidence | **MISSING** | Within 48 hours of `install_completed` |
| `first_widget_rendered` | First successful visible render for one installation and app version, deduplicated server-side | Approved signed pseudonymous beacon; merchant screenshot may support QA but is not analytics | **MISSING** | Within 48 hours of `install_completed` |
| `trial_started` | A non-development shop's Shopify App Pricing subscription enters a real trial | Shopify subscription/provider evidence | **UNAVAILABLE**; development-store plan excluded | From install through seven days |
| `paid_conversion` | A non-development subscription moves from trial to a paid/active provider state | Shopify Partner subscription evidence | 0 verified paid installs | Through 14 days after trial start |
| `verified_paid_install` | `paid_conversion` plus a corresponding Shopify-processed, non-refunded app charge/earnings record for the same non-development installation | Shopify subscription plus earnings/transaction evidence | **0** | Through `D+60`, or earlier when evidence clears |
| `churn` | A verified paid installation cancels, expires, freezes, or uninstalls, with effective UTC time | Shopify subscription evidence plus signed uninstall webhook | **UNAVAILABLE** | Charge date through day 30 |
| `cleared_revenue` | Shopify earnings/payout record for the charge is processed and remains non-refunded | Shopify earnings/finance/payout evidence | **$0** | Charge date through covering statement; checkpoint `D+60` |
| `support_incident` | One merchant-reported issue classified by severity and resolved/unresolved state, without copying free-text PII into analytics | Manual support ledger | **MISSING** | Install through day 30 |

Test stores, development-store plans, internal events, retries, and duplicate
events must carry `test=true` and be excluded from every commercial numerator
and denominator.

## 5. Maximum-five merchant validation experiment

This is a sequential feasibility test, not an A/B test. Five merchants cannot
support statistical significance, a reliable conversion rate, or a conversion-
lift claim.

**Hypothesis:** If the full install-to-render path and public trust gates are
proven, then a factual, permission-based invitation to at most five qualified
small DTC merchants will produce at least two verified activations and at least
one `verified_paid_install`, because the segment values a delivery window that
follows its real store-timezone dispatch schedule.

**Primary metric:** `verified_paid_install` count.
**Guardrails:** zero severe accuracy/auth/billing/privacy incidents; no more than
one unresolved support incident per participating merchant; zero unauthorised
scope, store, message, data, spend, or review actions.
**Required sample:** maximum five qualified, permissioned merchants. This cap is
operational, not statistically powered.
**Run time:** enrollment for at most 14 days; per-merchant trial follow-up for 14
days; cash/retention reconciliation through `D+60` if needed.
**Sequence:** invite one merchant; verify install, onboarding, theme activation,
and first render before inviting the next. Do not expose the full cohort to a
broken funnel.

### Pre-declared thresholds

| Result | Threshold | Decision |
|---|---|---|
| Commercial success | At least one `verified_paid_install` from the capped cohort | Record first paid proof; reconcile cleared revenue; do not scale until 30-day retention and support review |
| Cash confirmed | `cleared_revenue` becomes greater than $0 for that same installation and remains non-refunded | Terminal cash proof for this experiment |
| Activation pass | At least two non-development installs complete onboarding, activate the block, and show a verified accurate render | Continue the capped test if guardrails remain green |
| Acquisition/message failure | Five permissions but zero completed installs within 48 hours of each invitation | Stop; review fit, message, listing trust, and installation friction; do not widen the cohort |
| Onboarding failure | At least three completed installs but fewer than two verified renders | Stop acquisition; fix product/onboarding/theme compatibility |
| Value failure | At least two verified renders, but zero merchants choose to remain paid after the full trial window | Stop; interview with permission and change wedge/value before more outreach |
| Cash pending | Active paid state exists but processed/non-refunded earnings or payout evidence is not yet available | Report **UNAVAILABLE/pending**, never a sale or cleared revenue |
| Inconclusive | Fewer than three qualified merchants opt in by `D+14` | End without a conversion claim; reassess the permission channel, not the product conversion rate |

### Immediate stop conditions

- any install loop, invalid callback, entitlement bypass, unexpected charge, or
  provider mismatch;
- any delivery date that disagrees with the merchant-approved schedule;
- the block renders without a valid active entitlement or persists after
  cancellation beyond the reviewed cache window;
- any support/privacy URL fails, policy conflicts with actual collection, or
  personal data appears in analytics/logs;
- two merchants report the same correctness or theme-compatibility defect;
- one unresolved severe incident or support load above one unresolved issue per
  participating merchant; or
- any action would require new scopes, a live-store edit by the operator,
  unapproved contact, spend, incentive, review request, or automation.

## 6. Zero-spend acquisition routes after readiness

### A. Shopify App Store optimisation

Prepare locally, then request exact listing-edit approval for one factual batch:

1. replace the old Telegraph policy with the verified branded `/privacy` URL;
2. expose the selected support inbox and verified `/support` page;
3. use the approved listing packet and real current UI screenshots;
4. request the most accurate delivery/estimated-arrival category placement if
   Shopify permits it; do not misclassify the app to gain visibility;
5. state the $6.99 price, seven-day trial, OS2 compatibility limits, and estimate-
   not-guarantee language clearly; and
6. remove any unsupported conversion, universal-theme, accuracy, or social-
   proof claim.

Do not count a listing edit, page view, keyword position, or install button click
as revenue.

### B. Help, SEO, and answer-engine content

Drafts may be prepared locally. Each publication needs exact approval and live
verification. Prioritise pages that resolve installation and accuracy objections:

1. “How Shopify estimated delivery dates use processing time and shipping time”;
2. “How order cutoffs, weekends, and working days change a delivery window”;
3. “Why the store timezone matters for a Shopify delivery cutoff”;
4. “How to preview a Shopify theme app block before publishing it”; and
5. “Estimated delivery date versus carrier delivery guarantee.”

Each page should use original examples, link to support/privacy and the App
Store listing, state product limits, and include only valid FAQ/schema markup.
Indexing, impressions, clicks, and assisted installs remain leading indicators.

### C. Legitimate partner channels

Prepare a one-page compatibility and handoff guide for opt-in introductions from:

- Shopify theme developers who support Online Store 2.0 app blocks;
- Shopify-focused agencies already helping small DTC stores with product-page
  clarity;
- ecommerce operations consultants who encounter delivery-certainty questions;
  and
- existing professional relationships willing to make a permission-based
  introduction.

No scraped directories, personal-data enrichment, purchased lists, bulk cold
email, unsolicited direct messages, affiliate or commission promise, co-
marketing commitment, or partner form submission is authorised. Commercial
terms require a separate decision.

### D. Permission-based merchant validation

The maximum-five cohort may come only from Darren's existing relationships or
explicit opt-in introductions. Before any send, provide Darren with the exact
named recipient, proof of permission, final copy, sender, timing, and expected
follow-up for action-time approval. This document deliberately contains none of
those items.

Ask for an honest evaluation of setup, calculation, theme placement, and value;
never ask for a positive review, offer an incentive for a review, or condition
support on feedback. Any later honest-review request needs separate approval and
must follow Shopify policy.

## 7. Approval-ready operating sequence

| Phase | Activity | Owner | Due relative to launch | Status |
|---|---|---|---|---|
| Product | Finish independent review and preserve the tested source/release rollback reference | SupaDatewise engineering | Before G1 | In progress outside this document |
| Provider | Release only the reviewed single scope, proxy and theme-extension configuration after the verified identity baseline | Darren | G1 | Fly secret deployment complete; Shopify version release and controlled-store reauthorization are blocked until redacting access logs are live |
| QA | Complete the development-store lifecycle and two-theme test | Darren with SupaDatewise engineering | G2 | Existing install, $0 plan, embedded v7 settings/setup and prior unsaved Savor preview verified; remaining steps paused on the logging fix |
| Release | Deploy only the reviewed commit and retain Fly/Shopify rollback IDs | Darren | G3 | Fly v7 is live but fails the access-log privacy gate; a reviewed replacement image requires exact approval. Shopify version 10 remains active |
| Support/privacy | Select support owner/inbox; verify branded pages; reconcile policy | Darren | G4 | Branded pages are live; access-log remediation, support owner and listing edit remain **MISSING** |
| Listing | Apply one reviewed factual listing batch | Darren | After G4 | Approval required |
| Measurement | Enable only approved events and validate test exclusion/deduplication | SupaDatewise engineering | G5 | Partly missing |
| Merchant test | Approve exact recipient and copy, then invite sequentially up to five | Darren | `D0`–`D+14` | No targets; no sends |
| Support | Triage each participating merchant before the next invitation | Darren or named support owner | During test | Owner beyond Darren **MISSING** |
| Finance | Capture subscription, earnings/refund, and payout evidence for the same installation | Darren | Paid event through `D+60` | Baseline $0 |
| Decision | Write continue/iterate/stop result against pre-declared thresholds | Darren | By `D+60` or earlier hard stop | Not started |

### D0 checklist

- [ ] G0–G5 evidence is complete and current.
- [ ] Live listing, support, privacy, install, pricing, settings, theme render,
      cancellation, uninstall, and rollback checks pass.
- [ ] Baseline provider snapshot records one development-store install, zero
      verified paid installs, and $0 earnings.
- [ ] Test stores and internal events are excluded.
- [ ] Exact merchant permission, recipient, copy, sender, and time are approved.
- [ ] No contact list, bulk send, paid channel, incentive, review request, or
      recurring automation is active.
- [ ] A named human is ready to handle support before the next invitation.

## 8. Reporting format and decision discipline

At each manual checkpoint, report separate rows for:

1. provider-confirmed listing views and install starts, or **UNAVAILABLE**;
2. completed non-development installs;
3. onboarding completions, active blocks, and verified first renders;
4. real trials and paid states, excluding development/test plans;
5. `verified_paid_install` evidence;
6. processed, refunded, pending, and cleared earnings separately;
7. churn/retention state;
8. support incidents and accuracy failures; and
9. the next gate or stop decision.

Never backfill missing provider data with an estimated zero. Never combine
pipeline and cash. Do not change thresholds after seeing results, stop early
because a leading metric looks encouraging, or claim a conversion rate from a
five-merchant feasibility cohort.

## 9. Next approval boundary

**Merchant acquisition is not yet eligible for approval.** Fly v7, the branded
trust pages, provider identities and the $0 development contract function, but
v7's stock access logger records authenticated Shopify query strings. The next
external gate is therefore only deployment of the exact reviewed path-only
logging image `registry.fly.io/edd-supadesign:5548e96343092082ac460ded003846eb316f2684-preflight`,
manifest `sha256:07d8f34b4b0265ce8a131e7eeed0fb3a3515a68b07599397b02d24b541e0aa1c`,
to the existing `edd-supadesign` app while retaining v7 and v6 rollback
references. That gate does not include the Shopify configuration release. After live log verification,
the separately approved Shopify/development-store action can release only
`write_app_proxy`, `/apps/supadatewise` and the reviewed theme extension,
reauthorize only the controlled store, and prove the signed entitlement-to-render
lifecycle without saving the live theme. Theme save, listing edit, measurement
and merchant contact remain later separate gates.
