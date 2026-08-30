# SupaDatewise paid-install readiness — evidence and launch packet

Evidence window: 2026-08-30 10:02–10:10 UTC for public checks and through
10:39 UTC for local verification.

Commercial target: the first real processed, non-refunded Shopify app
subscription.

Current verified paid installs: **UNAVAILABLE**.

Current cleared subscription revenue: **UNAVAILABLE**.

Traffic, listing visibility, public reviews, draft code, test charges, installs
without provider-confirmed payment, and pipeline are not revenue.

## 1. Immutable source and provider identities

| Identity | Verified value | Evidence/state |
|---|---|---|
| Repository | `sheepdontmoo/Niche-finder` | Git remote refreshed 2026-08-30 |
| Product path | `products/s1-estimated-delivery-date` | Source inspection |
| Preserved dirty checkout | `C:\Users\basde\Desktop\Niche-finder` at `7971931` | Three user-modified files; not touched |
| Clean implementation base | `4e7c2aea5ff1ae4969a0982452598b6ad8f52128` | `agent/seo/supadatewise-weekends-answer-20260830` |
| Isolated branch | `agent/growth/supadatewise-paid-readiness-20260830` | Created from the clean base |
| Shopify app client ID | `26c0cd1cd1992a8d9114c826f47e3e5b` | Local TOML matches public listing install metadata and live App Bridge shell |
| Shopify App Home handle | **UNAVAILABLE** | Local TOML omits it; the public App Store listing slug does not prove the admin App Home handle required by Shopify's plan-selector URL |
| Shopify Partner organization ID | **UNAVAILABLE** | Legacy repository note says `208004935`; not accepted without authenticated provider evidence |
| Shopify app GraphQL ID | **UNAVAILABLE** | Legacy repository note says numeric app `328515354625`; the required current `gid://shopify/App/...` value is not authenticated |
| Partner API client/token | **UNAVAILABLE** | Dashboard access is blocked; requires an approved, securely stored client with Manage apps permission |
| Shopify listing | [SupaDatewise: Delivery Date](https://apps.shopify.com/estimated-delivery-date-6) | Public, HTTP 200, active Install control |
| Fly app/domain | `edd-supadesign` / `https://edd-supadesign.fly.dev` | DNS, TLS, Fly headers, local config, matching App Bridge key |
| Exact production commit/release | **UNAVAILABLE** | Fly CLI has no access token and live app exposes no immutable commit receipt |
| Shopify Partner/Dev Dashboard | **UNAVAILABLE** | Read-only browser attempt reached Shopify login; connected Chrome session unavailable |

The unrelated repository default branch and the dirty desktop checkout are not
safe release bases. Provider-generated config changes in the dirty checkout
must be reconciled only after authenticated Shopify verification.

## 2. Verified funnel and biggest leaks

| Funnel stage | Current evidence | Status and leak |
|---|---|---|
| App Store listing | Public since 2026-07-20; $6.99/month; seven-day trial; 0 reviews | **LIVE**, but weak proof |
| Discoverability | Product content category has 454 apps; SupaDatewise is page 18, Shopify intra-position 8; absent from page one of Delivery and pickup | **LOW** visibility |
| Listing view | Shopify page is reachable | Counts/conversion **UNAVAILABLE** |
| Install start | Public Install control opens Shopify's login/store-selection flow | Store-specific start count **UNAVAILABLE** |
| Install completion/OAuth | Local app config and auth routes exist; public `/auth/login` responds | End-to-end result **UNAVAILABLE** without controlled store |
| Scopes/permissions | Live source declares none; branch adds only `write_app_proxy` for an HMAC-authenticated storefront entitlement request; listing discloses store-owner contact data associated with the app session | Scope/config and merchant reauthorization impact **UNAVAILABLE** until controlled install |
| Shopify App Pricing | Public listing shows $6.99/month and trial; pre-batch routes and theme fallback did not enforce an active plan | Provider state and required Partner API configuration are **UNAVAILABLE**; branch gates app root, settings mutation and storefront render, and requires authenticated handle, organization, app GID and client token before release |
| Onboarding | Embedded settings and setup routes exist locally | Live production route/version **UNAVAILABLE** |
| Settings save | Local GraphQL writes an app-owned shop metafield | Live result **UNAVAILABLE** |
| Rules reach storefront | Pre-batch Liquid used `shop.metafields.app.settings`, not Shopify's `$app` reserved-namespace syntax | **BROKEN in reviewed source**; fixed locally, with the unentitled fallback removed |
| Cutoff accuracy | Pre-batch renderer used the shopper device timezone despite UI promising shop time | **BROKEN cross-timezone**; fixed locally with Shopify `ianaTimezone` |
| Theme loading | Script declared in schema and loaded again with a manual tag | Duplicate load; fixed locally |
| Theme activation | Manual instructions only | One-click product-template deep link added locally; live test **UNAVAILABLE** |
| First widget rendered | No trustworthy event exists | **MISSING** measurement and live proof |
| Public support | `/support` returns 404; listing exposes no support URL or mail link | **BLOCKER** for trust/acquisition |
| Public privacy | `/privacy` returns 404; listing points to an older Telegraph policy | **BLOCKER** for trust/listing quality |
| Webhooks | Pre-batch uninstall deletion depended on a session and `shop/redact` only acknowledged; unsigned probes fail closed | Unconditional idempotent shop deletion fixed locally; signed provider delivery **UNAVAILABLE** |
| Current installs/trials | No authenticated Shopify provider evidence | **UNAVAILABLE**, never infer zero |
| Paid subscriptions/churn/refunds | No authenticated Shopify provider evidence | **UNAVAILABLE** |
| Cleared revenue | No Shopify payout/transaction evidence | **UNAVAILABLE** |

**Biggest leak:** the reviewed source allowed app use without enforcing an
active App Pricing plan, could save rules without applying them to the theme
block, and could shift cutoffs outside the store timezone. Marketing that state
would amplify a free or inaccurate activation path. The branch fixes those
defects; acquisition remains suppressed until controlled-store and live release
proof exist.

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
9. Delete shop sessions unconditionally and idempotently on uninstall and
   `shop/redact`, including when Shopify no longer returns a webhook session.
10. Fail closed when Shopify does not return a valid IANA timezone instead of
   silently presenting UTC as the merchant timezone. Legacy storefront
   settings without a timezone stay hidden until the merchant resaves.
11. Track `package-lock.json` so the Dockerfile's `npm ci` build is reproducible.
12. Use Rollup's official WebAssembly Node distribution so the production build
   remains reproducible on this Windows host without weakening Application
   Control or executing a blocked native addon.
13. Replace the single-stage Dockerfile—which installed production-only packages
   before trying to run a development-tool build—with a build/runtime split.
14. Replace stale Billing API and unsupported conversion-lift claims in launch
   material with factual Shopify App Pricing language.

This branch intentionally adds only `write_app_proxy` and its reviewed proxy
configuration to close the storefront billing bypass. No provider setting,
plan, price, trial, live listing, Fly app, merchant store, or production release
has been changed; scope/config deployment remains approval-gated.

## 4. Competitive scorecard

All facts below are current public Shopify App Store signals, accessed
2026-08-30. Private installs, activation, subscriptions, churn and revenue are
**UNAVAILABLE** for every competitor. Scores are public-signal judgments, not
hands-on correctness proof. Five is strongest.

| App | Positioning/features | Visible pricing | Rating/reviews | BFS | Problem clarity | Config signal | Setup | Theme/reliability | Value | Trust | Listing |
|---|---|---|---:|:---:|---:|---:|---:|---:|---:|---:|---:|
| [Essent](https://apps.shopify.com/essential-estimated-delivery) | Simple ETA/timer, cutoffs, holidays, geolocation, languages | Free | 5.0 / 914 | Yes | 3 | 3 | 5 | 5 | 5 | 4 | 4 |
| [S Estimated Delivery Date Plus](https://apps.shopify.com/omega-estimated-shipping-date) | Broad surfaces, country/ZIP/shipping/variant/metafield rules, analytics | Free; $4.99/$9.99/$29.99, three-day paid trials | 4.9 / 404 | Yes | 3 | 5 | 4 | 4 | 4 | 3 | 4 |
| [Estimated Delivery Date – ETA](https://apps.shopify.com/estimated-delivery-days) | Inventory, warehouse, vendor, tag, location, ZIP, pickup, analytics | Free capped tier; $6.99/$14.99/$19.99, seven-day trials | 4.9 / 466 | No badge visible | 4 | 5 | 3 | 2 | 4 | 3 | 5 |
| [C-EDD](https://apps.shopify.com/estimated-delivery-date-plus) | Product/collection/vendor/country rules, holidays, local timezone | Free; $4.98/$8.98 | 4.9 / 337 | No badge visible | 3 | 2 | 2 | 2 | 4 | 1 | 3 |
| [CodeRagon ETA](https://apps.shopify.com/order-delivery-estimated) | Product/variant/vendor/tag/shipping/country/ZIP rules, analytics | Free; $4.99/$8.99, seven-day trials | 5.0 / 78 | Yes | 4 | 5 | 4 | 5 | 5 | 3 | 5 |
| [ArrivesBy](https://apps.shopify.com/arrives-by) | Variant/location/inventory-aware ETA, preorder, promised-vs-actual tracking | Free; $4.99/$19.99, seven-day trials | 4.5 / 45 | Yes | 5 | 5 | 4 | 4 | 3 | 4 | 5 |
| SupaDatewise live | One store-wide rule, theme block, no order/customer-record scopes | $6.99, seven-day trial | 0 / 0 | No | 2 | 2 | 2 | 2 | 2 | 2 | 3 |
| SupaDatewise branch | Store-timezone rule, saved-settings fix, preview deep link, tested lightweight block | Same; unchanged | Still 0 / 0 | No | 4 | 4 local | 4 local | 4 local | 2 | 3 | Draft only |

### First-party review themes to design against

- Incorrect business-day math and widgets disappearing after variant changes.
- Slow one-by-one rule setup for larger catalogs.
- Changes becoming live before a merchant can preview safely.
- Theme placement/compatibility failures and unresolved support.
- Confusing free-tier or price expectations.
- Price complaints where advanced surfaces sit behind expensive plans.

These are competitor review themes, not proof that SupaDatewise has solved
every issue. This branch directly addresses store-timezone math, saved-rule
delivery, duplicate loading, and safe preview; variant changes, catalog-scale
rules, and multiple fulfillment locations remain outside current capability.

## 5. Smallest defensible wedge

**Now:** “Store-timezone delivery windows for small DTC stores with one clear
dispatch schedule—without order or customer-record access.”

This is narrower than the incumbent rule engines and matches capabilities that
can be proved. It avoids the false claim that a basic ETA widget alone is novel;
Essent already offers a highly reviewed free alternative.

**Next product wedge, only after live readiness and merchant validation:**
made-to-order/preorder catalogs with variant, inventory, location, or metafield
lead times; bulk rule management; and promised-versus-actual accuracy. Do not
place these features in listing copy until built and verified.

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
| `trial_started` | Shopify App Pricing subscription enters trial | Shopify Partner API subscription evidence | **UNAVAILABLE** |
| `paid_conversion` | Subscription becomes paid after trial and is not a no-charge/test contract | Shopify Partner API subscription evidence | **UNAVAILABLE** |
| `churn` | Paid subscription cancels/expires or app uninstalls, with effective time | Shopify pricing event + signed uninstall webhook | **UNAVAILABLE** |
| `cleared_revenue` | Shopify payout/transaction is processed and non-refunded | Shopify payout/finance evidence | **UNAVAILABLE** |

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

No message, listing edit, submission, deploy, spend, or form has been sent or
performed.

## 8. Release QA and rollback gate

Local gate before draft PR (verified 2026-08-30):

- clean isolated branch based on `4e7c2ae`; original dirty checkout preserved;
- fresh `npm ci` succeeds from the tracked lockfile;
- `npm run check` succeeds: 50 tests, lint, route type generation, TypeScript,
  client build and server build;
- `shopify app build` succeeds, including Shopify Theme Check and bundling the
  `delivery-date` theme app extension;
- `npm audit --omit=dev` reports zero known runtime vulnerabilities;
- the complete audit still reports 15 high-severity advisories confined to
  development/build tooling (`@shopify/api-codegen-preset`, GraphQL codegen,
  TypeScript ESLint, `lodash` and `minimatch`). The offered fixes are major or
  misleading downgrades and are not forced into this commercial correctness
  batch;
- local production-server preview returns 200 for `/`, `/support`, `/privacy`,
  `/robots.txt`, `/sitemap.xml` and `/llms.txt`; unsigned uninstall and
  compliance webhook probes return 400; an unauthenticated `/app` request
  fails closed with 410 and `/auth/login` returns 200;
- no secrets are present; the local branch adds the single `write_app_proxy`
  scope and proxy configuration, but neither has been applied to Shopify;
- Docker is not installed on this host, so a clean Docker build is
  **UNAVAILABLE**. Prisma's native migration engine is also blocked by Windows
  Application Control locally; the preview database was created from the
  checked-in migration through Prisma Client without bypassing that policy.
  Linux container startup and migration remain release-gate checks, not proven
  production facts.

Controlled-store gate before deployment approval:

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

Rollback: do not deploy from the dirty checkout. Deploy only the reviewed PR
commit; retain its Fly release ID and Shopify app-version ID. On regression,
roll Fly back to the immediately previous release and reactivate the previous
Shopify app version, then verify `/`, `/app`, signed webhooks, and one controlled
storefront render. Those provider IDs are **MISSING** until an approved release.

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

Remaining material steps are commit, push and draft PR. No recurring automation
is proposed or created.
