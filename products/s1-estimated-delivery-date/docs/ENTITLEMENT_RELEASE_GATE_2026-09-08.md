# SupaDatewise entitlement expiry fix — 2026-09-08

This packet supersedes the Shopify-only next gate in the earlier readiness
ledger. Darren approved the source remediation after the review reproduced
an already-open widget remaining visible after 61 simulated seconds with no
second subscription request. No production release was approved or performed
in this remediation session.

## Candidate and production identities

- PR: https://github.com/sheepdontmoo/Niche-finder/pull/17
- Branch: agent/growth/supadatewise-paid-readiness-20260830
- Parent reviewed before this fix: 5a1b4fbe530548e5885de3c1d0ef50248f206aeb.
- The replacement candidate is the commit containing this packet. Use the
  exact resulting PR commit SHA, never an unrelated default or dirty checkout.
- Last provider receipt: Fly v8, source
  5548e96343092082ac460ded003846eb316f2684, immutable manifest
  sha256:07d8f34b4b0265ce8a131e7eeed0fb3a3515a68b07599397b02d24b541e0aa1c.
- Last provider receipt: active Shopify
  supadatewise-delivery-date-10 / 1056076529665, missing the proxy/scope/update.
- No newer authenticated provider receipt was obtained here. Browser automatic
  approval review rejected Shopify account access. Inventory remains the last
  recorded one controlled development store, not a newly verified assertion.
- No verified paid merchants or revenue were added by this work.

## What changed and why

The server now returns active plus validForMs, the remaining lifetime of the
current authoritative cache entry. A cached answer does not receive a new
sixty-second browser allowance. Admin checks prime the cache using the start
of verification, so response latency does not extend the validity window.

The theme rechecks every fifteen seconds (sooner near expiry), shares each
request between its blocks, and maintains a separate hard expiry timer. It
subtracts the entire request round trip by measuring validity from the local
monotonic request-start time. An inactive, malformed, failed or timed-out
response hides the widget; a later successful check can restore it. Requests
time out after five seconds, and late responses cannot restore expired access.

Hidden pages and pages entering the back-forward cache hide their blocks and
cancel pending work. Restored pages require a new check before showing a
widget. JavaScript cannot execute while the browser suspends a page, so the
guarantee is a one-minute maximum cached grant on a running page plus
fail-closed restoration. It is measured against Shopify's authoritative
subscription response, not an assumed cancellation event or inferred revenue.

The new theme deliberately rejects the old v8 response without validForMs.
Consequently the server update MUST be deployed and verified before the new
Shopify extension. The old theme accepts the new server's additional field,
so server-first deployment preserves the existing response contract. Neither
step alone proves the complete lifecycle is live.

## Validation

- Dependencies: installed from the unchanged lockfile with
  npm ci --offline --ignore-scripts --no-audit --no-fund.
- The original 62 tests plus 10 lifecycle regressions pass, including the
  compiled-server HTML check after the production build: 72 total.
- Lint: PASS.
- Client and server production build: PASS.
- npm audit --omit=dev: zero runtime vulnerabilities.
- git diff --check: PASS.
- Integrated fake-clock regression uses the actual server cache/entitlement
  implementation and actual theme JavaScript: visible at 59,999 ms; hidden at
  60,000 ms after the provider ceases granting access. No page reload.
- Additional regressions cover valid subscription renewal, provider outage and
  recovery, hung and late responses, short grants, response latency, malformed
  and old-protocol responses, multiple blocks, tab suspension and back-forward
  cache restoration.
- Typecheck: BLOCKED, not passed. The offline install intentionally skipped
  lifecycle scripts, so PrismaClient is not generated. The only reported
  TypeScript error is TS2305 in app/db.server.ts. Normal Prisma generation and
  its no-engine alternative were stopped by network approval cancellation.
- Shopify app build: BLOCKED, not passed. It began Theme Check but was stopped
  by network approval cancellation before a completion result.
- No full local runtime/database or signed Shopify lifecycle QA is claimed.
  No new independent reviewer verdict or Fly image receipt is claimed.

## Required continuation before asking to release

1. Reconcile this exact commit, authenticated app identities, Fly v8 state and
   the current installed-store inventory without printing secrets. If any
   non-controlled installation exists, reassess the release impact before
   changing the active app version.
2. In an authorised environment, complete a normal locked installation and
   Prisma generation, npm run check, Shopify app build and runtime audit.
   Preserve the existing extension UID:
   1e29f333-f7c8-b03f-d483-a7cdb79cd8f96c31405f.
3. Build the exact replacement server image without releasing it. Record its
   source SHA, image and immutable manifest. Retain v8 as the server rollback;
   v7/v6 restore the historical full-query access logger and are unsuitable
   as routine rollback targets for this fix.
4. Prepare the app-version diff against the actually active Shopify version.
   It may contain only write_app_proxy; app_proxy url /apps/supadatewise,
   prefix apps, subpath supadatewise; and the existing delivery-date extension.
   Stop for any unrelated configuration, scope or extension change.
5. Present the concrete image/manifest and Shopify candidate version/diff for
   exact action-time approval. The previous Shopify-only gate is insufficient.

## Approval text to complete only after those prerequisites pass

"Approve deployment of SupaDatewise PR #17 commit [EXACT_SHA] using Fly image
[EXACT_IMAGE] at manifest [EXACT_MANIFEST] to the existing edd-supadesign app.
Verify health, authenticated app entry, path-only logs and the signed
entitlement response with its bounded validForMs first. Then release only
Shopify version [EXACT_VERSION], whose reviewed diff is write_app_proxy,
the /apps/supadatewise proxy and the existing delivery-date theme extension.
Reauthorise only controlled store supadesign-8073. Run the agreed controlled
entitlement-to-render QA using preview surfaces without saving a live theme.
Stop on any failed check or unexpected installed merchant. No listing,
pricing, measurement, outreach, merge or spend changes are approved."

Do not use the placeholders as a deployment authorisation. Any controlled
settings save, plan cancellation, uninstall/reinstall, or signed deletion QA
needed beyond the approved preview/reauthorisation must be explicitly included
in its concrete action-time gate. A complete lifecycle pass must distinguish
active, inactive, frozen, provider-error and recovery states, and must test
revocation on an already-open page against the live proxy.

After both releases pass, record the new Fly and Shopify identifiers and
controlled QA results before calling acquisition ready. A $0 development
contract remains QA evidence only.
