# SupaDatewise paid-install readiness — evidence and launch packet

**Next gate superseded, 2026-09-08:** the subsequent entitlement-lifecycle
review found that the theme cached access for the lifetime of an open page.
The approved source fix now requires a replacement server response with a
bounded remaining lifetime, followed by the matching theme extension. See
[`ENTITLEMENT_RELEASE_GATE_2026-09-08.md`](./ENTITLEMENT_RELEASE_GATE_2026-09-08.md)
for current validation limits and the server-first release gate. Fly v8 and
Shopify v10 below are retained historical provider receipts; neither was
changed or independently reverified during this source remediation.

Evidence window: 2026-08-30 10:02–10:10 UTC for public checks, through
10:39 UTC for local verification, through 15:30 UTC for authenticated
Shopify/Fly reconciliation and controlled development-store billing checks,
refreshed 2026-08-31 for the staged-secret and release preflight, and refreshed
again 2026-09-08 for Shopify commercial evidence, dependency security, the
approved Fly v7 and v8 releases, controlled-store embedded-app verification,
and the access-log privacy remediation gate.

Commercial target: the first real processed, non-refunded Shopify app
subscription.

Current verified paid installs: **0**. Shopify's authenticated app history and
current-install view show no real paid recurring subscription. The sole current
install is Darren's controlled development store.

Current cleared subscription revenue: **USD $0.00**, reverified 2026-09-08.
Shopify Partner Dashboard shows total earnings, recurring charges and last-30-
day payouts of $0.00, with no paid event after the August 30 controlled-store
free subscription.

Traffic, listing visibility, public reviews, draft code, test charges, installs
without provider-confirmed payment, and pipeline are not revenue.

Companion packets:

- [`COMPETITIVE_SCORECARD_2026-08-30.md`](./COMPETITIVE_SCORECARD_2026-08-30.md)
- [`FIRST_PAID_MERCHANT_PLAN_2026-08-30.md`](./FIRST_PAID_MERCHANT_PLAN_2026-08-30.md)
- [`../store-listing.md`](../store-listing.md)

## 1. Immutable source and provider identities

