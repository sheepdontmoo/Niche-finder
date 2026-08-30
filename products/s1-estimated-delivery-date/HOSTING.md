# Production hosting runbook (Fly.io)

The storefront block is hosted by Shopify, while the **admin app (settings +
billing) needs a permanent host**. Public evidence confirms the existing Fly
app `edd-supadesign`; do not create another app or volume.

This is an approval-gated runbook. Do not change Fly secrets/settings or deploy
until the controlled-store gate passes and Darren gives exact action-time
approval. Run one command at a time only after that approval.

## 1. Install the Fly CLI (PowerShell)
```
pwsh -Command "iwr https://fly.io/install.ps1 -useb | iex"
```
Close and reopen PowerShell, then `cd` back to the app folder. Verify:
```
fly version
```

## 2. Sign in
```
fly auth login
```
Do not sign up for or purchase a new Fly account. Current authenticated app,
release and cost evidence is **UNAVAILABLE** until access is restored.

## 3. Confirm provider configuration
In the Dev Dashboard → your app → **API credentials** (a.k.a. Client
credentials): copy the **Client ID** (this is `SHOPIFY_API_KEY`) and the
**Client secret** (this is `SHOPIFY_API_SECRET`). Also copy the app's **App Home
handle** from the current provider configuration; this is
`SHOPIFY_APP_HANDLE`. The public App Store listing slug is not sufficient proof
of the App Home handle.

The app root verifies active Shopify App Pricing subscriptions with Shopify's
Partner API. Confirm the owning **organization ID** (`SHOPIFY_PARTNER_ORG_ID`)
and the app's full GraphQL ID (`SHOPIFY_APP_GID`, such as
`gid://shopify/App/...`). Reuse an existing Partner API client only if it has
the required **Manage apps** permission, or request exact approval before an
organization owner creates or changes one. Its token is
`SHOPIFY_PARTNER_API_ACCESS_TOKEN`. Treat that token like a password: store it
only as a provider secret and never print or commit it.

## 4. Reconcile the existing app, then set configuration
First verify `fly status`, current releases, the persistent `data` volume and
the current secret names. Do not print secret values. Only if the reviewed
release requires a missing or changed value, request the exact settings-change
gate and then set the required values:
```
fly secrets set SHOPIFY_API_KEY=<Client ID> SHOPIFY_API_SECRET=<Client secret> SHOPIFY_APP_HANDLE=<App Home handle> SHOPIFY_PARTNER_ORG_ID=<Organization ID> SHOPIFY_APP_GID=<App GraphQL ID> SHOPIFY_PARTNER_API_ACCESS_TOKEN=<Partner API token> SHOPIFY_APP_URL=https://edd-supadesign.fly.dev
```
After the separate deployment gate, deploy the reviewed commit only:
```
fly deploy
```
Record the immutable Fly release ID and retain the immediately previous release
for rollback.

## 5. Point Shopify at the permanent URL
Edit `shopify.app.toml`:
- `application_url = "https://edd-supadesign.fly.dev"`
- under `[auth]`, set every redirect URL to that domain, e.g.
  `redirect_urls = ["https://edd-supadesign.fly.dev/auth/callback", "https://edd-supadesign.fly.dev/auth/shopify/callback", "https://edd-supadesign.fly.dev/api/auth/callback"]`
- keep the reviewed `write_app_proxy` scope and `/apps/supadatewise` proxy
  configuration together; this is the storefront subscription gate.

Only if authenticated reconciliation proves the checked-in URLs differ, update
the file and request the Shopify scope/configuration-deploy gate. Existing
installs may require reauthorization; do not expose merchants to that change
until it passes on a controlled store. Then:
```
npm run deploy
```

## 6. Verify
- Start from an unpaid controlled development-store install and verify the root
  route redirects to Shopify's hosted plan selector.
- Select the no-charge development-store plan and confirm the app opens.
- Confirm the Partner API check returns an active subscription for that store
  and no subscription for an unpaid controlled store.
- Save settings, activate the block, and confirm store-timezone rendering.
- Seed a legacy metafield without `timeZone`; confirm the block stays hidden,
  then open the app, save once, and confirm Shopify's IANA timezone is persisted
  before the block can render.
- Confirm `/apps/supadatewise/entitlement` is HMAC-authenticated through Shopify,
  an unpaid/frozen/provider-error state keeps the block hidden, and an active
  plan reveals it. Cancellation can remain cached for no more than one minute.
- Verify signed uninstall and `shop/redact` deliveries delete all shop sessions.

## Local dev after this change
`schema.prisma` now uses `env("DATABASE_URL")`. For local `npm run dev`, add
the values below to the untracked `.env`. Shopify CLI can inject its own app URL
and client credentials; never commit or print any real secret:
```
DATABASE_URL="file:dev.sqlite"
SHOPIFY_APP_HANDLE="<provider-confirmed App Home handle>"
SHOPIFY_PARTNER_ORG_ID="<provider-confirmed organization ID>"
SHOPIFY_APP_GID="gid://shopify/App/<provider-confirmed numeric ID>"
SHOPIFY_PARTNER_API_ACCESS_TOKEN="<Partner API client token>"
```

## Note on cost
Current Fly cost is **UNAVAILABLE** without authenticated billing evidence. Do
not create infrastructure, resize machines or incur spend without a separate
exact cap and approval.