| Identity | Verified value | Evidence/state |
|---|---|---|
| Repository | `sheepdontmoo/Niche-finder` | Git remote refreshed 2026-08-30 |
| Product path | `products/s1-estimated-delivery-date` | Source inspection |
| Preserved dirty checkout | `C:\Users\basde\Desktop\Niche-finder` at `7971931` | Three user-modified files; not touched |
| Clean implementation base | `4e7c2aea5ff1ae4969a0982452598b6ad8f52128` | `agent/seo/supadatewise-weekends-answer-20260830` |
| Isolated branch | `agent/growth/supadatewise-paid-readiness-20260830` | Created from the clean base |
| Shopify app client ID | `26c0cd1cd1992a8d9114c826f47e3e5b` | Local TOML matches public listing install metadata and live App Bridge shell |
| Shopify App Home handle | `estimated-delivery-date-34` | Authenticated App Home/admin route and hosted Shopify plan selector |
| Shopify Partner organization ID | `4774175` | Authenticated Partner Dashboard URL and successful Partner API endpoint; Dev Dashboard organization `208004935` is a different identifier and returns 401 when used as the Partner API organization |
| Shopify app GraphQL ID | `gid://shopify/App/396333842433` | Authenticated Dev Dashboard plus successful app/shop-filtered Partner API queries |
| Controlled development shop | `supadesign-8073.myshopify.com`; `gid://shopify/Shop/79690203390` | Authenticated Dev Dashboard current-install view and Partner API query |
| Partner API client/token | Client `35086`, `SupaDatewise subscription status`; **Manage apps only** | Existing least-privilege client reconciled after exact approval. Its existing token was revealed only long enough to stage the Fly secret, hidden again, and never printed or committed |
| Shopify listing | [SupaDatewise: Delivery Date](https://apps.shopify.com/estimated-delivery-date-6) | Public, HTTP 200, active Install control |
| Fly app/domain | `edd-supadesign` / `https://edd-supadesign.fly.dev` | DNS, TLS, Fly headers, local config, matching App Bridge key |
| Fly production release | Release `v8`; image `registry.fly.io/edd-supadesign:5548e96343092082ac460ded003846eb316f2684-preflight`; manifest `sha256:07d8f34b4b0265ce8a131e7eeed0fb3a3515a68b07599397b02d24b541e0aa1c`; machine `812e3dc95e2728` in `lhr` | Released 2026-09-08 13:17 UTC after exact approval. Prisma found no pending migrations; the server listened on port 3000 and the 15-minute release gate passed |
| Preserved Fly rollback | Release `v7`; image `registry.fly.io/edd-supadesign:5fbbef8e248d0215da02e8d7f8765929ffb2149c-preflight`; older release `v6` / image `registry.fly.io/edd-supadesign:deployment-01KXTEPGG9RBKJNYBM2664B08H` | Both retained for availability rollback. Both use the old full-target logger, so either rollback would reopen the access-log privacy defect |
| Rejected prior candidate | `af0aa574ebcb8a718def2f7451ea92a363fc5e57` | Its image was never released and is rejected after the 2026-09-08 dependency advisory refresh |
| Deployed release source | `5548e96343092082ac460ded003846eb316f2684` | Exact source identity of the recorded Fly v8 release; the subsequent entitlement-expiry source fix is unreleased and requires the replacement gate linked above |
| Rejected Fly preflight image | `registry.fly.io/edd-supadesign:af0aa574ebcb8a718def2f7451ea92a363fc5e57-preflight`; manifest `sha256:9d6a34339698009b1f421a7a81326e02dfe6303c946a3cf5c2fc2b54d0a0f220` | Built but never released. Rejected on 2026-09-08 after new `qs` advisories made its prior zero-runtime-vulnerability result stale; do not deploy this image |
| Released Fly image | `registry.fly.io/edd-supadesign:5fbbef8e248d0215da02e8d7f8765929ffb2149c-preflight`; manifest `sha256:f51f123fda499ce1141d74d9f1e95f7ddcc9a3ad2c1e1c94285a20ef3dfd48af`; 110 MB | Built remotely from the clean replacement commit, then released unchanged as v7. The production dependency prune reported zero known vulnerabilities |
| Redaction production image | Commit `5548e96343092082ac460ded003846eb316f2684`; image `registry.fly.io/edd-supadesign:5548e96343092082ac460ded003846eb316f2684-preflight`; manifest `sha256:07d8f34b4b0265ce8a131e7eeed0fb3a3515a68b07599397b02d24b541e0aa1c`; 110 MB | Deployed unchanged as v8 after exact approval; runtime prune reported zero known vulnerabilities. New access logs passed path-only verification |
| Active Shopify app version | `supadatewise-delivery-date-10`; version `1056076529665` | Authenticated Dev Dashboard; active since 2026-07-18 11:13 UTC |
| Shopify Partner/Dev Dashboard | Organization/account `4774175`; Dev Dashboard organization `208004935`; app `396333842433` | Authenticated browser reconciliation |

The unrelated repository default branch and the dirty desktop checkout are not
safe release bases. Shopify CLI restored theme-extension UID
`1e29f333-f7c8-b03f-d483-a7cdb79cd8f96c31405f` during the verified app build;
it exactly matches the preserved dirty checkout and remained stable on a second
build. Retaining it prevents the reviewed extension from being treated as a
new extension, but it is not an immutable live-release receipt.

## 2. Verified funnel and biggest leaks

| Funnel stage | Current evidence | Status and leak |
|---|---|---|
| App Store listing | Public since 2026-07-20; $6.99/month; seven-day trial; 0 reviews | **LIVE**, but weak proof |
| Discoverability | Product content category has 454 apps; SupaDatewise is page 18, Shopify intra-position 8; absent from page one of Delivery and pickup | **LOW** visibility |
| Listing view | Shopify page is reachable | Counts/conversion **UNAVAILABLE** |
| Install start | Public Install control opens Shopify's login/store-selection flow | Store-specific start count **UNAVAILABLE** |
| Install completion/OAuth | Public `/auth/login` responds; the existing controlled store opens the v8 embedded app through Shopify Admin without an iframe or authentication loop | Existing-session path and path-only logging **PASS**; fresh install/reauthorization remains gated |
| Scopes/permissions | Fly v8 requests only `write_app_proxy` for an HMAC-authenticated storefront entitlement request; listing discloses store-owner contact data associated with the app session | Active Shopify version 10 still lacks the matching scope/proxy configuration, so reauthorization impact remains **UNAVAILABLE** |
| Shopify App Pricing | Public Standard plan is $6.99/30 days with a seven-day trial; controlled dev store can test it for $0 | Shopify hosted selector says `Current`; Partner API reports `EVERY_30_DAYS` with trial end `2026-09-06T15:25:55Z`; history records `SUBSCRIPTION_CREATED`. This is a no-charge development contract, not revenue. One isolated null response recovered to active; the branch now rereads one null only on a signed-in `plan_handle` return and still requires Partner API activation |
| Onboarding | Fly v8 embedded Settings and Setup guide routes opened successfully on the controlled development store | **LIVE** for the existing session; activation-status verification is not yet implemented |
| Settings save | V8 reads the existing app-owned settings and authoritative `America/New_York` timezone | Existing values **VERIFIED READ-ONLY**; a post-release save was not performed and remains controlled-store mutation work |
| Rules reach storefront | Pre-batch Liquid used `shop.metafields.app.settings`, not Shopify's `$app` reserved-namespace syntax | **BROKEN in reviewed source**; fixed locally, with the unentitled fallback removed |
| Cutoff accuracy | Pre-batch renderer used the shopper device timezone despite UI promising shop time | **BROKEN cross-timezone**; fixed locally with Shopify `ianaTimezone` |
| Theme loading | Script declared in schema and loaded again with a manual tag | Duplicate load; fixed locally |
| Theme activation | Published Savor theme initially had no app block | Theme editor successfully found and staged `Estimated Delivery Date`; the unsaved preview rendered a date window. Fly v8 serves the setup/deep-link UI, but the updated Shopify theme-extension version and any theme save remain separately gated |
| First widget rendered | No trustworthy event exists | **MISSING** measurement and live proof |
| Public support | `https://edd-supadesign.fly.dev/support` returns 200 with the reviewed support content | **LIVE**; listing still needs a separately approved URL update |
| Public privacy | `https://edd-supadesign.fly.dev/privacy` returns 200 with the reviewed data-lifecycle disclosure | **LIVE; NEW LOGGING VERIFIED:** v8 recorded 41 post-release requests, including authenticated embedded routes, with zero query strings, credential parameter names or synthetic sentinel values. Historical v7 log retention was not changed. The listing still points to the older policy until a separately approved edit |
| Webhooks | Pre-batch uninstall deletion depended on a session and `shop/redact` only acknowledged; unsigned probes fail closed | Unconditional idempotent shop deletion fixed locally; signed provider delivery **UNAVAILABLE** |
| Current installs/trials | Authenticated current-install view shows only `supadesign`, installed 2026-07-11; hosted selector and Partner API show its no-charge Standard trial | **1 controlled dev-store install; 1 $0 test contract**. Neither is a paid merchant |
| Paid subscriptions/churn/refunds | Authenticated history shows only free/test subscriptions; the one external store seen historically later closed | **0 verified paid subscriptions**; paid churn/refunds are not established because no paid subscription exists |
| Cleared revenue | Partner Dashboard total earnings and event history | **USD $0.00**; no recurring earning event |

### Growth-only shipping case — refreshed 2026-09-08

| Required evidence | Current proof |
|---|---|
| Concrete baseline | Shopify Partner Dashboard: 1 merchant with the app, 0 installs and 1 uninstall in the last 30 days, $0.00 total earnings, $0.00 recurring charges and $0.00 payouts. App history has no event after the August 30 controlled-store free subscription. Fly v8 serves support, privacy, robots, sitemap and llms routes, and the existing controlled-store session opens both embedded app routes with path-only logging. Active Shopify version 10 still has no app proxy or `write_app_proxy` scope |
| Exact target metric | At least 1 `verified_paid_install`, followed by `cleared_revenue > $0` for the same non-development merchant |
| Trusted defect/opportunity signal | The already-public $6.99 listing now reaches the reviewed v8 server, but Shopify version 10 cannot issue a signed app-proxy entitlement request and the updated theme extension has not been released. A merchant can configure rules without a verified path to the paid storefront render |
| Causal mechanism | Release the matching single-scope Shopify proxy/theme version, reauthorize only the controlled development store, then verify save, signed entitlement, store-timezone rendering and preview-before-publish before acquisition |
| Measurement source | Shopify Partner app history, subscription evidence, earnings/charge records and payouts; live endpoint and controlled-store checks are readiness evidence only |
| Bounded observation window | D0 begins only when both Fly and Shopify versions pass controlled-store QA. Measure activation and paid choice through D+14; reconcile the same merchant's processed, non-refunded earnings through D+60 |

This is a direct conversion and recurring-revenue repair, not a cold generic
SEO experiment. Do not start merchant acquisition until both provider releases
and controlled-store QA pass. If the live paid path or storefront render fails,
roll back immediately; if no merchant remains paid after the bounded validation,
stop and revise the value proposition before another release.

**Biggest leak:** Fly v8 now enforces the active-plan gate and serves the reviewed
settings, onboarding and trust surfaces, but active Shopify version 10 has no
configured app proxy and still owns the previous theme-extension bundle. The
storefront entitlement-to-render path therefore cannot pass end to end.
Acquisition remains suppressed until the matching Shopify version and
controlled-store proof exist.

### Authenticated production reconciliation

- Fly release `v8` runs one started machine in `lhr`. `SCOPES` and all seven
  required Shopify/Partner secret names are deployed:
  `SHOPIFY_API_KEY`, `SHOPIFY_API_SECRET`, `SHOPIFY_APP_URL`,
  `SHOPIFY_APP_HANDLE`, `SHOPIFY_PARTNER_ORG_ID`, `SHOPIFY_APP_GID` and
  `SHOPIFY_PARTNER_API_ACCESS_TOKEN`. Values are not recorded in this packet.
  Startup completed after Prisma reported no pending migrations; the initial
  port warning cleared when the application listened on port 3000.
- The active Shopify app version has `/auth/callback`,
  `/auth/shopify/callback`, and `/api/auth/callback` configured; the third path
  returns 404 in production. It registers uninstall, scope-update, and privacy
  callbacks, and includes the `delivery-date` theme extension.
- The active Shopify version has no app proxy. The branch's storefront
  entitlement check therefore cannot be verified against Shopify until the
  scope/proxy configuration is separately approved and released.
- Fly v7's stock logger previously recorded full authenticated request targets.
  Values are intentionally omitted here. V8 preserves the real request for
  Shopify authentication but records only the path: 41 new request records over
  the 15-minute release window contained zero query strings, credential parameter
  names or synthetic sentinel values and zero errors. Historical log retention
  was not changed. V7 and v6 both use the old logger and are availability-only
  rollback references, not privacy remediation.
- Last-30-day Partner evidence shows 0 installs, 1 uninstall, net -1, and
  payouts/earnings of $0.00. Historical installs include Darren's test stores
  and one external store that later closed; none has a verified paid recurring
  subscription.
- The hosted plan selector and three consecutive Partner API checks agree that
  the controlled dev store currently has the $0 Standard test contract. A
  prior single null response is retained as a billing-consistency risk, not
  overwritten or misreported.

## 3. Bounded implementation batch

1. Use Shopify's official `shop.metafields["$app"].settings` Liquid syntax.
2. Resolve the shop's IANA timezone from authenticated Admin GraphQL and persist
   it with the delivery settings.
3. Calculate cutoff and calendar dates in that timezone, using UTC only as a
   stable calendar carrier so runtime timezone cannot shift the displayed day.
4. Keep the TypeScript/admin and JavaScript/storefront algorithms aligned with
   a real storefront-script regression test.
5. Remove the duplicate manual script tag; Shopify loads the declared extension
   JavaScript asset once.
6. Add Shopify's official product-template app-block deep link so a merchant can
   preview placement before saving the theme.
7. Require provider-confirmed App Home, organization and app identities; query
   Shopify Partner API `activeSubscription` at the app root and every settings
   mutation; and redirect unpaid installs to Shopify's hosted plan selector.
8. Keep the storefront block hidden until Shopify's HMAC-authenticated app
   proxy confirms the subscription. Remove usable unsaved fallback rules and
   cache provider results for at most one minute to respect the four-request-
   per-second Partner API limit. Missing sessions, provider failures,
   cancellation and freeze states fail closed.
9. On Shopify's documented post-plan `plan_handle` return only, reread one
   initial null after 400 ms. The query must return an active contract on either
   attempt; the URL parameter and historical events never grant access. A
   five-second provider timeout and five-second per-shop reread cooldown prevent
   hung calls and repeated retry amplification.
10. Version storefront cache writes so an older in-flight request cannot
    overwrite a newer authenticated admin decision in either direction. This
    prevents a stale active response from replacing a cancellation result.
11. Delete shop sessions unconditionally and idempotently on uninstall and
   `shop/redact`, including when Shopify no longer returns a webhook session.
12. Fail closed when Shopify does not return a valid IANA timezone instead of
   silently presenting UTC as the merchant timezone. Legacy storefront
   settings without a timezone stay hidden until the merchant resaves.
13. Track `package-lock.json` so the Dockerfile's `npm ci` build is reproducible.
14. Use Rollup's official WebAssembly Node distribution so the production build
   remains reproducible on this Windows host without weakening Application
   Control or executing a blocked native addon.
15. Replace the single-stage Dockerfile—which installed production-only packages
   before trying to run a development-tool build—with a build/runtime split.
16. Replace stale Billing API and unsupported conversion-lift claims in launch
    material with factual Shopify App Pricing language.
17. Preload a pinned Morgan logger override that passes the full request URL to
    Shopify unchanged but records only the path in access logs. Preserve method,
    status, response size and latency, and reject query strings in an in-process
    HTTP regression test using synthetic credential names and values.
18. Explicitly hold the Shopify SDK at Info level with HTTP request logging off
    as defense in depth against a later environment or dependency change.

This branch intentionally adds only `write_app_proxy` and its reviewed proxy
configuration to close the storefront billing bypass. Fly v8 now runs the
reviewed path-only logger after exact approval. The remediation passes 62 local
tests, lint, typecheck, production build, Shopify app/theme build, a secret-
pattern scan, a zero-runtime-vulnerability audit, controlled embedded-route QA
and the 15-minute live gate. No plan, price, trial, live listing, Shopify app
version or merchant-store setting has changed; every later provider action
remains separately approval-gated.

## 4. Competitive scorecard

All facts below are current public Shopify App Store signals, refreshed
2026-09-08. Private installs, activation, subscriptions, churn and revenue are
**UNAVAILABLE** for every competitor. Scores are public-signal judgments, not
hands-on correctness proof. Five is strongest.

| App | Positioning/features | Visible pricing | Rating/reviews | BFS | Problem clarity | Config signal | Setup | Theme/reliability | Value | Trust | Listing |
|---|---|---|---:|:---:|---:|---:|---:|---:|---:|---:|---:|
| [Essent](https://apps.shopify.com/essential-estimated-delivery) | Simple ETA/timer, cutoffs, holidays, geolocation, languages | Free | 5.0 / 980 | Yes | 3 | 3 | 5 | 5 | 5 | 4 | 4 |
| [S Estimated Delivery Date Plus](https://apps.shopify.com/omega-estimated-shipping-date) | Broad surfaces, country/ZIP/shipping/variant/metafield rules, analytics | Free; $4.99/$9.99/$29.99, three-day paid trials | 4.9 / 441 | Yes | 3 | 5 | 4 | 4 | 4 | 3 | 4 |
| [Estimated Delivery Date – ETA](https://apps.shopify.com/estimated-delivery-days) | Inventory, warehouse, vendor, tag, location, ZIP, pickup, analytics | Free capped tier; $6.99/$14.99/$19.99, seven-day trials | 4.9 / 493 | No badge visible | 4 | 5 | 3 | 2 | 4 | 3 | 5 |
| [C-EDD](https://apps.shopify.com/estimated-delivery-date-plus) | Product/collection/vendor/country rules, holidays, local timezone | Free; $4.98/$8.98 | 4.9 / 382 | No badge visible | 3 | 2 | 2 | 2 | 4 | 1 | 3 |
| [CodeRagon ETA](https://apps.shopify.com/order-delivery-estimated) | Product/variant/vendor/tag/shipping/country/ZIP rules, analytics | Free; $4.99/$8.99, seven-day trials | 4.7 / 84 | Yes | 4 | 5 | 4 | 5 | 5 | 3 | 5 |
| [ArrivesBy](https://apps.shopify.com/arrives-by) | Variant/location/inventory-aware ETA, preorder, promised-vs-actual tracking | Free; $4.99/$19.99, seven-day trials | 4.6 / 46 | Yes | 5 | 5 | 4 | 4 | 3 | 4 | 5 |
| SupaDatewise current split release | Fly v8 settings/billing/trust and path-only access logging live; Shopify v10 still owns the old proxy/theme configuration | $6.99, seven-day trial | 0 / 0 | No | 4 | 3 | 4 | 3 | 2 | 4 | 3 |
| SupaDatewise after controlled Shopify QA | Store-timezone rule, saved-settings fix, signed proxy, preview deep link and tested lightweight block | Same; unchanged | Still 0 / 0 | No | 4 | 3 | 4 | 4 | 2 | 4 | 4 conditional |

### First-party review themes to design against

- Incorrect business-day math and widgets disappearing after variant changes.
- Slow one-by-one rule setup for larger catalogs.
- Changes becoming live before a merchant can preview safely.
- Theme placement/compatibility failures and unresolved support.
- Confusing free-tier or price expectations.
- Price complaints where advanced surfaces sit behind expensive plans.

These are competitor review themes, not proof that SupaDatewise has solved
every issue. Fly v8 directly addresses store-timezone math, saved-rule delivery,
duplicate loading and safe preview, and its path-only logging is verified, but
the matching Shopify proxy/theme version is not released. Variant changes,
catalog-scale rules and multiple fulfillment locations remain outside current
capability.

## 5. Smallest defensible wedge

**Conditional next wedge, only after implementation and controlled-store QA:**
“Preview-first Delivery Promise QA for small DTC stores: test cutoff, weekend
and holiday boundaries in the store timezone, verify theme placement, and keep
the promise explicitly unpublished until the merchant chooses.”

This is narrower than the incumbent rule engines and matches capabilities that
can be proved. It avoids the false claim that a basic ETA widget alone is novel;
Essent already offers a highly reviewed free alternative.

**Next bounded product batch, only after the current split release passes:** add
published-theme activation status, boundary-case QA, a theme-editor reload
handler and backward-compatible Inline/Card presentation with a restrained
`Order → Dispatch → Arrives` timeline. Recapture only genuine controlled-store
screenshots. Do not place these capabilities in listing copy until built and
verified.

The paste-ready, character-limited listing packet is in `store-listing.md`.

## 6. Privacy-safe measurement contract

Persist every event with UTC time, app installation/shop pseudonymous ID,
source, app version, and a unique event ID. Never include shopper names, email,
addresses, cart/order contents, IP-derived location, or raw storefront URLs.
Shopify can append a logged-in customer ID to signed app-proxy requests; the
entitlement route ignores it and must not copy it into application logs/events.

| Event | Exact definition | Trusted source | Current state |
|---|---|---|---|
| `listing_view` | Shopify counted listing detail-page view | Shopify Partner/App Store analytics | **UNAVAILABLE** |
| `install_start` | Shopify creates/records the install attempt | Shopify provider event/analytics | **UNAVAILABLE** |
| `install_completed` | First valid authenticated offline session stored for a production shop | Signed Shopify OAuth + session DB | Code path exists; event **MISSING** |
| `onboarding_completed` | First successful `$app.settings` save with no GraphQL user errors | Authenticated app action | Code path exists; event **MISSING** |
| `theme_block_active` | Shopify `app.extensions()` reports the delivery block active on the published theme | Authenticated App Bridge | **MISSING** |
| `first_widget_rendered` | First successful render for an installation/version, deduplicated server-side | Proposed signed/pseudonymous beacon with no buyer payload | **MISSING**; do not add silently |
| `trial_started` | Shopify App Pricing subscription enters trial | Shopify Partner API subscription evidence | **VERIFIED for controlled $0 dev test only** at 2026-08-30 15:25:55 UTC; not a merchant conversion |
| `paid_conversion` | Subscription becomes paid after trial and is not a no-charge/test contract | Shopify Partner API subscription evidence plus non-test charge | **0 verified** |
| `churn` | Paid subscription cancels/expires or app uninstalls, with effective time | Shopify pricing event + signed uninstall webhook | **UNAVAILABLE** |
| `cleared_revenue` | Shopify payout/transaction is processed and non-refunded | Shopify payout/finance evidence | **USD $0.00 verified** through 2026-09-08 |

Do not equate `install_completed`, a $0 development-store plan, or
`paid_conversion` with cleared revenue. Reconcile subscription and payout
evidence monthly.

## 7. Acquisition experiment ready for approval

**Precondition:** controlled store install, pricing selection, settings save,
theme activation, first render, uninstall, privacy/support URLs, and production
rollback all pass. Until then: no traffic campaign and no merchant outreach.

### Test: five permission-based small-store validations

- Audience: five Shopify merchants from Darren's existing relationships or
  opt-in introductions from Shopify/theme partners; one primary dispatch
  schedule, Online Store 2.0, and a current delivery-certainty problem.
- No scraped contacts, purchased lists, cold bulk email, incentives, fake
  reviews, or request for a positive review.
- Ask: install from the public listing, configure the real schedule, preview the
  block, and decide during the existing seven-day trial whether it is worth
  $6.99/month.
- Research questions: where setup stalls; whether the delivery window matches
  the merchant's manual calculation; whether the message changes buyer/support
  conversations; what capability blocks payment.
- Primary outcome: provider-confirmed processed, non-refunded paid subscription.
- Leading funnel: 5 permissions → install starts → completed installs → settings
  saves → active blocks → trials → paid conversions.
- Stop rules: any auth/billing/render/privacy failure; two merchants report the
  same accuracy defect; support burden exceeds one unresolved issue per test
  merchant; or any request requires broader scopes without a reviewed need.

### Supporting zero-spend channels after readiness

1. **App Store optimisation:** publish the factual packet, real screenshots,
   branded privacy/support URLs, and only verified compatibility claims.
2. **Help/SEO:** release `/support`, `/privacy`, weekend/cutoff explanations,
   canonical metadata, sitemap and robots files already in source; verify live
   before counting indexing.
3. **Partner channel:** give opt-in theme developers a short compatibility and
   deep-link guide; no commission or promise until commercial terms are approved.
4. **Merchant validation:** run the five-person test above one at a time so a
   broken funnel is not amplified.

No merchant message, listing edit, App Store submission, Shopify app-version
release, theme save, spend, or form has been sent or performed. Fly releases v7
and v8 were each performed only after exact action-time approval; v7 and v6
remain available as availability rollbacks, with the documented privacy caveat.

## 8. Release QA and rollback gate

Local and image gate (refreshed 2026-09-08):

- clean isolated branch based on `4e7c2ae`; original dirty checkout preserved;
- fresh `npm ci` succeeds from the tracked lockfile;
- `npm run check` succeeds: 62 tests, lint, route type generation, TypeScript,
  client build and server build;
- `shopify app build` succeeds, including Shopify Theme Check and bundling the
  `delivery-date` theme app extension;
- `npm audit --omit=dev` reports zero known runtime vulnerabilities after
  overriding the sole transitive `qs` parser to patched version `6.16.0`;
- the complete audit still reports 15 high-severity advisories confined to
  development/build tooling (`@shopify/api-codegen-preset`, GraphQL codegen,
  TypeScript ESLint, `lodash` and `minimatch`). The offered fixes are major or
  misleading downgrades and are not forced into this commercial correctness
  batch;
- local production-server preview returns 200 for `/`, `/support`, `/privacy`,
  `/robots.txt`, `/sitemap.xml` and `/llms.txt`; unsigned uninstall and
  compliance webhook probes return 400; an unauthenticated `/app` request
  fails closed with 410 and `/auth/login` returns 200;
- all seven required secret values are deployed to Fly v8; the local branch adds
  the single `write_app_proxy` scope and proxy configuration, but neither has
  been applied to Shopify;
- Docker is not installed on this host. The exact `af0aa574` candidate passed
  Fly's remote Docker build, but that old preflight image is rejected
  because two `qs` denial-of-service advisories published after the build made
  its audit result stale. The reviewed replacement pins `qs 6.16.0`, passes the
  published advisory reproductions and has zero current runtime audit findings.
  Fly then built and pushed the exact clean replacement commit as the immutable
  110 MB image and manifest recorded above; its production prune also reported
  zero known vulnerabilities. The approved release became v7. Prisma reported
  no pending migration; port 3000 opened; `/`, `/support`, `/privacy`,
  `/robots.txt`, `/sitemap.xml`, `/llms.txt` and `/auth/login` returned 200;
  unauthenticated `/app` returned 410; and both embedded routes opened through
  Shopify Admin on the existing controlled-store session. A later reviewed
  redaction commit, `5548e96343092082ac460ded003846eb316f2684`, was built as
  the exact image/manifest recorded above and released unchanged as Fly v8.
  Migrations completed, port 3000 opened, the same routes passed, and a
  15-minute watch completed 13 of 13 successful probes. The existing controlled
  store reloaded both Settings and Setup guide without a save or other mutation.
  Forty-one new access records contained zero query strings, credential
  parameter names or synthetic sentinel values and zero errors.

Controlled-store gate after the verified Fly v8 release:

1. In the authenticated Dev Dashboard, confirm the App Home handle, Partner
   organization ID and app GraphQL ID. Reuse or create a Partner API client
   with Manage apps permission only under the exact account/settings gate, then
   store its token and those identifiers as Fly secrets under that same scoped
   gate. Reconcile the existing Fly app/volume/release without printing values.
2. Apply the reviewed `write_app_proxy` and `/apps/supadatewise` configuration
   only under the separate scope/config gate. Fresh install and any required
   reauthorization must complete with no iframe loop.
3. An unpaid install is redirected to Shopify's hosted plan selector; after the
   no-charge development-store plan is selected, Partner API
   `activeSubscription` returns the active record and the app opens.
4. Save settings and verify `$app.settings` through the real Liquid block.
   Seed one legacy value without `timeZone`; it must stay hidden until a resave
   persists Shopify's authoritative IANA timezone.
5. Test same UTC instant on opposite sides of the store-timezone cutoff.
6. Activate via deep link on Dawn and one non-native Online Store 2.0 theme.
7. Verify product page and a compatible cart section; no duplicate script work.
   Confirm the block stays hidden without a saved config or active subscription,
   and hides within one minute of cancellation/freeze/provider failure.
8. Trigger signed compliance and uninstall paths; session is removed.
9. Confirm the live privacy/support pages and the rollback procedure.

Rollback: do not deploy from the dirty checkout. Deploy only the exact approved
reviewed PR commit; retain its Fly release ID and Shopify app-version ID. Current
Fly production is release `v8`; its immediate rollback is release `v7` / image
`5fbbef8e248d0215da02e8d7f8765929ffb2149c-preflight`, with older release `v6` /
image `deployment-01KXTEPGG9RBKJNYBM2664B08H` also retained. Both old Fly
releases use the full-target logger, so they are emergency availability
rollbacks that reopen the privacy defect. The preserved Shopify rollback is
`supadatewise-delivery-date-10` / version `1056076529665`. On regression, roll
Fly back only for availability, reactivate the previous Shopify app version if
required, then verify `/`, `/app`, signed webhooks, and one controlled storefront
render. A replacement Shopify app-version ID remains **MISSING** until that
separate release is approved.

## 9. Material-step ledger (30 maximum)

1. Refreshed repository, branches, worktrees and dirty state.
2. Verified GitHub identity, open PR topology, and safe base.
3. Checked Fly CLI access and public Fly identity.
4. Verified Shopify listing identity, status, price, trial, reviews and data disclosure.
5. Verified public support/privacy/discoverability gaps.
6. Attempted read-only first-party dashboard access; marked blocked metrics `UNAVAILABLE`.
7. Reviewed official Shopify metafield, theme, pricing, scopes and compliance guidance.
8. Benchmarked six official Shopify competitor listings and first-party review themes.
9. Created an isolated worktree and feature branch.
10. Repaired `$app.settings` Liquid resolution.
11. Implemented store-IANA-timezone delivery calculations and safe locale fallback.
12. Added storefront/server parity and persistence regression tests.
13. Removed duplicate script loading and added preview deep link.
14. Made the Docker dependency install reproducible with a tracked lockfile.
15. Produced the listing, measurement, acquisition and rollback packets.
16. Replaced the broken single-stage container build with separate build/runtime stages.
17. Switched Rollup to the official portable Node WebAssembly distribution after
    Windows Application Control blocked the native addon.
18. Verified a fresh dependency install, 35-test/lint/type/build gate, zero
    runtime audit findings and the six public routes in a local production-server preview.
19. Passed Shopify Theme Check and built the complete Shopify app package locally.
20. Completed a fresh read-only diff review and held the commit on billing,
    shop-redaction and timezone fail-closed findings.
21. Added the active-plan gate, unconditional shop data deletion and strict
    Shopify timezone validation with regression tests; corrected stale runbooks.
22. Removed the unverified listing-slug assumption from billing: release now
    fails closed until the provider-confirmed App Home handle is configured.
23. Replaced the legacy Admin billing-helper check with Shopify's current
    Partner API `activeSubscription` contract, strict provider identity checks,
    fail-closed error handling and regression tests.
24. Re-ran the full test/lint/type/build gate, Shopify Theme Check and app
    build, secret-pattern scan and dependency audits; runtime audit remains
    clean and the 15 findings remain confined to documented build tooling.
25. Closed the independent-review billing bypass: guarded the nested settings
    action, removed the functional unpaid theme fallback, and added a hidden,
    HMAC-authenticated app-proxy entitlement check with a one-minute cache.
26. Passed the final 50-test/lint/type/build gate and Shopify Theme Check after
    the entitlement, legacy-timezone and privacy changes; no provider or
    merchant state was changed.
27. Committed the reviewed implementation as
    `42d85f542545cb2f01f52d294c5ced0d70f39ab1`.
28. Pushed `agent/growth/supadatewise-paid-readiness-20260830` without changing
    `main` or force-pushing.
29. Opened draft pull request
    `https://github.com/sheepdontmoo/Niche-finder/pull/17` against the verified
    clean base `agent/seo/supadatewise-weekends-answer-20260830`.
30. Synchronized this evidence ledger with the immutable commit, branch and
    draft-PR identities; documentation only, with no product, provider or
    merchant-state change.

The initial 30-step cap was exhausted. Darren then explicitly restarted the
work with `go` and `Yes do what needs to be done to improve it and get users
paying`. The continuation remains limited to safe, reversible work and stops at
the original exact action-time gates. No recurring automation was proposed or
created.

## 10. Continuation ledger after renewed instruction

1. Authenticated and reconciled the existing Fly app, release, machine,
   environment names and zero-secret state without changing production.
2. Authenticated and reconciled Shopify organization, app, listing, active
   version, current installs, event history and earnings.
3. Reconciled the existing least-privilege Partner API client after exact
   approval; kept its token out of source, logs and this document.
4. Activated the Standard plan as a $0 development-store test contract after
   exact approval; verified the hosted-plan state and `SUBSCRIPTION_CREATED`
   event without claiming revenue.
5. Repeated the official Partner API query after one transient null response;
   three consecutive checks returned the active trial contract.
6. Staged the Estimated Delivery Date block in the published-theme editor
   without saving; verified a storefront preview render and retained the exact
   save gate.
7. Began the provider-evidence, competitive-scorecard and first-paid-merchant
   packet refresh; no deployment, listing edit, outreach, store save or spend.
8. Reproduced and fixed the post-plan transient-null loop and a storefront
   cache race that could overwrite a newer cancellation decision; the 57-test
   gate, lint, typecheck, production build, Shopify Theme Check/app build and
   zero-runtime-vulnerability audit pass.
9. Reconciled and retained Shopify CLI's stable theme-extension UID, which
   matches the preserved checkout; no app version was released.
10. Passed the fresh independent review after adding a five-second Partner API
    timeout, completion-time cache expiry and per-shop retry cooldown. Reviewer
    verdict: `ship`; production/provider actions remain separately gated.
11. Staged the seven required Shopify/Partner secret values on the existing Fly
    app after exact approval; retained the existing deployed `SCOPES` value.
12. Attempted the approved deployment of exact commit `30243bdf`; the Docker
    build failed before any production update because its Node 20 image could
    not satisfy locked Shopify CLI 4.7.0's Node `>=22.12.0` engine. Fly release
    `v6` and all live data remained unchanged; no rollback was required.
13. Aligned build and runtime images on Node 22, added a regression test, passed
    the 58-test/lint/type/build/app-build/runtime-audit gate, received a second
    independent `ship` verdict, and pushed exact commit
    `af0aa574ebcb8a718def2f7451ea92a363fc5e57` to draft PR #17.
14. Built and pushed the exact candidate as a Fly build-only image. The prior
    prune failure is fixed; the image manifest is retained above. Rechecking
    Fly proved there is still no release after `v6` and all seven new secrets
    remain staged. A production deployment of this new commit requires a new
    exact action-time approval.
15. Revalidated the production gate on 2026-09-08 and rejected the prior image
    before deployment when newly published `qs` advisories produced three
    moderate runtime audit findings. Fly remained on `v6`; staged secrets were
    not activated.
16. Overrode the sole Express/body-parser query parser to patched `qs 6.16.0`,
    updated the lockfile, and added a regression covering every locked `qs`
    copy. Fresh install, 59 tests, lint, typecheck, production build, Shopify app
    build, normal Express parsing, both published advisory reproductions and
    `npm audit --omit=dev` all pass. Independent review verdict: ship for commit
    and build-only preflight; production remains separately gated.
17. Refreshed Shopify Partner evidence on 2026-09-08: 1 merchant, 0 installs and
    1 uninstall in the last 30 days, no paid event after the controlled-store
    free subscription, and $0.00 total earnings, recurring charges and payouts.
    Recorded the exact D0-to-D+14 paid-conversion and D0-to-D+60 cleared-cash
    measurement window before preparing another release candidate.
18. Built and pushed replacement commit
    `5fbbef8e248d0215da02e8d7f8765929ffb2149c` as the non-running Fly build-only
    image `registry.fly.io/edd-supadesign:5fbbef8e248d0215da02e8d7f8765929ffb2149c-preflight`,
    manifest `sha256:f51f123fda499ce1141d74d9f1e95f7ddcc9a3ad2c1e1c94285a20ef3dfd48af`,
    size 110 MB. Rechecked Fly afterward: production remains release `v6`, the
    seven required secret values remain staged, and the five missing public
    trust/discovery routes still return 404. No deployment occurred.
19. After Darren's exact approval, deployed the immutable replacement image and
    manifest to the existing Fly app using a rolling update. Fly recorded release
    `v7` on the existing `lhr` machine and deployed all seven staged secret
    values; release `v6` remains the rollback.
20. Investigated Fly's transient port warning before accepting the release.
    Prisma found one existing migration and no pending work; the application
    then listened on port 3000. Live checks returned 200 for the six public/trust
    routes and `/auth/login`, while unauthenticated `/app` failed closed with
    410. Post-start logs contained no new application error.
21. Opened the existing controlled development-store installation through
    Shopify Admin without changing its plan, settings, scopes or theme. The v7
    Settings route loaded the saved rules and authoritative `America/New_York`
    timezone; a clean reload of Setup guide loaded the activation instructions
    and theme-editor deep link without an iframe loop.
22. Refreshed the six-app public competitor benchmark. The evidence supports a
    next bounded wedge of preview-first Delivery Promise QA and a theme-native
    visual card, but no second code/release batch will be stacked before the
    current Shopify proxy/theme version completes controlled-store QA.
23. A post-release privacy review found that Fly v7's stock React Router access
    logger records full authenticated request query strings. The Shopify
    configuration gate and acquisition were paused immediately. Credential
    values are omitted; v6 used the same logger and is not a remediation.
24. Replaced the logging behavior locally with a pinned path-only Morgan token
    override while preserving the stock React Router server. Added synthetic
    regressions proving method, path, status and latency remain and query names
    and values do not. Explicitly disabled Shopify SDK HTTP request logging.
    Fresh install, 62 tests, lint, typecheck, production build, Shopify app/theme
    build, secret-pattern scan and `npm audit --omit=dev` all pass. No release or
    provider change occurred.
25. Independent review verdict: `PASS`. The reviewer confirmed that the log
    override never mutates Shopify's real request URL, runtime dependency
    resolution and Docker inclusion are correct, and the evidence packet is
    consistent. The production command reached Shopify SDK initialization; a
    full local route probe stopped on an unmigrated disposable SQLite fixture,
    not on the logging change. Live route/log proof remains a post-deployment
    requirement.
26. Committed the reviewed redaction and evidence batch as immutable source
    `5548e96343092082ac460ded003846eb316f2684`.
27. Built and pushed that exact commit as non-running Fly image
    `registry.fly.io/edd-supadesign:5548e96343092082ac460ded003846eb316f2684-preflight`,
    manifest `sha256:07d8f34b4b0265ce8a131e7eeed0fb3a3515a68b07599397b02d24b541e0aa1c`,
    size 110 MB. The image build and production dependency prune passed.
28. Rechecked Fly after the build-only action: release `v7`, image `5fbbef8e...`,
    machine `812e3dc95e2728` and the v6 rollback remain unchanged. The redaction
    candidate is not deployed.
29. After Darren's exact action-time approval, deployed only the immutable
    redaction image and manifest recorded above to `edd-supadesign` with a
    rolling update. Fly created release `v8` on the existing `lhr` machine;
    releases v7 and v6 remain retained and no Shopify configuration, listing,
    theme, pricing, outreach, merge or spend changed.
30. Accepted v8 only after migrations completed, port 3000 opened, all public
    routes passed, raw unauthenticated `/app` stayed fail-closed at 410, and the
    controlled Shopify installation reloaded Settings and Setup guide without a
    save. A 15-minute watch passed 13 of 13 probes; 41 new access records had
    zero query strings, credential parameter names, sentinel values or errors.
    This continuation cap is exhausted; the Shopify release remains a separate
    exact gate.
